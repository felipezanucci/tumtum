import uuid
from datetime import datetime, timedelta

from pydantic import BaseModel, Field

# Twelve hours at 1 Hz is 43 200 readings; a night is never that long, and a
# strap's real rate is under 1 Hz once gaps are counted. 30 000 covers eight
# hours of a strap with room, and stops one request from asking the server
# to hold millions of rows in memory.
MAX_DATA_POINTS = 30_000
MAX_NIGHT = timedelta(hours=12)
END_BEFORE_START = "A noite termina antes de começar. Confere o horário."
NIGHT_TOO_LONG = "Uma noite tem no máximo 12 horas."

# --- Wearable Connection ---


class WearableConnectRequest(BaseModel):
    """Which source the person reads from. No provider tokens (26/09): nothing
    on the server ever used them, and an idle credential to somebody's health
    account is only a liability. Extra fields an old client sends are ignored."""

    provider: str = Field(..., pattern="^(apple_health|google_fit|garmin|fitbit)$")


class WearableConnectionResponse(BaseModel):
    id: uuid.UUID
    provider: str
    status: str
    last_sync_at: datetime | None
    created_at: datetime

    model_config = {"from_attributes": True}


# --- HR Data Ingestion ---


class HRDataPointInput(BaseModel):
    """One reading. Time and bpm, and nothing finer.

    R-R intervals and motion were accepted until 26/09 and stored for
    nothing: no feature reads them, and each is more health data than a
    moment needs (LGPD audit, minimisation). An old client still sending them
    is not refused — pydantic ignores the extra fields — they are simply not
    kept. The columns stay in `hr_data`, empty, until a migration drops them.
    """

    time: datetime
    bpm: int = Field(..., ge=30, le=250)
    source: str | None = Field(None, max_length=100)


class HRSessionCreateRequest(BaseModel):
    """A night as the app uploads it, bounded before anything is stored.

    The span (end after start, twelve hours at most) is checked by the route,
    which answers in a sentence; a validator here would answer with a list
    nobody can show on a screen.
    """

    start_time: datetime
    end_time: datetime
    source_device: str | None = Field(None, max_length=100)
    event_id: uuid.UUID | None = None
    data_points: list[HRDataPointInput] = Field(..., max_length=MAX_DATA_POINTS)


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
    source: str | None

    model_config = {"from_attributes": True}


class HRSessionDetailResponse(HRSessionResponse):
    data_points: list[HRDataPointResponse] = []
