import uuid
from datetime import datetime
from pydantic import BaseModel, Field


# --- Wearable Connection ---

class WearableConnectRequest(BaseModel):
    provider: str = Field(..., pattern="^(apple_health|google_fit|garmin|fitbit)$")
    access_token: str
    refresh_token: str | None = None


class WearableConnectionResponse(BaseModel):
    id: uuid.UUID
    provider: str
    status: str
    last_sync_at: datetime | None
    created_at: datetime

    model_config = {"from_attributes": True}


# --- HR Data Ingestion ---

class HRDataPointInput(BaseModel):
    time: datetime
    bpm: int = Field(..., ge=30, le=250)
    rr_interval_ms: int | None = Field(None, ge=200, le=2000)
    motion_level: int | None = Field(None, ge=0, le=10)
    source: str | None = None


class HRSessionCreateRequest(BaseModel):
    start_time: datetime
    end_time: datetime
    source_device: str | None = None
    event_id: uuid.UUID | None = None
    data_points: list[HRDataPointInput]


class HRSessionResponse(BaseModel):
    id: uuid.UUID
    user_id: uuid.UUID
    event_id: uuid.UUID | None
    start_time: datetime
    end_time: datetime
    avg_bpm: int | None
    max_bpm: int | None
    min_bpm: int | None
    data_quality_score: int | None
    source_device: str | None
    created_at: datetime

    model_config = {"from_attributes": True}


class HRDataPointResponse(BaseModel):
    time: datetime
    bpm: int
    rr_interval_ms: int | None
    motion_level: int | None
    source: str | None

    model_config = {"from_attributes": True}


class HRSessionDetailResponse(HRSessionResponse):
    data_points: list[HRDataPointResponse] = []


# --- Sync ---

class SyncRequest(BaseModel):
    connection_id: uuid.UUID
    start_time: datetime
    end_time: datetime


class SyncStatusResponse(BaseModel):
    connection_id: uuid.UUID
    status: str  # syncing | completed | failed
    records_synced: int
    last_sync_at: datetime | None
