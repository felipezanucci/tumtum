"""Consent per purpose — the only legal basis for heart-rate data (CR-1).

Append-only, one purpose at a time, checked by the server on every request
that needs it, and refused with a body a client can act on.
"""

import hashlib
import importlib.util
import json
from pathlib import Path

import pytest
from sqlalchemy import func, select

from app.api.consents import get_consents, put_consents
from app.api.health import requires_keep_night
from app.core.auth import ConsentRequired, require_consent
from app.main import app, consent_required_handler
from app.models.consent import Consent
from app.models.hr_session import HRSession
from app.schemas.consent import ConsentsUpdate
from app.services import consents
from tests.conftest import add_user, grant, make_request, night_body


def test_the_constants_are_the_contracts():
    assert consents.CONSENT_TEXT_VERSION == "2026-09-26.1"
    assert consents.PURPOSES == (
        "terms",
        "read_heart_rate",
        "keep_night",
        "crowd_stats",
        "artist_compare",
        "improve_detection",
        "marketing",
    )


@pytest.mark.asyncio
async def test_get_lists_all_seven_unchecked_by_default(memdb):
    user = await add_user(memdb)
    answer = await get_consents(user, memdb)
    assert answer.text_version == "2026-09-26.1"
    assert [c.purpose for c in answer.consents] == list(consents.PURPOSES)
    assert not any(c.granted for c in answer.consents)
    assert all(c.granted_at is None and c.text_version is None for c in answer.consents)


async def _put(db, user, purposes, version="2026-09-26", client="android/187"):
    body = ConsentsUpdate(text_version=version, means="tap", purposes=purposes)
    request = make_request()
    request.scope["headers"].append((b"x-tumtum-client", client.encode()))
    return await put_consents(body, request, user, db)


@pytest.mark.asyncio
async def test_put_changes_only_the_purposes_it_names(memdb):
    user = await add_user(memdb)
    await _put(memdb, user, {"keep_night": True})
    answer = await _put(memdb, user, {"crowd_stats": False})
    state = {c.purpose: c.granted for c in answer.consents}
    assert state["keep_night"] is True
    assert state["crowd_stats"] is False
    row = (await memdb.execute(select(Consent))).scalar_one()
    assert (row.purpose, row.means, row.client) == ("keep_night", "tap", "android/187")


@pytest.mark.asyncio
async def test_revoking_stamps_the_row_and_granting_again_adds_one(memdb):
    user = await add_user(memdb)
    await _put(memdb, user, {"keep_night": True})
    answer = await _put(memdb, user, {"keep_night": False})
    entry = next(c for c in answer.consents if c.purpose == "keep_night")
    assert entry.granted is False and entry.revoked_at is not None
    await _put(memdb, user, {"keep_night": True})
    rows = (await memdb.execute(select(Consent))).scalars().all()
    assert len(rows) == 2  # the history is kept, never rewritten
    assert sum(r.revoked_at is None for r in rows) == 1
    assert await consents.active(memdb, user.id, "keep_night")


@pytest.mark.asyncio
async def test_granting_twice_under_the_same_text_is_one_row(memdb):
    user = await add_user(memdb)
    await _put(memdb, user, {"crowd_stats": True})
    await _put(memdb, user, {"crowd_stats": True})
    assert len((await memdb.execute(select(Consent))).scalars().all()) == 1


@pytest.mark.asyncio
async def test_a_new_text_version_closes_the_old_grant_and_opens_one(memdb):
    user = await add_user(memdb)
    await _put(memdb, user, {"marketing": True}, version="2026-09-26")
    await _put(memdb, user, {"marketing": True}, version="2026-12-01")
    rows = (await memdb.execute(select(Consent))).scalars().all()
    assert len(rows) == 2
    active = [r for r in rows if r.revoked_at is None]
    assert [r.text_version for r in active] == ["2026-12-01"]


def test_an_unknown_purpose_is_refused_by_the_schema():
    with pytest.raises(ValueError):
        ConsentsUpdate(text_version="x", means="tap", purposes={"sell_data": True})


@pytest.mark.asyncio
async def test_require_consent_refuses_with_the_purpose(memdb):
    user = await add_user(memdb)
    with pytest.raises(ConsentRequired) as refused:
        await requires_keep_night(user=user, db=memdb)
    assert refused.value.status_code == 403
    assert refused.value.purpose == "keep_night"

    response = await consent_required_handler(None, refused.value)
    body = json.loads(response.body)
    assert response.status_code == 403
    assert body["code"] == "consent_required"
    assert body["purpose"] == "keep_night"
    assert body["detail"]

    await grant(memdb, user, "keep_night")
    assert await requires_keep_night(user=user, db=memdb) is user


def test_require_consent_knows_only_the_contracts_purposes():
    with pytest.raises(ValueError):
        require_consent("anything")


def test_keeping_a_night_on_the_server_requires_keep_night():
    """Read from the router, so the guard cannot be lost in a refactor."""
    for route in app.routes:
        if getattr(route, "path", None) == "/api/health/sessions" and "POST" in (
            route.methods or set()
        ):
            names = {d.call.__name__ for d in route.dependant.dependencies}
            assert "require_consent_keep_night" in names
            return
    raise AssertionError("POST /api/health/sessions is gone")


# --- the ledger (v1.1 opinion, §4.2) ---

HEART_RATE_PURPOSES = (
    "read_heart_rate",
    "keep_night",
    "crowd_stats",
    "artist_compare",
    "improve_detection",
)


def test_every_purpose_has_a_legal_basis_and_a_scope():
    assert set(consents.LEGAL_BASES) == set(consents.PURPOSES)
    assert set(consents.SCOPES) == set(consents.PURPOSES)
    assert all(consents.LEGAL_BASES[p] == "consent_art11" for p in HEART_RATE_PURPOSES)
    assert consents.LEGAL_BASES["marketing"] == "consent_art7"
    assert all(0 < len(scope) <= 200 for scope in consents.SCOPES.values())


def test_the_proof_is_the_sha256_of_purpose_and_text_version():
    proof = consents.proof_of("keep_night", "2026-09-26")
    assert proof == hashlib.sha256(b"keep_night:2026-09-26").hexdigest()
    assert len(proof) == 64
    assert proof != consents.proof_of("keep_night", "2026-12-01")


@pytest.mark.asyncio
async def test_a_grant_records_its_basis_its_scope_and_the_proof_of_its_text(memdb):
    user = await add_user(memdb)
    answer = await _put(memdb, user, {"keep_night": True})
    row = (await memdb.execute(select(Consent))).scalar_one()
    assert row.legal_basis == "consent_art11"
    assert row.scope == "série, momentos e cards da noite no servidor, enquanto ativo"
    assert row.proof == consents.proof_of("keep_night", "2026-09-26")

    entries = {c.purpose: c for c in answer.consents}
    kept = entries["keep_night"]
    assert (kept.legal_basis, kept.scope, kept.proof) == (
        row.legal_basis,
        row.scope,
        row.proof,
    )
    # A purpose never answered still says what it would stand on, and has no
    # proof: nothing was agreed to.
    never = entries["crowd_stats"]
    assert never.legal_basis == "consent_art11" and never.proof is None


@pytest.mark.asyncio
async def test_the_terms_row_records_a_contract_not_a_consent(memdb):
    user = await add_user(memdb)
    await grant(memdb, user, "terms", "marketing")
    rows = {r.purpose: r for r in (await memdb.execute(select(Consent))).scalars()}
    assert rows["terms"].legal_basis == "contract_art7"
    assert rows["marketing"].legal_basis == "consent_art7"


@pytest.mark.asyncio
async def test_a_regrant_under_a_new_text_carries_the_new_proof(memdb):
    user = await add_user(memdb)
    await _put(memdb, user, {"marketing": True}, version="2026-09-26")
    await _put(memdb, user, {"marketing": True}, version="2026-12-01")
    rows = (
        (await memdb.execute(select(Consent).order_by(Consent.granted_at)))
        .scalars()
        .all()
    )
    assert [r.proof for r in rows] == [
        consents.proof_of("marketing", "2026-09-26"),
        consents.proof_of("marketing", "2026-12-01"),
    ]


def test_migration_023_froze_the_same_ledger_as_the_service():
    path = (
        Path(__file__).parent.parent / "alembic/versions/023_consent_ledger_fields.py"
    )
    spec = importlib.util.spec_from_file_location("migration_023", path)
    migration = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(migration)
    assert migration.down_revision == "022"
    assert migration.LEGAL_BASES == consents.LEGAL_BASES
    assert migration.SCOPES == consents.SCOPES


def test_the_catch_up_fills_the_ledger_before_it_is_not_null():
    from app.core.schema_catchup import backfills, statements

    added = " ".join(statements())
    for column in ("legal_basis", "scope", "proof"):
        assert f"consents ADD COLUMN IF NOT EXISTS {column}" in added
    fills = backfills()
    assert not any("DROP" in s for s in fills)
    updates = [i for i, s in enumerate(fills) if s.startswith("UPDATE")]
    not_nulls = [i for i, s in enumerate(fills) if s.endswith("SET NOT NULL")]
    assert len(updates) == 3 and len(not_nulls) == 2
    assert max(updates) < min(not_nulls)  # nothing left NULL when it closes
    assert all("IS NULL" in fills[i] for i in updates)  # idempotent
    assert "'terms' THEN 'contract_art7'" in fills[0]
    assert "sha256" in fills[2]


# --- revoking stops collection, end to end ---


@pytest.mark.asyncio
async def test_consent_revoke_stops_new_collection(memdb, api):
    user = await add_user(memdb)
    client = api(user)

    def consent(granted: bool) -> dict:
        return {
            "text_version": consents.CONSENT_TEXT_VERSION,
            "means": "tap",
            "purposes": {"keep_night": granted},
        }

    assert (await client.put("/api/consents", json=consent(True))).status_code == 200
    kept = await client.post("/api/health/sessions", json=night_body())
    assert kept.status_code == 201

    assert (await client.put("/api/consents", json=consent(False))).status_code == 200
    refused = await client.post("/api/health/sessions", json=night_body())

    assert refused.status_code == 403
    assert refused.json() == {
        "detail": consents.REQUIRED_SENTENCES["keep_night"],
        "code": "consent_required",
        "purpose": "keep_night",
    }
    nights = (await memdb.execute(select(func.count()).select_from(HRSession))).scalar()
    assert nights == 1  # only the one sent while it was granted
    (row,) = (await memdb.execute(select(Consent))).scalars().all()
    assert row.purpose == "keep_night" and row.revoked_at is not None
    assert row.legal_basis == "consent_art11"
    listed = (await client.get("/api/consents")).json()["consents"]
    entry = next(c for c in listed if c["purpose"] == "keep_night")
    assert entry["granted"] is False and entry["revoked_at"] is not None
