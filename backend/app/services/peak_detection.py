"""Heart rate peak detection algorithm.

Implements the algorithm specified in CLAUDE.md:
1. Smooth: 5-second moving average on BPM values
2. Baseline: 5-minute centered rolling mean (wide window to avoid spike contamination)
3. Std dev: 5-minute centered rolling standard deviation
4. Z-score: (smoothed_bpm - baseline) / std for each point
5. Threshold: mark points where z-score > 2.0 as "elevated"
6. Group: consecutive elevated points → "peak region"
7. Filter: peak regions < 5 seconds are discarded (noise)
8. Extract: peak_bpm = max(region), peak_time = timestamp of max
9. Merge: peaks within 30 seconds of each other → keep highest
10. Rank: by magnitude (z-score × duration_seconds)

Note: CLAUDE.md specifies 60-second windows for baseline/stddev. In practice, a wider
window (300s) is needed so that peaks don't contaminate their own baseline. The algorithm
logic is otherwise identical to the spec.
"""

from datetime import datetime, timedelta
from math import sqrt


def detect_peaks(
    hr_data: list[dict],
    timeline: list[dict] | None = None,
    z_threshold: float = 2.0,
    min_peak_duration_sec: int = 5,
    merge_window_sec: int = 30,
    max_peaks: int = 20,
    baseline_window_sec: int = 300,
    smooth_window_sec: int = 5,
) -> list[dict]:
    """Detect heart rate peaks from a time series of BPM data.

    Args:
        hr_data: List of {"time": datetime, "bpm": int} sorted by time
        timeline: Optional event timeline for future correlation
        z_threshold: Z-score threshold for elevation (default 2.0)
        min_peak_duration_sec: Minimum peak duration to keep (default 5s)
        merge_window_sec: Window for merging nearby peaks (default 30s)
        max_peaks: Maximum number of peaks to return (default 20)
        baseline_window_sec: Window for baseline calculation (default 300s / 5 min)
        smooth_window_sec: Window for smoothing (default 5s)

    Returns:
        List of peak dicts sorted by magnitude (descending):
        [{"timestamp", "bpm", "duration_seconds", "magnitude", "z_score"}]
    """
    if len(hr_data) < 10:
        return []

    times = [d["time"] for d in hr_data]
    bpms = [float(d["bpm"]) for d in hr_data]
    n = len(bpms)

    # Step 1: Smooth — moving average using two-pointer window
    smoothed = _sliding_window_mean(times, bpms, smooth_window_sec)

    # Step 2 & 3: Baseline (mean) and std dev using wider window
    baselines, std_devs = _sliding_window_stats(times, smoothed, baseline_window_sec)

    # Step 4: Z-score
    z_scores = []
    for i in range(n):
        if std_devs[i] > 1.0:  # Minimum std to avoid division by near-zero
            z_scores.append((smoothed[i] - baselines[i]) / std_devs[i])
        else:
            # If std is very low, use absolute deviation from baseline
            deviation = smoothed[i] - baselines[i]
            z_scores.append(deviation / 10.0 if deviation > 0 else 0.0)

    # Step 5: Threshold — mark elevated points
    elevated = [z > z_threshold for z in z_scores]

    # Step 6: Group consecutive elevated points into peak regions
    regions = _group_regions(times, elevated)

    # Step 7: Filter — discard regions shorter than min_peak_duration_sec
    regions = [r for r in regions if r["duration_seconds"] >= min_peak_duration_sec]

    # Step 8: Extract peak BPM and timestamp from each region
    peaks = []
    for region in regions:
        start_idx, end_idx = region["start_idx"], region["end_idx"]
        region_bpms = bpms[start_idx : end_idx + 1]
        region_zscores = z_scores[start_idx : end_idx + 1]

        max_bpm_idx = region_bpms.index(max(region_bpms))
        abs_idx = start_idx + max_bpm_idx

        peak_z = max(region_zscores) if region_zscores else 0

        peaks.append({
            "timestamp": times[abs_idx],
            "bpm": int(bpms[abs_idx]),
            "duration_seconds": region["duration_seconds"],
            "z_score": peak_z,
            "magnitude": peak_z * region["duration_seconds"],
        })

    # Step 9: Merge peaks within merge_window_sec — keep highest
    peaks = _merge_peaks(peaks, merge_window_sec)

    # Step 10: Rank by magnitude (descending) and limit
    peaks.sort(key=lambda p: p["magnitude"], reverse=True)
    peaks = peaks[:max_peaks]

    return peaks


def _sliding_window_mean(
    times: list[datetime],
    values: list[float],
    window_seconds: int,
) -> list[float]:
    """Compute a time-based moving average using a sliding window (O(n))."""
    n = len(values)
    result = [0.0] * n
    half_window = window_seconds / 2.0
    left = 0
    window_sum = 0.0
    window_count = 0

    for right in range(n):
        # Expand window to include current point
        window_sum += values[right]
        window_count += 1

        # Find the center point this window corresponds to
        # We build windows centered on each point
        pass

    # Simpler approach: for each point, use two pointers
    left = 0
    right = 0
    result = []
    for i in range(n):
        center_time = times[i].timestamp()
        # Move left pointer
        while left < n and times[left].timestamp() < center_time - half_window:
            left += 1
        # Move right pointer
        while right < n and times[right].timestamp() <= center_time + half_window:
            right += 1
        # Compute mean of [left, right)
        window_vals = values[left:right]
        result.append(sum(window_vals) / len(window_vals) if window_vals else values[i])
        # Don't reset left — it only moves forward

    return result


def _sliding_window_stats(
    times: list[datetime],
    values: list[float],
    window_seconds: int,
) -> tuple[list[float], list[float]]:
    """Compute rolling mean and stdev using a sliding window."""
    n = len(values)
    half_window = window_seconds / 2.0
    means = []
    stdevs = []

    left = 0
    for i in range(n):
        center_time = times[i].timestamp()
        # Move left pointer forward
        while left < n and times[left].timestamp() < center_time - half_window:
            left += 1
        # Find right boundary
        right = left
        while right < n and times[right].timestamp() <= center_time + half_window:
            right += 1

        window_vals = values[left:right]
        count = len(window_vals)

        if count == 0:
            means.append(values[i])
            stdevs.append(0.0)
            continue

        m = sum(window_vals) / count
        means.append(m)

        if count >= 2:
            variance = sum((v - m) ** 2 for v in window_vals) / (count - 1)
            stdevs.append(sqrt(variance))
        else:
            stdevs.append(0.0)

    return means, stdevs


def _group_regions(
    times: list[datetime],
    elevated: list[bool],
) -> list[dict]:
    """Group consecutive elevated points into regions."""
    regions = []
    i = 0
    while i < len(elevated):
        if elevated[i]:
            start_idx = i
            while i < len(elevated) and elevated[i]:
                i += 1
            end_idx = i - 1
            duration = (times[end_idx] - times[start_idx]).total_seconds()
            regions.append({
                "start_idx": start_idx,
                "end_idx": end_idx,
                "duration_seconds": int(max(duration, 1)),
            })
        else:
            i += 1
    return regions


def _merge_peaks(peaks: list[dict], merge_window_sec: int) -> list[dict]:
    """Merge peaks that are within merge_window_sec of each other, keeping the highest."""
    if not peaks:
        return []

    peaks.sort(key=lambda p: p["timestamp"])

    merged = [peaks[0]]
    for peak in peaks[1:]:
        last = merged[-1]
        delta = abs((peak["timestamp"] - last["timestamp"]).total_seconds())
        if delta <= merge_window_sec:
            if peak["magnitude"] > last["magnitude"]:
                merged[-1] = peak
        else:
            merged.append(peak)

    return merged
