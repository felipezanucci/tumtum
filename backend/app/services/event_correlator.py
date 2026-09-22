"""Event correlator service.

Gives a detected peak its name: the song that was playing, the goal that had
just gone in.

Until 2026-09-17 a peak matched the nearest timeline entry within ±60 s of the
peak's own timestamp. That is right for a spike and wrong for a moment that
lasts: a favourite song sung for four minutes peaks wherever the heart was
highest, often three minutes after the song began, and the entry that names it
is then outside any window measured from the peak. The rule now is causal —
**the latest entry that happened between the start of the elevated region and
the peak** — with the old nearest-within-±60 s rule as the fallback for a peak
that no entry precedes.

One entry never reaches any of that: a **derived** time. See [is_tentative].
"""

from datetime import timedelta

from app.services.peak_detection import region_bounds


def is_tentative(metadata: dict | None) -> bool:
    """Whether an entry's time was derived rather than measured.

    **A measured time asserts a name; a derived one names nothing.** A match
    built from the schedule with no anchor puts a goal up to ten minutes from
    where it happened, and an entry that far out would name the wrong moment
    — which is worse than leaving the moment unnamed, because a card that
    lies costs trust that an unlabelled one does not.

    Derived entries used to survive as a guess list the fan picked from. That
    went on 2026-09-22, at Felipe's instruction: *"a gente não pode contar com
    o usuário para ele ter que lembrar que aquele batimento foi de uma música
    determinada."* Either the app knows or it says nothing. So a tentative
    entry is now simply withheld, and the work of making times measured moved
    to where it belongs — the API's periods for a match, the operator's taps
    for a show.
    """
    if not metadata:
        return False
    return bool(metadata.get("estimated")) or metadata.get("anchored") is False


def correlate_peaks_to_timeline(
    peaks: list[dict],
    timeline: list[dict],
    max_window_sec: int = 60,
    after_peak_tolerance_sec: int = 15,
) -> list[dict]:
    """Match peaks to timeline entries.

    Args:
        peaks: Detected peaks from peak_detection.detect_peaks()
            Each has "timestamp", "duration_seconds", and — when fresh from the
            detector — "start_time" and "end_time" for the whole region.
        timeline: Timeline entries, each {"time": datetime, "label": str, "id": uuid}
        max_window_sec: How far before the region an entry may sit and still
            count as its cause, and the radius of the fallback rule (default 60 s)
        after_peak_tolerance_sec: An entry this soon *after* the peak still
            counts, absorbing a clock a few seconds out (default 15 s)

    Returns:
        The same peaks with "timeline_entry_id" and "matched_label" set,
        None where nothing matched.
    """
    if not timeline:
        return peaks

    before = timedelta(seconds=max_window_sec)
    after = timedelta(seconds=after_peak_tolerance_sec)

    for peak in peaks:
        peak_time = peak["timestamp"]
        region_start, _ = region_bounds(peak)

        # The cause: the most recent entry inside the region's reach.
        causes = [
            entry
            for entry in timeline
            if region_start - before <= entry["time"] <= peak_time + after
        ]
        if causes:
            match = max(causes, key=lambda entry: entry["time"])
        else:
            # Nothing preceded it: fall back to the nearest entry around the peak.
            match = None
            best_delta = float("inf")
            for entry in timeline:
                delta = abs((peak_time - entry["time"]).total_seconds())
                if delta <= max_window_sec and delta < best_delta:
                    best_delta = delta
                    match = entry

        peak["timeline_entry_id"] = match["id"] if match else None
        peak["matched_label"] = match["label"] if match else None

    return peaks
