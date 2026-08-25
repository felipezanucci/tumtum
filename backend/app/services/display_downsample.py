"""Reduce a heart-rate series to something a phone can actually draw.

A six-hour capture at roughly a reading per second is about 21,600 points. The
curve is at most a thousand pixels wide, so all but a handful of those points
land on a pixel that is already painted — but the phone still pays for every
one of them, in transfer, in parsing, and in a single SVG path long enough to
stall a mid-range device.

The reduction has to preserve extremes rather than sample evenly. Peaks are
detected from the full series and stored separately, so a curve that dropped
the highest reading in a bucket would draw a lower summit than the peak marker
sitting on top of it, and the two would disagree on screen.
"""

from collections.abc import Sequence
from typing import Protocol, TypeVar


class _Point(Protocol):
    time: object
    bpm: int


P = TypeVar("P", bound=_Point)

#: Two points per bucket, so this yields at most this many points overall.
DISPLAY_TARGET_POINTS = 2000


def downsample_for_display(
    points: Sequence[P], target: int = DISPLAY_TARGET_POINTS
) -> list[P]:
    """Thin `points` to about `target`, keeping the min and max of each bucket.

    The series is assumed to be sorted by time, and the result stays sorted.
    The first and last readings are always kept so the curve spans the whole
    session.
    """
    if target < 4:
        raise ValueError("target must leave room for the endpoints and a bucket")
    if len(points) <= target:
        return list(points)

    # Each bucket contributes up to two points, and the endpoints are held back.
    bucket_count = (target - 2) // 2
    inner = points[1:-1]
    kept: list[P] = [points[0]]

    for i in range(bucket_count):
        start = len(inner) * i // bucket_count
        end = len(inner) * (i + 1) // bucket_count
        bucket = inner[start:end]
        if not bucket:
            continue
        # Positions, not values: searching for the point again would compare
        # with ==, which a model class is free to define however it likes.
        low_at = min(range(len(bucket)), key=lambda j: bucket[j].bpm)
        high_at = max(range(len(bucket)), key=lambda j: bucket[j].bpm)
        if low_at == high_at:
            kept.append(bucket[low_at])
            continue
        # Emit in the order they were recorded, so the line never doubles back.
        for j in sorted((low_at, high_at)):
            kept.append(bucket[j])

    kept.append(points[-1])
    return kept
