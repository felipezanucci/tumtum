"""A moment that lasts is named by what started it, not by what is nearest.

The old rule — nearest entry within ±60 s of the peak — is right for a spike
and wrong for a song: the heart peaks three minutes in, and the song's own
entry is then out of reach while the next song's may be inside it.
"""

from datetime import UTC, datetime, timedelta

from app.services.event_correlator import correlate_peaks_to_timeline

START = datetime(2026, 9, 25, 22, 0, tzinfo=UTC)


def at(seconds: int) -> datetime:
    return START + timedelta(seconds=seconds)


def entry(seconds: int, label: str) -> dict:
    return {"time": at(seconds), "label": label, "id": label}


def peak(timestamp: int, start: int | None = None, end: int | None = None) -> dict:
    p = {
        "timestamp": at(timestamp),
        "bpm": 150,
        "duration_seconds": 10,
        "magnitude": 30.0,
    }
    if start is not None and end is not None:
        p["start_time"] = at(start)
        p["end_time"] = at(end)
        p["duration_seconds"] = end - start
    return p


SETLIST = [entry(0, "Abertura"), entry(240, "Música 2"), entry(480, "Música 3")]


def test_a_peak_three_minutes_into_a_song_is_named_after_that_song():
    """Old rule: 420 s is 60 s from 'Música 3' → wrong. New rule: 'Música 2'."""
    [matched] = correlate_peaks_to_timeline([peak(420, start=250, end=470)], SETLIST)

    assert matched["matched_label"] == "Música 2"


def test_a_region_that_begins_during_the_intro_still_matches_the_song():
    """Excitement starts as the previous song fades: the entry is inside reach."""
    [matched] = correlate_peaks_to_timeline([peak(300, start=225, end=470)], SETLIST)

    assert matched["matched_label"] == "Música 2"


def test_a_goal_just_before_the_region_is_the_cause():
    timeline = [
        entry(0, "Início do Jogo"),
        entry(2232, "⚽ Gol!"),
        entry(2700, "Intervalo"),
    ]
    [matched] = correlate_peaks_to_timeline(
        [peak(2265, start=2236, end=2400)], timeline
    )

    assert matched["matched_label"] == "⚽ Gol!"


def test_a_spike_with_nothing_before_it_falls_back_to_the_nearest_entry():
    """An entry 40 s *after* the peak, nothing before: the old rule applies."""
    timeline = [entry(1000, "Refrão")]
    [matched] = correlate_peaks_to_timeline([peak(960, start=955, end=975)], timeline)

    assert matched["matched_label"] == "Refrão"


def test_nothing_within_reach_matches_nothing():
    timeline = [entry(0, "Abertura"), entry(3000, "Bis")]
    [matched] = correlate_peaks_to_timeline(
        [peak(1500, start=1490, end=1520)], timeline
    )

    assert matched["matched_label"] is None
    assert matched["timeline_entry_id"] is None


def test_a_stored_peak_without_region_bounds_still_matches():
    """Peaks read back from the database carry only timestamp and duration."""
    stored = {
        "timestamp": at(420),
        "bpm": 150,
        "duration_seconds": 200,
        "magnitude": 30.0,
    }
    [matched] = correlate_peaks_to_timeline([stored], SETLIST)

    assert matched["matched_label"] == "Música 2"


def test_an_empty_timeline_leaves_peaks_untouched():
    peaks = [peak(420, start=250, end=470)]

    assert correlate_peaks_to_timeline(peaks, []) is peaks
    assert "matched_label" not in peaks[0]
