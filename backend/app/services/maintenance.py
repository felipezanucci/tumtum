"""What the privacy policy promises about time, done once a day.

Every retention figure the policy states is enforced here, not by hand
(LGPD audit, AL-9 and AL-8):

- **Raw readings**: `hr_data` of a night is deleted
  `raw_readings_retention_days` (30) after its moments were detected
  (`hr_sessions.analyzed_at`). The moments, the night's summary and its cards
  stay — they are what the night is kept for.
- **A withdrawn `keep_night`**: when a person revokes keeping their nights
  and has not granted it again for a day, their nights go, with everything
  made from them. The day is the grace for a switch flipped by mistake.
  Accounts that never answered the question (made before 26/09) are left
  alone: never having been asked is not a withdrawal, and deleting their
  nights is a decision for a person, not for a loop.
- **Access log**: rows older than 180 days (Marco Civil, art. 15).
- **Spent credentials**: refresh tokens, password-reset links, sign-up codes
  and e-mail-change codes a day after they expired — kept that day so a
  late reuse is still recognised and refused as such.

Runs inside the API process (`run_forever`, started in the lifespan like the
match watch), and by hand with `python -m app.services.maintenance`.
"""

import asyncio
import logging
from dataclasses import asdict, dataclass
from datetime import UTC, datetime, timedelta

from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.models.consent import Consent
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.password_reset_token import PasswordResetToken
from app.models.privacy import AccessLog, EmailChange
from app.models.refresh_token import RefreshToken
from app.models.signup_code import SignupCode
from app.services.access_log import ACCESS_LOG_RETENTION
from app.services.night_deletion import delete_nights

log = logging.getLogger(__name__)

EVERY = timedelta(days=1)
# The first pass waits for the deploy to settle rather than competing with it.
FIRST_RUN_AFTER = timedelta(minutes=2)
EXPIRED_GRACE = timedelta(hours=24)
REVOCATION_GRACE = timedelta(hours=24)


@dataclass
class Report:
    readings_deleted: int = 0
    nights_deleted: int = 0
    access_log_deleted: int = 0
    refresh_tokens_deleted: int = 0
    password_reset_tokens_deleted: int = 0
    signup_codes_deleted: int = 0
    email_changes_deleted: int = 0


async def _withdrawn_keep_night(db: AsyncSession, before: datetime) -> list:
    """Users whose `keep_night` was revoked before `before` and not re-granted."""
    active = select(Consent.user_id).where(
        Consent.purpose == "keep_night", Consent.revoked_at.is_(None)
    )
    rows = await db.execute(
        select(Consent.user_id)
        .where(
            Consent.purpose == "keep_night",
            Consent.revoked_at.is_not(None),
            Consent.revoked_at < before,
            Consent.user_id.not_in(active),
        )
        .distinct()
    )
    return list(rows.scalars().all())


async def run_once(db: AsyncSession, now: datetime | None = None) -> Report:
    """One pass. The caller commits."""
    now = now or datetime.now(UTC)
    report = Report()

    analysed_before = now - timedelta(days=settings.raw_readings_retention_days)
    expired_sessions = select(HRSession.id).where(
        HRSession.analyzed_at.is_not(None), HRSession.analyzed_at < analysed_before
    )
    result = await db.execute(
        delete(HRData).where(HRData.session_id.in_(expired_sessions))
    )
    report.readings_deleted = result.rowcount or 0

    withdrawn = await _withdrawn_keep_night(db, now - REVOCATION_GRACE)
    if withdrawn:
        nights = list(
            (
                await db.execute(
                    select(HRSession.id).where(HRSession.user_id.in_(withdrawn))
                )
            )
            .scalars()
            .all()
        )
        report.nights_deleted = await delete_nights(db, nights)

    result = await db.execute(
        delete(AccessLog).where(AccessLog.at < now - ACCESS_LOG_RETENTION)
    )
    report.access_log_deleted = result.rowcount or 0

    spent = now - EXPIRED_GRACE
    for model, field in (
        (RefreshToken, "refresh_tokens_deleted"),
        (PasswordResetToken, "password_reset_tokens_deleted"),
        (SignupCode, "signup_codes_deleted"),
        (EmailChange, "email_changes_deleted"),
    ):
        result = await db.execute(delete(model).where(model.expires_at < spent))
        setattr(report, field, result.rowcount or 0)

    await db.flush()
    return report


async def run_and_commit() -> Report:
    from app.core.database import async_session

    async with async_session() as db:
        report = await run_once(db)
        await db.commit()
    log.info("maintenance: %s", asdict(report))
    print(f"Maintenance: {asdict(report)}")
    return report


async def run_forever() -> None:
    """Once a day, for as long as the process lives. One bad pass is logged,
    and the next day tries again."""
    await asyncio.sleep(FIRST_RUN_AFTER.total_seconds())
    while True:
        try:
            await run_and_commit()
        except asyncio.CancelledError:
            raise
        except Exception:
            log.exception("maintenance pass failed")
        await asyncio.sleep(EVERY.total_seconds())


if __name__ == "__main__":
    asyncio.run(run_and_commit())
