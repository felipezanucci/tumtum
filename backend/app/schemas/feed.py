"""What the event feed says on the wire.

Two rules shape these shapes, and both come from the same place — a post
carries somebody's heart rate:

- **No e-mail ever leaves here.** The author is a display name and initials,
  which is what a feed needs, and nothing that identifies the person outside
  TumTum.
- **A refusal is a value, not an absence.** [CrowdResponse.enough] is false,
  so the screen can say *"ainda somos poucos aqui"* rather than drawing
  zeros — an empty state is a claim. Since 26/09 the count behind a refusal
  is withheld too (null): how few is a fact about the few.
"""

import uuid
from datetime import date, datetime

from pydantic import BaseModel, Field

from app.services.crowd import Crowd


class FeedAuthor(BaseModel):
    """Who posted, at the resolution a feed needs and no finer."""

    name: str
    initials: str

    @classmethod
    def of(cls, user) -> "FeedAuthor":
        name = (user.name or "").strip() or "Alguém"
        parts = [p for p in name.split() if p]
        initials = "".join(p[0] for p in parts[:2]).upper() or "TT"
        return cls(name=name, initials=initials)


class FeedPostCreate(BaseModel):
    """One moment, published on purpose."""

    session_id: uuid.UUID
    bpm: int = Field(..., ge=20, le=250)
    moment_at: datetime
    label: str | None = Field(None, max_length=255)
    quote: str | None = Field(None, max_length=280)
    skin: str = Field("BLACK", max_length=20)
    # Show it to everybody at any date of the tour (#33, #65). Chosen at the
    # moment of posting; without it only the people at this night see it.
    to_series: bool = False


class FeedPostResponse(BaseModel):
    id: uuid.UUID
    author: FeedAuthor
    bpm: int
    moment_at: datetime
    label: str | None
    quote: str | None
    skin: str
    created_at: datetime
    reactions: int
    reacted_by_me: bool
    # Whether the viewer may take this one down. The screen shows the undo
    # only where it exists, rather than offering it and then refusing.
    mine: bool
    # Which night it is from: in a tour's feed posts from several dates sit
    # together, and each one carries its own date and city (#33, #65).
    event_name: str | None = None
    event_date: date | None = None
    event_city: str | None = None
    # Always set: the night a post belongs to — the "Só a minha noite" filter
    # reads it, and so does its author's undo.
    event_id: uuid.UUID | None = None

    @classmethod
    def of(
        cls,
        post,
        author,
        reactions: int,
        reacted_by_me: bool,
        mine: bool,
        event=None,
    ) -> "FeedPostResponse":
        return cls(
            event_id=post.event_id,
            event_name=event.name if event is not None else None,
            event_date=event.date if event is not None else None,
            event_city=event.city if event is not None else None,
            id=post.id,
            author=FeedAuthor.of(author),
            bpm=post.bpm,
            moment_at=post.moment_at,
            label=post.label,
            quote=post.quote,
            skin=post.skin,
            created_at=post.created_at,
            reactions=reactions,
            reacted_by_me=reacted_by_me,
            mine=mine,
        )


class SeriesBrief(BaseModel):
    """The tour, club or championship an event belongs to (#33)."""

    id: uuid.UUID
    name: str
    kind: str
    dates: int


class SeriesEvent(BaseModel):
    id: uuid.UUID
    name: str
    date: date
    city: str | None

    @classmethod
    def of(cls, event) -> "SeriesEvent":
        return cls(id=event.id, name=event.name, date=event.date, city=event.city)


class EventFeedResponse(BaseModel):
    """The one feed an event opens onto (#65).

    When the event is part of a tour, ``series`` names it and ``posts`` come
    from every date, each carrying its own; the night is a filter the screen
    applies, never a second feed. ``events`` is the dates, oldest first — one
    entry for an event outside any tour.
    """

    event_id: uuid.UUID
    event_name: str
    venue: str | None
    date: date
    posts: list[FeedPostResponse]
    series: SeriesBrief | None = None
    events: list[SeriesEvent] = []
    # Posts a block (either way) kept out of this feed — a count, nothing
    # more, so an empty feed can say why it is empty (#63).
    hidden_by_block: int = 0


class SeriesFeedResponse(BaseModel):
    series_id: uuid.UUID
    name: str
    kind: str
    events: list[SeriesEvent]
    posts: list[FeedPostResponse]
    hidden_by_block: int = 0


class SeriesCreate(BaseModel):
    name: str = Field(..., min_length=2, max_length=255)
    kind: str = Field("tour", pattern="^(tour|club|league)$")


class SeriesAssign(BaseModel):
    """Put an event in a series, or take it out with null."""

    series_id: uuid.UUID | None = None


class CrowdMomentResponse(BaseModel):
    at: datetime
    # "10+" | "25+" | "50+" | "100+" | "250+" — never an exact count.
    people_band: str
    label: str | None


class CrowdResponse(BaseModel):
    """Card 04, or an honest account of why there is no card 04 yet.

    `measured_nights` is null unless `enough`: below the floor, how few is
    itself a fact about the few.
    """

    measured_nights: int | None
    enough: bool
    shared_count: int
    moments: list[CrowdMomentResponse]
    top: CrowdMomentResponse | None

    @classmethod
    def of(cls, crowd: Crowd) -> "CrowdResponse":
        def moment(m) -> CrowdMomentResponse:
            return CrowdMomentResponse(
                at=m.at, people_band=m.people_band, label=m.label
            )

        return cls(
            measured_nights=crowd.measured_nights if crowd.enough else None,
            enough=crowd.enough,
            shared_count=crowd.shared_count,
            moments=[moment(m) for m in crowd.moments],
            top=moment(crowd.top) if crowd.top else None,
        )


class ReportRequest(BaseModel):
    """abuse · fake · other. Anything else is read as other."""

    reason: str | None = Field(None, max_length=16)


class BlockedPerson(BaseModel):
    """Somebody this account blocked — the name, so the list is readable, and nothing else."""

    id: uuid.UUID
    name: str
    initials: str
    created_at: datetime


class ReportedPost(BaseModel):
    """One post in the operator's queue, with how many people reported it and why."""

    post_id: uuid.UUID
    event_id: uuid.UUID
    event_name: str
    author: FeedAuthor
    bpm: int
    moment_at: datetime
    label: str | None
    quote: str | None
    reports: int
    reasons: dict[str, int]
    first_reported_at: datetime
    hidden: bool


class ResolveReportRequest(BaseModel):
    action: str = Field(..., pattern="^(keep|remove)$")
