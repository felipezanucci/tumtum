import uuid
from datetime import date

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import select, or_
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.user import User
from app.models.event import Event
from app.models.event_timeline import EventTimeline
from app.schemas.event import (
    EventCreateRequest,
    EventResponse,
    EventDetailResponse,
    TimelineEntryCreate,
    TimelineEntryResponse,
)

router = APIRouter(prefix="/api/events", tags=["events"])


@router.post("", response_model=EventResponse, status_code=status.HTTP_201_CREATED)
async def create_event(
    body: EventCreateRequest,
    _: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    event = Event(**body.model_dump())
    db.add(event)
    await db.flush()
    return event


@router.get("", response_model=list[EventResponse])
async def list_events(
    q: str | None = Query(None),
    event_type: str | None = Query(None),
    city: str | None = Query(None),
    date_from: date | None = Query(None),
    date_to: date | None = Query(None),
    db: AsyncSession = Depends(get_db),
):
    query = select(Event)

    if q:
        pattern = f"%{q}%"
        query = query.where(
            or_(Event.name.ilike(pattern), Event.venue.ilike(pattern), Event.subtitle.ilike(pattern))
        )
    if event_type:
        query = query.where(Event.event_type == event_type)
    if city:
        query = query.where(Event.city.ilike(f"%{city}%"))
    if date_from:
        query = query.where(Event.date >= date_from)
    if date_to:
        query = query.where(Event.date <= date_to)

    query = query.order_by(Event.date.desc()).limit(50)
    result = await db.execute(query)
    return result.scalars().all()


@router.get("/{event_id}", response_model=EventDetailResponse)
async def get_event(event_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Event).where(Event.id == event_id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado")
    return event


@router.post("/{event_id}/timeline", response_model=TimelineEntryResponse, status_code=status.HTTP_201_CREATED)
async def add_timeline_entry(
    event_id: uuid.UUID,
    body: TimelineEntryCreate,
    _: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    # Verify event exists
    result = await db.execute(select(Event).where(Event.id == event_id))
    if not result.scalar_one_or_none():
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado")

    entry = EventTimeline(
        event_id=event_id,
        timestamp=body.timestamp,
        label=body.label,
        entry_type=body.entry_type,
        metadata_=body.metadata,
    )
    db.add(entry)
    await db.flush()
    return entry


@router.get("/{event_id}/timeline", response_model=list[TimelineEntryResponse])
async def get_timeline(event_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(EventTimeline)
        .where(EventTimeline.event_id == event_id)
        .order_by(EventTimeline.timestamp)
    )
    return result.scalars().all()
