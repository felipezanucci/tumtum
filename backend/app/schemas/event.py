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


class TimelineEntryCreate(BaseModel):
    timestamp: datetime
    label: str
    entry_type: str = Field(
        ..., pattern="^(song_start|goal|halftime|encore|highlight)$"
    )
    metadata: dict | None = None


class TimelineEntryResponse(BaseModel):
    id: uuid.UUID
    event_id: uuid.UUID
    timestamp: datetime
    label: str
    entry_type: str
    metadata: dict | None = None

    model_config = {"from_attributes": True}


class EventDetailResponse(EventResponse):
    timeline: list[TimelineEntryResponse] = []


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
