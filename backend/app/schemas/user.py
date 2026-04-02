import uuid
from datetime import datetime
from pydantic import BaseModel


class UserProfileResponse(BaseModel):
    id: uuid.UUID
    email: str
    name: str
    avatar_url: str | None
    auth_provider: str
    created_at: datetime
    total_sessions: int = 0
    total_events: int = 0
    total_cards: int = 0
    highest_bpm: int | None = None

    model_config = {"from_attributes": True}


class UserUpdateRequest(BaseModel):
    name: str | None = None
    avatar_url: str | None = None


class PublicProfileResponse(BaseModel):
    name: str
    avatar_url: str | None
    created_at: datetime
    total_sessions: int = 0
    total_events: int = 0
    total_cards: int = 0

    model_config = {"from_attributes": True}
