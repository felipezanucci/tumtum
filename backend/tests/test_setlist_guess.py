"""An estimated entry offers a name; it never asserts one.

The setlist gives order, not times. Four minutes a song from the advertised
start is right for the first song and wrong by the tenth, so a moment that
falls in an estimated window gets the two or three songs that might have
been playing — for the person to pick — and never a name on the card.
"""

from datetime import UTC, datetime, timedelta

from app.services.setlist_guess import candidate_labels, is_tentative

START = datetime(2026, 10, 10, 21, 0, tzinfo=UTC)


def at(seconds: int) -> datetime:
    return START + timedelta(seconds=seconds)


def song(index: int, label: str, duration: int = 240) -> dict:
    return {
        "time": at(index * 240),
        "label": label,
        "entry_type": "song_start",
        "metadata": {
            "estimated": True,
            "index": index,
            "duration_sec": duration,
            "uncertainty_sec": 60 * (index + 1),
        },
    }


SETLIST = [song(i, f"Música {i + 1}") for i in range(10)]


def peak(seconds: int, region_start: int | None = None) -> dict:
    p = {
        "timestamp": at(seconds),
        "bpm": 150,
        "duration_seconds": 30,
        "magnitude": 30.0,
    }
    if region_start is not None:
        p["start_time"] = at(region_start)
        p["end_time"] = at(seconds)
    return p


def test_a_moment_inside_the_first_song_offers_that_song_first():
    assert candidate_labels(peak(90), SETLIST)[0] == "Música 1"


def test_the_moment_is_placed_by_the_start_of_its_region_not_its_peak():
    # The region opened at 2:00 into song 1 and peaked 3 minutes later.
    labels = candidate_labels(peak(300, region_start=120), SETLIST)
    assert labels[0] == "Música 1"


def test_later_in_the_set_the_widened_windows_overlap_and_more_songs_are_offered():
    # 34 minutes in: the estimate says song 9, but the drift allows 7 and 8.
    labels = candidate_labels(peak(34 * 60), SETLIST)
    assert 2 <= len(labels) <= 3
    assert "Música 9" in labels


def test_the_song_the_estimate_lands_on_is_ranked_first_and_earlier_songs_follow():
    # Drift runs late, so a moment in song 9's estimated window offers 9, then
    # the ones that may still have been playing — never the next one.
    labels = candidate_labels(peak(34 * 60), SETLIST)
    assert labels[0] == "Música 9"
    assert "Música 10" not in labels


def test_never_more_than_three():
    assert len(candidate_labels(peak(34 * 60), SETLIST)) <= 3


def test_a_moment_nothing_covers_gets_no_guess():
    # Twenty minutes before the show's estimated start: nobody was playing.
    assert candidate_labels(peak(-20 * 60), SETLIST) == []
    assert candidate_labels(peak(90), []) == []


def test_a_football_entry_without_an_anchor_is_offered_with_its_own_uncertainty():
    goal = {
        "time": at(70 * 60),
        "label": "⚽ Gol! Yuri Alberto",
        "entry_type": "goal",
        "metadata": {"anchored": False, "uncertainty_sec": 600},
    }
    # Eight minutes after the schedule's guess is inside a ten-minute drift.
    assert candidate_labels(peak(78 * 60), [goal]) == ["⚽ Gol! Yuri Alberto"]
    # Twenty minutes after is not.
    assert candidate_labels(peak(90 * 60), [goal]) == []


def test_which_entries_are_tentative():
    assert is_tentative({"estimated": True})
    assert is_tentative({"anchored": False})
    assert not is_tentative({"anchored": True})
    assert not is_tentative({"player": "Yuri"})
    assert not is_tentative(None)
