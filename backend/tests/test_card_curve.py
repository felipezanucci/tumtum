"""The card draws a night, and it must not draw one that did not happen.

Two failures are possible and both are the project's oldest bug class on a
public object: a gap in the capture joined into a continuous line, and copy
asserting something the numbers do not establish. Everything here guards one
or the other.
"""

from datetime import UTC, datetime, timedelta

from app.services.card_curve import CURVE_SLOTS, resample, segments, slot_of
from app.services.card_generator import moment_copy


class Reading:
    def __init__(self, at: datetime, bpm: int):
        self.time = at
        self.bpm = bpm


START = datetime(2026, 8, 29, 21, 11, tzinfo=UTC)
END = START + timedelta(minutes=366)


def readings(offset_minutes: float, count: int, step_seconds: int = 1, bpm: int = 80):
    first = START + timedelta(minutes=offset_minutes)
    return [
        Reading(first + timedelta(seconds=step_seconds * i), bpm) for i in range(count)
    ]


# --------------------------------------------------------------- resampling


def test_the_realness_hole_stays_a_hole():
    """The capture that drove all of this: 79 minutes with nothing in them.

    Roughly the first fifth of the card's width must come back empty. Filling
    it would draw six hours of evidence for a night we only have five of.
    """
    curve = resample(START, END, readings(79, 287 * 60))

    empty_prefix = 0
    for value in curve:
        if value is not None:
            break
        empty_prefix += 1

    # Floor, not round: the first reading falls *inside* slot 38, so 38
    # slots stay empty and the 39th is where the night starts.
    assert empty_prefix == int(79 / 366 * CURVE_SLOTS) == 38
    assert all(v is not None for v in curve[empty_prefix:])


def test_a_slot_keeps_its_highest_reading_not_its_average():
    """The peak marker comes from the peaks table. If a slot averaged, the
    summit would sit below the dot drawn on top of it and the card would
    visibly contradict itself."""
    at = START + timedelta(minutes=100)
    curve = resample(
        START, END, [Reading(at, 70), Reading(at + timedelta(seconds=1), 130)]
    )

    assert max(v for v in curve if v is not None) == 130


def test_readings_outside_the_window_are_not_drawn():
    curve = resample(START, END, [Reading(END + timedelta(minutes=10), 190)])

    assert all(v is None for v in curve)


def test_an_inverted_window_draws_nothing_rather_than_guessing():
    assert resample(END, START, readings(10, 60)) == []


def test_the_last_reading_lands_inside_the_card():
    """An offset of exactly the full span must not index off the end."""
    curve = resample(START, END, [Reading(END, 99)])

    assert curve[-1] == 99


# ----------------------------------------------------------------- segments


def test_a_gap_splits_the_line_in_two():
    curve = [80, 82, None, None, 91, 88]

    assert segments(curve) == [[(0, 80), (1, 82)], [(4, 91), (5, 88)]]


def test_a_lone_reading_survives_as_its_own_run():
    """It cannot be drawn as a line, and dropping it would silently shorten
    the night. The caller draws a dot."""
    assert segments([None, 77, None]) == [[(1, 77)]]


def test_nothing_captured_is_no_runs_not_one_empty_run():
    assert segments([None, None]) == []


# ------------------------------------------------------------------ marking


def test_the_marker_lands_where_the_moment_is():
    half = START + timedelta(minutes=183)

    assert slot_of(START, END, half) == CURVE_SLOTS // 2


def test_a_moment_outside_the_session_is_not_marked():
    assert slot_of(START, END, END + timedelta(minutes=1)) is None


# --------------------------------------------------------------------- copy


def test_the_night_is_the_reference_when_there_was_a_rise():
    assert moment_copy(116, 78, "01h24") == ("78 A NOITE INTEIRA.", "ATÉ 01H24.")


def test_without_an_average_the_card_claims_no_baseline():
    """The replaced constant asserted a calm baseline on every card ever
    made. With no average to stand on, the copy must say less, not invent."""
    first, second = moment_copy(116, None, "01h24")

    assert "NOITE INTEIRA" not in first + second
    assert "01H24" in second


def test_a_peak_below_the_average_is_never_dressed_as_a_rise():
    """A quiet moment on a loud night. "Até" would promise a climb that the
    two numbers flatly contradict."""
    first, second = moment_copy(70, 78, "01h24")

    assert "ATÉ" not in second
    assert "78" not in first


def test_a_peak_equal_to_the_average_is_not_a_rise_either():
    first, _ = moment_copy(78, 78, "01h24")

    assert first != "78 A NOITE INTEIRA."


def test_copy_survives_a_moment_with_no_clock_time():
    for line in moment_copy(116, 78, None):
        assert line.strip()
        assert "NONE" not in line.upper()
