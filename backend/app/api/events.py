import uuid
from datetime import date, datetime
from zoneinfo import ZoneInfo

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.core.auth import get_current_user, require_admin
from app.core.database import get_db
from app.models.event import Event
from app.models.event_timeline import EventTimeline
from app.models.user import User
from app.schemas.event import (
    EventCreateRequest,
    EventDetailResponse,
    EventResponse,
    EventUpdateRequest,
    FixtureAttachRequest,
    FixtureBrief,
    SetlistAttachRequest,
    SetlistBrief,
    TimelineEntryCreate,
    TimelineEntryResponse,
)
from app.services import football_service, setlist_service

router = APIRouter(prefix="/api/events", tags=["events"])

# Events are TumTum's; the fan never creates one (product rule, 21/09). What
# was "any signed-in person can fix one" while the people using this could be
# counted is now the operator's: the accounts in `admin_emails`, checked by
# the server. The Android operator sheet and the site's event pages both
# reach these with the operator's own account.


@router.post("", response_model=EventResponse, status_code=status.HTTP_201_CREATED)
async def create_event(
    body: EventCreateRequest,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    event = Event(**body.model_dump())
    db.add(event)
    await db.flush()
    return event


# The POST twin exists for the Android app: java.net.HttpURLConnection —
# deliberately the app's only HTTP client — refuses the PATCH verb outright
# (ProtocolException, a fixed method list that predates PATCH). Same handler,
# same semantics, hidden from the schema so the API still reads as PATCH.
@router.patch("/{event_id}", response_model=EventResponse)
@router.post("/{event_id}", response_model=EventResponse, include_in_schema=False)
async def update_event(
    event_id: uuid.UUID,
    body: EventUpdateRequest,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Correct an event someone already created.

    There was no way to do this at all: an event typed with the wrong date
    could only be abandoned and typed again, leaving the wrong one in the list
    on the night it mattered. A wrong date is not cosmetic — it is what ties a
    capture to the moments inside an event.
    """
    result = await db.execute(select(Event).where(Event.id == event_id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado"
        )

    changes = body.model_dump(exclude_unset=True, exclude_none=True)
    for field, value in changes.items():
        setattr(event, field, value)
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
            or_(
                Event.name.ilike(pattern),
                Event.venue.ilike(pattern),
                Event.subtitle.ilike(pattern),
            )
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


# --- The two sources a timeline can be built from. Operator only: each call
# spends a request from a daily quota, and the result rewrites an event that
# everyone at it shares. Declared before ``/{event_id}`` so "sources" is never
# read as an id.


@router.get("/sources/football", response_model=list[FixtureBrief])
async def search_football_fixtures(
    team: str | None = Query(None, min_length=2),
    on: date | None = Query(None, description="Match date, YYYY-MM-DD"),
    _: User = Depends(require_admin),
):
    """Matches on API-Football, to pick the one an event is."""
    if not settings.api_football_key:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="A chave da API-Football não está configurada no servidor.",
        )
    if not team and not on:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Informe o time ou a data.",
        )
    fixtures = await football_service.search_fixtures(
        team_name=team, date=on.isoformat() if on else None
    )
    return [FixtureBrief.from_api(f) for f in fixtures]


@router.get("/sources/setlist", response_model=list[SetlistBrief])
async def search_setlists(
    artist: str = Query(..., min_length=2),
    on: date | None = Query(None, description="Show date, YYYY-MM-DD"),
    _: User = Depends(require_admin),
):
    """Setlists on Setlist.fm for an artist, to pick the one a show is."""
    if not settings.setlist_fm_api_key:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="A chave do Setlist.fm não está configurada no servidor.",
        )
    result = await setlist_service.search_setlists(
        artist_name=artist, date=on.strftime("%d-%m-%Y") if on else None
    )
    return [SetlistBrief.from_api(s) for s in result.get("setlist", [])]


@router.get("/{event_id}", response_model=EventDetailResponse)
async def get_event(event_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Event).where(Event.id == event_id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado"
        )
    return event


@router.post(
    "/{event_id}/timeline",
    response_model=TimelineEntryResponse,
    status_code=status.HTTP_201_CREATED,
)
async def add_timeline_entry(
    event_id: uuid.UUID,
    body: TimelineEntryCreate,
    _: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    # Verify event exists
    result = await db.execute(select(Event).where(Event.id == event_id))
    if not result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado"
        )

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
    return await _timeline(db, event_id)


@router.delete(
    "/{event_id}/timeline/{entry_id}", status_code=status.HTTP_204_NO_CONTENT
)
async def delete_timeline_entry(
    event_id: uuid.UUID,
    entry_id: uuid.UUID,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Remove one entry — a mark tapped by mistake, a song that was not played."""
    result = await db.execute(
        select(EventTimeline).where(
            EventTimeline.id == entry_id, EventTimeline.event_id == event_id
        )
    )
    entry = result.scalar_one_or_none()
    if not entry:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Marca não encontrada"
        )
    await db.delete(entry)
    await db.flush()


@router.post(
    "/{event_id}/timeline/football", response_model=list[TimelineEntryResponse]
)
async def attach_fixture(
    event_id: uuid.UUID,
    body: FixtureAttachRequest,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Build the event's timeline from a match on API-Football.

    Every goal and card the API reports becomes an entry, at the wall-clock
    time the operator's two anchor taps (APITO INICIAL, 2º TEMPO) put it at
    — or, with no tap, at the schedule's guess, marked as such so it offers a
    name rather than asserting one. Safe to call again after the taps land or
    the match ends: the rows this source wrote before are replaced, and every
    other entry (the taps themselves, what a person typed) stays.
    """
    event = await _event_or_404(db, event_id)
    if not settings.api_football_key:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="A chave da API-Football não está configurada no servidor.",
        )
    fixture = await football_service.get_fixture(body.fixture_id)
    if not fixture:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Jogo não encontrado na API-Football.",
        )
    api_events = await football_service.get_fixture_events(body.fixture_id)

    existing = await _timeline(db, event_id)
    kickoff_at, second_half_at = football_service.anchors_from_timeline(
        [{"timestamp": e.timestamp, "entry_type": e.entry_type} for e in existing]
    )
    entries = football_service.parse_fixture_to_timeline(
        fixture, api_events, kickoff_at=kickoff_at, second_half_at=second_half_at
    )
    await _replace_source(db, existing, football_service.SOURCE, event_id, entries)
    event.external_id = f"{football_service.SOURCE}:{body.fixture_id}"
    await db.flush()
    return await _timeline(db, event_id)


@router.post("/{event_id}/timeline/setlist", response_model=list[TimelineEntryResponse])
async def attach_setlist(
    event_id: uuid.UUID,
    body: SetlistAttachRequest,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Build the event's timeline from a setlist on Setlist.fm.

    Order only, never times: each song is placed four minutes after the last,
    from the event's own start, and every entry is marked estimated so it
    reaches a fan as a guess to pick from and never as a name on a card.
    """
    event = await _event_or_404(db, event_id)
    if not settings.setlist_fm_api_key:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="A chave do Setlist.fm não está configurada no servidor.",
        )
    if event.start_time is None:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="O evento precisa de um horário de começo para estimar o setlist.",
        )
    setlist = await setlist_service.get_setlist_by_id(body.setlist_id)
    if not setlist:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Setlist não encontrado no Setlist.fm.",
        )
    # The stored time's offset carries no information (see offset_aware): the
    # digits are the event's own wall clock, in the display timezone.
    start = datetime.combine(
        event.date,
        event.start_time.replace(tzinfo=None),
        tzinfo=ZoneInfo(settings.display_timezone),
    )
    entries = setlist_service.parse_setlist_to_timeline(setlist, start)
    existing = await _timeline(db, event_id)
    await _replace_source(db, existing, setlist_service.SOURCE, event_id, entries)
    event.external_id = f"{setlist_service.SOURCE}:{body.setlist_id}"
    await db.flush()
    return await _timeline(db, event_id)


async def _event_or_404(db: AsyncSession, event_id: uuid.UUID) -> Event:
    result = await db.execute(select(Event).where(Event.id == event_id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado"
        )
    return event


async def _timeline(db: AsyncSession, event_id: uuid.UUID) -> list[EventTimeline]:
    result = await db.execute(
        select(EventTimeline)
        .where(EventTimeline.event_id == event_id)
        .order_by(EventTimeline.timestamp)
    )
    return list(result.scalars().all())


async def _replace_source(
    db: AsyncSession,
    existing: list[EventTimeline],
    source: str,
    event_id: uuid.UUID,
    entries: list[dict],
) -> None:
    """Swap the rows one source wrote for its new ones; touch nothing else."""
    for row in existing:
        if (row.metadata_ or {}).get("source") == source:
            await db.delete(row)
    for entry in entries:
        db.add(
            EventTimeline(
                event_id=event_id,
                timestamp=entry["timestamp"],
                label=entry["label"],
                entry_type=entry["entry_type"],
                metadata_=entry["metadata"],
            )
        )
    await db.flush()
