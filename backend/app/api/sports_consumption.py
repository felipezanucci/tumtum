"""Endpoints for Brazil sports consumption market research data."""

from fastapi import APIRouter, HTTPException

from app.schemas.sports_consumption import SportData, SportsConsumptionResponse
from app.services.sports_consumption_service import (
    get_sport_by_name,
    get_sports_consumption_data,
)

router = APIRouter(prefix="/api/sports-consumption", tags=["sports-consumption"])


@router.get("/", response_model=SportsConsumptionResponse)
async def list_sports_consumption():
    """Return the full sports consumption dataset."""
    return get_sports_consumption_data()


@router.get("/sports/{sport_name}", response_model=SportData)
async def get_sport(sport_name: str):
    """Return data for a specific sport by name."""
    sport = get_sport_by_name(sport_name)
    if sport is None:
        raise HTTPException(status_code=404, detail=f"Esporte '{sport_name}' não encontrado")
    return sport
