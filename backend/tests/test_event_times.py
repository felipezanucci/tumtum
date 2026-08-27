"""The event hours that broke saving a festival.

`events.start_time` and `events.end_time` are `TIME WITH TIME ZONE`, and
asyncpg encodes that type as `obj.tzinfo.utcoffset(None)`. A naive
`datetime.time` — exactly what parsing "22:00:00" produces — has no `tzinfo`,
so the driver raised `AttributeError` and the browser was told only
"Erro interno do servidor".

These lock the coercion in place. They stay meaningful after migration 007
drops the timezone from the columns: an offset-aware time cast to a plain
`time` keeps the wall-clock digits, which is all anybody is ever shown.
"""

from datetime import UTC, date, time

from app.schemas.event import EventCreateRequest, EventUpdateRequest, offset_aware


class TestOffsetAware:
    def test_gives_a_bare_time_an_offset(self):
        assert offset_aware(time(22, 0)).tzinfo is not None

    def test_keeps_the_wall_clock_digits(self):
        result = offset_aware(time(22, 0))
        assert (result.hour, result.minute) == (22, 0)

    def test_leaves_a_time_that_already_has_one_alone(self):
        already = time(3, 0, tzinfo=UTC)
        assert offset_aware(already) is already

    def test_absent_stays_absent(self):
        # None means "leave this column alone", never midnight.
        assert offset_aware(None) is None


class TestEventSchemas:
    def test_create_accepts_the_festival_hours(self):
        body = EventCreateRequest(
            name="Realness Festival 2026",
            date=date(2026, 8, 29),
            event_type="festival",
            start_time="22:00:00",
            end_time="03:00:00",
        )
        assert body.start_time.tzinfo is not None
        assert body.end_time.tzinfo is not None
        assert (body.start_time.hour, body.start_time.minute) == (22, 0)
        assert (body.end_time.hour, body.end_time.minute) == (3, 0)

    def test_update_accepts_them_too(self):
        """The path that actually failed: the event existed, the hours did not."""
        body = EventUpdateRequest(start_time="22:00:00", end_time="03:00:00")
        assert body.start_time.tzinfo is not None
        assert body.end_time.tzinfo is not None

    def test_update_without_times_touches_nothing(self):
        body = EventUpdateRequest(name="Realness Festival 2026")
        assert body.start_time is None
        assert body.end_time is None
        assert "start_time" not in body.model_dump(exclude_unset=True)
