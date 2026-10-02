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
    # The @ (28/09). Optional in the schema so a build from before it still
    # signs up — the server then picks a free one from the name.
    username: str | None = Field(None, max_length=40)


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
    # Bounded, so one request cannot make the server hash a megabyte; 128
    # and not the sign-up's 64 because a reset accepted any length until
    # 26/09, and nobody who chose a long one there may be locked out. Only
    # the first 72 bytes reach bcrypt either way (api/auth.py).
    password: str = Field(max_length=128)


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
    # The @, unique (28/09). Null for accounts made before it: the clients
    # then ask for one, once.
    username: str | None = None
    # Whether this account operates the platform (settings.admin_emails). The
    # site uses it only to show or hide the operator's doors; every operator
    # endpoint checks for itself.
    is_admin: bool = False

    model_config = {"from_attributes": True}


class ForgotPasswordRequest(BaseModel):
    email: EmailStr


class ResetPasswordRequest(BaseModel):
    token: str = Field(max_length=256)
    password: str = Field(min_length=6, max_length=128)


class ResetWithCodeRequest(BaseModel):
    """The app's way back in (02/10): the address, the six digits from the
    mail, and the new password — no link, no browser."""

    email: EmailStr
    code: str = Field(max_length=20)
    password: str = Field(min_length=6, max_length=128)


class MessageResponse(BaseModel):
    message: str


class UsernameCheck(BaseModel):
    """Whether an @ can be had, asked while the person types (28/09)."""

    username: str
    available: bool
    # The sentence to show under the field when it cannot: too short, a
    # character it does not take, the platform's, or already somebody's.
    reason: str | None = None
