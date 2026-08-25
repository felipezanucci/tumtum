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
    metadata_: dict | None = Field(None, alias="metadata_")
    created_at: datetime

    model_config = {"from_attributes": True, "populate_by_name": True}


class ShareRequest(BaseModel):
    # "native" is the system share sheet: the person picked an app inside it
    # and the page is never told which, so recording a guess would be worse
    # than recording that we do not know.
    platform: str = Field(..., pattern="^(instagram|tiktok|x|whatsapp|link|native)$")


class ShareResponse(BaseModel):
    id: uuid.UUID
    card_id: uuid.UUID
    platform: str
    shared_at: datetime

    model_config = {"from_attributes": True}


class PublicCardResponse(BaseModel):
    """What a shared card shows to someone who is not signed in.

    Mirrors the image and nothing more. No user_id, no session_id, no other
    heart-rate reading — a share link publishes one moment, not a person.
    """

    id: uuid.UUID
    event_name: str
    event_date: str
    peak_bpm: int
    moment_label: str | None = None
    moment_time: str | None = None
    user_name: str
