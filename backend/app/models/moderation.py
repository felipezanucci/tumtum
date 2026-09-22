import uuid
from datetime import UTC, datetime

from sqlalchemy import DateTime, ForeignKey, String, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class PostReport(Base):
    """Somebody who was at the event says a post should not be there.

    Item 55 / #36 (22/09). A report lands where a human reads it — the
    operator page `/admin/denuncias`, and an e-mail to the operators when
    one can be sent — and three distinct reports take a post out of the feed
    until somebody looks. One per person per post: reporting twice is not
    twice the evidence.
    """

    __tablename__ = "post_reports"
    __table_args__ = (
        UniqueConstraint("post_id", "reporter_id", name="uq_post_report"),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    post_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("event_posts.id"), nullable=False, index=True
    )
    reporter_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), nullable=False, index=True
    )
    # abuse · fake · other — a choice of three, no free text: a report box is
    # the one place strangers could otherwise write to each other.
    reason: Mapped[str] = mapped_column(String(16), nullable=False, default="other")
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )
    # kept · removed — what the operator decided, and when.
    resolution: Mapped[str | None] = mapped_column(String(16))
    resolved_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class UserBlock(Base):
    """One person no longer sees another, in either direction.

    Blocking is done **from a post**, because the feed exposes no ids, no
    handles and no profiles; the server resolves the post to its author. It
    works both ways — neither sees the other's posts — so a block cannot be
    used to watch somebody who can no longer see you.
    """

    __tablename__ = "user_blocks"
    __table_args__ = (
        UniqueConstraint("blocker_id", "blocked_id", name="uq_user_block"),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    blocker_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), nullable=False, index=True
    )
    blocked_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), nullable=False, index=True
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )
