"""How much of a night we actually have.

The score shown as "Qualidade" on the night screen answers one question, and
it must answer it honestly: *how much of this session do we really hold, at
the resolution the detector needs?*

The formula this replaces measured **volume** instead of **continuity** — it
divided the reading count by an expected `duration × 12` (one per 5 s) and
capped the ratio at 1.0. The chest strap delivers one reading per *second*,
twelve times that rate, so the ratio saturated: a capture could lose four
readings in five and still report 100%.

That is not theoretical. The Realness capture of 2026-08-29 ran 21:11–03:17
with a **79-minute hole** in it — the strap was connected briefly at home,
then again at the venue — and the app announced **"Qualidade 100%"** while
the curve beside it drew the gap as one long straight line. The chart was
honest and the number was not, which is this project's oldest bug class.

Counting slots fixes both failures with one measure. A hole empties its
slots, so it shows. A sparse source — a watch writing once a minute — fills
one slot in twelve, so it shows too. The result is a fraction with a plain
meaning: *the share of this night we can actually see*.
"""

from collections.abc import Iterable
from datetime import datetime

# The detector smooths over 5 seconds, so a 5-second slot is the finest
# resolution any downstream answer depends on. Keep this in step with
# `SLOT_MILLIS` in the Android app's `Cadence`, which measures the same thing
# on the phone before an upload is ever offered.
SLOT_SECONDS = 5


def coverage(
    start: datetime,
    end: datetime,
    times: Iterable[datetime],
) -> float:
    """Fraction (0..1) of the session's 5-second slots holding a reading.

    Readings outside `[start, end]` are ignored rather than counted: they
    cannot describe a window they fall outside of, and letting them inflate
    the score would be the same lie in a new place.
    """
    span = (end - start).total_seconds()
    if span < 0:
        return 0.0

    total_slots = int(span // SLOT_SECONDS) + 1
    filled: set[int] = set()
    for at in times:
        offset = (at - start).total_seconds()
        if 0 <= offset <= span:
            filled.add(int(offset // SLOT_SECONDS))

    if not filled:
        return 0.0
    return len(filled) / total_slots


def score(start: datetime, end: datetime, times: Iterable[datetime]) -> int:
    """`coverage` as the 0–100 integer stored on the session."""
    return round(coverage(start, end, times) * 100)
