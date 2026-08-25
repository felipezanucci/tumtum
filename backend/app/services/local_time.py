"""Render a stored instant as the clock time the person actually saw.

Everything is stored in UTC, which is right. But a share card reads "19h31"
for a moment felt at 16h31, and that is the line people post publicly — the
one part of the product where being three hours off is visible to everyone who
sees it.

Phase 0 runs on São Paulo events, so a single configured zone is enough. When
events carry their own timezone this should take it from the event, and the
setting becomes the fallback for a session with no event attached.
"""

from datetime import UTC, datetime
from zoneinfo import ZoneInfo

from app.config import settings


def to_local(moment: datetime) -> datetime:
    """Move an instant into the display timezone.

    A naive datetime is assumed to be UTC: that is how these are stored, and
    reading one as local time would shift it by the offset in the wrong
    direction — the failure this function exists to prevent.
    """
    if moment.tzinfo is None:
        moment = moment.replace(tzinfo=UTC)
    return moment.astimezone(ZoneInfo(settings.display_timezone))


def format_moment_time(moment: datetime) -> str:
    """The clock time of a moment, as a card shows it: "22h47"."""
    return to_local(moment).strftime("%Hh%M")
