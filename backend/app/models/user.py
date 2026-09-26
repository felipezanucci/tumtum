import uuid
from datetime import UTC, date, datetime

from sqlalchemy import Date, DateTime, String
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class User(Base):
    __tablename__ = "users"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    email: Mapped[str] = mapped_column(
        String(255), unique=True, nullable=False, index=True
    )
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    avatar_url: Mapped[str | None] = mapped_column(String(500))
    auth_provider: Mapped[str] = mapped_column(
        String(50), nullable=False
    )  # google | apple | email
    auth_provider_id: Mapped[str | None] = mapped_column(String(255))
    hashed_password: Mapped[str | None] = mapped_column(String(255))
    # TumTum is for adults (LGPD audit, CR-4). Given at sign-up; null only
    # for accounts made before 26/09, which are asked once and then it is
    # fixed. A column on an existing table: migration 015, and the startup
    # catch-up in `core/schema_catchup.py`.
    birth_date: Mapped[date | None] = mapped_column(Date)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=lambda: datetime.now(UTC),
        onupdate=lambda: datetime.now(UTC),
    )

    wearable_connections = relationship(
        "WearableConnection", back_populates="user", lazy="selectin"
    )
    hr_sessions = relationship("HRSession", back_populates="user", lazy="selectin")
