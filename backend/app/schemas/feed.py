"""What the event feed says on the wire.

Two rules shape these shapes, and both come from the same place — a post
carries somebody's heart rate:

- **No e-mail ever leaves here.** The author is a display name and initials,
  which is what a feed needs, and nothing that identifies the person outside
  TumTum.
- **A refusal is a value, not an absence.** [CrowdResponse.enough] is false
  with the count that made it false, so the screen can say *"ainda somos
  poucos aqui"* rather than drawing zeros — an empty state is a claim.
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

    @classmethod
    def of(
        cls,
        post,
        author,
        reactions: int,
        reacted_by_me: bool,
        mine: bool,
    ) -> "FeedPostResponse":
        return cls(
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


class EventFeedResponse(BaseModel):
    event_id: uuid.UUID
    event_name: str
    venue: str | None
    date: date
    posts: list[FeedPostResponse]


class CrowdMomentResponse(BaseModel):
    at: datetime
    people: int
    label: str | None


class CrowdResponse(BaseModel):
    """Card 04, or an honest account of why there is no card 04 yet."""

    measured_nights: int
    enough: bool
    shared_count: int
    moments: list[CrowdMomentResponse]
    top: CrowdMomentResponse | None

    @classmethod
    def of(cls, crowd: Crowd) -> "CrowdResponse":
        return cls(
            measured_nights=crowd.measured_nights,
            enough=crowd.enough,
            shared_count=crowd.shared_count,
            moments=[
                CrowdMomentResponse(at=m.at, people=m.people, label=m.label)
                for m in crowd.moments
            ],
            top=(
                CrowdMomentResponse(
                    at=crowd.top.at, people=crowd.top.people, label=crowd.top.label
                )
                if crowd.top
                else None
            ),
        )
