#!/usr/bin/env python3
"""Can a football moment be detected — and can a watch see it?

Two questions the pilot needs answered before a match is chosen, and neither
can be answered from a capture we own: every real capture we have is a
concert, where a moment lasts 8-22 seconds. A goal is a different shape. The
crowd roars, people jump, hug, scream, and the heart stays up for **minutes**,
not seconds.

So this simulates a match at 1 Hz, decimates it to the cadences actually
measured on Health Connect (one reading per ~32 s inside a workout, one per
60 s in background — decision log, 2026-08-29/30), and runs the real
`detect_peaks()` over each.

**This is a simulation, not a measurement.** The response shape is assumed,
not observed. What it can do is predict a failure that a real match would
otherwise discover the expensive way, and it predicts two:

1. The 300 s baseline window suppresses any elevation longer than about half
   of it. A long celebration sits inside its own reference window and raises
   the mean it is measured against — the very contamination the 300 s window
   was widened to prevent, reappearing at the timescale of a goal.
2. A watch sampling once per 32 s can resolve a goal (it cannot resolve a
   13-second concert moment), but only once the baseline is wide enough. At
   one reading per minute almost nothing survives.

Run: python3 scripts/simulate_match_detection.py
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
    print("\nDoes widening the baseline cost anything on a concert-shaped night?")
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


if __name__ == "__main__":
    how_long_before_it_is_lost()
    match_against_baseline_and_cadence()
    a_wider_baseline_costs_nothing_at_a_concert()
