import uuid
from datetime import UTC, datetime

from sqlalchemy import DateTime, ForeignKey, Integer, String, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class EventPost(Base):
    """One moment somebody chose to put in front of the people who were there.

    The feed is **per event** (Felipe, 2026-09-22): *"apenas as pessoas que
    estiveram no evento podem interagir. Isso deve criar um senso de
    comunidade maior."* That is card 04 of the brand manual, and the closed
    group is what makes it work — a reason to open the app the day after a
    match, and a moderation problem the size of one crowd rather than the
    whole platform.

    **A row here is published health data.** It carries a person's heart rate
    at a named minute, readable by strangers who happen to have been at the
    same event. So it is written only on an explicit act, it is never
    back-filled from a night, and [deleted_at] means the person took it down
    and the feed must forget it — the undo that the consent depends on.

    What proves somebody was there is not stored here: it is an `hr_sessions`
    row carrying the same `event_id`, which is evidence rather than a claim
    and needs no new data. See `require_attendance`.
    """

    __tablename__ = "event_posts"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    event_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("events.id"), nullable=False, index=True
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), nullable=False, index=True
    )
    # The night the moment came from, so a deleted account or a deleted night
    # can take its posts with it.
    session_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), ForeignKey("hr_sessions.id"), index=True
    )

    # The moment itself, copied rather than joined: a post is what the person
    # published, and re-analysing their night later must not silently rewrite
    # what other people already read.
    bpm: Mapped[int] = mapped_column(Integer, nullable=False)
    moment_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    # What named the moment, when anything did — "Yellow", "⚽ Gol de pênalti".
    label: Mapped[str | None] = mapped_column(String(255))
    # The person's own words. The unit of the feed is the card plus a line.
    quote: Mapped[str | None] = mapped_column(String(280))
    skin: Mapped[str] = mapped_column(String(20), nullable=False, default="BLACK")

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )
    # Taken down by its author. Kept as a row so a re-post is not mistaken for
    # an edit, and read as gone everywhere.
    deleted_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class EventPostReaction(Base):
    """SENTI TB — the only reaction there is.

    One button, no scale, no ranking. The brand manual's line on the feed is
    *"histórias, nunca ranking de BPM"*, and a single reaction is what keeps
    it that way: there is no way to say somebody felt it *less*. It also keeps
    moderation small — a feed with one positive verb has very little to
    report.
    """

    __tablename__ = "event_post_reactions"
    __table_args__ = (
        UniqueConstraint("post_id", "user_id", name="uq_event_post_reaction"),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    post_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("event_posts.id"), nullable=False, index=True
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("users.id"), nullable=False, index=True
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )
