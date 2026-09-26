"""Deleting an account: everything the person's data touches, in foreign-key order.

``tumtum.cc/privacidade`` promises that deleting the account deletes the
account, the readings, the moments and the cards. Until 2026-09-18 that was
done by hand (decision log, item 32). This is the code that keeps the promise.

Events and their timelines are shared between everyone who attended, so they
stay. Everything owned by the person goes, children before parents because
two of the foreign keys (``hr_sessions.user_id``, ``wearable_connections.user_id``)
and ``hr_data.session_id`` carry no ON DELETE clause — a bulk delete in the
wrong order is refused by the database, not silently cascaded.

Since 26/09 (LGPD remediation) it also takes what sits *around* the account
and names the person: sign-up and e-mail-change codes for the address, the
waitlist entry for it, the consent record, the data-subject requests, the
log of reads of their data, and the cached images of their cards. What stays
is one row in `deletion_log` with a date and no identifier — and the access
log, which the Marco Civil (art. 15) requires kept for six months and which
the maintenance loop purges after that.
"""

from sqlalchemy import delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.card import Card, Share
from app.models.consent import Consent
from app.models.event_post import EventPost, EventPostReaction
from app.models.event_series import SeriesPost
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.moderation import PostReport, UserBlock
from app.models.password_reset_token import PasswordResetToken
from app.models.peak import Peak
from app.models.privacy import (
    DataAccessLog,
    DataSubjectRequest,
    DeletionLog,
    EmailChange,
)
from app.models.refresh_token import RefreshToken
from app.models.signup_code import SignupCode
from app.models.user import User
from app.models.waitlist_entry import WaitlistEntry
from app.models.wearable_connection import WearableConnection
from app.services import card_cache

# Children before parents. Tested, because the order *is* the correctness.
#
# The feed's four tables were missing until 2026-09-22 (#36): an account that
# had posted, reacted or been reported could not be deleted at all — the
# database refused, because event_posts points at both the user and the
# night. A test now reads every foreign key in the schema, so a new table that
# points at a person cannot be forgotten here again.
DELETION_ORDER = (
    "post_reports",
    "series_posts",
    "event_post_reactions",
    "event_posts",
    "user_blocks",
    "shares",
    "cards",
    "hr_data",
    "peaks",
    "hr_sessions",
    "wearable_connections",
    "password_reset_tokens",
    "refresh_tokens",
    "consents",
    "email_changes",
    "data_subject_requests",
    "users",
)


async def delete_account(db: AsyncSession, user: User) -> None:
    """Remove the user and every row that belongs to them. Irreversible."""
    email_key = user.email.strip().lower()
    card_id_list = list(
        (await db.execute(select(Card.id).where(Card.user_id == user.id)))
        .scalars()
        .all()
    )
    session_ids = (
        select(HRSession.id).where(HRSession.user_id == user.id).scalar_subquery()
    )
    card_ids = select(Card.id).where(Card.user_id == user.id).scalar_subquery()
    post_ids = (
        select(EventPost.id).where(EventPost.user_id == user.id).scalar_subquery()
    )

    # The feed: reports by them or about their posts, reactions by them or on
    # their posts, their posts, and every block they are part of.
    await db.execute(
        delete(PostReport).where(
            (PostReport.reporter_id == user.id) | PostReport.post_id.in_(post_ids)
        )
    )
    await db.execute(delete(SeriesPost).where(SeriesPost.post_id.in_(post_ids)))
    await db.execute(
        delete(EventPostReaction).where(
            (EventPostReaction.user_id == user.id)
            | EventPostReaction.post_id.in_(post_ids)
        )
    )
    await db.execute(delete(EventPost).where(EventPost.user_id == user.id))
    await db.execute(
        delete(UserBlock).where(
            (UserBlock.blocker_id == user.id) | (UserBlock.blocked_id == user.id)
        )
    )

    await db.execute(delete(Share).where(Share.card_id.in_(card_ids)))
    await db.execute(delete(Card).where(Card.user_id == user.id))
    await db.execute(delete(HRData).where(HRData.session_id.in_(session_ids)))
    await db.execute(delete(Peak).where(Peak.session_id.in_(session_ids)))
    await db.execute(delete(HRSession).where(HRSession.user_id == user.id))
    await db.execute(
        delete(WearableConnection).where(WearableConnection.user_id == user.id)
    )
    await db.execute(
        delete(PasswordResetToken).where(PasswordResetToken.user_id == user.id)
    )
    await db.execute(delete(RefreshToken).where(RefreshToken.user_id == user.id))
    await db.execute(delete(Consent).where(Consent.user_id == user.id))
    await db.execute(
        delete(EmailChange).where(
            (EmailChange.user_id == user.id) | (EmailChange.email_key == email_key)
        )
    )
    await db.execute(
        delete(DataSubjectRequest).where(DataSubjectRequest.user_id == user.id)
    )
    await db.execute(
        delete(DataAccessLog).where(DataAccessLog.subject_user_id == user.id)
    )
    await db.execute(delete(SignupCode).where(SignupCode.email_key == email_key))
    await db.execute(
        delete(WaitlistEntry).where(func.lower(WaitlistEntry.email) == email_key)
    )
    await db.execute(delete(User).where(User.id == user.id))
    db.add(DeletionLog())
    await db.flush()

    await card_cache.forget(card_id_list)
