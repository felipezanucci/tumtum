"""The score has to be able to say a night is incomplete.

The formula this replaces could not. It divided readings by an expected
`duration × 12` and capped at 1.0, so anything sampling faster than once
per 5 s saturated — and the chest strap samples once per second. The first
test below is the capture that exposed it, at the real numbers.
"""

from datetime import UTC, datetime, timedelta

from app.services.data_quality import coverage, score


def night(start_hhmm: str, minutes: int) -> tuple[datetime, datetime]:
    hour, minute = (int(part) for part in start_hhmm.split(":"))
    start = datetime(2026, 8, 29, hour, minute, tzinfo=UTC)
    return start, start + timedelta(minutes=minutes)


def every(start: datetime, seconds: int, count: int) -> list[datetime]:
    return [start + timedelta(seconds=seconds * i) for i in range(count)]


def test_realness_hole_is_visible():
    """The capture that proved the old formula blind.

    21:11 → 03:17 is 366 minutes. The strap was on for the last 287 of them
    at 1 Hz; the first 79 are the gap between a brief test at home and the
    real connection at the venue. The old formula reported 100%.
    """
    start, end = night("21:11", 366)
    readings = every(start + timedelta(minutes=79), 1, 287 * 60)

    assert score(start, end, readings) == 78


def test_one_per_second_across_the_whole_window_is_full():
    start, end = night("22:00", 60)
    readings = every(start, 1, 60 * 60)

    assert score(start, end, readings) == 100


def test_one_per_five_seconds_is_also_full():
    """The detector's own resolution is the ceiling — nothing above it counts
    for more, which is exactly why volume was the wrong measure."""
    start, end = night("22:00", 60)
    readings = every(start, 5, 12 * 60)

    assert score(start, end, readings) == 100


def test_a_watch_writing_once_a_minute_scores_like_one():
    """Health Connect's background cadence, measured 2026-08-29: one reading
    per minute fills one slot in twelve, and the score says so instead of
    flattering it."""
    start, end = night("22:00", 60)
    readings = every(start, 60, 60)

    assert score(start, end, readings) == 8


def test_volume_alone_cannot_buy_a_score():
    """A thousand readings crammed into one minute of a one-hour window
    describe one minute, not the hour — the exact failure the old formula
    could not see."""
    start, end = night("22:00", 60)
    readings = [start + timedelta(milliseconds=100 * i) for i in range(1000)]

    assert score(start, end, readings) < 5


def test_readings_outside_the_window_do_not_count():
    start, end = night("22:00", 10)
    outside = every(end + timedelta(minutes=5), 1, 600)

    assert score(start, end, outside) == 0


def test_nothing_captured_is_zero_not_an_error():
    start, end = night("22:00", 60)

    assert score(start, end, []) == 0


def test_an_inverted_window_is_zero_rather_than_negative():
    start, end = night("22:00", 60)

    assert coverage(end, start, [start]) == 0.0


def test_a_single_instant_session_with_one_reading_is_full():
    """Degenerate but real: start == end has exactly one slot, and one
    reading fills it. It must not divide by zero."""
    start = datetime(2026, 8, 29, 22, 0, tzinfo=UTC)

    assert score(start, start, [start]) == 100
