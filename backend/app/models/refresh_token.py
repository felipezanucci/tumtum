import uuid
from datetime import UTC, datetime

from sqlalchemy import DateTime, ForeignKey, String
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class RefreshToken(Base):
    """One link in a signed-in device's chain of refresh tokens.

    Added 2026-09-22 (#34). Until then the only credential was a 24-hour access
    token with nothing to renew it, so everybody was signed out once a day —
    and the app went on showing their name and avatar while the server
    refused them, which Felipe read, correctly, as the app contradicting
    itself.

    Every use **rotates**: the presented token is revoked and a new one issued
    in the same ``family_id``, 90 days from now. So a device in regular use
    never signs in again, and one left untouched for 90 days does. A token
    presented *after* it was rotated means two parties hold the chain — a
    copy leaked — and the whole family is revoked, which signs out the copy
    and the original alike.

    As with password resets, only ``token_hash`` is stored: reading this table
    grants nobody a session.
    """

    __tablename__ = "refresh_tokens"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    # One sign-in on one device, across every rotation that follows it.
    family_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False, index=True
    )
    token_hash: Mapped[str] = mapped_column(
        String(64), nullable=False, unique=True, index=True
    )
    expires_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False
    )
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    # rotated · logout · reset · reuse — why it stopped working. Only a
    # rotation can be retried (see services/refresh_tokens.RETRY_GRACE).
    revoke_reason: Mapped[str | None] = mapped_column(String(16))
    # The token this one was rotated from. A retry is honest only while this
    # link's child has never been used.
    parent_id: Mapped[uuid.UUID | None] = mapped_column(UUID(as_uuid=True))
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )
