"""Consent per purpose — the only legal basis for heart-rate data (CR-1).

Append-only, one purpose at a time, checked by the server on every request
that needs it, and refused with a body a client can act on.
"""

import json

import pytest
from sqlalchemy import select

from app.api.consents import get_consents, put_consents
from app.api.health import requires_keep_night
from app.core.auth import ConsentRequired, require_consent
from app.main import app, consent_required_handler
from app.models.consent import Consent
from app.schemas.consent import ConsentsUpdate
from app.services import consents
from tests.conftest import add_user, grant, make_request


def test_the_constants_are_the_contracts():
    assert consents.CONSENT_TEXT_VERSION == "2026-09-26"
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
    assert answer.text_version == "2026-09-26"
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
