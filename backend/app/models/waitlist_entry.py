import uuid
from datetime import UTC, datetime

from sqlalchemy import DateTime, String
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class WaitlistEntry(Base):
    """Someone who asked to be told when TumTum reaches their next event.

    The public site collects this and nothing else. No name, no phone, no
    device — the landing page promises "a gente só usa seu e-mail pra te avisar
    dos próximos eventos", and a column we do not have is a promise we cannot
    accidentally break.

    `email` is unique so a second submission is not a second person. The site
    treats the repeat as success rather than as an error: the person's intent
    was "put me on the list", and they are on it.
    """

    __tablename__ = "waitlist_entries"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    email: Mapped[str] = mapped_column(
        String(320), nullable=False, unique=True, index=True
    )
    # Which page sent them, when we know. Useful for telling a festival crowd
    # apart from people who arrived cold, and cheap to carry.
    source: Mapped[str | None] = mapped_column(String(100))
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )
