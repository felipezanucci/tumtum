"""API-Football integration.

Fetches match events (goals, cards, halftime, etc.) to build event timelines.
API docs: https://www.api-football.com/documentation-v3
Rate limit: 100 requests/day (free tier).
"""

from datetime import datetime, timezone

import httpx

from app.config import settings

API_FOOTBALL_BASE = "https://v3.football.api-sports.io"


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
) -> list[dict]:
    """Convert API-Football fixture events into timeline entries.

    Args:
        fixture: Fixture data with match info and kickoff time
        events: List of match events (goals, cards, subs)

    Returns:
        List of timeline entry dicts ready for EventTimeline creation.
    """
    kickoff_str = fixture.get("fixture", {}).get("date", "")
    try:
        kickoff = datetime.fromisoformat(kickoff_str.replace("Z", "+00:00"))
    except (ValueError, AttributeError):
        kickoff = datetime.now(timezone.utc)

    timeline = []

    # Kickoff
    timeline.append({
        "timestamp": kickoff,
        "label": "Início do Jogo",
        "entry_type": "highlight",
        "metadata": {
            "home": fixture.get("teams", {}).get("home", {}).get("name"),
            "away": fixture.get("teams", {}).get("away", {}).get("name"),
        },
    })

    from datetime import timedelta

    for event in events:
        elapsed = event.get("time", {}).get("elapsed", 0)
        extra = event.get("time", {}).get("extra") or 0
        event_time = kickoff + timedelta(minutes=elapsed + extra)

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
            timeline.append({
                "timestamp": event_time,
                "label": label,
                "entry_type": "goal",
                "metadata": {"player": player, "team": team, "detail": detail, "elapsed": elapsed},
            })

        elif event_type == "card":
            card_type = "Amarelo" if "yellow" in detail.lower() else "Vermelho"
            timeline.append({
                "timestamp": event_time,
                "label": f"🟨 Cartão {card_type} — {player} ({team})" if card_type == "Amarelo"
                else f"🟥 Cartão {card_type} — {player} ({team})",
                "entry_type": "highlight",
                "metadata": {"player": player, "team": team, "card": card_type, "elapsed": elapsed},
            })

    # Halftime (at 45 minutes)
    timeline.append({
        "timestamp": kickoff + timedelta(minutes=45),
        "label": "Intervalo",
        "entry_type": "halftime",
        "metadata": None,
    })

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
