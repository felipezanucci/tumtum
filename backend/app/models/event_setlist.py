import uuid
from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Integer, String, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class EventSetlist(Base):
    """The operator's script for a show: the order, and what has started.

    Added 2026-09-22, when the rule became absolute — the card must arrive
    naming the moment, and nothing may be asked of the fan. A concert has no
    API. Setlist.fm publishes order and never times, audio fingerprinting
    matches a studio recording and not a band playing live, and a time
    derived from an assumed four minutes a song is outside the correlator's
    window by the third song. What is left is a person, and the cheapest
    thing a person can do while watching a show is press one button.

    So this table is two things at once. Before the show it is the **plan**:
    the order, pasted in, with ``started_at`` null. During the show each tap
    stamps the next row with the instant it really happened, and that instant
    becomes a ``song_start`` entry on the event's timeline — a measured time,
    which is the only kind that names a moment.

    The order can be prepared days ahead: a tour plays close to the same set
    every night, so last night's is a good draft. Nothing here is shown to a
    fan; it is a script for staff.
    """

    __tablename__ = "event_setlist"
    __table_args__ = (
        UniqueConstraint("event_id", "position", name="uq_event_setlist_position"),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    event_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("events.id"), nullable=False, index=True
    )
    # 1-based, and the order the operator pasted. Gaps never happen: the list
    # is replaced whole rather than edited row by row.
    position: Mapped[int] = mapped_column(Integer, nullable=False)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    # When the operator said it started. Null until then — and null is what
    # the screen counts to know which song is next.
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
