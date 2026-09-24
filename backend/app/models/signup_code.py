import uuid
from datetime import UTC, datetime

from sqlalchemy import DateTime, Integer, String
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class SignupCode(Base):
    """An account waiting for its e-mail to be proved (#64, 24/09).

    Everything the account will be — address, name, password hash — waits
    here, so the account is created only by the code coming back and the
    password never travels twice. `code_hash`, never the code. No foreign
    key: nobody with a row here has an account yet, and a row nobody
    confirms is deleted after a day (`signup_codes.KEEP_UNCONFIRMED`).

    A table of its own and not a column on `users`: the deployed server
    creates missing tables at startup and never alters existing ones
    (decision log, open item 16), so a new column would silently not exist.
    """

    __tablename__ = "signup_codes"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    # As typed, because that is how the account will be stored and signed
    # into; `email_key` is the lowercased form every lookup uses.
    email: Mapped[str] = mapped_column(String(255), nullable=False)
    email_key: Mapped[str] = mapped_column(String(255), nullable=False, index=True)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    hashed_password: Mapped[str] = mapped_column(String(255), nullable=False)
    code_hash: Mapped[str] = mapped_column(String(64), nullable=False)
    attempts: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    expires_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False
    )
    # Set when the code is spent: confirmed, replaced by a newer one, or
    # guessed wrong too many times.
    used_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC), index=True
    )
