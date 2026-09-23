import re
import uuid
from datetime import UTC, date, datetime

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.core.auth import require_admin
from app.core.database import get_db
from app.models.event import Event
from app.models.event_setlist import EventSetlist
from app.models.event_timeline import EventTimeline
from app.models.user import User
from app.schemas.event import (
    EventCreateRequest,
    EventDetailResponse,
    EventResponse,
    EventUpdateRequest,
    FixtureAttachRequest,
    FixtureBrief,
    SetlistReplaceRequest,
    SetlistSong,
    SetlistStartRequest,
    TimelineEntryCreate,
    TimelineEntryResponse,
)
from app.services import football_service

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
    try:
        fixtures = await football_service.search_fixtures(
            team_name=team, date=on.isoformat() if on else None
        )
    except football_service.FootballApiError as exc:
        # Never an empty list for a failed question: an empty state is a
        # claim, and this one used to claim "no such match" for a wrong key,
        # a spent quota and a missing parameter alike (22/09).
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"A busca não foi feita — {exc.reason}",
        ) from exc
    return [FixtureBrief.from_api(f) for f in fixtures]


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
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """One entry on the event's timeline — an operator act (item 47, 22/09).

    **The timeline is the event's truth, not one person's note.** Every entry
    here names moments for *everybody* who was at that event, because the
    correlator reads this table for all of them. It accepted any signed-in
    account until now, which was wrong twice over: a fan typing "golaço kkkk"
    on their own moment wrote a label onto strangers' cards, and what they
    meant as a private note about their own night was visible to the operator
    and to anyone else at that event.

    So the two uses are separated rather than sharing one pipe. A fan naming
    their own moment now stays on their phone (`NightRepository.nameMoment`),
    where it already survives re-analysis; the event's timeline is written by
    the operator, the match feed, and the show's setlist screen.
    """
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
    try:
        fixture = await football_service.get_fixture(body.fixture_id)
        if not fixture:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Jogo não encontrado na API-Football.",
            )
        api_events = await football_service.get_fixture_events(body.fixture_id)
    except football_service.FootballApiError as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"A timeline não foi montada — {exc.reason}",
        ) from exc

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


# --- The operator's setlist for a show (22/09) ---
#
# Felipe's rule, made absolute the same day: the card arrives naming the
# moment, and nothing is asked of the fan. A match gets that from the API. A
# show has no API — so a person taps, and the tap is the measurement.


SETLIST_SOURCE = "operator-setlist"


def _clean_songs(raw: list[str]) -> list[str]:
    """What a pasted setlist becomes: one title a line, in order.

    People paste from anywhere, so the numbering that comes with it ("1.",
    "03 -", "12)") is stripped and blank lines are dropped. Nothing else is
    touched: a song's real name may contain anything at all.

    A dot or a bracket after the number is unambiguous numbering and may sit
    tight against the title. **A dash or a colon must be followed by a
    space**, because a title can open with a number and a hyphen — strip
    those blind and "1-800-273-8255" is filed as "800-273-8255".
    """
    songs = []
    for line in raw:
        title = re.sub(r"^\s*\d{1,3}\s*(?:[.)]\s*|[-–—:]\s+)", "", line or "").strip()
        if title:
            songs.append(title[:255])
    return songs


def _merge_started(
    existing: list[tuple[int, str, datetime | None]],
    songs: list[str],
) -> list[tuple[int, str, datetime | None]]:
    """The new order, keeping every time already measured.

    **A measured time belongs to the position, not to the title** (Felipe,
    2026-09-22). Until then the key was ``(position, title)``, on the reasoning
    that a song which moved should not carry its old stamp onto a new slot.
    That reasoning was about the wrong thing. What the operator taps is a
    *slot*: "the third thing started at 21h44" is a fact about the show's
    third thing, whatever it turns out to be called.

    Keying on the title made a typo destroy a measurement. Felipe corrected
    one word in a title and the row's time vanished, COMEÇOU lit up again as
    if the song had not started, and the only recovery the screen offered was
    to tap it — which would have written *now* into a song that began an hour
    earlier. **A false measurement is worse than a lost one**, and this had
    made the false one the easy path.

    So the times stay at their positions. Correcting a spelling, fixing the
    order after the fact, or extending the tail all keep the show's record
    intact. Nothing here ever invents a time: a position nobody tapped comes
    back ``None``, and shortening the list drops the tail's times with it.
    """
    kept = {position: at for position, _title, at in existing if at is not None}
    return [(i, title, kept.get(i)) for i, title in enumerate(songs, start=1)]


def _timeline_changes(
    entries: list[tuple[int, str]],
    merged: list[tuple[int, str, datetime | None]],
) -> tuple[dict[int, str], set[int]]:
    """What a corrected list does to the timeline: (renames, drops).

    The timeline entry is what names the moment on every fan's card, so a
    correction that stops at the setlist table is a correction nobody sees —
    Felipe fixed "Love Sensation" on 23/09 and the timeline kept the old
    name (#54). An entry whose position still has a measured time takes the
    new title; one whose position is gone, or no longer measured, goes.
    """
    now = {position: title for position, title, at in merged if at is not None}
    renames = {
        position: now[position]
        for position, label in entries
        if position in now and now[position] != label
    }
    drops = {position for position, _label in entries if position not in now}
    return renames, drops


async def _sync_timeline(
    db: AsyncSession,
    event_id: uuid.UUID,
    merged: list[tuple[int, str, datetime | None]],
) -> None:
    """Carry a corrected setlist onto the timeline entries its taps wrote."""
    result = await db.execute(
        select(EventTimeline).where(
            EventTimeline.event_id == event_id,
            EventTimeline.entry_type == "song_start",
        )
    )
    ours = [
        entry
        for entry in result.scalars().all()
        if (entry.metadata_ or {}).get("source") == SETLIST_SOURCE
        and isinstance((entry.metadata_ or {}).get("position"), int)
    ]
    renames, drops = _timeline_changes(
        [(e.metadata_["position"], e.label) for e in ours], merged
    )
    for entry in ours:
        position = entry.metadata_["position"]
        if position in drops:
            await db.delete(entry)
        elif position in renames:
            entry.label = renames[position]


async def _setlist(db: AsyncSession, event_id: uuid.UUID) -> list[EventSetlist]:
    result = await db.execute(
        select(EventSetlist)
        .where(EventSetlist.event_id == event_id)
        .order_by(EventSetlist.position)
    )
    return list(result.scalars().all())


@router.get("/{event_id}/setlist", response_model=list[SetlistSong])
async def get_setlist(
    event_id: uuid.UUID,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """The operator's script, and how far through it the show is."""
    await _event_or_404(db, event_id)
    return await _setlist(db, event_id)


@router.put("/{event_id}/setlist", response_model=list[SetlistSong])
async def replace_setlist(
    event_id: uuid.UUID,
    body: SetlistReplaceRequest,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Paste the order, before the show.

    A tour plays close to the same set every night, so last night's is a good
    draft and this can be done days ahead with no pressure. Replacing the
    list keeps every time already measured, at the position it was measured
    at — correcting song 14 during the show, or a typo in song 3's name, must
    not erase that the third song began at 21h44. See [_merge_started].
    """
    await _event_or_404(db, event_id)
    existing = await _setlist(db, event_id)
    merged = _merge_started(
        [(row.position, row.title, row.started_at) for row in existing],
        _clean_songs(body.songs),
    )
    for row in existing:
        await db.delete(row)
    await db.flush()

    for position, title, started_at in merged:
        db.add(
            EventSetlist(
                event_id=event_id,
                position=position,
                title=title,
                started_at=started_at,
            )
        )
    await _sync_timeline(db, event_id, merged)
    await db.flush()
    return await _setlist(db, event_id)


@router.post("/{event_id}/setlist/start", response_model=SetlistSong)
async def start_song(
    event_id: uuid.UUID,
    body: SetlistStartRequest,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """COMEÇOU — one tap, and the song has a measured time.

    The tap stamps the row and writes a ``song_start`` entry on the event's
    timeline, which is what the correlator reads and what names the moment on
    every fan's card. With no position given it advances to the first song
    nobody has started yet, so the whole show is one button.

    Missing a tap costs only that song: the next one re-anchors, and the gap
    is visible on the operator's screen rather than silently guessed at.
    """
    await _event_or_404(db, event_id)
    songs = await _setlist(db, event_id)
    if not songs:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Cole a ordem das músicas antes do show começar.",
        )

    if body.position is not None:
        song = next((s for s in songs if s.position == body.position), None)
        if song is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Música não encontrada."
            )
    else:
        song = next((s for s in songs if s.started_at is None), None)
        if song is None:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Todas as músicas já começaram.",
            )

    at = body.at or datetime.now(UTC)
    song.started_at = at

    # The timeline entry is the authority — the setlist row only lets the
    # operator's screen show what is done. A song re-tapped replaces its own
    # entry rather than giving the correlator two names for one moment.
    existing = await db.execute(
        select(EventTimeline).where(
            EventTimeline.event_id == event_id,
            EventTimeline.entry_type == "song_start",
        )
    )
    for entry in existing.scalars().all():
        meta = entry.metadata_ or {}
        if (
            meta.get("source") == SETLIST_SOURCE
            and meta.get("position") == song.position
        ):
            await db.delete(entry)

    db.add(
        EventTimeline(
            event_id=event_id,
            timestamp=at,
            label=song.title,
            entry_type="song_start",
            metadata_={
                "source": SETLIST_SOURCE,
                "position": song.position,
                # Measured, by a person who was standing there. This is the
                # whole point: it asserts a name instead of offering one.
                "anchored": True,
            },
        )
    )
    await db.flush()
    return song
