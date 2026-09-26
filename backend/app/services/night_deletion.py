"""Deleting nights: one (the person's "Apagar esta noite"), or every night of
an event (the pilot wipe).

Everything a night produced goes with it, children before parents, for the
reason `account_deletion` spells out: `hr_data.session_id` and several feed
keys carry no ON DELETE clause, so the order is the correctness. What goes:

- the readings (`hr_data`) and the moments (`peaks`);
- the cards made from the night, their share records and their cached images;
- the feed posts made from the night, with their reactions, reports and
  tour-wide consent (`series_posts`);
- the night itself.

The event and its timeline stay — they are TumTum's, shared by everybody who
was there.
"""

import uuid
from collections.abc import Sequence

from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.card import Card, Share
from app.models.event_post import EventPost, EventPostReaction
from app.models.event_series import SeriesPost
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.moderation import PostReport
from app.models.peak import Peak
from app.services import card_cache


async def delete_posts(db: AsyncSession, post_ids: Sequence[uuid.UUID]) -> None:
    """Posts and everything that points at them."""
    if not post_ids:
        return
    await db.execute(delete(PostReport).where(PostReport.post_id.in_(post_ids)))
    await db.execute(delete(SeriesPost).where(SeriesPost.post_id.in_(post_ids)))
    await db.execute(
        delete(EventPostReaction).where(EventPostReaction.post_id.in_(post_ids))
    )
    await db.execute(delete(EventPost).where(EventPost.id.in_(post_ids)))


async def delete_nights(db: AsyncSession, session_ids: Sequence[uuid.UUID]) -> int:
    """Delete these nights and everything made from them. Returns how many.

    The caller has already decided the person may: this function checks
    nothing about ownership.
    """
    session_ids = list(session_ids)
    if not session_ids:
        return 0

    post_ids = list(
        (
            await db.execute(
                select(EventPost.id).where(EventPost.session_id.in_(session_ids))
            )
        )
        .scalars()
        .all()
    )
    await delete_posts(db, post_ids)

    card_ids = list(
        (await db.execute(select(Card.id).where(Card.session_id.in_(session_ids))))
        .scalars()
        .all()
    )
    if card_ids:
        await db.execute(delete(Share).where(Share.card_id.in_(card_ids)))
        await db.execute(delete(Card).where(Card.id.in_(card_ids)))

    await db.execute(delete(HRData).where(HRData.session_id.in_(session_ids)))
    await db.execute(delete(Peak).where(Peak.session_id.in_(session_ids)))
    result = await db.execute(delete(HRSession).where(HRSession.id.in_(session_ids)))
    await db.flush()

    # After the rows: the cache is only ever a copy of them.
    await card_cache.forget(card_ids)
    return result.rowcount or 0
