"""The night, reduced to something a card can draw honestly.

Card 01 used to carry no chart at all, and `cards.py` said so in a comment
explaining why the readings were deliberately not loaded. That decision is
reversed here, for a reason worth writing down: a card with only a number on
it gives a stranger no reason to believe anything happened. The curve is the
evidence, and the brand manual permits exactly this — the person's own data,
as product information, never as decoration.

The resampling is onto a **uniform time grid**, not onto the readings. That
distinction is the whole point. Spacing the readings evenly would squeeze a
gap in the capture down to nothing and draw a continuous line across hours
nobody measured — the same lie the quality score was telling until it was
fixed to count slots instead of readings. Here an empty slot stays empty, the
line breaks, and the card shows a night with a hole in it as a night with a
hole in it.
"""

from collections.abc import Iterable, Sequence
from datetime import datetime
from typing import Protocol

#: Enough shape for a card three hundred-odd pixels of curve wide, and small
#: enough to live in the card's metadata as plain JSON. The card is a snapshot:
#: it stores what it drew rather than re-reading a session that may since have
#: been re-analysed.
CURVE_SLOTS = 180


class _Point(Protocol):
    time: datetime
    bpm: int


def resample(
    start: datetime,
    end: datetime,
    points: Iterable[_Point],
    slots: int = CURVE_SLOTS,
) -> list[int | None]:
    """The window as `slots` equal spans, each holding its highest reading.

    Highest rather than mean because the peak marker is drawn from the peak
    table: averaging would put the summit of the curve below the dot sitting
    on top of it, and the card would visibly disagree with itself.

    Returns `None` for a span with no reading in it — a gap to be drawn as a
    gap, not interpolated away.
    """
    span = (end - start).total_seconds()
    if span <= 0 or slots < 2:
        return []

    out: list[int | None] = [None] * slots
    for p in points:
        offset = (p.time - start).total_seconds()
        if not 0 <= offset <= span:
            continue
        i = min(int(offset / span * slots), slots - 1)
        current = out[i]
        if current is None or p.bpm > current:
            out[i] = p.bpm
    return out


def slot_of(
    start: datetime, end: datetime, at: datetime, slots: int = CURVE_SLOTS
) -> int | None:
    """Which slot a moment falls in, so the marker lands where the curve is."""
    span = (end - start).total_seconds()
    if span <= 0:
        return None
    offset = (at - start).total_seconds()
    if not 0 <= offset <= span:
        return None
    return min(int(offset / span * slots), slots - 1)


def segments(curve: Sequence[int | None]) -> list[list[tuple[int, int]]]:
    """Split the curve into runs of consecutive readings.

    Each run is drawn as its own polyline, so the breaks between them stay
    breaks. A lone reading with gaps on both sides becomes a one-point run,
    which the caller draws as a dot rather than dropping.
    """
    runs: list[list[tuple[int, int]]] = []
    current: list[tuple[int, int]] = []
    for i, value in enumerate(curve):
        if value is None:
            if current:
                runs.append(current)
                current = []
        else:
            current.append((i, value))
    if current:
        runs.append(current)
    return runs
