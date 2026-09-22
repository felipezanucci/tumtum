"""The guess list: what an unnamed moment *might* have been.

Setlist.fm publishes the order of the songs and never their times, so a
timeline built from it is an estimate — four minutes a song from the
advertised start — and the estimate drifts: talk between songs, a stretched
intro, a solo that ran long. By the third song the guess is outside the
correlator's window and by the tenth it would name the wrong song, which is
worse than no name, because a card that lies costs trust.

So an estimated entry never *asserts* a name. It **offers** one: the two or
three songs whose window, widened by how far the estimate may have drifted,
covers the moment. The person taps the right one (§5.6 of the 19/09 research
— recognition over recall, the same principle behind "Toca pra dizer o que
tava rolando"). The same rule covers a football timeline built from the
schedule with no operator anchor: a time the app derived offers, a time it
measured asserts.

Which entries are tentative is decided by their metadata: ``estimated: true``
(setlist) or ``anchored: false`` (football). Everything else is exact and goes
to the correlator as before.
"""

from datetime import datetime, timedelta

from app.services.peak_detection import region_bounds

# What a song is assumed to last when the entry does not say.
DEFAULT_SONG_SEC = 240
# What any other tentative entry is assumed to last — a goal's celebration.
DEFAULT_ENTRY_SEC = 120
# How far an estimate is assumed to have drifted when the entry does not say.
DEFAULT_UNCERTAINTY_SEC = 60
# Drift runs one way — a show runs later than its estimate, a kick-off later
# than its schedule, never earlier — so a window opens only this much before
# the estimate (a song shorter than assumed) and the whole drift after it.
EARLY_TOLERANCE_SEC = 60
MAX_CANDIDATES = 3


def is_tentative(metadata: dict | None) -> bool:
    """An entry whose time was derived rather than measured."""
    if not metadata:
        return False
    return bool(metadata.get("estimated")) or metadata.get("anchored") is False


def candidate_labels(
    peak: dict,
    tentative: list[dict],
    max_candidates: int = MAX_CANDIDATES,
) -> list[str]:
    """The labels whose widened window covers the peak, nearest first.

    Args:
        peak: A detected peak — ``timestamp``, and ``start_time`` when fresh
            from the detector (the region's start is what an entry causes).
        tentative: Entries ``{"time", "label", "entry_type", "metadata"}``
            whose times are estimates.

    Returns:
        At most ``max_candidates`` labels. Empty when nothing plausibly
        covers the moment — an empty list is "we have no idea", and the
        screen says that rather than offering a wrong name.
    """
    if not tentative:
        return []
    region_start, _ = region_bounds(peak)
    at: datetime = region_start

    scored: list[tuple[float, datetime, str]] = []
    for entry in tentative:
        meta = entry.get("metadata") or {}
        start: datetime = entry["time"]
        default = (
            DEFAULT_SONG_SEC
            if entry.get("entry_type") == "song_start"
            else DEFAULT_ENTRY_SEC
        )
        duration = timedelta(seconds=int(meta.get("duration_sec") or default))
        drift = timedelta(
            seconds=int(meta.get("uncertainty_sec") or DEFAULT_UNCERTAINTY_SEC)
        )
        end = start + duration
        if start - timedelta(seconds=EARLY_TOLERANCE_SEC) <= at <= end + drift:
            # Distance from the moment to the unwidened window; zero inside it.
            if at < start:
                score = (start - at).total_seconds()
            elif at > end:
                score = (at - end).total_seconds()
            else:
                score = 0.0
            scored.append((score, start, entry["label"]))

    scored.sort()
    seen: set[str] = set()
    labels: list[str] = []
    for _, _, label in scored:
        if label in seen:
            continue
        seen.add(label)
        labels.append(label)
        if len(labels) == max_candidates:
            break
    return labels
