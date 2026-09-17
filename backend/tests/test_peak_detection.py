"""The detector has to see a song, not only a spike.

Until 2026-09-17 the baseline was a 300 s rolling mean, and a mean contains
the event it is the reference for: any elevation longer than about 90 s
raised its own baseline and vanished. Every moment Realness reported lasted
8-22 s — which is what that detector could report, not necessarily what the
night held. These tests plant emotions of known length and shape and check
they come back, at strap density and at the cadence of a watch.
"""

import math
import random
from datetime import UTC, datetime, timedelta

from app.services.peak_detection import detect_peaks

START = datetime(2026, 9, 25, 22, 0, tzinfo=UTC)


def capture(
    events: list[tuple[int, int, int, int, int]],
    duration_sec: int,
    *,
    base: float = 88.0,
    drift: float = 6.0,
    noise: float = 2.5,
    seed: int = 7,
    step_sec: int = 1,
) -> list[dict]:
    """A synthetic capture: slow drift, noise, and planted elevations.

    Each event is (offset_sec, amplitude_bpm, rise_sec, plateau_sec, decay_sec).
    step_sec > 1 decimates the series the way a slow watch would.
    """
    random.seed(seed)

    def excitement(t: int) -> float:
        total = 0.0
        for offset, amp, rise, plateau, decay in events:
            d = t - offset
            if d < 0:
                continue
            if d < rise:
                total += amp * d / rise
            elif d < rise + plateau:
                total += amp
            elif d < rise + plateau + decay:
                total += amp * (1 - (d - rise - plateau) / decay)
        return total

    return [
        {
            "time": START + timedelta(seconds=t),
            "bpm": base
            + drift * math.sin(t / 900.0)
            + excitement(t)
            + random.gauss(0, noise),
        }
        for t in range(0, duration_sec, step_sec)
    ]


def seconds(peak: dict, key: str = "timestamp") -> float:
    return (peak[key] - START).total_seconds()


def found_within(peaks: list[dict], start: int, end: int) -> list[dict]:
    return [p for p in peaks if start <= seconds(p) <= end]


def test_a_short_spike_is_still_a_moment():
    """The kind of moment Realness reported: 15 s, +35 bpm."""
    peaks = detect_peaks(capture([(3600, 35, 4, 15, 30)], 7200))

    assert len(peaks) == 1
    assert 3600 <= seconds(peaks[0]) <= 3660
    assert peaks[0]["duration_seconds"] < 90


def test_a_favourite_song_sung_from_start_to_finish_is_one_moment():
    """Four minutes of euphoria: invisible before, one region now.

    The peak is wherever the heart was highest, and the region bounds
    the whole song rather than a sliver of it.
    """
    song_start, song_len = 3600, 240
    peaks = detect_peaks(capture([(song_start, 32, 12, song_len - 40, 30)], 7200))

    assert len(peaks) == 1, [p["duration_seconds"] for p in peaks]
    peak = peaks[0]
    assert song_start - 30 <= seconds(peak, "start_time") <= song_start + 30
    assert (
        song_start + song_len - 60
        <= seconds(peak, "end_time")
        <= song_start + song_len + 30
    )
    assert peak["duration_seconds"] >= 150
    assert seconds(peak, "start_time") <= seconds(peak) <= seconds(peak, "end_time")


def test_a_goal_celebration_survives_a_watch_sampling_every_32_seconds():
    """A goal lasts minutes, so a slow watch has samples inside it."""
    goal = 40 * 60
    slow = capture([(goal, 48, 8, 50, 120)], 100 * 60, step_sec=32)
    peaks = detect_peaks(slow)

    assert found_within(peaks, goal - 60, goal + 180)


def test_a_quiet_drifting_night_reports_nothing():
    """Three hours of slow drift and noise is not a moment."""
    for seed in (11, 21, 31):
        quiet = capture([], 3 * 3600, base=78, drift=8, seed=seed)
        assert detect_peaks(quiet) == []


def test_a_small_wobble_is_not_a_moment():
    """+4 bpm for a minute clears a tight spread but not a person's notice."""
    peaks = detect_peaks(capture([(3600, 4, 5, 60, 20)], 7200, noise=1.0))

    assert peaks == []


def test_a_song_does_not_fragment_on_noise():
    """Hysteresis: one dip below the entry threshold must not split a song."""
    peaks = detect_peaks(capture([(3600, 30, 10, 200, 40)], 7200, noise=4.0))

    assert len(found_within(peaks, 3540, 3900)) == 1


def test_two_moments_five_seconds_apart_merge_and_keep_both_bounds():
    peaks = detect_peaks(capture([(3600, 35, 3, 20, 5), (3633, 35, 3, 20, 5)], 7200))

    assert len(peaks) == 1
    assert seconds(peaks[0], "start_time") <= 3605
    assert seconds(peaks[0], "end_time") >= 3650


def test_a_realness_shaped_night_is_fully_reported():
    """Twenty moments of 8-22 s across six hours: the night we already have."""
    random.seed(3)
    events = [
        (
            900 + i * 900 + random.randint(-200, 200),
            random.randint(22, 38),
            3,
            random.randint(8, 22),
            25,
        )
        for i in range(20)
    ]
    peaks = detect_peaks(capture(events, 6 * 3600 + 400, base=78, drift=8, seed=11))

    assert len(peaks) == 20
    for offset, *_ in events:
        assert found_within(peaks, offset - 60, offset + 60), offset


def test_a_match_with_goals_and_a_near_miss_reports_each_once():
    """Three goals, a near-miss and a saved penalty: five moments, no extras."""
    events = [
        (12 * 60, 45, 8, 45, 110),
        (39 * 60, 25, 6, 20, 45),
        (72 * 60, 48, 8, 50, 120),
        (88 * 60, 35, 5, 30, 70),
        (110 * 60, 52, 8, 60, 140),
    ]
    peaks = detect_peaks(capture(events, 135 * 60))

    assert len(peaks) == 5
    for offset, _amp, rise, plateau, decay in events:
        assert found_within(peaks, offset - 60, offset + rise + plateau + decay), offset


def test_the_song_outranks_the_spike():
    """Magnitude is z × duration: the moment that lasted comes first."""
    peaks = detect_peaks(
        capture([(1800, 32, 12, 200, 30), (5400, 38, 4, 15, 30)], 7200)
    )

    assert len(peaks) == 2
    assert 1800 <= seconds(peaks[0]) <= 2100


def test_too_little_data_is_no_data():
    assert detect_peaks(capture([], 9)) == []


def test_a_six_hour_night_is_analysed_in_under_two_seconds():
    """The rolling median must not turn a night into a wait."""
    import time

    night = capture([], 6 * 3600, base=78, drift=8, seed=11)
    started = time.perf_counter()
    detect_peaks(night)

    assert time.perf_counter() - started < 2.0
