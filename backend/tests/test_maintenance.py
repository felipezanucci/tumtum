"""The retention the privacy policy states, done by the daily loop (AL-9)."""

import uuid
from datetime import UTC, datetime, timedelta

import pytest
from sqlalchemy import func, select

from app.models.consent import Consent
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.password_reset_token import PasswordResetToken
from app.models.peak import Peak
from app.models.privacy import AccessLog, EmailChange
from app.models.refresh_token import RefreshToken
from app.models.signup_code import SignupCode
from app.services import maintenance
from tests.conftest import add_night, add_user, grant

NOW = datetime(2026, 12, 1, 12, 0, tzinfo=UTC)


async def _readings(db, night) -> int:
    return (
        await db.execute(
            select(func.count())
            .select_from(HRData)
            .where(HRData.session_id == night.id)
        )
    ).scalar_one()


@pytest.mark.asyncio
async def test_raw_readings_go_thirty_days_after_the_moments_were_found(memdb):
    user = await add_user(memdb)
    old = await add_night(memdb, user)
    old.analyzed_at = NOW - timedelta(days=31)
    recent = await add_night(memdb, user)
    recent.analyzed_at = NOW - timedelta(days=29)
    never = await add_night(memdb, user)  # not analysed: its clock never started
    memdb.add(
        Peak(
            session_id=old.id,
            timestamp=NOW,
            bpm=150,
            duration_seconds=30,
            magnitude=3.0,
            rank=1,
        )
    )
    await memdb.flush()

    report = await maintenance.run_once(memdb, NOW)

    assert report.readings_deleted == 20
    assert await _readings(memdb, old) == 0
    assert await _readings(memdb, recent) == 20
    assert await _readings(memdb, never) == 20
    # The night and its moment stay: they are what it is kept for.
    assert await memdb.get(HRSession, old.id) is not None
    assert (
        await memdb.execute(select(func.count()).select_from(Peak))
    ).scalar_one() == 1


@pytest.mark.asyncio
async def test_a_withdrawn_keep_night_takes_the_nights_after_a_day(memdb):
    withdrew = await add_user(memdb, "Ana")
    await grant(memdb, withdrew, "keep_night")
    row = (await memdb.execute(select(Consent))).scalar_one()
    row.revoked_at = NOW - timedelta(hours=25)
    gone = await add_night(memdb, withdrew)

    just_now = await add_user(memdb, "Bia")
    await grant(memdb, just_now, "keep_night")
    fresh = (
        await memdb.execute(select(Consent).where(Consent.user_id == just_now.id))
    ).scalar_one()
    fresh.revoked_at = NOW - timedelta(hours=2)  # still inside the grace
    kept = await add_night(memdb, just_now)

    legacy = await add_user(memdb, "Cris")  # never asked: not a withdrawal
    untouched = await add_night(memdb, legacy)
    await memdb.flush()

    report = await maintenance.run_once(memdb, NOW)

    assert report.nights_deleted == 1
    assert await memdb.get(HRSession, gone.id) is None
    assert await memdb.get(HRSession, kept.id) is not None
    assert await memdb.get(HRSession, untouched.id) is not None


@pytest.mark.asyncio
async def test_access_log_is_kept_180_days(memdb):
    memdb.add_all(
        [
            AccessLog(
                method="GET", path="/api/x", status=200, at=NOW - timedelta(days=181)
            ),
            AccessLog(
                method="GET", path="/api/y", status=200, at=NOW - timedelta(days=179)
            ),
        ]
    )
    await memdb.flush()
    report = await maintenance.run_once(memdb, NOW)
    assert report.access_log_deleted == 1
    paths = (await memdb.execute(select(AccessLog.path))).scalars().all()
    assert paths == ["/api/y"]


@pytest.mark.asyncio
async def test_spent_codes_and_tokens_go_a_day_after_they_expire(memdb):
    user = await add_user(memdb)
    long_ago = NOW - timedelta(hours=25)
    yesterday = NOW - timedelta(hours=23)
    for expires in (long_ago, yesterday):
        memdb.add_all(
            [
                RefreshToken(
                    user_id=user.id,
                    family_id=uuid.uuid4(),
                    token_hash=uuid.uuid4().hex,
                    expires_at=expires,
                ),
                PasswordResetToken(
                    user_id=user.id, token_hash=uuid.uuid4().hex, expires_at=expires
                ),
                SignupCode(
                    email="x@x.cc",
                    email_key="x@x.cc",
                    name="X",
                    hashed_password="h",
                    code_hash="h",
                    expires_at=expires,
                ),
                EmailChange(
                    user_id=user.id,
                    new_email="n@x.cc",
                    email_key="n@x.cc",
                    code_hash="h",
                    expires_at=expires,
                ),
            ]
        )
    await memdb.flush()

    report = await maintenance.run_once(memdb, NOW)

    assert (
        report.refresh_tokens_deleted,
        report.password_reset_tokens_deleted,
        report.signup_codes_deleted,
        report.email_changes_deleted,
    ) == (1, 1, 1, 1)
    for model in (RefreshToken, PasswordResetToken, SignupCode, EmailChange):
        left = (
            await memdb.execute(select(func.count()).select_from(model))
        ).scalar_one()
        assert left == 1, model.__tablename__


def test_the_retention_default_is_thirty_days():
    from app.config import settings

    assert settings.raw_readings_retention_days == 30
