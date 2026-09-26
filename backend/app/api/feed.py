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
from dataclasses import dataclass
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import delete, func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.event import Event
from app.models.event_post import EventPost, EventPostReaction
from app.models.event_series import SeriesPost
from app.models.hr_session import HRSession
from app.models.moderation import PostReport, UserBlock
from app.models.peak import Peak
from app.models.user import User
from app.schemas.feed import (
    CrowdResponse,
    EventFeedResponse,
    FeedPostCreate,
    FeedPostResponse,
    ReportRequest,
    SeriesEvent,
)
from app.services import consents, moderation
from app.services.crowd import collective_moments
from app.services.email import EmailNotConfigured, send_email

router = APIRouter(prefix="/api/events", tags=["feed"])


async def _event_or_404(db: AsyncSession, event_id: uuid.UUID) -> Event:
    result = await db.execute(select(Event).where(Event.id == event_id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado"
        )
    return event


# The source every simulated night carries (`/api/demo/simulate`). A demo is
# not a night anybody lived, so it is neither proof of having been there nor
# a heart in the crowd (LGPD audit, AL-10).
DEMO_SOURCE_PREFIX = "Tumtum Demo"


def measured(column=HRSession.source_device):
    """The SQL condition that a night was really measured, not simulated."""
    return or_(column.is_(None), ~column.like(f"{DEMO_SOURCE_PREFIX}%"))


async def was_there(db: AsyncSession, user_id: uuid.UUID, event_id: uuid.UUID) -> bool:
    """Whether this account has a measured night at this event."""
    result = await db.execute(
        select(HRSession.id)
        .where(
            HRSession.user_id == user_id,
            HRSession.event_id == event_id,
            measured(),
        )
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
            detail="Esse feed é de quem estava lá. Sua noite não chegou aqui.",
        )
    return user


async def _blocks_of(db: AsyncSession, viewer_id: uuid.UUID) -> set[tuple]:
    """Every block this person is part of, as (blocker, blocked) pairs."""
    rows = await db.execute(
        select(UserBlock.blocker_id, UserBlock.blocked_id).where(
            (UserBlock.blocker_id == viewer_id) | (UserBlock.blocked_id == viewer_id)
        )
    )
    return {(a, b) for a, b in rows.all()}


async def _open_report_counts(
    db: AsyncSession, post_ids: list[uuid.UUID]
) -> dict[uuid.UUID, int]:
    if not post_ids:
        return {}
    rows = await db.execute(
        select(PostReport.post_id, func.count())
        .where(PostReport.post_id.in_(post_ids), PostReport.resolved_at.is_(None))
        .group_by(PostReport.post_id)
    )
    return dict(rows.all())


async def attended_events(
    db: AsyncSession, user_id: uuid.UUID, event_ids: list[uuid.UUID]
) -> set[uuid.UUID]:
    """Which of these events this account has a measured night at."""
    if not event_ids:
        return set()
    rows = await db.execute(
        select(HRSession.event_id)
        .where(
            HRSession.user_id == user_id,
            HRSession.event_id.in_(event_ids),
            measured(),
        )
        .distinct()
    )
    return set(rows.scalars().all())


async def feed_events(db: AsyncSession, event: Event) -> tuple:
    """The feed an event opens onto: (series or None, its events by date).

    One feed per event (#65, Felipe 23/09): the tour's when the event is in
    one, the event's own when it is not. The night becomes a filter inside
    it, never a second screen.
    """
    from app.api.series import series_events, series_of

    series = await series_of(db, event.id)
    if series is None:
        return None, [event]
    return series, await series_events(db, series.id)


async def feed_rows(
    db: AsyncSession, viewer_id: uuid.UUID, series, events: list[Event]
) -> list[tuple]:
    """(post, author) rows this person may see in the feed, newest first.

    One rule, whichever date a post is from: a post is seen by the people who
    were at its own night, and by everybody at any date of the tour only if
    its author said so when posting (`series_posts`). Posts made before the
    feeds became one, "só pro rolê", keep exactly the audience they were
    given — they are not silently widened.
    """
    ids = [e.id for e in events]
    attended = await attended_events(db, viewer_id, ids)
    query = (
        select(EventPost, User)
        .join(User, User.id == EventPost.user_id)
        .where(EventPost.event_id.in_(ids), EventPost.deleted_at.is_(None))
        .order_by(EventPost.created_at.desc())
    )
    if series is not None:
        query = query.outerjoin(
            SeriesPost,
            (SeriesPost.post_id == EventPost.id) & (SeriesPost.series_id == series.id),
        ).where(
            SeriesPost.post_id.is_not(None) | EventPost.event_id.in_(list(attended))
        )
    else:
        query = query.where(EventPost.event_id.in_(list(attended)))
    return list((await db.execute(query)).all())


@dataclass
class Rendered:
    """The posts a feed shows, and how many a block kept out of it (#63).

    The count is what lets an empty feed tell "nobody posted" apart from
    "somebody did, and you blocked them" — an empty state is a claim.
    """

    posts: list[FeedPostResponse]
    hidden_by_block: int


async def render_posts(
    db: AsyncSession,
    posts: list[tuple],
    viewer_id: uuid.UUID,
    events: dict | None = None,
) -> Rendered:
    """(post, author) rows as the feed shows them, moderation applied.

    Shared by the event feed and the series feed (#33), so a block or a
    report means the same thing on both. ``events`` maps an event id to the
    event, so each post says which night it is from.
    """

    # Moderation (#36): a block hides both people from each other, and a post
    # with enough open reports waits for an operator — except for its own
    # author, who is not told their post vanished by having it vanish.
    blocks = await _blocks_of(db, viewer_id)
    reported = await _open_report_counts(db, [p.id for p, _ in posts])
    unblocked = [
        (post, author)
        for post, author in posts
        if not moderation.blocked_either_way(viewer_id, post.user_id, blocks)
    ]
    hidden_by_block = len(posts) - len(unblocked)
    posts = [
        (post, author)
        for post, author in unblocked
        if post.user_id == viewer_id
        or not moderation.hidden_by_reports(reported.get(post.id, 0))
    ]
    if not posts:
        return Rendered(posts=[], hidden_by_block=hidden_by_block)

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
    return Rendered(
        posts=[
            FeedPostResponse.of(
                post,
                author=author,
                reactions=counts.get(post.id, 0),
                reacted_by_me=post.id in mine,
                mine=post.user_id == viewer_id,
                event=(events or {}).get(post.event_id),
            )
            for post, author in posts
        ],
        hidden_by_block=hidden_by_block,
    )


@router.get("/{event_id}/feed", response_model=EventFeedResponse)
async def get_event_feed(
    event_id: uuid.UUID,
    user: User = Depends(require_attendance),
    db: AsyncSession = Depends(get_db),
):
    """The one feed this event opens onto — its tour's, when it has one (#65)."""
    event = await _event_or_404(db, event_id)
    series, events = await feed_events(db, event)
    rendered = await render_posts(
        db,
        await feed_rows(db, user.id, series, events),
        user.id,
        events={e.id: e for e in events},
    )
    return EventFeedResponse(
        event_id=event.id,
        event_name=event.name,
        venue=event.venue,
        date=event.date,
        posts=rendered.posts,
        series=series,
        events=[SeriesEvent.of(e) for e in events],
        hidden_by_block=rendered.hidden_by_block,
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

    # The wider audience, only when asked for at this moment (#33, #65). The
    # row is the consent; an event outside any tour has nowhere wider to go.
    if body.to_series:
        from app.api.series import series_of

        series = await series_of(db, event_id)
        if series is not None:
            db.add(SeriesPost(post_id=post.id, series_id=series.id))
            await db.flush()

    event = await _event_or_404(db, event_id)
    return FeedPostResponse.of(
        post, author=user, reactions=0, reacted_by_me=False, mine=True, event=event
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

    The post is found by its id and its owner alone: in a tour's feed your
    post from another date sits next to this one, and taking it down must not
    depend on which date's door you came in through (#65).
    """
    result = await db.execute(
        select(EventPost).where(
            EventPost.id == post_id,
            EventPost.user_id == user.id,
        )
    )
    post = result.scalar_one_or_none()
    if post is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Post não encontrado"
        )
    post.deleted_at = datetime.now(UTC)
    # Its place in the series feed goes with it — the consent row, withdrawn.
    await db.execute(delete(SeriesPost).where(SeriesPost.post_id == post.id))
    await db.flush()


@router.post("/{event_id}/feed/{post_id}/senti", response_model=FeedPostResponse)
async def toggle_reaction(
    event_id: uuid.UUID,
    post_id: uuid.UUID,
    user: User = Depends(require_attendance),
    db: AsyncSession = Depends(get_db),
):
    """SENTI TB — the only reaction there is, and it toggles."""
    return await toggle(db, await _visible_post(db, event_id, post_id, user.id), user)


async def toggle(db: AsyncSession, post: EventPost, user: User) -> FeedPostResponse:
    """The reaction itself, shared with the series feed (#33)."""
    existing = await db.execute(
        select(EventPostReaction).where(
            EventPostReaction.post_id == post.id,
            EventPostReaction.user_id == user.id,
        )
    )
    row = existing.scalar_one_or_none()
    if row is None:
        db.add(EventPostReaction(post_id=post.id, user_id=user.id))
    else:
        await db.delete(row)
    await db.flush()

    author = (
        await db.execute(select(User).where(User.id == post.user_id))
    ).scalar_one()
    count = (
        await db.execute(
            select(func.count()).where(EventPostReaction.post_id == post.id)
        )
    ).scalar_one()
    event = await _event_or_404(db, post.event_id)
    return FeedPostResponse.of(
        post,
        author=author,
        reactions=count,
        reacted_by_me=row is None,
        mine=post.user_id == user.id,
        event=event,
    )


@router.get("/{event_id}/crowd", response_model=CrowdResponse)
async def get_crowd(
    event_id: uuid.UUID,
    user: User = Depends(require_attendance),
    db: AsyncSession = Depends(get_db),
):
    """Card 04 — the event as a crowd, or an honest refusal to say.

    Only the nights of people who granted `crowd_stats` count, and never a
    simulated one. Nothing is published below `settings.crowd_min_nights`;
    the screen gets `enough=false` and says *"ainda somos poucos aqui"*
    instead of drawing a zero.
    """
    await _event_or_404(db, event_id)

    sessions = list(
        (
            await db.execute(
                select(HRSession.id).where(
                    HRSession.event_id == event_id,
                    measured(),
                    HRSession.user_id.in_(consents.active_consent_users("crowd_stats")),
                )
            )
        )
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
        top_peaks,
        measured_nights=len(sessions),
        shared_count=shared,
        min_nights=settings.crowd_min_nights,
        min_cell=settings.crowd_min_cell,
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


async def _visible_post(
    db: AsyncSession, event_id: uuid.UUID, post_id: uuid.UUID, viewer_id: uuid.UUID
) -> EventPost:
    """A post in the feed this event opens onto that this person may see.

    The same rule as [feed_rows], asked of one post: a tap on something you
    cannot see is refused exactly as if it did not exist.
    """
    event = await _event_or_404(db, event_id)
    series, events = await feed_events(db, event)
    visible = [
        p for p, _ in await feed_rows(db, viewer_id, series, events) if p.id == post_id
    ]
    if not visible:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Post não encontrado"
        )
    return visible[0]


@router.post(
    "/{event_id}/feed/{post_id}/report", status_code=status.HTTP_204_NO_CONTENT
)
async def report_post(
    event_id: uuid.UUID,
    post_id: uuid.UUID,
    body: ReportRequest,
    user: User = Depends(require_attendance),
    db: AsyncSession = Depends(get_db),
):
    """Say a post should not be there. It lands where a person reads it.

    Only somebody who can see the post can report it, and once: a second
    report from the same person changes nothing and is not an error, because
    the person's intent was the same both times.
    """
    await report(
        db, await _visible_post(db, event_id, post_id, user.id), user, body.reason
    )


async def report(db: AsyncSession, post: EventPost, user: User, reason: str | None):
    """One report, shared with the series feed (#33)."""
    if post.user_id == user.id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Esse é seu. Pra tirar, é só tocar em “tirar do feed”.",
        )
    already = (
        await db.execute(
            select(PostReport.id).where(
                PostReport.post_id == post.id, PostReport.reporter_id == user.id
            )
        )
    ).scalar_one_or_none()
    if already is not None:
        return
    db.add(
        PostReport(
            post_id=post.id,
            reporter_id=user.id,
            reason=moderation.clean_reason(reason),
        )
    )
    await db.flush()
    await _tell_operators(db, post)


async def _tell_operators(db: AsyncSession, post: EventPost) -> None:
    """An e-mail to every operator, when e-mail is configured. Never blocks the report."""
    if not settings.admins:
        return
    event = await _event_or_404(db, post.event_id)
    link = f"{settings.site_url}/admin/denuncias"
    text = (
        f"Um post no feed de “{event.name}” foi denunciado.\n\nVeja e decida em {link}"
    )
    for to in settings.admins:
        try:
            await send_email(
                to=to,
                subject="TumTum · denúncia no feed",
                html=f"<p>{text.replace(chr(10), '<br>')}</p>",
                text=text,
            )
        except EmailNotConfigured:
            return
        except Exception:  # a mail outage must not undo a report
            continue


@router.post("/{event_id}/feed/{post_id}/block", status_code=status.HTTP_204_NO_CONTENT)
async def block_author(
    event_id: uuid.UUID,
    post_id: uuid.UUID,
    user: User = Depends(require_attendance),
    db: AsyncSession = Depends(get_db),
):
    """Stop seeing the person who posted this — and stop being seen by them.

    Done from a post because nothing in the feed names a person any other
    way. Undone from the list in the app's settings.
    """
    await block(db, await _visible_post(db, event_id, post_id, user.id), user)


async def block(db: AsyncSession, post: EventPost, user: User):
    """One block, shared with the series feed (#33)."""
    if post.user_id == user.id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Não dá pra bloquear a si mesmo.",
        )
    exists = (
        await db.execute(
            select(UserBlock.id).where(
                UserBlock.blocker_id == user.id, UserBlock.blocked_id == post.user_id
            )
        )
    ).scalar_one_or_none()
    if exists is None:
        db.add(UserBlock(blocker_id=user.id, blocked_id=post.user_id))
        await db.flush()
