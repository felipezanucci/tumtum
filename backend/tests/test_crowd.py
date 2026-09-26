"""Card 04 — the crowd, and the line where a crowd becomes a person.

The brand manual asks for "a statistically and privately valid collective
sample". These tests defend the *privately* half, which is the one that can
hurt somebody: a collective figure over a small crowd is not collective — it
is a fact about each person in it.

Since 26/09 (LGPD audit, AL-4) the floors are the legal opinion's — 100
nights, 10 people a minute — and a count leaves only as a band. The logic
tests below pass small floors so a crowd fits on a screen; the defaults are
pinned on their own.
"""

from datetime import UTC, datetime, timedelta

from app.config import settings
from app.services.crowd import MIN_CELL, MIN_CROWD, collective_moments, people_band

START = datetime(2026, 11, 6, 22, 0, tzinfo=UTC)


def at(minutes: float) -> datetime:
    return START + timedelta(minutes=minutes)


def peaks(*specs: tuple[float, str | None]) -> list[dict]:
    return [{"at": at(m), "label": label} for m, label in specs]


def together(n: int, minute: float, label: str | None) -> list[tuple]:
    """n people peaking within the same bucket."""
    return [(minute + i * 0.01, label) for i in range(n)]


def crowd_of(specs, nights=None, **kw):
    kw.setdefault("min_nights", 4)
    kw.setdefault("min_cell", 10)
    return collective_moments(peaks(*specs), measured_nights=nights or len(specs), **kw)


# --- the floors and their defaults ---


def test_the_defaults_are_the_legal_opinions_numbers():
    assert MIN_CROWD == 100 and MIN_CELL == 10
    assert settings.crowd_min_nights == 100
    assert settings.crowd_min_cell == 10


def test_a_crowd_too_small_publishes_nothing_not_even_its_size():
    crowd = collective_moments(peaks(*together(20, 0, "Yellow")), measured_nights=99)
    assert crowd.enough is False
    assert crowd.moments == [] and crowd.top is None
    # "três noites" is itself a fact about three people.
    assert crowd.measured_nights is None


def test_the_floor_is_the_number_of_nights_not_the_number_of_peaks():
    """One person with many peaks is still one person."""
    many = peaks(*together(200, 0, "Yellow"))
    assert collective_moments(many, measured_nights=2).enough is False


def test_exactly_at_the_floor_it_publishes_and_says_how_many():
    crowd = collective_moments(peaks(*together(100, 0, "Yellow")), measured_nights=100)
    assert crowd.enough is True
    assert crowd.measured_nights == 100
    assert crowd.top is not None and crowd.top.people_band == "100+"


def test_no_peaks_at_all_is_not_enough_however_big_the_crowd():
    crowd = collective_moments([], measured_nights=500)
    assert crowd.enough is False


# --- bands, never counts ---


def test_people_are_published_as_bands():
    assert people_band(9) is None
    assert people_band(10) == "10+"
    assert people_band(24) == "10+"
    assert people_band(25) == "25+"
    assert people_band(57) == "50+"
    assert people_band(100) == "100+"
    assert people_band(1000) == "250+"


def test_no_moment_carries_an_exact_count():
    crowd = crowd_of(together(37, 0, "Yellow"))
    assert crowd.top is not None
    assert crowd.top.people_band == "25+"
    assert not hasattr(crowd.top, "people")


def test_a_minute_below_the_cell_floor_is_suppressed():
    """Nine people together is a fact about nine people."""
    spec = together(9, 0, "Yellow") + [(40 + i, "Clocks") for i in range(3)]
    crowd = crowd_of(spec, min_cell=5)
    assert crowd.enough is True
    assert crowd.moments == []  # 9 ≥ min_cell, but no band says 9 truthfully


def test_the_cell_floor_is_configurable_upwards():
    crowd = crowd_of(together(12, 0, "Yellow"), min_cell=20)
    assert crowd.moments == []


# --- what a collective moment is ---


def test_hearts_rising_together_become_one_moment():
    crowd = crowd_of(together(12, 0, "Yellow"))
    assert crowd.top is not None
    assert crowd.top.people_band == "10+"
    assert crowd.top.label == "Yellow"


def test_people_alone_in_a_minute_are_not_a_collective_moment():
    spec = [*together(12, 0, "Yellow"), (40, "Clocks"), (60, "Fix You")]
    crowd = crowd_of(spec)
    assert [m.label for m in crowd.moments] == ["Yellow"]


def test_two_real_moments_are_both_kept_in_time_order():
    spec = together(15, 0, "Yellow") + together(30, 30, "Fix You")
    crowd = crowd_of(spec)
    assert [m.label for m in crowd.moments] == ["Yellow", "Fix You"]
    assert [m.people_band for m in crowd.moments] == ["10+", "25+"]
    assert crowd.moments[0].at < crowd.moments[1].at
    # The top is the biggest minute, decided on the exact count.
    assert crowd.top is not None and crowd.top.label == "Fix You"


def test_a_moment_nothing_named_stays_unnamed():
    """The app either knows or says nothing — it never guesses a name here."""
    crowd = crowd_of(together(12, 0, None))
    assert crowd.top is not None
    assert crowd.top.label is None


def test_the_name_is_the_one_most_of_the_crowd_gave_that_minute():
    spec = together(8, 0, "Yellow") + together(4, 0.5, "Clocks")
    crowd = crowd_of(spec)
    assert crowd.top is not None
    assert crowd.top.label == "Yellow"


def test_peaks_seconds_apart_are_the_same_moment():
    """A goal is felt across a stadium over a few seconds, not one minute."""
    spec = [(i * 0.1, "GOL") for i in range(12)]
    crowd = crowd_of(spec)
    assert len(crowd.moments) == 1


def test_the_shared_count_is_carried_through_untouched():
    crowd = crowd_of(together(12, 0, "Yellow"), nights=90, shared_count=3)
    assert crowd.shared_count == 3
    assert crowd.measured_nights == 90
