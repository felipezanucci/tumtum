"""Card 04 — the crowd, and the line where a crowd becomes a person.

The brand manual asks for "a statistically and privately valid collective
sample". These tests defend the *privately* half, which is the one that can
hurt somebody: a collective figure over a small crowd is not collective — it
is a fact about each person in it.
"""

from datetime import UTC, datetime, timedelta

from app.services.crowd import MIN_CROWD, collective_moments

START = datetime(2026, 11, 6, 22, 0, tzinfo=UTC)


def at(minutes: float) -> datetime:
    return START + timedelta(minutes=minutes)


def peaks(*specs: tuple[float, str | None]) -> list[dict]:
    return [{"at": at(m), "label": label} for m, label in specs]


# --- the privacy floor ---


def test_a_crowd_too_small_publishes_nothing():
    crowd = collective_moments(peaks((0, "Yellow"), (0.5, "Yellow")), measured_nights=2)
    assert crowd.enough is False
    assert crowd.moments == []
    assert crowd.top is None


def test_the_floor_is_the_number_of_nights_not_the_number_of_peaks():
    """One person with many peaks is still one person."""
    many = peaks(*[(i * 0.1, "Yellow") for i in range(40)])
    assert collective_moments(many, measured_nights=2).enough is False


def test_the_count_it_refuses_on_is_still_reported():
    """The screen must be able to say "ainda somos poucos", not draw a zero."""
    crowd = collective_moments(peaks((0, None)), measured_nights=3)
    assert crowd.enough is False
    assert crowd.measured_nights == 3


def test_exactly_at_the_floor_it_publishes():
    spec = [(0, "Yellow"), (0.4, "Yellow"), (0.8, "Yellow"), (1.2, "Yellow")]
    crowd = collective_moments(peaks(*spec), measured_nights=MIN_CROWD)
    assert crowd.enough is True
    assert crowd.top is not None


def test_no_peaks_at_all_is_not_enough_however_big_the_crowd():
    crowd = collective_moments([], measured_nights=500)
    assert crowd.enough is False


# --- what a collective moment is ---


def test_hearts_rising_together_become_one_moment():
    spec = [(0, "Yellow"), (0.3, "Yellow"), (0.9, "Yellow"), (1.5, "Yellow")]
    crowd = collective_moments(peaks(*spec), measured_nights=4)

    assert crowd.top is not None
    assert crowd.top.people == 4
    assert crowd.top.label == "Yellow"


def test_one_person_alone_in_a_minute_is_not_a_collective_moment():
    """Four people, three together and one wandering off on their own."""
    spec = [(0, "Yellow"), (0.3, "Yellow"), (0.6, "Yellow"), (40, "Clocks")]
    crowd = collective_moments(peaks(*spec), measured_nights=4)

    assert [m.people for m in crowd.moments] == [3]
    assert crowd.top is not None
    assert crowd.top.label == "Yellow"


def test_two_real_moments_are_both_kept_in_time_order():
    spec = [
        (0, "Yellow"),
        (0.3, "Yellow"),
        (0.6, "Yellow"),
        (30, "Fix You"),
        (30.4, "Fix You"),
        (30.8, "Fix You"),
    ]
    crowd = collective_moments(peaks(*spec), measured_nights=6)

    assert [m.people for m in crowd.moments] == [3, 3]
    assert [m.label for m in crowd.moments] == ["Yellow", "Fix You"]
    assert crowd.moments[0].at < crowd.moments[1].at


def test_a_moment_nothing_named_stays_unnamed():
    """The app either knows or says nothing — it never guesses a name here."""
    spec = [(0, None), (0.3, None), (0.6, None), (0.9, None)]
    crowd = collective_moments(peaks(*spec), measured_nights=4)

    assert crowd.top is not None
    assert crowd.top.people == 4
    assert crowd.top.label is None


def test_the_name_is_the_one_most_of_the_crowd_gave_that_minute():
    spec = [(0, "Yellow"), (0.3, "Yellow"), (0.6, "Clocks"), (0.9, "Yellow")]
    crowd = collective_moments(peaks(*spec), measured_nights=4)

    assert crowd.top is not None
    assert crowd.top.label == "Yellow"


def test_peaks_seconds_apart_are_the_same_moment():
    """A goal is felt across a stadium over a few seconds, not one minute."""
    spec = [(0, "GOL"), (0.2, "GOL"), (0.7, "GOL"), (1.1, "GOL")]
    crowd = collective_moments(peaks(*spec), measured_nights=4)

    assert len(crowd.moments) == 1
    assert crowd.moments[0].people == 4


def test_the_shared_count_is_carried_through_untouched():
    spec = [(0, "Yellow"), (0.3, "Yellow"), (0.6, "Yellow"), (0.9, "Yellow")]
    crowd = collective_moments(peaks(*spec), measured_nights=9, shared_count=3)

    assert crowd.shared_count == 3
    assert crowd.measured_nights == 9
