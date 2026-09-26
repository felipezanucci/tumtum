import uuid
from datetime import UTC, datetime

from sqlalchemy import DateTime, ForeignKey, Index, String
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class Consent(Base):
    """One grant of one purpose, and — when it ends — its revocation.

    Append-only (LGPD audit, CR-1): a grant is a row, a revocation stamps
    `revoked_at` on that row, and granting again is a new row. The state of a
    purpose is its newest row, and the history is every row, which is what
    proves *who* agreed to *what text*, *when*, and *through which screen*.
    Consent is the only legal basis this product has for heart-rate data (art.
    11 LGPD), and a basis that cannot be proved is not one.

    `purpose` is one of `services.consents.PURPOSES`; `means` is how the
    person expressed it (tap, checkbox, button, form); `client` is the
    `X-Tumtum-Client` header of the request that recorded it.
    """

    __tablename__ = "consents"
    __table_args__ = (Index("ix_consents_user_purpose", "user_id", "purpose"),)

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    purpose: Mapped[str] = mapped_column(String(40), nullable=False)
    text_version: Mapped[str] = mapped_column(String(20), nullable=False)
    granted_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False
    )
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    means: Mapped[str] = mapped_column(String(20), nullable=False)
    client: Mapped[str | None] = mapped_column(String(80))
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )
