import uuid
from datetime import datetime, timezone

from sqlalchemy import String, DateTime, ForeignKey
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class Card(Base):
    __tablename__ = "cards"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    session_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("hr_sessions.id", ondelete="CASCADE"), nullable=False, index=True)
    peak_id: Mapped[uuid.UUID | None] = mapped_column(UUID(as_uuid=True), ForeignKey("peaks.id", ondelete="SET NULL"))
    card_type: Mapped[str] = mapped_column(String(20), nullable=False)  # solo | comparison
    image_url: Mapped[str | None] = mapped_column(String(500))
    video_url: Mapped[str | None] = mapped_column(String(500))
    metadata_: Mapped[dict | None] = mapped_column("metadata", JSONB)
    status: Mapped[str] = mapped_column(String(20), nullable=False, default="pending")  # pending | generating | ready | failed
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))

    user = relationship("User")
    session = relationship("HRSession")
    peak = relationship("Peak")
    shares = relationship("Share", back_populates="card", lazy="selectin")


class Share(Base):
    __tablename__ = "shares"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    card_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("cards.id", ondelete="CASCADE"), nullable=False, index=True)
    platform: Mapped[str] = mapped_column(String(50), nullable=False)  # instagram | tiktok | x | whatsapp | link
    shared_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))

    card = relationship("Card", back_populates="shares")
