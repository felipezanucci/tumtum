import math
from dataclasses import dataclass

import pytest

from app.services.display_downsample import (
    DISPLAY_TARGET_POINTS,
    downsample_for_display,
)


@dataclass
class Point:
    time: int
    bpm: int


def series(n: int) -> list[Point]:
    """A six-hour-shaped series: slow drift, one reading a second."""
    return [Point(i, 90 + round(30 * math.sin(i / 400))) for i in range(n)]


def test_short_series_is_returned_untouched():
    points = series(500)
    assert downsample_for_display(points) == points


def test_series_at_the_target_is_returned_untouched():
    points = series(DISPLAY_TARGET_POINTS)
    assert downsample_for_display(points) == points


def test_six_hours_comes_back_under_the_target():
    points = series(21_600)
    out = downsample_for_display(points)
    assert len(out) <= DISPLAY_TARGET_POINTS
    assert len(out) > DISPLAY_TARGET_POINTS // 2


def test_the_highest_reading_survives():
    """The curve and the peak marker must agree on how high the night went."""
    points = series(21_600)
    points[12_345].bpm = 187
    out = downsample_for_display(points)
    assert max(p.bpm for p in out) == 187
    assert any(p.time == 12_345 for p in out)


def test_the_lowest_reading_survives():
    points = series(21_600)
    points[9_001].bpm = 41
    out = downsample_for_display(points)
    assert min(p.bpm for p in out) == 41


def test_endpoints_are_kept_so_the_curve_spans_the_session():
    points = series(21_600)
    out = downsample_for_display(points)
    assert out[0] is points[0]
    assert out[-1] is points[-1]


def test_result_stays_in_time_order():
    points = series(21_600)
    # A spike whose fall is steeper than its rise: the bucket's max comes
    # before its min, and emitting them low-first would draw the line backwards.
    for i in range(9_000, 9_050):
        points[i].bpm = 180
    out = downsample_for_display(points)
    assert all(out[i].time <= out[i + 1].time for i in range(len(out) - 1))


def test_a_flat_bucket_contributes_one_point_not_two():
    points = [Point(i, 100) for i in range(21_600)]
    out = downsample_for_display(points)
    # Every bucket has min == max, so each yields a single point.
    assert len(out) <= DISPLAY_TARGET_POINTS // 2 + 2


def test_a_target_too_small_to_be_meaningful_is_rejected():
    with pytest.raises(ValueError):
        downsample_for_display(series(100), target=3)
