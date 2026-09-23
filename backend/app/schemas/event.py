import uuid
from datetime import UTC, date, datetime, time

from pydantic import BaseModel, Field, field_validator

# A field called `date` hides the type called `date`. In `x: T = v` Python
# stores the value before evaluating the annotation, so by the time
# `date | None` is read, `date` is already the None just assigned — which the
# create schema never hit only because its `date` field has no default.
EventDate = date


def offset_aware(value: time | None) -> time | None:
    """Give a bare time an offset, because the column insists on one.

    `events.start_time` and `events.end_time` are `TIME WITH TIME ZONE`.
    asyncpg encodes that type as `obj.tzinfo.utcoffset(None)`, so a naive
    `datetime.time` — which is exactly what parsing "22:00:00" produces —
    raises `AttributeError` inside the driver and surfaces as a bare
    "Erro interno do servidor". Found on 2026-08-27 trying to put the
    festival's hours on its event; creating the event had worked only
    because the times were left empty and the columns never written.

    **The offset stored here carries no information and must not be read.**
    A time-of-day with an offset but no date cannot even account for a
    daylight rule, which is why Postgres's own documentation discourages the
    type. The real fix is migration 007, which drops the timezone from both
    columns — dormant until this project actually runs its migrations, since
    the deployed app calls `create_all` and that only ever creates missing
    tables. Once the column is a plain `time`, this coercion becomes a no-op:
    the cast keeps the wall-clock digits and discards the offset, which is
    all anybody was ever shown.
    """
    if value is None or value.tzinfo is not None:
        return value
    return value.replace(tzinfo=UTC)


# --- Event ---


class EventCreateRequest(BaseModel):
    name: str
    subtitle: str | None = None
    venue: str | None = None
    city: str | None = None
    country: str | None = None
    date: date
    start_time: time | None = None
    end_time: time | None = None
    event_type: str = Field(..., pattern="^(concert|sports|festival)$")
    external_id: str | None = None
    cover_image_url: str | None = None

    _offsets = field_validator("start_time", "end_time")(offset_aware)


class EventUpdateRequest(BaseModel):
    """Every field optional: a correction usually touches one of them.

    `None` means "leave this alone" rather than "clear this", which is the
    right trade here — the fields people clear are rare and the fields people
    fix are common.
    """

    name: str | None = None
    subtitle: str | None = None
    venue: str | None = None
    city: str | None = None
    country: str | None = None
    date: EventDate | None = None
    start_time: time | None = None
    end_time: time | None = None
    event_type: str | None = Field(None, pattern="^(concert|sports|festival)$")
    cover_image_url: str | None = None

    _offsets = field_validator("start_time", "end_time")(offset_aware)


class EventResponse(BaseModel):
    id: uuid.UUID
    name: str
    subtitle: str | None
    venue: str | None
    city: str | None
    country: str | None
    date: date
    start_time: time | None
    end_time: time | None
    event_type: str
    external_id: str | None
    cover_image_url: str | None
    created_at: datetime

    model_config = {"from_attributes": True}


class EventSearchQuery(BaseModel):
    q: str | None = None
    event_type: str | None = None
    city: str | None = None
    date_from: date | None = None
    date_to: date | None = None


# --- Event Timeline ---

ENTRY_TYPES = "^(song_start|goal|halftime|encore|highlight|kickoff|second_half)$"


class TimelineEntryCreate(BaseModel):
    timestamp: datetime
    label: str
    # ``kickoff`` and ``second_half`` are the operator's two anchor taps on a
    # match (22/09): the instants the halves really started, which turn every
    # API-Football minute into a wall-clock time for everyone at the game.
    entry_type: str = Field(..., pattern=ENTRY_TYPES)
    metadata: dict | None = None


class TimelineEntryResponse(BaseModel):
    id: uuid.UUID
    event_id: uuid.UUID
    timestamp: datetime
    label: str
    entry_type: str
    # The ORM column is ``metadata_`` because ``metadata`` on a SQLAlchemy model
    # is the table registry. Read from attributes without the alias, this field
    # got a MetaData object, failed validation, and every POST to the timeline
    # answered 500 — found on 2026-09-18, the first time a mark was sent.
    metadata: dict | None = Field(default=None, validation_alias="metadata_")

    model_config = {"from_attributes": True, "populate_by_name": True}


class EventDetailResponse(EventResponse):
    timeline: list[TimelineEntryResponse] = []


# --- Timeline sources (operator) ---


class FixtureBrief(BaseModel):
    """A match as API-Football lists it, enough to recognise the right one."""

    fixture_id: int
    kickoff: datetime | None
    home: str | None
    away: str | None
    league: str | None
    status: str | None

    @classmethod
    def from_api(cls, item: dict) -> "FixtureBrief":
        fixture = item.get("fixture", {})
        raw = fixture.get("date")
        try:
            kickoff = (
                datetime.fromisoformat(raw.replace("Z", "+00:00")) if raw else None
            )
        except ValueError:
            kickoff = None
        return cls(
            fixture_id=fixture.get("id", 0),
            kickoff=kickoff,
            home=item.get("teams", {}).get("home", {}).get("name"),
            away=item.get("teams", {}).get("away", {}).get("name"),
            league=item.get("league", {}).get("name"),
            status=fixture.get("status", {}).get("long"),
        )


class FixtureAttachRequest(BaseModel):
    fixture_id: int


# --- The operator's setlist for a show ---


class SetlistSong(BaseModel):
    """One line of the operator's script."""

    id: uuid.UUID
    position: int
    title: str
    # Null until the operator taps COMEÇOU on it. The instant is measured,
    # which is what lets it name a moment.
    started_at: datetime | None = None

    model_config = {"from_attributes": True}


class SetlistReplaceRequest(BaseModel):
    """The order, pasted. One song a line; blanks and numbering are dropped.

    Replaces the whole list rather than editing it, because that is what
    pasting a corrected setlist means. Songs already started keep their
    time if their title still matches at the same position — the operator
    fixing a typo in song 14 must not erase that song 3 began at 21h44.
    """

    songs: list[str] = Field(default_factory=list, max_length=200)


class SetlistStartRequest(BaseModel):
    """Which song just started. Omit the position to advance to the next."""

    position: int | None = None
    # The instant it started, when it is not now: a tap logged a little late
    # is still better placed by hand than left wrong.
    at: datetime | None = None


# --- Peak ---


class PeakResponse(BaseModel):
    id: uuid.UUID
    session_id: uuid.UUID
    timestamp: datetime
    bpm: int
    duration_seconds: int
    magnitude: float
    timeline_entry_id: uuid.UUID | None
    rank: int | None
    matched_label: str | None = None

    model_config = {"from_attributes": True}


class HRSessionSummary(BaseModel):
    id: uuid.UUID
    event_id: uuid.UUID | None
    start_time: datetime
    end_time: datetime
    avg_bpm: int | None
    max_bpm: int | None
    min_bpm: int | None
    data_quality_score: int | None
    source_device: str | None

    model_config = {"from_attributes": True}


class HRDataPointBrief(BaseModel):
    time: datetime
    bpm: int


class ExperienceResponse(BaseModel):
    session: HRSessionSummary
    peaks: list[PeakResponse]
    timeline: list[TimelineEntryResponse]
    hr_data: list[HRDataPointBrief] = []


class MatchWatchResponse(BaseModel):
    """The live watch of one match (#52), as the operator's screen shows it.

    ``state`` is off (no API key), idle (not picked up — the event is not
    today, or no match is attached), waiting (before the window), watching,
    or done. ``notes`` are the watch's own sentences about what it chose not
    to record and why.
    """

    state: str
    status: str | None = None
    scheduled: datetime | None = None
    last_polled_at: datetime | None = None
    kickoff_at: datetime | None = None
    second_half_at: datetime | None = None
    polls: int = 0
    notes: list[str] = []
    spent_today: int = 0
    budget: int = 0
    last_error: str | None = None
