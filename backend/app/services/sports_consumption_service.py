"""Service for loading Brazil sports consumption market research data.

This data helps Tumtum prioritize which sports events to surface and
understand Gen Z engagement habits in the Brazilian market.
"""

import json
from pathlib import Path

from app.schemas.sports_consumption import SportsConsumptionResponse

_DATA_PATH = Path(__file__).resolve().parent.parent / "data" / "brazil_sports_consumption.json"

_cached_data: SportsConsumptionResponse | None = None


def get_sports_consumption_data() -> SportsConsumptionResponse:
    """Load and return the sports consumption dataset (cached after first read)."""
    global _cached_data
    if _cached_data is None:
        raw = json.loads(_DATA_PATH.read_text(encoding="utf-8"))
        _cached_data = SportsConsumptionResponse(**raw)
    return _cached_data


def get_sport_by_name(sport_name: str) -> dict | None:
    """Look up a specific sport by name (case-insensitive)."""
    data = get_sports_consumption_data()
    for sport in data.sports:
        if sport.sport_name.lower() == sport_name.lower():
            return sport.model_dump()
    return None
