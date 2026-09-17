#!/usr/bin/env python3
"""How long may an emotion last before the detector stops seeing it?

Every real capture we own is a concert, and every moment the detector found
at Realness lasted 8-22 seconds. This script asks whether that number is a
property of the nights or of the instrument — by simulating emotions of known
length and shape, running the real `detect_peaks()` over them, and checking
what comes back.

Three scenarios:

1. **A football match** — three goals, a near-miss, a saved penalty — at 1 Hz
   and decimated to the cadences measured on Health Connect (one reading per
   ~32 s inside a workout, one per 60 s in background; decision log,
   2026-08-29/30). A goal is roared, jumped and hugged, and the heart stays up
   for minutes.
2. **A show where the favourite song is the moment** — euphoria sustained for
   the whole 3'50" of three songs, plus three 15-second spikes (the drop, the
   guest, the key change) inside other songs. Felipe's question, 2026-09-17:
   a song lasts 3-5 minutes, so is it not the same case as the goal?
3. **The rule** — a single elevation of each length, and the narrowest
   baseline window that still sees it — plus false positives on a quiet
   capture as the window grows.

**This is a simulation, not a measurement.** The response shapes are assumed.
What it can do is predict a failure that a real event would otherwise
discover the expensive way, and it predicts this one: the 300 s baseline
window is contaminated by any elevation approaching half its length. A
13-second spike is safe. A goal celebration is not. A favourite song is
not — and the detector then reports only the short spikes, which is exactly
what the Realness night looked like.

Run: python3 scripts/simulate_moment_detection.py
"""

import math
import random
import sys
from datetime import UTC, datetime, timedelta
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "backend"))

from app.services.peak_detection import detect_peaks  # noqa: E402

START = datetime(2026, 10, 10, 21, 30, tzinfo=UTC)

# (offset_seconds, label, amplitude_bpm, rise_s, plateau_s, decay_s)
MATCH_EVENTS = [
    (12 * 60, "Gol 1", 45, 8, 45, 110),
    (39 * 60, "Quase-gol", 25, 6, 20, 45),
    (72 * 60, "Gol 2", 48, 8, 50, 120),
    (88 * 60, "Pênalti defendido", 35, 5, 30, 70),
    (110 * 60, "Gol 3", 52, 8, 60, 140),
]


def build(events, duration_sec, seed=7, base=88, drift=6, drift_period=900.0):
    """A plausible capture: a slowly drifting baseline plus the events."""
    random.seed(seed)

    def excitement(t):
        total = 0.0
        for offset, _label, amp, rise, plateau, decay in events:
            d = t - offset
            if d < 0:
                continue
            if d < rise:
                total += amp * (d / rise)
            elif d < rise + plateau:
                total += amp
            elif d < rise + plateau + decay:
                total += amp * (1 - (d - rise - plateau) / decay)
        return total

    return [
        {
            "time": START + timedelta(seconds=t),
            "bpm": max(45, min(200, base + drift * math.sin(t / drift_period)
                               + excitement(t) + random.gauss(0, 2.5))),
        }
        for t in range(duration_sec)
    ]


def hits(events, data, baseline_window_sec):
    """Which planted events land within the correlator's ±60 s window."""
    peaks = detect_peaks(data, baseline_window_sec=baseline_window_sec)
    found = [
        label
        for offset, label, *_ in events
        if any(abs((p["timestamp"] - START).total_seconds() - offset) <= 60 for p in peaks)
    ]
    return found, len(peaks)


def how_long_before_it_is_lost():
    print("How long may an elevation last before the detector loses it?")
    print("(one event, +45 bpm, strap at 1 Hz, default 300 s baseline)\n")
    for plateau, decay in [(5, 10), (10, 20), (20, 40), (30, 60),
                           (45, 90), (60, 120), (90, 180), (120, 240)]:
        events = [(60 * 60, "evento", 45, 8, plateau, decay)]
        found, _ = hits(events, build(events, 135 * 60), 300)
        print(f"   {8 + plateau + decay:>4} s of elevation -> "
              f"{'detected' if found else 'LOST'}")


def match_against_baseline_and_cadence():
    print("\n\nA five-event match, by baseline window and sampling cadence:\n")
    series = build(MATCH_EVENTS, 135 * 60)
    for baseline in (300, 600, 900, 1200):
        for step, name in ((1, "strap, 1 Hz"), (32, "watch, 32 s"), (60, "watch, 60 s")):
            found, total = hits(MATCH_EVENTS, series[::step], baseline)
            print(f"   baseline {baseline:>4} s  {name:<13} "
                  f"{len(found)}/5 events  ({total} peaks)  {found}")
        print()


def a_wider_baseline_costs_nothing_at_a_concert():
    print("\n\nDoes widening the baseline cost anything on a night of SHORT moments?")
    print("(20 planted moments of 8-22 s, six hours, strap at 1 Hz)\n")
    random.seed(3)
    events = [
        (900 + i * 900 + random.randint(-200, 200), f"m{i + 1}",
         random.randint(22, 38), 3, random.randint(8, 22), 25)
        for i in range(20)
    ]
    night = build(events, 6 * 3600 + 400, seed=11, base=78, drift=8, drift_period=1800.0)
    quiet = build([], 3 * 3600, seed=11, base=78, drift=8, drift_period=1800.0)
    for baseline in (300, 600, 900, 1200):
        found, total = hits(events, night, baseline)
        false_positives = len(detect_peaks(quiet, baseline_window_sec=baseline))
        print(f"   baseline {baseline:>4} s -> {len(found):>2}/20 moments found, "
              f"{total} peaks reported, {false_positives} peaks on a quiet 3 h")


SONG, GAP, N_SONGS = 230, 30, 26        # a 3'50" song, the gap, ~1h52 of show
FAVOURITES = [5, 14, 22]                 # euphoria for the whole song
SPIKES = [2, 9, 18]                      # a 15 s spike inside an ordinary song


def show_events():
    events = []
    for i in range(N_SONGS):
        start = i * (SONG + GAP)
        if i in FAVOURITES:
            events.append((start, f"song {i + 1} (favourite)", 32, 12, SONG - 40, 60))
        elif i in SPIKES:
            events.append((start + 90, f"song {i + 1} (spike)", 38, 4, 15, 30))
    return events


def a_show_where_the_song_is_the_moment():
    print("\n\nA show where the favourite song is the moment:")
    print("three songs of sustained euphoria (~3'50\"), three 15 s spikes, strap at 1 Hz\n")
    events = show_events()
    series = build(events, N_SONGS * (SONG + GAP), seed=5, base=95, drift=5, drift_period=1200.0)
    for baseline in (300, 600, 900, 1200, 1800, 2700):
        peaks = detect_peaks(series, baseline_window_sec=baseline)
        secs = [(p["timestamp"] - START).total_seconds() for p in peaks]
        favourites = sum(
            1 for off, label, *_ in events
            if "favourite" in label and any(off - 60 <= t <= off + SONG for t in secs)
        )
        spikes = sum(
            1 for off, label, *_ in events
            if "spike" in label and any(abs(t - off) <= 60 for t in secs)
        )
        print(f"   baseline {baseline:>4} s -> favourite songs {favourites}/3   "
              f"spikes {spikes}/3   ({len(peaks)} moments reported)")


def the_rule():
    print("\n\nThe narrowest baseline that still sees one elevation (+32 bpm, 1 Hz):\n")
    print(f"   {'elevation':>10} {'min baseline':>14} {'ratio':>7}")
    for plateau in (5, 20, 50, 80, 140, 200, 260, 360):
        events = [(3600, "event", 32, 10, plateau, 30)]
        length = 10 + plateau + 30
        data = build(events, 7200, seed=5, base=95, drift=5, drift_period=1200.0)
        best = None
        for window in (120, 180, 240, 300, 420, 600, 900, 1200, 1500, 1800, 2400, 3000, 3600):
            peaks = detect_peaks(data, baseline_window_sec=window)
            if any(3600 - 60 <= (p["timestamp"] - START).total_seconds() <= 3600 + length
                   for p in peaks):
                best = window
                break
        shown = f"{best} s" if best else "never"
        ratio = f"{best / length:.1f}x" if best else ""
        print(f"   {length:>8} s {shown:>14} {ratio:>7}")

    print("\n   False positives on a quiet 2 h as the window grows:")
    quiet = build([], 7200, seed=9, base=95, drift=5, drift_period=1200.0)
    for window in (300, 900, 1200, 1800, 2700, 3600):
        print(f"   baseline {window:>4} s -> "
              f"{len(detect_peaks(quiet, baseline_window_sec=window))} peaks")


if __name__ == "__main__":
    how_long_before_it_is_lost()
    match_against_baseline_and_cadence()
    a_wider_baseline_costs_nothing_at_a_concert()
    a_show_where_the_song_is_the_moment()
    the_rule()
