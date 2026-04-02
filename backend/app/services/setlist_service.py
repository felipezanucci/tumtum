"""Setlist.fm API integration.

Fetches concert setlists to build event timelines.
API docs: https://api.setlist.fm/docs/1.0/index.html
Rate limit: 2 requests/second.
"""

import asyncio
from datetime import datetime, timezone

import httpx

from app.config import settings

SETLIST_FM_BASE = "https://api.setlist.fm/rest/1.0"


async def search_setlists(
    artist_name: str,
    city: str | None = None,
    date: str | None = None,
    page: int = 1,
) -> dict:
    """Search setlists by artist name, optionally filtering by city and date.

    Args:
        artist_name: Artist/band name
        city: City name filter
        date: Date in dd-MM-yyyy format
        page: Page number (1-based)

    Returns:
        Raw API response with setlist results.
    """
    params: dict[str, str | int] = {
        "artistName": artist_name,
        "p": page,
    }
    if city:
        params["cityName"] = city
    if date:
        params["date"] = date

    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SETLIST_FM_BASE}/search/setlists",
            params=params,
            headers={
                "Accept": "application/json",
                "x-api-key": settings.setlist_fm_api_key,
            },
        )

    if response.status_code != 200:
        return {"setlist": [], "total": 0}

    return response.json()


async def get_setlist_by_id(setlist_id: str) -> dict | None:
    """Fetch a specific setlist by its Setlist.fm ID."""
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{SETLIST_FM_BASE}/setlist/{setlist_id}",
            headers={
                "Accept": "application/json",
                "x-api-key": settings.setlist_fm_api_key,
            },
        )

    if response.status_code != 200:
        return None

    return response.json()


def parse_setlist_to_timeline(
    setlist_data: dict,
    event_start_time: datetime,
    avg_song_duration_minutes: int = 4,
) -> list[dict]:
    """Convert a Setlist.fm setlist into timeline entries.

    Since Setlist.fm doesn't provide exact timestamps per song, we estimate them
    based on song order and an average duration per song.

    Args:
        setlist_data: Raw setlist data from Setlist.fm API
        event_start_time: When the event started (used as base for timestamps)
        avg_song_duration_minutes: Average song duration for timestamp estimation

    Returns:
        List of timeline entry dicts ready for EventTimeline creation.
    """
    timeline = []
    current_time = event_start_time
    song_delta = avg_song_duration_minutes * 60  # seconds

    sets = setlist_data.get("sets", {}).get("set", [])
    for setlist_set in sets:
        is_encore = setlist_set.get("encore", 0) > 0

        if is_encore and timeline:
            # Add 5-minute break before encore
            current_time = current_time.replace(
                second=current_time.second,
                minute=current_time.minute,
            )
            from datetime import timedelta
            current_time += timedelta(minutes=5)

            timeline.append({
                "timestamp": current_time,
                "label": "Encore",
                "entry_type": "encore",
                "metadata": None,
            })

        for song in setlist_set.get("song", []):
            song_name = song.get("name", "Unknown")
            if not song_name:
                continue

            timeline.append({
                "timestamp": current_time,
                "label": song_name,
                "entry_type": "song_start",
                "metadata": {
                    "cover": song.get("cover", {}).get("name") if song.get("cover") else None,
                    "with": song.get("with", {}).get("name") if song.get("with") else None,
                    "info": song.get("info"),
                },
            })

            from datetime import timedelta
            current_time += timedelta(seconds=song_delta)

    return timeline
