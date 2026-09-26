"""The data-subject rights on the wire (LGPD art. 18; contract of 26/09).

`GET /api/users/me/data` is everything TumTum holds about the person except
the second-by-second readings; `GET /api/users/me/export` is the same with
the readings, as a file. The shapes mirror the tables they come from, in
the names the rest of the API already uses, so what the person downloads is
what the server has — not a friendlier summary of it.
"""

import uuid
from datetime import date, datetime
from typing import Literal

from pydantic import BaseModel, Field

from app.schemas.consent import ConsentHistoryEntry

RequestKind = Literal[
    "access", "portability", "correction", "deletion", "revocation", "other"
]
RequestStatus = Literal["open", "answered", "closed"]


class DataUser(BaseModel):
    id: uuid.UUID
    email: str
    name: str
    avatar_url: str | None
    auth_provider: str
    created_at: datetime
    updated_at: datetime | None

    model_config = {"from_attributes": True}


class DataEvent(BaseModel):
    id: uuid.UUID
    name: str
    date: date


class DataPoint(BaseModel):
    time: datetime
    bpm: int


class DataSession(BaseModel):
    id: uuid.UUID
    event: DataEvent | None
    start_time: datetime
    end_time: datetime
    avg_bpm: int | None
    max_bpm: int | None
    min_bpm: int | None
    data_quality_score: int | None
    source_device: str | None
    created_at: datetime
    analyzed_at: datetime | None = None


class ExportSession(DataSession):
    data_points: list[DataPoint] = []


class DataPeak(BaseModel):
    id: uuid.UUID
    session_id: uuid.UUID
    timestamp: datetime
    bpm: int
    duration_seconds: int
    magnitude: float
    rank: int | None

    model_config = {"from_attributes": True}


class DataCard(BaseModel):
    id: uuid.UUID
    session_id: uuid.UUID
    peak_id: uuid.UUID | None
    card_type: str
    status: str
    created_at: datetime
    published_at: datetime | None
    metadata: dict | None


class DataPost(BaseModel):
    id: uuid.UUID
    event_id: uuid.UUID
    session_id: uuid.UUID | None
    bpm: int
    moment_at: datetime
    label: str | None
    quote: str | None
    skin: str
    created_at: datetime
    deleted_at: datetime | None

    model_config = {"from_attributes": True}


class DataReaction(BaseModel):
    id: uuid.UUID
    post_id: uuid.UUID
    created_at: datetime

    model_config = {"from_attributes": True}


class DataShare(BaseModel):
    id: uuid.UUID
    card_id: uuid.UUID
    platform: str
    shared_at: datetime

    model_config = {"from_attributes": True}


class DataBlock(BaseModel):
    """A block this person made. The other person appears by display name
    only — what the block list in the app already shows them."""

    id: uuid.UUID
    blocked_name: str
    created_at: datetime


class DataReport(BaseModel):
    id: uuid.UUID
    post_id: uuid.UUID
    reason: str
    created_at: datetime
    resolution: str | None
    resolved_at: datetime | None

    model_config = {"from_attributes": True}


class DataSubjectRequestCreate(BaseModel):
    kind: RequestKind
    message: str = Field(default="", max_length=4000)


class DataSubjectRequestResponse(BaseModel):
    id: uuid.UUID
    kind: str
    message: str
    status: str
    opened_at: datetime
    due_at: datetime
    answered_at: datetime | None
    answer: str | None

    model_config = {"from_attributes": True}


class AdminDataSubjectRequest(DataSubjectRequestResponse):
    """The operator's view: who asked, so the answer reaches them."""

    user_id: uuid.UUID
    user_email: str | None
    user_name: str | None


class DataSubjectRequestUpdate(BaseModel):
    status: RequestStatus
    answer: str | None = Field(default=None, max_length=8000)


class MyDataResponse(BaseModel):
    user: DataUser
    birth_date: date | None
    consents: list[ConsentHistoryEntry]
    sessions: list[DataSession]
    peaks: list[DataPeak]
    cards: list[DataCard]
    posts: list[DataPost]
    reactions: list[DataReaction]
    shares: list[DataShare]
    blocks: list[DataBlock]
    reports_filed: list[DataReport]
    requests: list[DataSubjectRequestResponse]


class MyExportResponse(MyDataResponse):
    sessions: list[ExportSession]
