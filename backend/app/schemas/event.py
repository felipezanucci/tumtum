import uuid
from datetime import date, time, datetime
from pydantic import BaseModel, Field


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
    entry_type: str = Field(..., pattern="^(song_start|goal|halftime|encore|highlight)$")
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


class ExperienceResponse(BaseModel):
    session: "HRSessionSummary"
    peaks: list[PeakResponse]
    timeline: list[TimelineEntryResponse]


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
