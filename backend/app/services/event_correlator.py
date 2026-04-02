"""Event correlator service.

Matches detected HR peaks to event timeline entries.
Each peak is matched to the nearest timeline entry within a ±60 second window.
"""

from datetime import datetime


def correlate_peaks_to_timeline(
    peaks: list[dict],
    timeline: list[dict],
    max_window_sec: int = 60,
) -> list[dict]:
    """Match peaks to the nearest event timeline entry.

    Args:
        peaks: List of detected peaks from peak_detection.detect_peaks()
            Each has: {"timestamp", "bpm", "duration_seconds", "magnitude"}
        timeline: List of timeline entries
            Each has: {"time": datetime, "label": str, "id": uuid}
        max_window_sec: Maximum time distance for matching (default ±60s)

    Returns:
        Updated peaks list with "timeline_entry_id" and "matched_label" added.
    """
    if not timeline:
        return peaks

    for peak in peaks:
        peak_time = peak["timestamp"]
        best_match = None
        best_delta = float("inf")

        for entry in timeline:
            entry_time = entry["time"]
            delta = abs((peak_time - entry_time).total_seconds())

            if delta <= max_window_sec and delta < best_delta:
                best_delta = delta
                best_match = entry

        if best_match:
            peak["timeline_entry_id"] = best_match["id"]
            peak["matched_label"] = best_match["label"]
        else:
            peak["timeline_entry_id"] = None
            peak["matched_label"] = None

    return peaks
