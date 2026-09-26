"""When an event happened, as instants — for checking a night belongs to it.

An event is a date and two wall-clock hours (`events.start_time`,
`end_time`), and **the offset stored with those hours carries no information
and must not be read** (`schemas/event.py`, migration 007). So the hours are
read as the display timezone's wall clock on the event's date, which is what
the operator typed.

A night uploaded with an `event_id` must overlap the event, with two hours
of slack either side (contract of 26/09): the doors open early, a strap is
put on in the queue, the encore runs late. Without the check an `event_id` is
a claim anybody can make, and it opens the feed of an event the person was
never at (LGPD audit, AL-10).
"""

from datetime import UTC, datetime, time, timedelta
from zoneinfo import ZoneInfo

from app.config import settings
from app.models.event import Event

SLACK = timedelta(hours=2)

NOT_THIS_EVENT = "Essa noite não bate com o horário do evento."


def _wall(value: time | None, fallback: time) -> time:
    if value is None:
        return fallback
    return value.replace(tzinfo=None)


def event_bounds(event: Event) -> tuple[datetime, datetime]:
    """The event's start and end, as aware instants.

    No hours means the whole day. An end at or before the start is an event
    that crosses midnight — "termina 03:00" — and ends the next day.
    """
    zone = ZoneInfo(settings.display_timezone)
    start_clock = _wall(event.start_time, time(0, 0))
    start = datetime.combine(event.date, start_clock, tzinfo=zone)
    if event.end_time is None:
        end = datetime.combine(event.date + timedelta(days=1), time(0, 0), tzinfo=zone)
    else:
        end = datetime.combine(
            event.date, _wall(event.end_time, time(0, 0)), tzinfo=zone
        )
        if end <= start:
            end += timedelta(days=1)
    return start, end


def event_window(event: Event) -> tuple[datetime, datetime]:
    """The span a night at this event may fall in: the event ± two hours."""
    start, end = event_bounds(event)
    return start - SLACK, end + SLACK


def _aware(moment: datetime) -> datetime:
    return moment if moment.tzinfo else moment.replace(tzinfo=UTC)


def night_fits(event: Event, start_time: datetime, end_time: datetime) -> bool:
    """Whether a night from `start_time` to `end_time` overlaps the window."""
    low, high = event_window(event)
    return _aware(start_time) <= high and _aware(end_time) >= low
