"""Card 04 — *A galera*: the event as a crowd rather than as one heart.

The brand manual asks for "a statistically and privately valid collective
sample", and both halves of that are load-bearing.

**Privately valid** is the one with teeth, and it is why this module exists
instead of a `COUNT(*)` in the route. A collective figure computed over a
small crowd is not collective at all — it is a fact about the people in it.
With two nights uploaded, "64% bateram o próprio pico" says exactly what each
of those two people did, and "o pico da galera foi às 22h41" points at
whoever was there. So nothing is published below [MIN_CROWD], and the screen
is told *why* it has no numbers rather than being handed zeros, because a
zero is a claim about the world (the empty-state rule, 22/09).

**Statistically valid** is thinner and honest about it: this counts nights
that were actually uploaded and analysed, never "people at the event". A
stadium holds sixty thousand and TumTum will have measured eleven of them.
Every label this produces says *da galera do TumTum*, never *da galera*.
"""

from collections import Counter
from dataclasses import dataclass, field
from datetime import datetime, timedelta

# Below this many measured nights, no collective number is published. Four is
# the smallest crowd where one person's reading is not recoverable from the
# aggregate by someone who knows their own: with three, anybody who knows two
# of the values knows the third.
MIN_CROWD = 4

# How wide a bucket is when looking for the minute the crowd shared. A minute
# is too narrow — two people feeling the same goal peak seconds apart — and
# five is wide enough to merge two different songs.
BUCKET = timedelta(minutes=2)

# How many of the crowd must peak inside one bucket before it is called a
# collective moment rather than a coincidence.
COLLECTIVE_SHARE = 0.3


@dataclass(frozen=True)
class CrowdMoment:
    """A minute enough hearts rose together for it to mean something."""

    at: datetime
    people: int
    label: str | None = None


@dataclass(frozen=True)
class Crowd:
    """What can be said about an event without saying it about a person.

    [enough] is false when the crowd is too small to publish. The screen then
    says so — *"ainda somos poucos aqui"* — instead of drawing an empty chart
    that reads as "nobody felt anything".
    """

    measured_nights: int
    enough: bool
    shared_count: int = 0
    moments: list[CrowdMoment] = field(default_factory=list)
    top: CrowdMoment | None = None


def _bucket_start(at: datetime, origin: datetime) -> datetime:
    """The bucket an instant falls in, counted from the first peak."""
    elapsed = at - origin
    steps = int(elapsed // BUCKET)
    return origin + steps * BUCKET


def collective_moments(
    peaks: list[dict],
    measured_nights: int,
    shared_count: int = 0,
    min_crowd: int = MIN_CROWD,
) -> Crowd:
    """Where the crowd rose together, from one peak per person.

    Args:
        peaks: One entry per person, ``{"at": datetime, "label": str | None}``
            — **their single biggest moment**, not every peak they had. A
            person with nine peaks would otherwise outvote three people who
            had one each, and the question being asked is how many *people*
            felt something at once.
        measured_nights: How many nights were uploaded and analysed for the
            event. This, not the number of peaks, decides whether anything
            may be published.
        shared_count: How many of them chose to post a moment to the feed.
        min_crowd: Overridable for tests; never lowered in production.

    Returns:
        A [Crowd]. When there are fewer than ``min_crowd`` measured nights it
        carries ``enough=False`` and nothing else — the caller must not fill
        the gap with zeros.
    """
    if measured_nights < min_crowd or not peaks:
        return Crowd(measured_nights=measured_nights, enough=False)

    origin = min(p["at"] for p in peaks)
    buckets: Counter = Counter()
    labels: dict[datetime, Counter] = {}
    for peak in peaks:
        start = _bucket_start(peak["at"], origin)
        buckets[start] += 1
        label = peak.get("label")
        if label:
            labels.setdefault(start, Counter())[label] += 1

    floor = max(2, round(len(peaks) * COLLECTIVE_SHARE))
    moments = [
        CrowdMoment(
            at=start,
            people=people,
            # The name the most people's own timeline gave that minute. A
            # moment nothing named stays unnamed — the app either knows or
            # says nothing (product rule, 22/09).
            label=(labels.get(start) or Counter()).most_common(1)[0][0]
            if labels.get(start)
            else None,
        )
        for start, people in sorted(buckets.items())
        if people >= floor
    ]

    return Crowd(
        measured_nights=measured_nights,
        enough=True,
        shared_count=shared_count,
        moments=moments,
        top=max(moments, key=lambda m: m.people) if moments else None,
    )
