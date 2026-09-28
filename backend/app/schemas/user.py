import uuid
from datetime import date, datetime
from typing import Annotated

from pydantic import BaseModel, EmailStr, Field, HttpUrl, UrlConstraints

# https only, and no longer than the column (`users.avatar_url`, 500).
AvatarUrl = Annotated[
    HttpUrl, UrlConstraints(allowed_schemes=["https"], max_length=500)
]


class UserProfileResponse(BaseModel):
    id: uuid.UUID
    email: str
    name: str
    avatar_url: str | None
    auth_provider: str
    created_at: datetime
    birth_date: date | None = None
    username: str | None = None
    total_sessions: int = 0
    total_events: int = 0
    total_cards: int = 0
    highest_bpm: int | None = None

    model_config = {"from_attributes": True}


class UserUpdateRequest(BaseModel):
    name: str | None = Field(None, min_length=1, max_length=120)
    # An https address or nothing. Any string used to be stored and then
    # rendered as an <img src> on a public profile: `javascript:`, `data:`,
    # an http tracker, or a megabyte of text.
    avatar_url: AvatarUrl | None = None
    # Accepted once, while the account has none (accounts made before 26/09).
    birth_date: date | None = None
    # Accepted once too, while the account has none (accounts made before
    # the @ existed on the server, 28/09). "O @ é fixo" is a promise.
    username: str | None = Field(None, max_length=40)


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
    """What anybody may read about an account. No count of nights or events:
    how often somebody records their heart is health-adjacent (26/09)."""

    name: str
    username: str | None = None
    avatar_url: str | None
    created_at: datetime
    total_cards: int = 0

    model_config = {"from_attributes": True}
