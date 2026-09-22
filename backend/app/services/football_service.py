"""API-Football integration.

Fetches match events (goals, cards, halftime, etc.) to build event timelines.
API docs: https://www.api-football.com/documentation-v3
Rate limit: 100 requests/day (free tier).

The API gives every event a **match minute** (``elapsed`` + ``extra``), and a
match minute is not a wall-clock time. Until 2026-09-22 this module added the
minute to the *scheduled* kick-off and stopped there, which is wrong twice:

1. It ignored the interval. Every second-half event landed ~15–20 minutes
   early — fifteen times outside the correlator's ±60 s window.
2. It trusted the fixture's scheduled time. Kick-offs slip by minutes as a
   matter of routine, and first-half stoppage pushes the second half further.

A match is two clocks, one per half, and each needs an **anchor**: the
instant the half actually started. The anchors come from two operator taps
during the capture — APITO INICIAL and 2º TEMPO, stored as timeline entries
of type ``kickoff`` and ``second_half`` — and this module reads them back.
Without an anchor the clock falls back to the schedule and an assumed
15-minute interval, and says so in every entry's metadata, because a time
the app derived is not a time the app measured.

**A third source sits between the two (2026-09-22).** The fixture object
carries ``periods.first`` and ``periods.second`` — UNIX timestamps for each
half's start — and until now this module read neither. The research that
found them could not settle whether they are the *real* whistles or the
scheduled time restated, and an anchor we cannot vouch for is worse than
none: it would make the app call a derived time measured.

The fixture answers that itself, per match. **A period that differs from the
scheduled kick-off cannot be the schedule restated** — something measured it.
So a differing period is treated as measured; one equal to the schedule stays
undetermined and the clock falls back as before. Both halves are judged
together, because the evidence is about the feed, not about one number: once
``periods.first`` is shown to be real for a fixture, ``periods.second`` from
the same payload is real too.

Every entry records which source timed it in ``clock_source`` — ``tap``,
``api_periods`` or ``schedule`` — so the question answers itself over real
matches instead of waiting on a document nobody can open.
"""

from dataclasses import dataclass
from datetime import UTC, datetime, timedelta

import httpx

from app.config import settings

API_FOOTBALL_BASE = "https://v3.football.api-sports.io"

# The regulation half, in minutes. API-Football counts the second half from
# 46, so an event at ``elapsed`` ≥ 46 is (elapsed − 45) minutes into it.
HALF_MINUTES = 45
# What the interval is assumed to last when nobody tapped 2º TEMPO. The rule
# book says fifteen; real ones run fifteen to twenty. The assumption is
# recorded on every entry that depends on it.
HALF_TIME_MINUTES = 15
# How far an unanchored time may be from the truth, for the guess list: a
# kick-off routinely slips up to five minutes; the second half adds the
# interval's own spread on top.
FIRST_HALF_UNANCHORED_SEC = 300
SECOND_HALF_UNANCHORED_SEC = 600

# What the operator's two taps are stored as, and what the app sends.
KICKOFF = "kickoff"
SECOND_HALF = "second_half"
SOURCE = "api-football"

# Who timed a half, most trusted first. The operator stood there; the API's
# periods are a third party that proved itself on this fixture; the schedule
# is a guess.
CLOCK_TAP = "tap"
CLOCK_API = "api_periods"
CLOCK_SCHEDULE = "schedule"

# How far apart a period and the scheduled kick-off must be before the
# difference counts as evidence rather than clock skew between two systems.
PERIOD_DIFFERS_SEC = 60


@dataclass(frozen=True)
class MatchClock:
    """When each half actually started, and how much of that is known."""

    first_half_start: datetime
    second_half_start: datetime
    first_half_anchored: bool
    second_half_anchored: bool
    # Stoppage added to the first half, in minutes, as the API reports it
    # (the largest ``extra`` on an event at minute 45). Zero when no event
    # fell in stoppage, which is a floor, not a measurement.
    first_half_stoppage: int
    # Who timed each half — CLOCK_TAP, CLOCK_API or CLOCK_SCHEDULE. Carried
    # onto every entry so a real match says, in its own data, whether the
    # API's periods are worth anything.
    first_half_source: str = CLOCK_SCHEDULE
    second_half_source: str = CLOCK_SCHEDULE

    def wall_clock(self, elapsed: int, extra: int = 0) -> datetime:
        """The instant of a match minute."""
        if elapsed <= HALF_MINUTES:
            return self.first_half_start + timedelta(minutes=elapsed + extra)
        return self.second_half_start + timedelta(
            minutes=elapsed - HALF_MINUTES + extra
        )

    def anchored_for(self, elapsed: int) -> bool:
        return (
            self.first_half_anchored
            if elapsed <= HALF_MINUTES
            else self.second_half_anchored
        )

    def source_for(self, elapsed: int) -> str:
        return (
            self.first_half_source
            if elapsed <= HALF_MINUTES
            else self.second_half_source
        )


def periods_from_fixture(fixture: dict) -> tuple[datetime | None, datetime | None]:
    """``periods.first`` and ``periods.second``, as instants.

    Both are UNIX seconds in the fixture object, and either can be null —
    before kick-off, and for a match the feed never covered live.
    """

    def at(key: str) -> datetime | None:
        raw = fixture.get("fixture", {}).get("periods", {}).get(key)
        if raw is None:
            return None
        try:
            return datetime.fromtimestamp(int(raw), tz=UTC)
        except (TypeError, ValueError, OSError, OverflowError):
            return None

    return at("first"), at("second")


def periods_are_measured(
    scheduled: datetime,
    period_first: datetime | None,
) -> bool:
    """Whether this fixture's periods are evidence or an echo of the schedule.

    The only thing that distinguishes a measured whistle from the advertised
    time restated is that a measured one **differs**. A match that genuinely
    kicked off on the minute is indistinguishable from a feed that never
    measured anything, and the safe reading of a tie is the pessimistic one:
    fall back, and let a match that ran late settle it.
    """
    if period_first is None:
        return False
    return abs((period_first - scheduled).total_seconds()) >= PERIOD_DIFFERS_SEC


def match_clock(
    scheduled_kickoff: datetime,
    events: list[dict],
    kickoff_at: datetime | None = None,
    second_half_at: datetime | None = None,
    period_first: datetime | None = None,
    period_second: datetime | None = None,
) -> MatchClock:
    """Build the two clocks from what is known.

    Sources, most trusted first: the operator's tap, the API's periods when
    this fixture proved them measured, then the schedule.

    Args:
        scheduled_kickoff: The fixture's advertised time — the fallback.
        events: The API's events, read for first-half stoppage.
        kickoff_at: The operator's APITO INICIAL tap, if there was one.
        second_half_at: The operator's 2º TEMPO tap, if there was one.
        period_first: ``periods.first`` from the fixture, if the feed has it.
        period_second: ``periods.second`` from the fixture, if the feed has it.
    """
    stoppage = max(
        (
            int(e.get("time", {}).get("extra") or 0)
            for e in events
            if int(e.get("time", {}).get("elapsed") or 0) == HALF_MINUTES
        ),
        default=0,
    )
    # One verdict for the payload, not one per number: what is being judged
    # is whether this feed measured the match at all.
    measured = periods_are_measured(scheduled_kickoff, period_first)

    if kickoff_at is not None:
        first, first_source = kickoff_at, CLOCK_TAP
    elif measured and period_first is not None:
        first, first_source = period_first, CLOCK_API
    else:
        first, first_source = scheduled_kickoff, CLOCK_SCHEDULE

    if second_half_at is not None:
        second, second_source = second_half_at, CLOCK_TAP
    elif measured and period_second is not None:
        second, second_source = period_second, CLOCK_API
    else:
        second = first + timedelta(minutes=HALF_MINUTES + stoppage + HALF_TIME_MINUTES)
        second_source = CLOCK_SCHEDULE

    return MatchClock(
        first_half_start=first,
        second_half_start=second,
        first_half_anchored=first_source != CLOCK_SCHEDULE,
        second_half_anchored=second_source != CLOCK_SCHEDULE,
        first_half_stoppage=stoppage,
        first_half_source=first_source,
        second_half_source=second_source,
    )


def anchors_from_timeline(
    timeline: list[dict],
) -> tuple[datetime | None, datetime | None]:
    """The operator's two taps, read from an event's existing timeline.

    Each entry is ``{"timestamp": datetime, "entry_type": str}``. If a tap was
    repeated, the earliest wins: a second APITO INICIAL is a slip, and the
    first one is the one that was closest to the whistle.
    """
    kickoff = [e["timestamp"] for e in timeline if e.get("entry_type") == KICKOFF]
    second = [e["timestamp"] for e in timeline if e.get("entry_type") == SECOND_HALF]
    return (min(kickoff) if kickoff else None, min(second) if second else None)


def scheduled_kickoff(fixture: dict) -> datetime:
    kickoff_str = fixture.get("fixture", {}).get("date", "")
    try:
        return datetime.fromisoformat(kickoff_str.replace("Z", "+00:00"))
    except (ValueError, AttributeError):
        return datetime.now(UTC)


class FootballApiError(RuntimeError):
    """API-Football refused, **in its own words**.

    Until 2026-09-22 every call here ended ``if response.status_code != 200:
    return []`` and never once read ``data["errors"]`` — which is where this
    API writes "your plan does not cover", "invalid key", "quota spent" and
    "this parameter needs that one", **with HTTP 200 and an empty list**. So a
    wrong key, an uncovered season, a spent quota and a genuine zero all
    produced the same screen: *"Nenhum jogo com esses dados."*

    It cost an evening to find that the real message had been there all along:
    ``{"season": "The Season field is required."}``. An empty state is a claim,
    and this exception exists so the operator reads the API's sentence instead
    of ours.
    """

    def __init__(self, reason: str, status_code: int | None = None):
        super().__init__(reason)
        self.reason = reason
        self.status_code = status_code


def _reason_from(errors: object) -> str:
    """API-Football's own words: a dict of field → complaint, or a string."""
    if isinstance(errors, dict):
        return "; ".join(f"{k}: {v}" for k, v in errors.items())
    return str(errors)


async def _get(path: str, params: dict) -> dict:
    """One call to API-Football, or an exception that says why not.

    Nothing here returns an empty result to mean a failure. The two ways this
    API says no — a non-200, and a 200 carrying ``errors`` — both raise.
    """
    async with httpx.AsyncClient(timeout=15.0) as client:
        try:
            response = await client.get(
                f"{API_FOOTBALL_BASE}/{path}",
                params=params,
                headers={"x-apisports-key": settings.api_football_key},
            )
        except httpx.HTTPError as exc:
            raise FootballApiError(f"não deu pra falar com a API-Football: {exc}")

    if response.status_code != 200:
        raise FootballApiError(
            f"a API-Football respondeu {response.status_code}",
            status_code=response.status_code,
        )

    try:
        data = response.json()
    except ValueError:
        raise FootballApiError("a API-Football respondeu algo que não é JSON")

    # The one that hid for a whole evening: HTTP 200, empty list, and the
    # reason sitting right here.
    if errors := data.get("errors"):
        raise FootballApiError(_reason_from(errors), status_code=200)
    return data


def _get_response(data: dict) -> list[dict]:
    """The payload's ``response`` list. Only ever reached after [_get] passed."""
    return data.get("response", []) or []


def season_for(date: str) -> int:
    """The season a match on ``date`` (YYYY-MM-DD) belongs to.

    API-Football rejects ``team`` or ``league`` without a season, so this is
    not optional. The calendar year is right for Brazilian football, whose
    championships run January to December, and Brazil is the whole of Phase 0.
    European leagues label 2026/27 as season 2026, so a January fixture there
    would need ``year - 1``; when that day comes the API will say so out loud
    now that [FootballApiError] carries its words.
    """
    return int(date[:4])


async def search_fixtures(
    team_name: str | None = None,
    league_id: int | None = None,
    date: str | None = None,
    season: int | None = None,
) -> list[dict]:
    """Search for football fixtures/matches.

    Args:
        team_name: Team name (will search for team ID first)
        league_id: League ID (e.g., 71 for Brasileirão Série A)
        date: Date in YYYY-MM-DD format
        season: Season year

    Returns:
        List of fixture dicts from the API.
    """
    params: dict[str, str | int] = {}
    if date:
        params["date"] = date
    if league_id:
        params["league"] = league_id

    # If team_name is provided, first resolve to team ID
    if team_name:
        team_id = await _find_team_id(team_name)
        if team_id is None:
            raise FootballApiError(
                f'a API-Football não conhece nenhum time chamado "{team_name}"'
            )
        params["team"] = team_id

    if not params:
        return []

    # **The season is not optional** (2026-09-22). API-Football answers
    # ``team`` or ``league`` without one with HTTP 200, an empty list and
    # ``{"season": "The Season field is required."}`` — which this module used
    # to discard, so a Palmeiras match that was sitting right there read as
    # "Nenhum jogo com esses dados" on the operator's screen.
    if season:
        params["season"] = season
    elif ("team" in params or "league" in params) and date:
        params["season"] = season_for(date)

    return _get_response(await _get("fixtures", params))


async def get_fixture(fixture_id: int) -> dict | None:
    """One fixture by id: teams, scheduled kick-off, status."""
    results = _get_response(await _get("fixtures", {"id": fixture_id}))
    return results[0] if results else None


async def get_fixture_events(fixture_id: int) -> list[dict]:
    """Fetch events (goals, cards, substitutions) for a specific fixture."""
    return _get_response(await _get("fixtures/events", {"fixture": fixture_id}))


def parse_fixture_to_timeline(
    fixture: dict,
    events: list[dict],
    kickoff_at: datetime | None = None,
    second_half_at: datetime | None = None,
) -> list[dict]:
    """Convert API-Football fixture events into timeline entries.

    Args:
        fixture: Fixture data with match info and scheduled kick-off
        events: List of match events (goals, cards, subs)
        kickoff_at: When the first half really started (operator's tap)
        second_half_at: When the second half really started (operator's tap)

    Returns:
        List of timeline entry dicts ready for EventTimeline creation. Every
        entry's metadata says whether its time was ``anchored`` on a tap or
        derived from the schedule, and carries ``source`` so a rebuild can
        replace exactly these rows and no operator's mark.
    """
    period_first, period_second = periods_from_fixture(fixture)
    clock = match_clock(
        scheduled_kickoff(fixture),
        events,
        kickoff_at,
        second_half_at,
        period_first,
        period_second,
    )
    home = fixture.get("teams", {}).get("home", {}).get("name")
    away = fixture.get("teams", {}).get("away", {}).get("name")

    def meta(elapsed: int, **extra: object) -> dict:
        m: dict = {
            "source": SOURCE,
            "anchored": clock.anchored_for(elapsed),
            # Which of the three timed this half. Read across real matches it
            # answers whether the API's periods are ever worth reading.
            "clock_source": clock.source_for(elapsed),
            "elapsed": elapsed,
        }
        if not clock.anchored_for(elapsed):
            # A time derived from the schedule names nothing — the
            # correlator never sees it (see event_correlator.is_tentative).
            # How far it may be off is recorded anyway, because it is what
            # an operator needs to judge whether a tap is still worth making.
            m["uncertainty_sec"] = (
                FIRST_HALF_UNANCHORED_SEC
                if elapsed <= HALF_MINUTES
                else SECOND_HALF_UNANCHORED_SEC
            )
            if elapsed > HALF_MINUTES:
                m["assumed_half_time_min"] = HALF_TIME_MINUTES
        m.update(extra)
        return m

    timeline = []

    # The two half starts are entries too — a moment at the whistle is named
    # by the whistle. When the operator tapped one, the tap already is that
    # entry, and a second copy would only give the correlator two names for
    # the same instant.
    if not clock.first_half_anchored:
        timeline.append(
            {
                "timestamp": clock.first_half_start,
                "label": "Início do jogo",
                "entry_type": KICKOFF,
                "metadata": meta(0, home=home, away=away),
            }
        )
    if not clock.second_half_anchored:
        timeline.append(
            {
                "timestamp": clock.second_half_start,
                "label": "Segundo tempo",
                "entry_type": SECOND_HALF,
                "metadata": meta(HALF_MINUTES + 1),
            }
        )

    for event in events:
        elapsed = int(event.get("time", {}).get("elapsed") or 0)
        extra = int(event.get("time", {}).get("extra") or 0)
        event_time = clock.wall_clock(elapsed, extra)

        event_type = event.get("type", "").lower()
        detail = event.get("detail", "")
        player = event.get("player", {}).get("name", "")
        team = event.get("team", {}).get("name", "")

        if event_type == "goal":
            label = f"⚽ Gol! {player} ({team})"
            if "own goal" in detail.lower():
                label = f"⚽ Gol contra — {player} ({team})"
            elif "penalty" in detail.lower():
                label = f"⚽ Gol de pênalti — {player} ({team})"
            timeline.append(
                {
                    "timestamp": event_time,
                    "label": label,
                    "entry_type": "goal",
                    "metadata": meta(
                        elapsed, player=player, team=team, detail=detail, extra=extra
                    ),
                }
            )

        elif event_type == "card":
            card_type = "Amarelo" if "yellow" in detail.lower() else "Vermelho"
            timeline.append(
                {
                    "timestamp": event_time,
                    "label": f"🟨 Cartão {card_type} — {player} ({team})"
                    if card_type == "Amarelo"
                    else f"🟥 Cartão {card_type} — {player} ({team})",
                    "entry_type": "highlight",
                    "metadata": meta(
                        elapsed, player=player, team=team, card=card_type, extra=extra
                    ),
                }
            )

    # The interval: where the first half really ended, stoppage included.
    timeline.append(
        {
            "timestamp": clock.first_half_start
            + timedelta(minutes=HALF_MINUTES + clock.first_half_stoppage),
            "label": "Intervalo",
            "entry_type": "halftime",
            "metadata": meta(HALF_MINUTES, stoppage=clock.first_half_stoppage),
        }
    )

    # Sort by timestamp
    timeline.sort(key=lambda x: x["timestamp"])
    return timeline


async def _find_team_id(team_name: str) -> int | None:
    """Search for a team by name and return its API-Football ID."""
    results = _get_response(await _get("teams", {"search": team_name}))
    if results:
        return results[0].get("team", {}).get("id")
    return None
