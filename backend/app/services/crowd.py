"""Card 04 — *A galera*: the event as a crowd rather than as one heart.

The brand manual asks for "a statistically and privately valid collective
sample", and both halves of that are load-bearing.

**Privately valid** is the one with teeth, and it is why this module exists
instead of a `COUNT(*)` in the route. A collective figure computed over a
small crowd is not collective at all — it is a fact about the people in it.
With two nights uploaded, "64% bateram o próprio pico" says exactly what each
of those two people did, and "o pico da galera foi às 22h41" points at
whoever was there. So nothing is published below `CROWD_MIN_NIGHTS`, and the
screen is told *that* it has no numbers rather than being handed zeros,
because a zero is a claim about the world (the empty-state rule, 22/09).

**Tightened on 26/09** (LGPD audit, AL-4): only nights whose owner granted
`crowd_stats` count at all; the floor is the legal opinion's 100 nights, not
four; a published minute must hold at least `CROWD_MIN_CELL` people; and the
number of people is published as a band — "10+", "25+" — never as an exact
count, so no one can subtract a friend out of it. Below the floor even the
number of measured nights is withheld: "três noites" is itself a fact about
three people.

**Statistically valid** is thinner and honest about it: this counts nights
that were actually uploaded and analysed, never "people at the event". A
stadium holds sixty thousand and TumTum will have measured eleven of them.
Every label this produces says *da galera do TumTum*, never *da galera*.
"""

from collections import Counter
from dataclasses import dataclass, field
from datetime import datetime, timedelta

# The defaults of `settings.crowd_min_nights` and `settings.crowd_min_cell`,
# the legal opinion's figures. The route passes the settings; tests pass
# their own.
MIN_CROWD = 100
MIN_CELL = 10

# How many people a published figure may say, from the top down. A count
# below the smallest band is never published at all — no band describes it
# truthfully — whatever `min_cell` is configured to.
BANDS = (250, 100, 50, 25, 10)

# How wide a bucket is when looking for the minute the crowd shared. A minute
# is too narrow — two people feeling the same goal peak seconds apart — and
# five is wide enough to merge two different songs.
BUCKET = timedelta(minutes=2)

# How many of the crowd must peak inside one bucket before it is called a
# collective moment rather than a coincidence.
COLLECTIVE_SHARE = 0.3


def people_band(people: int) -> str | None:
    """ "25+" for 37 people; None below the smallest band."""
    for floor in BANDS:
        if people >= floor:
            return f"{floor}+"
    return None


@dataclass(frozen=True)
class CrowdMoment:
    """A minute enough hearts rose together for it to mean something.

    `people_band` and never `people`: the exact count stays inside this
    module.
    """

    at: datetime
    people_band: str
    label: str | None = None


@dataclass(frozen=True)
class Crowd:
    """What can be said about an event without saying it about a person.

    [enough] is false when the crowd is too small to publish. The screen then
    says so — *"ainda somos poucos aqui"* — instead of drawing an empty chart
    that reads as "nobody felt anything". [measured_nights] is None then:
    how few is not published either.
    """

    measured_nights: int | None
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
    min_nights: int = MIN_CROWD,
    min_cell: int = MIN_CELL,
) -> Crowd:
    """Where the crowd rose together, from one peak per person.

    Args:
        peaks: One entry per person, ``{"at": datetime, "label": str | None}``
            — **their single biggest moment**, not every peak they had. A
            person with nine peaks would otherwise outvote three people who
            had one each, and the question being asked is how many *people*
            felt something at once.
        measured_nights: How many consenting nights were uploaded and
            analysed for the event. This, not the number of peaks, decides
            whether anything may be published.
        shared_count: How many of them chose to post a moment to the feed.
        min_nights: The floor below which nothing is published.
        min_cell: The fewest people one published minute may describe.

    Returns:
        A [Crowd]. Below ``min_nights`` it carries ``enough=False``,
        ``measured_nights=None`` and nothing else — the caller must not fill
        the gap with zeros.
    """
    if measured_nights < min_nights or not peaks:
        return Crowd(measured_nights=None, enough=False, shared_count=shared_count)

    origin = min(p["at"] for p in peaks)
    buckets: Counter = Counter()
    labels: dict[datetime, Counter] = {}
    for peak in peaks:
        start = _bucket_start(peak["at"], origin)
        buckets[start] += 1
        label = peak.get("label")
        if label:
            labels.setdefault(start, Counter())[label] += 1

    floor = max(min_cell, round(len(peaks) * COLLECTIVE_SHARE))
    counted = [
        (start, people)
        for start, people in sorted(buckets.items())
        if people >= floor and people_band(people) is not None
    ]
    moments = [
        CrowdMoment(
            at=start,
            people_band=people_band(people),
            # The name the most people's own timeline gave that minute. A
            # moment nothing named stays unnamed — the app either knows or
            # says nothing (product rule, 22/09).
            label=(labels.get(start) or Counter()).most_common(1)[0][0]
            if labels.get(start)
            else None,
        )
        for start, people in counted
    ]
    top = None
    if counted:
        # The biggest minute, by the exact count — which is then dropped.
        best = max(range(len(counted)), key=lambda i: counted[i][1])
        top = moments[best]

    return Crowd(
        measured_nights=measured_nights,
        enough=True,
        shared_count=shared_count,
        moments=moments,
        top=top,
    )
