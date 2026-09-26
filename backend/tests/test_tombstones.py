"""A deleted account stays deleted when a backup is restored (v1.1 §11).

The restore is simulated by putting the account's rows back as a backup
would carry them — same id, same address, the creation date it always had —
after the deletion wrote its tombstone.
"""

import hashlib
import uuid
from datetime import UTC, datetime, timedelta

import pytest
from sqlalchemy import func, select

from app.models.hr_session import HRSession
from app.models.privacy import DeletionTombstone, EmailChange
from app.models.signup_code import SignupCode
from app.models.user import User
from app.models.waitlist_entry import WaitlistEntry
from app.services import maintenance, tombstones
from app.services.account_deletion import delete_account
from tests.conftest import add_night, add_user

BEFORE = datetime(2026, 1, 10, tzinfo=UTC)


async def _count(db, model, *where) -> int:
    query = select(func.count()).select_from(model)
    for condition in where:
        query = query.where(condition)
    return (await db.execute(query)).scalar_one()


async def _restore(db, user_id, email, created_at=BEFORE):
    """What a backup from before the deletion brings back."""
    user = User(
        id=user_id,
        email=email,
        name="Ana",
        auth_provider="email",
        created_at=created_at,
    )
    db.add(user)
    await db.flush()
    await add_night(db, user)
    return user


@pytest.mark.asyncio
async def test_deletion_leaves_two_hashes_and_nothing_readable(memdb, fake_redis):
    ana = await add_user(memdb, "Ana", email="Ana@X.cc")
    await delete_account(memdb, ana)

    (stone,) = (await memdb.execute(select(DeletionTombstone))).scalars().all()
    assert stone.subject_key == hashlib.sha256(str(ana.id).encode()).hexdigest()
    assert stone.email_key == tombstones.email_fingerprint("ana@x.cc")
    # Keyed: not the plain hash anybody could compute from a list of addresses.
    assert stone.email_key != hashlib.sha256(b"ana@x.cc").hexdigest()
    for column in (stone.subject_key, stone.email_key):
        assert "ana" not in column.lower() and str(ana.id) not in column


@pytest.mark.asyncio
async def test_a_restored_account_is_deleted_again_and_a_new_one_is_not(
    memdb, fake_redis
):
    ana = await add_user(memdb, "Ana", email="ana@x.cc")
    ana_id = ana.id
    await delete_account(memdb, ana)
    deleted_at = (await memdb.execute(select(DeletionTombstone))).scalar_one()
    after = deleted_at.deleted_at + timedelta(days=1)

    # The restore: her account and night, a pending code and a waitlist entry
    # for her address, all from before the deletion.
    await _restore(memdb, ana_id, "ana@x.cc")
    memdb.add_all(
        [
            SignupCode(
                email="Ana@x.cc",
                email_key="ana@x.cc",
                name="Ana",
                hashed_password="h",
                code_hash="h",
                expires_at=BEFORE,
                created_at=BEFORE,
            ),
            WaitlistEntry(email="ANA@x.cc", created_at=BEFORE),
            # Somebody else's, untouched.
            WaitlistEntry(email="bia@x.cc", created_at=BEFORE),
        ]
    )
    # Somebody who came back with the same address *after* the deletion — a
    # new person as far as the tombstone is concerned, and theirs to keep.
    back = User(
        id=uuid.uuid4(),
        email="ana@x.cc".upper(),
        name="Ana de novo",
        auth_provider="email",
        created_at=after,
    )
    bia = await add_user(memdb, "Bia")
    memdb.add(back)
    await memdb.flush()

    removed = await tombstones.sweep(memdb)

    assert removed == 1
    assert await memdb.get(User, ana_id) is None
    assert await _count(memdb, HRSession, HRSession.user_id == ana_id) == 0
    assert await _count(memdb, SignupCode) == 0
    assert (await memdb.execute(select(WaitlistEntry.email))).scalars().all() == [
        "bia@x.cc"
    ]
    assert await memdb.get(User, back.id) is not None
    assert await memdb.get(User, bia.id) is not None
    # Deleted twice, tombstoned once.
    assert await _count(memdb, DeletionTombstone) == 1


@pytest.mark.asyncio
async def test_the_sweep_takes_a_restored_code_to_a_new_address(memdb, fake_redis):
    ana = await add_user(memdb, "Ana", email="ana@x.cc")
    other = await add_user(memdb, "Caio")
    await delete_account(memdb, ana)
    memdb.add(
        EmailChange(
            user_id=other.id,
            new_email="ana@x.cc",
            email_key="ana@x.cc",
            code_hash="h",
            expires_at=BEFORE,
            created_at=BEFORE,
        )
    )
    await memdb.flush()

    assert await tombstones.sweep(memdb) == 0
    assert await _count(memdb, EmailChange) == 0
    assert await memdb.get(User, other.id) is not None


@pytest.mark.asyncio
async def test_no_tombstones_means_nothing_is_touched(memdb):
    user = await add_user(memdb)
    assert await tombstones.sweep(memdb) == 0
    assert await memdb.get(User, user.id) is not None


@pytest.mark.asyncio
async def test_maintenance_sweeps_and_forgets_old_tombstones(memdb, fake_redis):
    now = datetime.now(UTC)
    ana = await add_user(memdb, "Ana")
    ana_id = ana.id
    await delete_account(memdb, ana)
    await _restore(memdb, ana_id, "ana@x.cc")
    memdb.add(
        DeletionTombstone(
            subject_key="a" * 64,
            email_key="b" * 64,
            deleted_at=now - timedelta(days=401),
        )
    )
    await memdb.flush()

    report = await maintenance.run_once(memdb, now)

    assert report.restored_accounts_removed == 1
    assert report.tombstones_deleted == 1
    assert await memdb.get(User, ana_id) is None
    (left,) = (await memdb.execute(select(DeletionTombstone))).scalars().all()
    assert left.subject_key == tombstones.subject_fingerprint(ana_id)


def test_the_tombstone_outlives_any_backup_by_default():
    from app.config import Settings

    assert Settings.model_fields["tombstone_retention_days"].default == 400


@pytest.mark.asyncio
async def test_the_startup_sweeps_once(memdb, fake_redis, monkeypatch, capsys):
    """A restore is followed by a start: the lifespan sweeps before serving."""
    from app import main
    from app.core import database

    ana = await add_user(memdb, "Ana")
    ana_id = ana.id
    await delete_account(memdb, ana)
    await _restore(memdb, ana_id, "ana@x.cc")
    await memdb.commit()

    # create_all and the catch-up need Postgres; they print a warning and the
    # start goes on, which is what this fake engine makes them do.
    monkeypatch.setattr(database, "engine", _NoEngine())
    monkeypatch.setattr(database, "async_session", memdb.maker)
    async with main.lifespan(main.app):
        pass

    assert "Tombstone sweep: 1 restored account(s) deleted again" in (
        capsys.readouterr().out
    )
    memdb.expire_all()
    assert await memdb.get(User, ana_id) is None


class _NoConnection:
    async def __aenter__(self):
        raise RuntimeError("no database engine in tests")

    async def __aexit__(self, *_exc):
        return False


class _NoEngine:
    def begin(self):
        return _NoConnection()
