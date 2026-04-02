import uuid
from datetime import datetime
from pydantic import BaseModel, Field


class CardCreateRequest(BaseModel):
    session_id: uuid.UUID
    peak_id: uuid.UUID | None = None
    card_type: str = Field(default="solo", pattern="^(solo|comparison)$")
    format: str = Field(default="story", pattern="^(story|feed)$")


class CardResponse(BaseModel):
    id: uuid.UUID
    user_id: uuid.UUID
    session_id: uuid.UUID
    peak_id: uuid.UUID | None
    card_type: str
    image_url: str | None
    video_url: str | None
    status: str
    metadata: dict | None = None
    created_at: datetime

    model_config = {"from_attributes": True}


class ShareRequest(BaseModel):
    platform: str = Field(..., pattern="^(instagram|tiktok|x|whatsapp|link)$")


class ShareResponse(BaseModel):
    id: uuid.UUID
    card_id: uuid.UUID
    platform: str
    shared_at: datetime

    model_config = {"from_attributes": True}
