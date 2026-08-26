from datetime import datetime

from pydantic import BaseModel, EmailStr, Field


class WaitlistJoinRequest(BaseModel):
    email: EmailStr
    first_name: str | None = Field(default=None, max_length=100)
    last_name: str | None = Field(default=None, max_length=100)
    source: str | None = Field(default=None, max_length=100)


class WaitlistJoinResponse(BaseModel):
    """What the site tells the person.

    `already_joined` exists so the page can say something true either way
    without ever turning a repeat submission into a failure.
    """

    email: EmailStr
    already_joined: bool


class WaitlistCountResponse(BaseModel):
    total: int


class WaitlistEntryResponse(BaseModel):
    email: str
    first_name: str | None
    last_name: str | None
    source: str | None
    created_at: datetime

    model_config = {"from_attributes": True}
