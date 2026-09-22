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


def match_clock(
    scheduled_kickoff: datetime,
    events: list[dict],
    kickoff_at: datetime | None = None,
    second_half_at: datetime | None = None,
) -> MatchClock:
    """Build the two clocks from what is known.

    Args:
        scheduled_kickoff: The fixture's advertised time — the fallback.
        events: The API's events, read for first-half stoppage.
        kickoff_at: The operator's APITO INICIAL tap, if there was one.
        second_half_at: The operator's 2º TEMPO tap, if there was one.
    """
    stoppage = max(
        (
            int(e.get("time", {}).get("extra") or 0)
            for e in events
            if int(e.get("time", {}).get("elapsed") or 0) == HALF_MINUTES
        ),
        default=0,
    )
    first = kickoff_at or scheduled_kickoff
    second = second_half_at or (
        first + timedelta(minutes=HALF_MINUTES + stoppage + HALF_TIME_MINUTES)
    )
    return MatchClock(
        first_half_start=first,
        second_half_start=second,
        first_half_anchored=kickoff_at is not None,
        second_half_anchored=second_half_at is not None,
        first_half_stoppage=stoppage,
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
    if season:
        params["season"] = season

    # If team_name is provided, first resolve to team ID
    if team_name:
        team_id = await _find_team_id(team_name)
        if team_id:
            params["team"] = team_id

    if not params:
        return []

    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{API_FOOTBALL_BASE}/fixtures",
            params=params,
            headers={
                "x-apisports-key": settings.api_football_key,
            },
        )

    if response.status_code != 200:
        return []

    data = response.json()
    return data.get("response", [])


async def get_fixture(fixture_id: int) -> dict | None:
    """One fixture by id: teams, scheduled kick-off, status."""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{API_FOOTBALL_BASE}/fixtures",
            params={"id": fixture_id},
            headers={
                "x-apisports-key": settings.api_football_key,
            },
        )

    if response.status_code != 200:
        return None

    data = response.json()
    results = data.get("response", [])
    return results[0] if results else None


async def get_fixture_events(fixture_id: int) -> list[dict]:
    """Fetch events (goals, cards, substitutions) for a specific fixture."""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{API_FOOTBALL_BASE}/fixtures/events",
            params={"fixture": fixture_id},
            headers={
                "x-apisports-key": settings.api_football_key,
            },
        )

    if response.status_code != 200:
        return []

    data = response.json()
    return data.get("response", [])


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
    clock = match_clock(scheduled_kickoff(fixture), events, kickoff_at, second_half_at)
    home = fixture.get("teams", {}).get("home", {}).get("name")
    away = fixture.get("teams", {}).get("away", {}).get("name")

    def meta(elapsed: int, **extra: object) -> dict:
        m: dict = {
            "source": SOURCE,
            "anchored": clock.anchored_for(elapsed),
            "elapsed": elapsed,
        }
        if not clock.anchored_for(elapsed):
            # A time derived from the schedule offers a name; it does not
            # assert one (see setlist_guess). How far it may be off: a
            # kick-off slips by minutes; the second half adds the interval.
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
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{API_FOOTBALL_BASE}/teams",
            params={"search": team_name},
            headers={
                "x-apisports-key": settings.api_football_key,
            },
        )

    if response.status_code != 200:
        return None

    data = response.json()
    results = data.get("response", [])
    if results:
        return results[0].get("team", {}).get("id")
    return None
