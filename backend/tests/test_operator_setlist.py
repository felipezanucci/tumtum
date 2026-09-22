"""The operator's script for a show.

A concert has no API that says which song was playing at 22h12. Setlist.fm
publishes order and never times; audio fingerprinting matches a studio
recording, not a band playing live; four-minutes-a-song is outside the
correlator's window by the third song. What is left is a person tapping, and
the tap is the measurement.

The rule these tests defend: **a time this table reports is one somebody
measured.** Anything else must come back null rather than plausible.
"""

from datetime import UTC, datetime

from app.api.events import _clean_songs, _merge_started

AT = datetime(2026, 11, 6, 22, 12, tzinfo=UTC)
LATER = datetime(2026, 11, 6, 22, 41, tzinfo=UTC)


# --- what a paste becomes ---


def test_blank_lines_are_dropped():
    assert _clean_songs(["Yellow", "", "   ", "Clocks"]) == ["Yellow", "Clocks"]


def test_numbering_people_paste_is_stripped():
    pasted = ["1. Yellow", "02 - Clocks", "3) Fix You", "12: Viva La Vida"]
    assert _clean_songs(pasted) == ["Yellow", "Clocks", "Fix You", "Viva La Vida"]


def test_an_en_dash_and_an_em_dash_count_as_numbering_too():
    assert _clean_songs(["4 – Trouble", "5 — Shiver"]) == ["Trouble", "Shiver"]


def test_a_number_that_is_part_of_the_name_survives():
    """Stripping numbering must not eat a song called by a number."""
    assert _clean_songs(["1. 42", "2. 7 Years"]) == ["42", "7 Years"]


def test_a_song_that_is_only_a_number_is_kept():
    assert _clean_songs(["42"]) == ["42"]


def test_a_title_longer_than_the_column_is_cut_not_refused():
    assert _clean_songs(["x" * 400]) == ["x" * 255]


def test_none_and_whitespace_do_not_crash_a_paste():
    assert _clean_songs([None, "\t\n", "Yellow"]) == ["Yellow"]


# --- correcting the list mid-show ---


def test_a_correction_keeps_the_times_already_measured():
    existing = [(1, "Yellow", AT), (2, "Clocks", LATER), (3, "Fix You", None)]
    merged = _merge_started(existing, ["Yellow", "Clocks", "Fix You", "Trouble"])
    assert merged == [
        (1, "Yellow", AT),
        (2, "Clocks", LATER),
        (3, "Fix You", None),
        (4, "Trouble", None),
    ]


def test_correcting_a_typo_does_not_cost_the_show_its_measurement():
    """The one Felipe hit, 22/09, and the reason the key changed.

    He fixed one word in a title. The row's time vanished, COMEÇOU lit up as
    if the song had not started, and the only way back the screen offered was
    to tap it — writing *now* into a song that began an hour before. A false
    measurement is worse than a lost one.
    """
    existing = [(1, "Yellow", AT), (2, "Clocs", LATER), (3, "Fix You", None)]
    merged = _merge_started(existing, ["Yellow", "Clocks", "Fix You"])
    assert merged == [(1, "Yellow", AT), (2, "Clocks", LATER), (3, "Fix You", None)]


def test_the_time_stays_with_the_slot_when_the_order_is_corrected():
    """The tap was on the slot: "the second thing started at 21h44"."""
    existing = [(1, "Yellow", AT), (2, "Clocks", LATER)]
    merged = _merge_started(existing, ["Clocks", "Yellow"])
    assert merged == [(1, "Clocks", AT), (2, "Yellow", LATER)]


def test_a_position_nobody_tapped_comes_back_unmeasured():
    """Nothing is ever invented: no tap, no time."""
    existing = [(1, "Yellow", None)]
    assert _merge_started(existing, ["Trouble", "Clocks"]) == [
        (1, "Trouble", None),
        (2, "Clocks", None),
    ]


def test_a_paste_can_never_raise_the_number_of_measured_rows():
    existing = [(1, "Yellow", AT), (2, "Clocks", None)]
    merged = _merge_started(existing, ["Yellow", "Clocks", "Fix You", "Trouble"])
    assert sum(1 for _p, _t, at in merged if at is not None) == 1


def test_a_song_dropped_from_the_list_takes_its_time_with_it():
    existing = [(1, "Yellow", AT), (2, "Clocks", LATER)]
    assert _merge_started(existing, ["Yellow"]) == [(1, "Yellow", AT)]


def test_clearing_the_list_clears_everything():
    assert _merge_started([(1, "Yellow", AT)], []) == []


def test_an_encore_of_the_same_song_is_its_own_slot():
    """Two slots, two moments: the stamp on 1 never leaks onto 3."""
    existing = [(1, "Yellow", AT), (2, "Clocks", None), (3, "Yellow", None)]
    merged = _merge_started(existing, ["Yellow", "Clocks", "Yellow"])
    assert merged == [(1, "Yellow", AT), (2, "Clocks", None), (3, "Yellow", None)]


def test_a_title_that_opens_with_a_number_and_a_hyphen_survives():
    """Strip a tight dash blind and Logic's song is filed as "800-273-8255"."""
    assert _clean_songs(["1-800-273-8255"]) == ["1-800-273-8255"]
    assert _clean_songs(["7. 1-800-273-8255"]) == ["1-800-273-8255"]


def test_numbering_tight_against_the_title_still_goes():
    assert _clean_songs(["1.Yellow", "2)Clocks"]) == ["Yellow", "Clocks"]
