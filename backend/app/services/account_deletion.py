"""Deleting an account: everything the person's data touches, in foreign-key order.

``tumtum.cc/privacidade`` promises that deleting the account deletes the
account, the readings, the moments and the cards. Until 2026-09-18 that was
done by hand (decision log, item 32). This is the code that keeps the promise.

Events and their timelines are shared between everyone who attended, so they
stay. Everything owned by the person goes, children before parents because
two of the foreign keys (``hr_sessions.user_id``, ``wearable_connections.user_id``)
and ``hr_data.session_id`` carry no ON DELETE clause — a bulk delete in the
wrong order is refused by the database, not silently cascaded.
"""

from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.card import Card, Share
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.password_reset_token import PasswordResetToken
from app.models.peak import Peak
from app.models.user import User
from app.models.wearable_connection import WearableConnection

# Children before parents. Tested, because the order *is* the correctness.
DELETION_ORDER = (
    "shares",
    "cards",
    "hr_data",
    "peaks",
    "hr_sessions",
    "wearable_connections",
    "password_reset_tokens",
    "users",
)


async def delete_account(db: AsyncSession, user: User) -> None:
    """Remove the user and every row that belongs to them. Irreversible."""
    session_ids = (
        select(HRSession.id).where(HRSession.user_id == user.id).scalar_subquery()
    )
    card_ids = select(Card.id).where(Card.user_id == user.id).scalar_subquery()

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
    await db.execute(delete(User).where(User.id == user.id))
    await db.flush()
