"""The level above one night: a tour, a club, a championship (#33, 22/09).

Felipe's call, on the proposal that *"a unidade social de música é o fandom,
não a sala"*: the feed of one event stays — intimate, the people who were in
that room — and above it sits the series, where somebody who went in São
Paulo meets somebody who went in Rio. Added, never put in the event's place.

Two rules carry over from the event feed and neither is relaxed:

- **The gate is evidence.** A measured night at *any* date of the series —
  not a claim of being a fan.
- **Consent is per post and per audience.** A post reaches this feed only if
  its author chose the series when posting (`series_posts`); everything
  posted to the rolê stays in the rolê.
"""

import uuid

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.feed import block, render_posts, report, toggle
from app.core.auth import get_current_user, require_admin
from app.core.database import get_db
from app.models.event import Event
from app.models.event_post import EventPost
from app.models.event_series import EventSeries, EventSeriesMember, SeriesPost
from app.models.hr_session import HRSession
from app.models.user import User
from app.schemas.feed import (
    FeedPostResponse,
    ReportRequest,
    SeriesAssign,
    SeriesBrief,
    SeriesCreate,
    SeriesEvent,
    SeriesFeedResponse,
)

router = APIRouter(tags=["series"])


async def _series_or_404(db: AsyncSession, series_id: uuid.UUID) -> EventSeries:
    series = (
        await db.execute(select(EventSeries).where(EventSeries.id == series_id))
    ).scalar_one_or_none()
    if series is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Turnê não encontrada"
        )
    return series


async def _brief(db: AsyncSession, series: EventSeries) -> SeriesBrief:
    dates = (
        await db.execute(
            select(func.count()).where(EventSeriesMember.series_id == series.id)
        )
    ).scalar_one()
    return SeriesBrief(id=series.id, name=series.name, kind=series.kind, dates=dates)


async def series_of(db: AsyncSession, event_id: uuid.UUID) -> SeriesBrief | None:
    """The series an event belongs to, or None."""
    series = (
        await db.execute(
            select(EventSeries)
            .join(EventSeriesMember, EventSeriesMember.series_id == EventSeries.id)
            .where(EventSeriesMember.event_id == event_id)
        )
    ).scalar_one_or_none()
    return await _brief(db, series) if series is not None else None


async def was_at_series(
    db: AsyncSession, user_id: uuid.UUID, series_id: uuid.UUID
) -> bool:
    """A measured night at any date of the series."""
    found = await db.execute(
        select(HRSession.id)
        .join(EventSeriesMember, EventSeriesMember.event_id == HRSession.event_id)
        .where(HRSession.user_id == user_id, EventSeriesMember.series_id == series_id)
        .limit(1)
    )
    return found.scalar_one_or_none() is not None


async def require_series_attendance(
    series_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> User:
    """The signed-in person, if they have a night at any date of this series."""
    await _series_or_404(db, series_id)
    if not await was_at_series(db, user.id, series_id):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Esse feed é de quem foi em alguma data. Sua noite não chegou aqui.",
        )
    return user


async def _series_post(
    db: AsyncSession, series_id: uuid.UUID, post_id: uuid.UUID
) -> EventPost:
    post = (
        await db.execute(
            select(EventPost)
            .join(SeriesPost, SeriesPost.post_id == EventPost.id)
            .where(
                SeriesPost.series_id == series_id,
                EventPost.id == post_id,
                EventPost.deleted_at.is_(None),
            )
        )
    ).scalar_one_or_none()
    if post is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Post não encontrado"
        )
    return post


# --- The fan's side ---


@router.get("/api/events/{event_id}/series", response_model=SeriesBrief | None)
async def event_series(event_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    """Which series an event belongs to — public, like the event itself."""
    return await series_of(db, event_id)


@router.get("/api/series/{series_id}/feed", response_model=SeriesFeedResponse)
async def series_feed(
    series_id: uuid.UUID,
    user: User = Depends(require_series_attendance),
    db: AsyncSession = Depends(get_db),
):
    """Every post shown to the series, from every date, newest first."""
    series = await _series_or_404(db, series_id)
    events = list(
        (
            await db.execute(
                select(Event)
                .join(EventSeriesMember, EventSeriesMember.event_id == Event.id)
                .where(EventSeriesMember.series_id == series_id)
                .order_by(Event.date)
            )
        )
        .scalars()
        .all()
    )
    rows = await db.execute(
        select(EventPost, User)
        .join(SeriesPost, SeriesPost.post_id == EventPost.id)
        .join(User, User.id == EventPost.user_id)
        .where(SeriesPost.series_id == series_id, EventPost.deleted_at.is_(None))
        .order_by(EventPost.created_at.desc())
    )
    return SeriesFeedResponse(
        series_id=series.id,
        name=series.name,
        kind=series.kind,
        events=[
            SeriesEvent(id=e.id, name=e.name, date=e.date, city=e.city) for e in events
        ],
        posts=await render_posts(
            db, list(rows.all()), user.id, events={e.id: e for e in events}
        ),
    )


@router.post(
    "/api/series/{series_id}/feed/{post_id}/senti", response_model=FeedPostResponse
)
async def series_senti(
    series_id: uuid.UUID,
    post_id: uuid.UUID,
    user: User = Depends(require_series_attendance),
    db: AsyncSession = Depends(get_db),
):
    return await toggle(db, await _series_post(db, series_id, post_id), user)


@router.post(
    "/api/series/{series_id}/feed/{post_id}/report",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def series_report(
    series_id: uuid.UUID,
    post_id: uuid.UUID,
    body: ReportRequest,
    user: User = Depends(require_series_attendance),
    db: AsyncSession = Depends(get_db),
):
    await report(db, await _series_post(db, series_id, post_id), user, body.reason)


@router.post(
    "/api/series/{series_id}/feed/{post_id}/block",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def series_block(
    series_id: uuid.UUID,
    post_id: uuid.UUID,
    user: User = Depends(require_series_attendance),
    db: AsyncSession = Depends(get_db),
):
    await block(db, await _series_post(db, series_id, post_id), user)


# --- The operator's side ---


@router.get("/api/series", response_model=list[SeriesBrief])
async def list_series(db: AsyncSession = Depends(get_db)):
    rows = (await db.execute(select(EventSeries).order_by(EventSeries.name))).scalars()
    return [await _brief(db, s) for s in rows.all()]


@router.post(
    "/api/series", response_model=SeriesBrief, status_code=status.HTTP_201_CREATED
)
async def create_series(
    body: SeriesCreate,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    series = EventSeries(name=body.name.strip(), kind=body.kind)
    db.add(series)
    await db.flush()
    return await _brief(db, series)


@router.put("/api/events/{event_id}/series", response_model=SeriesBrief | None)
async def assign_series(
    event_id: uuid.UUID,
    body: SeriesAssign,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Put an event in a series, move it, or take it out (null)."""
    event = (
        await db.execute(select(Event).where(Event.id == event_id))
    ).scalar_one_or_none()
    if event is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado"
        )
    await db.execute(
        delete(EventSeriesMember).where(EventSeriesMember.event_id == event_id)
    )
    if body.series_id is not None:
        await _series_or_404(db, body.series_id)
        db.add(EventSeriesMember(event_id=event_id, series_id=body.series_id))
    await db.flush()
    return await series_of(db, event_id)
