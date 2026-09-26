"""The consent API on the wire (contract of 26/09)."""

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field

from app.services.consents import CONSENT_TEXT_VERSION, ConsentState

Purpose = Literal[
    "terms",
    "read_heart_rate",
    "keep_night",
    "crowd_stats",
    "artist_compare",
    "improve_detection",
    "marketing",
]


class ConsentEntry(BaseModel):
    purpose: str
    granted: bool
    granted_at: datetime | None
    revoked_at: datetime | None
    text_version: str | None

    @classmethod
    def of(cls, state: ConsentState) -> "ConsentEntry":
        return cls(
            purpose=state.purpose,
            granted=state.granted,
            granted_at=state.granted_at,
            revoked_at=state.revoked_at,
            text_version=state.text_version,
        )


class ConsentsResponse(BaseModel):
    """Every purpose, always all seven, and the text version now in force."""

    text_version: str = CONSENT_TEXT_VERSION
    consents: list[ConsentEntry]


class ConsentsUpdate(BaseModel):
    """Only the purposes present change; an unknown purpose is refused."""

    text_version: str = Field(min_length=1, max_length=20)
    means: Literal["tap", "checkbox", "button", "form"]
    purposes: dict[Purpose, bool]


class ConsentHistoryEntry(BaseModel):
    """One row of the append-only record, as the person's export shows it.

    A superset of [ConsentEntry] — `granted` is whether this row is the one
    in force — so a screen can read either list the same way.
    """

    purpose: str
    granted: bool
    text_version: str
    granted_at: datetime
    revoked_at: datetime | None
    means: str
    client: str | None

    model_config = {"from_attributes": True}
