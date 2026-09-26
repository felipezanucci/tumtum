import uuid
from datetime import date, datetime

from pydantic import BaseModel, EmailStr, Field


class UserProfileResponse(BaseModel):
    id: uuid.UUID
    email: str
    name: str
    avatar_url: str | None
    auth_provider: str
    created_at: datetime
    birth_date: date | None = None
    total_sessions: int = 0
    total_events: int = 0
    total_cards: int = 0
    highest_bpm: int | None = None

    model_config = {"from_attributes": True}


class UserUpdateRequest(BaseModel):
    name: str | None = None
    avatar_url: str | None = None
    # Accepted once, while the account has none (accounts made before 26/09).
    birth_date: date | None = None


class EmailChangeRequest(BaseModel):
    email: EmailStr
    password: str = Field(max_length=128)


class EmailChangeStarted(BaseModel):
    """Where the code went, and how long the screen should wait."""

    email: str
    expires_in_seconds: int
    resend_after_seconds: int


class EmailChangeConfirm(BaseModel):
    code: str = Field(max_length=20)


class DeleteAccountRequest(BaseModel):
    password: str = Field(max_length=128)


class PublicProfileResponse(BaseModel):
    name: str
    avatar_url: str | None
    created_at: datetime
    total_sessions: int = 0
    total_events: int = 0
    total_cards: int = 0

    model_config = {"from_attributes": True}
