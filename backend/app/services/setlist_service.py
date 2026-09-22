"""Setlist.fm API integration.

Fetches concert setlists to build event timelines.
API docs: https://api.setlist.fm/docs/1.0/index.html
Rate limit: 2 requests/second.

**NOT CLEARED FOR PRODUCTION — see open item 44 in docs/decision-log.md.**
Researched 2026-09-22. Three separate walls stand in front of this source,
and the third is not solved by paying:

1. The API is non-commercial only, and commercial is defined by *purpose*:
   "If the primary purpose of your application is to derive revenue, it is
   considered commercial." Being pre-revenue is not a defence.
2. setlist.fm is a Live Nation / Ticketmaster property, so a commercial
   licence is a contract, not a self-serve upgrade — and the requests
   documented in their own forum go unanswered for months.
3. **The API terms (`setlist.fm/help/api-terms`, a different document from
   the general terms) forbid a persistent local datastore** — short-lived
   caching only, direct server calls, immediate distribution to end users —
   plus a mandatory followable attribution link per setlist
   (`json_Setlist.url`). `parse_setlist_to_timeline` exists to write songs
   into our own `event_timeline` and keep them, which is exactly that.

So this module works and must not be pointed at a real event until a human
has read those terms first-hand (every setlist.fm page is blocked from the
environment this was researched in, so all of the above is search-engine
extraction, not a primary read — item 46).

**What the guess list actually needs is the song ORDER, from anywhere.** The
research settled that setlist.fm's own OpenAPI spec gives `json_Song` exactly
five fields — cover, info, name, tape, with — with no timestamp, so the order
is all this source ever had. An operator typing it costs nothing and risks
nothing; `setlist_guess.py` does not care where the order came from.
"""

from datetime import datetime, timedelta

import httpx

from app.config import settings

SETLIST_FM_BASE = "https://api.setlist.fm/rest/1.0"
SOURCE = "setlist.fm"
# How much further off the estimate is assumed to be with every song: talk,
# a stretched intro, a solo. A twenty-song set runs 20–30 minutes past the
# sum of its studio versions.
DRIFT_PER_SONG_SEC = 60


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

    Setlist.fm publishes the order of the songs and never their times. The
    timestamps here are **estimates** — ``avg_song_duration_minutes`` a song
    from the event's start — and every entry says so in its metadata
    (``estimated``, with its ``index`` in the set, its assumed
    ``duration_sec`` and an ``uncertainty_sec`` that grows a minute a song).
    An estimated entry is never handed to the correlator as a cause: it feeds
    the guess list (``setlist_guess``) that offers the person two or three
    songs instead of asserting one. By the third song the estimate is outside
    the correlator's window, and by the tenth a name from it would simply be
    wrong.

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
    index = 0

    sets = setlist_data.get("sets", {}).get("set", [])
    for setlist_set in sets:
        is_encore = setlist_set.get("encore", 0) > 0

        if is_encore and timeline:
            # Add 5-minute break before encore
            current_time += timedelta(minutes=5)

            timeline.append(
                {
                    "timestamp": current_time,
                    "label": "Encore",
                    "entry_type": "encore",
                    "metadata": {
                        "source": SOURCE,
                        "estimated": True,
                        "index": index,
                        "uncertainty_sec": DRIFT_PER_SONG_SEC * (index + 1),
                    },
                }
            )

        for song in setlist_set.get("song", []):
            song_name = song.get("name", "Unknown")
            if not song_name:
                continue

            timeline.append(
                {
                    "timestamp": current_time,
                    "label": song_name,
                    "entry_type": "song_start",
                    "metadata": {
                        "source": SOURCE,
                        "estimated": True,
                        "index": index,
                        "duration_sec": song_delta,
                        "uncertainty_sec": DRIFT_PER_SONG_SEC * (index + 1),
                        "cover": song.get("cover", {}).get("name")
                        if song.get("cover")
                        else None,
                        "with": song.get("with", {}).get("name")
                        if song.get("with")
                        else None,
                        "info": song.get("info"),
                    },
                }
            )

            current_time += timedelta(seconds=song_delta)
            index += 1

    return timeline
