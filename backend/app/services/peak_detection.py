"""Heart rate peak detection.

Finds the moments of a night: the stretches where the heart ran above what it
had been doing for the last twenty minutes. Implements the algorithm specified
in CLAUDE.md, which has to change together with this file:

1.  Smooth: 5-second moving average on BPM values
2.  Baseline: 1200-second centred rolling **median**
3.  Spread: interquartile range of the same window, scaled to a standard
    deviation (IQR / 1.349)
4.  Z-score: (smoothed_bpm - baseline) / spread, with the guards described
    at the point of use
5.  Regions with hysteresis: a region opens where z > 2.0 and stays open
    while z > 1.0
6.  Filter: regions shorter than 5 seconds are discarded
7.  Extract: peak_bpm = max(region), peak_time = timestamp of that max
8.  Merge: regions separated by 30 seconds or less become one
9.  Rank: by magnitude (z-score × duration_seconds), keep the top 20

Why the median, and why twenty minutes — recorded 2026-09-17. The previous
version used a 300 s rolling *mean*. A mean includes the event it is meant to
be the reference for, so any elevation approaching half the window raised its
own baseline and disappeared. That widened the window from 60 s to 300 s once
already, for a 13-second spike; a goal celebration lasts two to three minutes,
and a favourite song sung from start to finish lasts four, and both were
invisible. A median does not move until the elevation fills half the window,
so the window can be wide enough for a song without a spike being lost to it,
and a slowly drifting quiet hour still reports nothing. Verified by
simulation in `scripts/simulate_moment_detection.py`; the guarantees are in
`tests/test_peak_detection.py`.

The algorithm is time-based, never index-based, so it reads a strap at 1 Hz
and a watch at one reading per minute with the same code — what changes is
what each can resolve, and that is the watch's limit, not this file's.
"""

from bisect import bisect_left, insort
from datetime import datetime


def detect_peaks(
    hr_data: list[dict],
    timeline: list[dict] | None = None,
    z_threshold: float = 2.0,
    z_exit: float = 1.0,
    min_rise_bpm: float = 10.0,
    min_peak_duration_sec: int = 5,
    merge_window_sec: int = 30,
    max_peaks: int = 20,
    baseline_window_sec: int = 1200,
    smooth_window_sec: int = 5,
) -> list[dict]:
    """Detect heart rate peaks from a time series of BPM data.

    Args:
        hr_data: List of {"time": datetime, "bpm": int} sorted by time
        timeline: Unused here; matching happens in event_correlator
        z_threshold: Z-score that opens a region (default 2.0)
        z_exit: Z-score below which an open region closes (default 1.0)
        min_rise_bpm: A region needs at least this many bpm above the
            baseline to open, and half of it to stay open (default 10)
        min_peak_duration_sec: Regions shorter than this are noise (default 5 s)
        merge_window_sec: Regions this close become one (default 30 s)
        max_peaks: Maximum number of peaks to return (default 20)
        baseline_window_sec: Reference window for the median (default 1200 s)
        smooth_window_sec: Window for smoothing (default 5 s)

    Returns:
        List of peak dicts sorted by magnitude (descending):
        [{"timestamp", "bpm", "duration_seconds", "magnitude", "z_score",
          "start_time", "end_time"}]
        start_time and end_time bound the whole elevated region; timestamp is
        the second inside it where the heart was highest.
    """
    if len(hr_data) < 10:
        return []

    times = [d["time"] for d in hr_data]
    secs = [t.timestamp() for t in times]
    bpms = [float(d["bpm"]) for d in hr_data]
    n = len(bpms)

    # Step 1: Smooth
    smoothed = _sliding_window_mean(secs, bpms, smooth_window_sec)

    # Steps 2 & 3: Baseline and spread, robust to the event they surround
    baselines, spreads = _sliding_window_median_and_spread(
        secs, smoothed, baseline_window_sec
    )

    # Step 4: Elevation score
    z_scores = []
    for i in range(n):
        deviation = smoothed[i] - baselines[i]
        if spreads[i] > 1.0:
            z = deviation / spreads[i]
        else:
            # A very steady stretch would otherwise divide by near-zero and
            # manufacture huge z-scores.
            z = deviation / 10.0 if deviation > 0 else 0.0
        if deviation > 30:
            # An absolute rise that large is always significant.
            z = max(z, deviation / 15.0)
        # A robust spread on a quiet, slowly drifting hour is only a couple of
        # bpm, so a z of 2 could be a 4 bpm wobble. A moment is a rise a
        # person would feel: it must clear min_rise_bpm to open a region and
        # half of that to keep one open.
        if deviation < min_rise_bpm / 2.0:
            z = 0.0
        elif deviation < min_rise_bpm:
            z = min(z, z_exit)
        z_scores.append(z)

    # Step 5: Regions, with hysteresis so one noisy dip does not split a song
    regions = _group_regions(secs, z_scores, z_threshold, z_exit)

    # Step 6: Filter
    regions = [r for r in regions if r["duration_seconds"] >= min_peak_duration_sec]

    # Step 7: Extract
    peaks = []
    for region in regions:
        start_idx, end_idx = region["start_idx"], region["end_idx"]
        region_bpms = bpms[start_idx : end_idx + 1]
        abs_idx = start_idx + region_bpms.index(max(region_bpms))
        peak_z = max(z_scores[start_idx : end_idx + 1])
        peaks.append(
            {
                "timestamp": times[abs_idx],
                "bpm": int(bpms[abs_idx]),
                "duration_seconds": region["duration_seconds"],
                "z_score": peak_z,
                "magnitude": peak_z * region["duration_seconds"],
                "start_time": times[start_idx],
                "end_time": times[end_idx],
            }
        )

    # Step 8: Merge
    peaks = _merge_peaks(peaks, merge_window_sec)

    # Step 9: Rank
    peaks.sort(key=lambda p: p["magnitude"], reverse=True)
    return peaks[:max_peaks]


def _sliding_window_mean(
    secs: list[float],
    values: list[float],
    window_seconds: int,
) -> list[float]:
    """Time-based moving average over a centred window, O(n)."""
    n = len(values)
    half_window = window_seconds / 2.0
    left = right = 0
    total = 0.0
    result = []
    for i in range(n):
        center = secs[i]
        while right < n and secs[right] <= center + half_window:
            total += values[right]
            right += 1
        while left < right and secs[left] < center - half_window:
            total -= values[left]
            left += 1
        result.append(total / (right - left) if right > left else values[i])
    return result


def _sliding_window_median_and_spread(
    secs: list[float],
    values: list[float],
    window_seconds: int,
) -> tuple[list[float], list[float]]:
    """Rolling median and IQR-based spread over a centred window.

    The window is kept as a sorted list: each sample enters and leaves it
    once, by binary search, so a six-hour night at 1 Hz costs well under a
    second. Both statistics are read straight off the sorted window.
    """
    n = len(values)
    half_window = window_seconds / 2.0
    window_sorted: list[float] = []
    left = right = 0
    medians = []
    spreads = []
    for i in range(n):
        center = secs[i]
        while right < n and secs[right] <= center + half_window:
            insort(window_sorted, values[right])
            right += 1
        while left < right and secs[left] < center - half_window:
            del window_sorted[bisect_left(window_sorted, values[left])]
            left += 1

        count = len(window_sorted)
        if count == 0:
            medians.append(values[i])
            spreads.append(0.0)
            continue
        medians.append(_quantile(window_sorted, 0.5))
        if count >= 4:
            iqr = _quantile(window_sorted, 0.75) - _quantile(window_sorted, 0.25)
            spreads.append(iqr / 1.349)
        else:
            spreads.append(0.0)
    return medians, spreads


def _quantile(sorted_values: list[float], q: float) -> float:
    """Linear-interpolated quantile of an already sorted list."""
    count = len(sorted_values)
    position = q * (count - 1)
    lower = int(position)
    upper = min(lower + 1, count - 1)
    fraction = position - lower
    return (
        sorted_values[lower] + (sorted_values[upper] - sorted_values[lower]) * fraction
    )


def _group_regions(
    secs: list[float],
    z_scores: list[float],
    z_enter: float,
    z_exit: float,
) -> list[dict]:
    """Group elevated points into regions, with hysteresis.

    A region opens where z exceeds z_enter and runs until z falls to z_exit
    or below. Without the second threshold a four-minute song fragments into
    a dozen slivers wherever the noise dips for a second.
    """
    regions = []
    i = 0
    n = len(z_scores)
    while i < n:
        if z_scores[i] > z_enter:
            start_idx = i
            while i < n and z_scores[i] > z_exit:
                i += 1
            end_idx = i - 1
            duration = secs[end_idx] - secs[start_idx]
            regions.append(
                {
                    "start_idx": start_idx,
                    "end_idx": end_idx,
                    "duration_seconds": int(max(duration, 1)),
                }
            )
        else:
            i += 1
    return regions


def _merge_peaks(peaks: list[dict], merge_window_sec: int) -> list[dict]:
    """Merge regions separated by merge_window_sec or less.

    The merged peak keeps the stronger region's numbers and the union of both
    regions' bounds, so the moment still says where it started and ended.
    """
    if not peaks:
        return []

    peaks.sort(key=lambda p: p["timestamp"])

    merged = [peaks[0]]
    for peak in peaks[1:]:
        last = merged[-1]
        gap = (peak["start_time"] - last["end_time"]).total_seconds()
        if gap <= merge_window_sec:
            keep = dict(peak if peak["magnitude"] > last["magnitude"] else last)
            keep["start_time"] = min(peak["start_time"], last["start_time"])
            keep["end_time"] = max(peak["end_time"], last["end_time"])
            merged[-1] = keep
        else:
            merged.append(peak)

    return merged


def region_bounds(peak: dict) -> tuple[datetime, datetime]:
    """Where a peak's elevated region starts and ends.

    Peaks produced here carry both bounds. Peaks read back from storage carry
    only the timestamp and the duration, so the region is reconstructed as
    ending at the peak — a conservative guess that keeps the correlator
    working on either.
    """
    if "start_time" in peak and "end_time" in peak:
        return peak["start_time"], peak["end_time"]
    from datetime import timedelta

    end = peak["timestamp"]
    return end - timedelta(seconds=peak.get("duration_seconds", 0)), end
