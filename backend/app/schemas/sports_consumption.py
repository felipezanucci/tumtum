from pydantic import BaseModel


class CitedValue(BaseModel):
    value: str
    value_citation: str


class SportData(BaseModel):
    sport_name: str
    sport_name_citation: str
    consumption_frequency: str
    consumption_frequency_citation: str
    average_spending: float | None = None
    average_spending_citation: str | None = None
    engagement_habits: str
    engagement_habits_citation: str


class EntertainmentData(BaseModel):
    preferred_platforms: list[CitedValue]
    consumption_frequency: str
    consumption_frequency_citation: str
    average_spending: float
    average_spending_citation: str
    engagement_habits: str
    engagement_habits_citation: str


class SportsConsumptionResponse(BaseModel):
    study_year: int
    study_year_citation: str
    sports: list[SportData]
    entertainment: EntertainmentData
