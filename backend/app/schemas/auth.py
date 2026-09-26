import uuid
from datetime import date, datetime

from pydantic import BaseModel, EmailStr, Field


class SignupStartRequest(BaseModel):
    """Step one of an account (#64): everything but the proof of the e-mail."""

    email: EmailStr
    name: str = Field(min_length=1, max_length=120)
    # bcrypt reads at most 72 bytes; a longer password would be cut silently.
    password: str = Field(min_length=6, max_length=64)
    # Required (contract of 26/09), but optional in the schema so that a
    # client which does not send them — every build before 26/09 — is told
    # in a sentence it can show, not a validation list it cannot.
    birth_date: date | None = None
    terms_accepted: bool = False
    consent_text_version: str | None = Field(None, max_length=20)
    # The sign-up screen may also carry the heart-rate reading consent; when
    # it does, the account is born with that row too.
    read_heart_rate: bool = False


class SignupStarted(BaseModel):
    """Where the code went, and how long the screens should wait."""

    email: str
    expires_in_seconds: int
    resend_after_seconds: int


class SignupConfirmRequest(BaseModel):
    """Step two: the code that came back from the mailbox."""

    email: EmailStr
    # Checked for six digits by the endpoint, which says so in words; a
    # schema pattern would answer with a list nobody can show on a screen.
    code: str = Field(max_length=20)


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    # Renews the session without a password (#34, 22/09). Optional in the
    # schema so older clients that never read it keep working.
    refresh_token: str | None = None


class RefreshRequest(BaseModel):
    refresh_token: str = Field(min_length=16, max_length=256)


class UserResponse(BaseModel):
    id: uuid.UUID
    email: str
    name: str
    avatar_url: str | None
    auth_provider: str
    created_at: datetime
    # Null for accounts made before 26/09: the clients then show the gate
    # (birth date and terms) before anything else.
    birth_date: date | None = None
    # Whether this account operates the platform (settings.admin_emails). The
    # site uses it only to show or hide the operator's doors; every operator
    # endpoint checks for itself.
    is_admin: bool = False

    model_config = {"from_attributes": True}


class ForgotPasswordRequest(BaseModel):
    email: EmailStr


class ResetPasswordRequest(BaseModel):
    token: str
    password: str = Field(min_length=6)


class MessageResponse(BaseModel):
    message: str
