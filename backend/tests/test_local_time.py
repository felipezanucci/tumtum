from datetime import UTC, datetime

from app.services.local_time import format_moment_time, to_local


def test_a_moment_reads_as_the_clock_time_it_was_lived_at():
    """The rehearsal on 2026-08-25 was felt at 16h31 and stored as 19h31 UTC."""
    assert format_moment_time(datetime(2026, 8, 25, 19, 31, tzinfo=UTC)) == "16h31"


def test_a_naive_timestamp_is_read_as_utc_not_as_local():
    """Reading a stored instant as local time would shift it the wrong way."""
    assert format_moment_time(datetime(2026, 8, 25, 19, 31)) == "16h31"


def test_a_moment_after_midnight_utc_belongs_to_the_evening_before():
    """A festival ending at 22h15 is already the next day in UTC."""
    local = to_local(datetime(2026, 8, 30, 1, 15, tzinfo=UTC))
    assert local.strftime("%Hh%M") == "22h15"
    assert local.day == 29


def test_an_instant_already_in_the_display_zone_is_left_where_it_is():
    same = to_local(datetime(2026, 8, 25, 19, 31, tzinfo=UTC))
    assert to_local(same) == same
