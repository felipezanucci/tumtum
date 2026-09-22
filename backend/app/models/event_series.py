import uuid
from datetime import UTC, datetime

from sqlalchemy import DateTime, ForeignKey, String
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class EventSeries(Base):
    """What a fan belongs to above one night: a **tour**, a **club**, a
    **championship** (#33, Felipe's call, 22/09).

    The per-event feed is intimate and small — at pilot size, five posts that
    look abandoned. The unit a fan identifies with is bigger: a Swiftie with
    The Eras Tour, not with "Morumbi, 14/11"; a corintiano with the club. But
    not "the artist": two nights of one tour share a setlist, an era and a
    production, and their crowds are comparable; 2019 and 2026 are not. So
    the level above the event is the series, and it is *added* to the event
    feed, never put in its place.

    The gate stays evidence, one level up: a measured night at **any** date
    of the series.
    """

    __tablename__ = "event_series"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    # tour · club · league — which word the app uses for it ("turnê",
    # "torcida", "campeonato").
    kind: Mapped[str] = mapped_column(String(16), nullable=False, default="tour")
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )


class EventSeriesMember(Base):
    """An event's place in a series. At most one series per event.

    A table of its own rather than a column on ``events``, because the
    deployed server creates new tables at startup but never alters old ones
    (decision log, open item 16).
    """

    __tablename__ = "event_series_members"

    event_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("events.id"), primary_key=True
    )
    series_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("event_series.id"), nullable=False, index=True
    )


class SeriesPost(Base):
    """A post its author chose to show to the whole series.

    **This row is the consent.** Everybody who posted before 22/09 was told
    *"Quem estava no mesmo rolê vai ver… Só eles."* — so a post reaches the
    series feed only when its author picked that audience at the moment of
    posting, and deleting the post (or this row) takes it back out.
    """

    __tablename__ = "series_posts"

    post_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("event_posts.id"), primary_key=True
    )
    series_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("event_series.id"), nullable=False, index=True
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(UTC)
    )
