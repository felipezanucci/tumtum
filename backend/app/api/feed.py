"""The feed of an event — only for the people who were at it.

Felipe's call, 2026-09-22: *"apenas as pessoas que estiveram no evento podem
interagir. Isso deve criar um senso de comunidade maior."* This module is
that rule and nothing else; everything here hangs off one question.

**What proves somebody was there?** An `hr_sessions` row carrying the event's
id. It is evidence rather than a claim, it needs no new table, and it ties
the right to take part to having a night to take part with. Its edge is
honest and worth stating: somebody whose strap died was there and cannot
join. The alternative — trusting the app's word that a person activated an
event — is not a gate at all, and events are TumTum's, so a fan never
declares anything about one (product rule, 21/09).

**A post is published health data.** It carries a person's heart rate at a
named minute in front of strangers. So it is only ever written by an explicit
act, never derived from a night that happens to exist, and it can be taken
down — the undo without which the consent is not real.
"""

import uuid
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.event import Event
from app.models.event_post import EventPost, EventPostReaction
from app.models.hr_session import HRSession
from app.models.peak import Peak
from app.models.user import User
from app.schemas.feed import (
    CrowdResponse,
    EventFeedResponse,
    FeedPostCreate,
    FeedPostResponse,
)
from app.services.crowd import collective_moments

router = APIRouter(prefix="/api/events", tags=["feed"])


async def _event_or_404(db: AsyncSession, event_id: uuid.UUID) -> Event:
    result = await db.execute(select(Event).where(Event.id == event_id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado"
        )
    return event


async def was_there(db: AsyncSession, user_id: uuid.UUID, event_id: uuid.UUID) -> bool:
    """Whether this account has a measured night at this event."""
    result = await db.execute(
        select(HRSession.id)
        .where(HRSession.user_id == user_id, HRSession.event_id == event_id)
        .limit(1)
    )
    return result.scalar_one_or_none() is not None


async def require_attendance(
    event_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> User:
    """The signed-in person, if they were at this event.

    A 403 and not a 404: the event exists and the honest answer is "not with
    this account". The screen says that in those words rather than showing an
    empty feed, because an empty state is a claim about the world.
    """
    await _event_or_404(db, event_id)
    if not await was_there(db, user.id, event_id):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Esse rolê é de quem estava lá. Sua noite não chegou aqui.",
        )
    return user


async def _posts_with_reactions(
    db: AsyncSession, event_id: uuid.UUID, viewer_id: uuid.UUID
) -> list[FeedPostResponse]:
    rows = await db.execute(
        select(EventPost, User)
        .join(User, User.id == EventPost.user_id)
        .where(EventPost.event_id == event_id, EventPost.deleted_at.is_(None))
        .order_by(EventPost.created_at.desc())
    )
    posts = list(rows.all())
    if not posts:
        return []

    ids = [p.id for p, _ in posts]
    counts = dict(
        (
            await db.execute(
                select(EventPostReaction.post_id, func.count())
                .where(EventPostReaction.post_id.in_(ids))
                .group_by(EventPostReaction.post_id)
            )
        ).all()
    )
    mine = {
        r
        for (r,) in (
            await db.execute(
                select(EventPostReaction.post_id).where(
                    EventPostReaction.post_id.in_(ids),
                    EventPostReaction.user_id == viewer_id,
                )
            )
        ).all()
    }
    return [
        FeedPostResponse.of(
            post,
            author=author,
            reactions=counts.get(post.id, 0),
            reacted_by_me=post.id in mine,
            mine=post.user_id == viewer_id,
        )
        for post, author in posts
    ]


@router.get("/{event_id}/feed", response_model=EventFeedResponse)
async def get_event_feed(
    event_id: uuid.UUID,
    user: User = Depends(require_attendance),
    db: AsyncSession = Depends(get_db),
):
    """Everybody who was at this event and chose to show a moment."""
    event = await _event_or_404(db, event_id)
    return EventFeedResponse(
        event_id=event.id,
        event_name=event.name,
        venue=event.venue,
        date=event.date,
        posts=await _posts_with_reactions(db, event_id, user.id),
    )


@router.post(
    "/{event_id}/feed",
    response_model=FeedPostResponse,
    status_code=status.HTTP_201_CREATED,
)
async def post_moment(
    event_id: uuid.UUID,
    body: FeedPostCreate,
    user: User = Depends(require_attendance),
    db: AsyncSession = Depends(get_db),
):
    """Put one of your moments in front of the people who were there.

    The session must be **yours and at this event**: the post carries a heart
    rate, and nobody publishes somebody else's. The moment is copied rather
    than joined, so re-analysing the night later cannot rewrite what other
    people have already read.
    """
    result = await db.execute(
        select(HRSession).where(
            HRSession.id == body.session_id,
            HRSession.user_id == user.id,
            HRSession.event_id == event_id,
        )
    )
    if result.scalar_one_or_none() is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Essa noite não é sua ou não é deste evento.",
        )

    post = EventPost(
        event_id=event_id,
        user_id=user.id,
        session_id=body.session_id,
        bpm=body.bpm,
        moment_at=body.moment_at,
        label=body.label,
        quote=body.quote,
        skin=body.skin,
    )
    db.add(post)
    await db.flush()
    return FeedPostResponse.of(
        post, author=user, reactions=0, reacted_by_me=False, mine=True
    )


@router.delete("/{event_id}/feed/{post_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_post(
    event_id: uuid.UUID,
    post_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Take it down.

    Deliberately **not** behind [require_attendance]: somebody must be able to
    withdraw what they published even if their night was deleted first. The
    consent to publish health data is only real while the undo is.
    """
    result = await db.execute(
        select(EventPost).where(
            EventPost.id == post_id,
            EventPost.event_id == event_id,
            EventPost.user_id == user.id,
        )
    )
    post = result.scalar_one_or_none()
    if post is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Post não encontrado"
        )
    post.deleted_at = datetime.now(UTC)
    await db.flush()


@router.post("/{event_id}/feed/{post_id}/senti", response_model=FeedPostResponse)
async def toggle_reaction(
    event_id: uuid.UUID,
    post_id: uuid.UUID,
    user: User = Depends(require_attendance),
    db: AsyncSession = Depends(get_db),
):
    """SENTI TB — the only reaction there is, and it toggles."""
    result = await db.execute(
        select(EventPost).where(
            EventPost.id == post_id,
            EventPost.event_id == event_id,
            EventPost.deleted_at.is_(None),
        )
    )
    post = result.scalar_one_or_none()
    if post is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Post não encontrado"
        )

    existing = await db.execute(
        select(EventPostReaction).where(
            EventPostReaction.post_id == post_id,
            EventPostReaction.user_id == user.id,
        )
    )
    row = existing.scalar_one_or_none()
    if row is None:
        db.add(EventPostReaction(post_id=post_id, user_id=user.id))
    else:
        await db.delete(row)
    await db.flush()

    author = (
        await db.execute(select(User).where(User.id == post.user_id))
    ).scalar_one()
    count = (
        await db.execute(
            select(func.count()).where(EventPostReaction.post_id == post_id)
        )
    ).scalar_one()
    return FeedPostResponse.of(
        post,
        author=author,
        reactions=count,
        reacted_by_me=row is None,
        mine=post.user_id == user.id,
    )


@router.get("/{event_id}/crowd", response_model=CrowdResponse)
async def get_crowd(
    event_id: uuid.UUID,
    user: User = Depends(require_attendance),
    db: AsyncSession = Depends(get_db),
):
    """Card 04 — the event as a crowd, or an honest refusal to say.

    Nothing is published below the floor in `services/crowd`, and the count it
    refuses on comes back anyway so the screen can say *"ainda somos poucos
    aqui"* instead of drawing a zero.
    """
    await _event_or_404(db, event_id)

    sessions = list(
        (await db.execute(select(HRSession.id).where(HRSession.event_id == event_id)))
        .scalars()
        .all()
    )
    # One peak per person — their biggest. Somebody with nine peaks is still
    # one person, and the question is how many *people* rose at once.
    top_peaks: list[dict] = []
    if sessions:
        rows = await db.execute(
            select(Peak.session_id, Peak.timestamp, Peak.bpm)
            .where(Peak.session_id.in_(sessions), Peak.rank == 1)
            .order_by(Peak.timestamp)
        )
        labels = await _peak_labels(db, sessions)
        top_peaks = [
            {"at": timestamp, "label": labels.get((session_id, timestamp))}
            for session_id, timestamp, _bpm in rows.all()
        ]

    shared = (
        await db.execute(
            select(func.count()).where(
                EventPost.event_id == event_id, EventPost.deleted_at.is_(None)
            )
        )
    ).scalar_one()

    crowd = collective_moments(
        top_peaks, measured_nights=len(sessions), shared_count=shared
    )
    return CrowdResponse.of(crowd)


async def _peak_labels(
    db: AsyncSession, session_ids: list[uuid.UUID]
) -> dict[tuple[uuid.UUID, datetime], str]:
    """What each top peak was called, where the correlator named one."""
    from app.models.event_timeline import EventTimeline

    rows = await db.execute(
        select(Peak.session_id, Peak.timestamp, EventTimeline.label)
        .join(EventTimeline, EventTimeline.id == Peak.timeline_entry_id)
        .where(Peak.session_id.in_(session_ids), Peak.rank == 1)
    )
    return {(s, t): label for s, t, label in rows.all() if label}
