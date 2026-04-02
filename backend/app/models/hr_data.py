import uuid
from datetime import datetime

from sqlalchemy import DateTime, SmallInteger, String, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class HRData(Base):
    """Heart rate data points — designed for TimescaleDB hypertable partitioned by time."""

    __tablename__ = "hr_data"

    time: Mapped[datetime] = mapped_column(DateTime(timezone=True), primary_key=True)
    session_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("hr_sessions.id"), primary_key=True, index=True
    )
    bpm: Mapped[int] = mapped_column(SmallInteger, nullable=False)
    rr_interval_ms: Mapped[int | None] = mapped_column(SmallInteger)
    motion_level: Mapped[int | None] = mapped_column(SmallInteger)
    source: Mapped[str | None] = mapped_column(String(50))  # apple_health | google_fit | garmin | fitbit

    session = relationship("HRSession", back_populates="data_points")
