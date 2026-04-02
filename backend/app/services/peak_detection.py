"""Heart rate peak detection algorithm.

Implements the algorithm specified in CLAUDE.md:
1. Smooth: 5-second moving average on BPM values
2. Baseline: 60-second centered rolling mean
3. Std dev: 60-second centered rolling standard deviation
4. Z-score: (smoothed_bpm - baseline) / std for each point
5. Threshold: mark points where z-score > 2.0 as "elevated"
6. Group: consecutive elevated points → "peak region"
7. Filter: peak regions < 5 seconds are discarded (noise)
8. Extract: peak_bpm = max(region), peak_time = timestamp of max
9. Merge: peaks within 30 seconds of each other → keep highest
10. Rank: by magnitude (z-score × duration_seconds)
"""

from datetime import datetime, timedelta
from statistics import mean, stdev


def detect_peaks(
    hr_data: list[dict],
    timeline: list[dict] | None = None,
    z_threshold: float = 2.0,
    min_peak_duration_sec: int = 5,
    merge_window_sec: int = 30,
    max_peaks: int = 20,
) -> list[dict]:
    """Detect heart rate peaks from a time series of BPM data.

    Args:
        hr_data: List of {"time": datetime, "bpm": int} sorted by time
        timeline: Optional event timeline for future correlation
        z_threshold: Z-score threshold for elevation (default 2.0)
        min_peak_duration_sec: Minimum peak duration to keep (default 5s)
        merge_window_sec: Window for merging nearby peaks (default 30s)
        max_peaks: Maximum number of peaks to return (default 20)

    Returns:
        List of peak dicts sorted by magnitude (descending):
        [{"timestamp", "bpm", "duration_seconds", "magnitude", "z_score"}]
    """
    if len(hr_data) < 10:
        return []

    times = [d["time"] for d in hr_data]
    bpms = [float(d["bpm"]) for d in hr_data]

    # Step 1: Smooth — 5-second moving average
    smoothed = _moving_average(times, bpms, window_seconds=5)

    # Step 2 & 3: Baseline and std dev — 60-second centered rolling
    baselines = _rolling_stat(times, smoothed, window_seconds=60, stat="mean")
    std_devs = _rolling_stat(times, smoothed, window_seconds=60, stat="stdev")

    # Step 4: Z-score
    z_scores = []
    for i in range(len(smoothed)):
        if std_devs[i] > 0:
            z_scores.append((smoothed[i] - baselines[i]) / std_devs[i])
        else:
            z_scores.append(0.0)

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


def _moving_average(
    times: list[datetime],
    values: list[float],
    window_seconds: int,
) -> list[float]:
    """Compute a time-based moving average."""
    result = []
    half_window = timedelta(seconds=window_seconds / 2)

    for i, t in enumerate(times):
        window_vals = []
        for j, tj in enumerate(times):
            if abs((tj - t).total_seconds()) <= half_window.total_seconds():
                window_vals.append(values[j])
        result.append(mean(window_vals) if window_vals else values[i])

    return result


def _rolling_stat(
    times: list[datetime],
    values: list[float],
    window_seconds: int,
    stat: str,
) -> list[float]:
    """Compute a time-based rolling statistic (mean or stdev)."""
    result = []
    half_window = timedelta(seconds=window_seconds / 2)

    for i, t in enumerate(times):
        window_vals = []
        for j, tj in enumerate(times):
            if abs((tj - t).total_seconds()) <= half_window.total_seconds():
                window_vals.append(values[j])

        if stat == "mean":
            result.append(mean(window_vals) if window_vals else values[i])
        elif stat == "stdev":
            result.append(stdev(window_vals) if len(window_vals) >= 2 else 0.0)
        else:
            result.append(0.0)

    return result


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

    # Sort by timestamp
    peaks.sort(key=lambda p: p["timestamp"])

    merged = [peaks[0]]
    for peak in peaks[1:]:
        last = merged[-1]
        delta = abs((peak["timestamp"] - last["timestamp"]).total_seconds())
        if delta <= merge_window_sec:
            # Keep the one with higher magnitude
            if peak["magnitude"] > last["magnitude"]:
                merged[-1] = peak
        else:
            merged.append(peak)

    return merged
