import uuid
from datetime import datetime

from sqlalchemy import DateTime, Float, SmallInteger, Integer, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class Peak(Base):
    __tablename__ = "peaks"

    id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    session_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("hr_sessions.id", ondelete="CASCADE"), nullable=False, index=True
    )
    timestamp: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    bpm: Mapped[int] = mapped_column(Integer, nullable=False)
    duration_seconds: Mapped[int] = mapped_column(Integer, nullable=False)
    magnitude: Mapped[float] = mapped_column(Float, nullable=False)
    timeline_entry_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), ForeignKey("event_timeline.id", ondelete="SET NULL")
    )
    rank: Mapped[int | None] = mapped_column(SmallInteger)

    session = relationship("HRSession", backref="peaks")
    timeline_entry = relationship("EventTimeline")
