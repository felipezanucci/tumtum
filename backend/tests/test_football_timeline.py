"""A match minute is not a wall-clock time.

Until 2026-09-22 the football timeline added the minute to the scheduled
kick-off and stopped: every second-half event landed ~15 minutes early, and a
kick-off that slipped by five pushed the whole match with it. A match is two
clocks, each anchored on the operator's tap — APITO INICIAL, 2º TEMPO — and
falls back to the schedule only when it has to, saying so.
"""

from datetime import UTC, datetime, timedelta

from app.services.football_service import (
    HALF_TIME_MINUTES,
    KICKOFF,
    SECOND_HALF,
    anchors_from_timeline,
    match_clock,
    parse_fixture_to_timeline,
)

SCHEDULED = datetime(2026, 10, 4, 19, 0, tzinfo=UTC)


def fixture(kickoff: datetime = SCHEDULED) -> dict:
    return {
        "fixture": {"id": 1, "date": kickoff.isoformat()},
        "teams": {"home": {"name": "Corinthians"}, "away": {"name": "Palmeiras"}},
    }


def goal(elapsed: int, extra: int | None = None, player: str = "Yuri Alberto") -> dict:
    return {
        "time": {"elapsed": elapsed, "extra": extra},
        "type": "Goal",
        "detail": "Normal Goal",
        "player": {"name": player},
        "team": {"name": "Corinthians"},
    }


def card(elapsed: int, extra: int | None = None) -> dict:
    return {
        "time": {"elapsed": elapsed, "extra": extra},
        "type": "Card",
        "detail": "Yellow Card",
        "player": {"name": "Alguém"},
        "team": {"name": "Palmeiras"},
    }


def minutes(n: int) -> timedelta:
    return timedelta(minutes=n)


def by_type(timeline: list[dict], entry_type: str) -> list[dict]:
    return [e for e in timeline if e["entry_type"] == entry_type]


# --- the clock ---


def test_a_first_half_minute_counts_from_the_first_half_start():
    clock = match_clock(SCHEDULED, [], kickoff_at=SCHEDULED + minutes(4))
    assert clock.wall_clock(12) == SCHEDULED + minutes(4 + 12)


def test_a_second_half_minute_counts_from_the_second_half_start_not_the_kickoff():
    second = SCHEDULED + minutes(45 + 3 + 17)  # stoppage 3, interval 17
    clock = match_clock(SCHEDULED, [], kickoff_at=SCHEDULED, second_half_at=second)
    # Minute 73 is 28 minutes into the second half — never 73 after kick-off.
    assert clock.wall_clock(73) == second + minutes(28)
    assert clock.wall_clock(73) != SCHEDULED + minutes(73)


def test_stoppage_time_is_added_to_the_minute():
    clock = match_clock(
        SCHEDULED, [], kickoff_at=SCHEDULED, second_half_at=SCHEDULED + minutes(63)
    )
    assert clock.wall_clock(45, extra=2) == SCHEDULED + minutes(47)
    assert clock.wall_clock(90, extra=4) == SCHEDULED + minutes(63 + 45 + 4)


def test_without_a_second_half_tap_the_interval_is_assumed_and_the_stoppage_read_from_events():
    clock = match_clock(SCHEDULED, [goal(45, extra=3)], kickoff_at=SCHEDULED)
    assert clock.first_half_stoppage == 3
    assert clock.second_half_start == SCHEDULED + minutes(45 + 3 + HALF_TIME_MINUTES)
    assert clock.first_half_anchored
    assert not clock.second_half_anchored


def test_without_any_tap_the_schedule_is_the_fallback_and_nothing_claims_to_be_anchored():
    clock = match_clock(SCHEDULED, [])
    assert clock.first_half_start == SCHEDULED
    assert not clock.first_half_anchored
    assert not clock.second_half_anchored


# --- the anchors, read back from the timeline the operator's taps went into ---


def test_anchors_are_read_from_the_operator_taps():
    kick = SCHEDULED + minutes(2)
    second = SCHEDULED + minutes(64)
    timeline = [
        {"timestamp": kick, "entry_type": KICKOFF},
        {"timestamp": SCHEDULED + minutes(20), "entry_type": "goal"},
        {"timestamp": second, "entry_type": SECOND_HALF},
    ]
    assert anchors_from_timeline(timeline) == (kick, second)


def test_a_repeated_tap_keeps_the_first():
    first = SCHEDULED + minutes(2)
    timeline = [
        {"timestamp": SCHEDULED + minutes(3), "entry_type": KICKOFF},
        {"timestamp": first, "entry_type": KICKOFF},
    ]
    assert anchors_from_timeline(timeline) == (first, None)


def test_no_taps_means_no_anchors():
    assert anchors_from_timeline([]) == (None, None)


# --- the timeline as a whole ---


def test_a_second_half_goal_lands_where_the_second_half_clock_says():
    kick = SCHEDULED + minutes(5)
    second = kick + minutes(45 + 2 + 16)
    timeline = parse_fixture_to_timeline(
        fixture(), [goal(45, extra=2), goal(73)], kickoff_at=kick, second_half_at=second
    )
    goals = by_type(timeline, "goal")
    assert goals[0]["timestamp"] == kick + minutes(47)
    assert goals[1]["timestamp"] == second + minutes(28)
    # The old arithmetic would have put minute 73 here — 18 minutes early.
    assert goals[1]["timestamp"] - (SCHEDULED + minutes(73)) == minutes(5 + 2 + 16)


def test_the_interval_sits_at_the_end_of_first_half_stoppage():
    timeline = parse_fixture_to_timeline(
        fixture(), [card(45, extra=4)], kickoff_at=SCHEDULED
    )
    (halftime,) = by_type(timeline, "halftime")
    assert halftime["timestamp"] == SCHEDULED + minutes(49)
    assert halftime["metadata"]["stoppage"] == 4


def test_an_operator_tap_is_not_duplicated_as_an_entry():
    with_taps = parse_fixture_to_timeline(
        fixture(), [], kickoff_at=SCHEDULED, second_half_at=SCHEDULED + minutes(60)
    )
    assert by_type(with_taps, KICKOFF) == []
    assert by_type(with_taps, SECOND_HALF) == []

    without = parse_fixture_to_timeline(fixture(), [])
    assert [e["label"] for e in by_type(without, KICKOFF)] == ["Início do jogo"]
    assert [e["label"] for e in by_type(without, SECOND_HALF)] == ["Segundo tempo"]


def test_every_entry_says_whether_its_time_was_measured_or_assumed():
    timeline = parse_fixture_to_timeline(
        fixture(), [goal(20), goal(70)], kickoff_at=SCHEDULED
    )
    first, second = by_type(timeline, "goal")
    assert first["metadata"]["anchored"] is True
    assert second["metadata"]["anchored"] is False
    assert second["metadata"]["assumed_half_time_min"] == HALF_TIME_MINUTES
    assert all(e["metadata"]["source"] == "api-football" for e in timeline)


def test_entries_come_out_in_time_order():
    timeline = parse_fixture_to_timeline(fixture(), [goal(70), goal(12), card(30)])
    stamps = [e["timestamp"] for e in timeline]
    assert stamps == sorted(stamps)
