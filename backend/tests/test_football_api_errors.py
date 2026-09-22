"""The reason a football search failed must survive the search.

Recorded 2026-09-22, after an evening spent on a bug that had written its own
diagnosis and thrown it away. Felipe searched Palmeiras on 20/09/2026 through
the admin and read *"Nenhum jogo com esses dados."* The match existed. What
API-Football had actually answered was HTTP 200 with:

    {"errors": {"season": "The Season field is required."}, "results": 0}

Every call in ``football_service`` ended ``if response.status_code != 200:
return []`` and not one of them ever read ``errors``. So a wrong key, an
uncovered season, a spent quota, a missing parameter and a genuine zero all
came out as the same empty list and the same sentence on screen.

These tests hold both halves of the fix: the season goes out with the request,
and a refusal arrives as an exception carrying the API's own words.
"""

from typing import ClassVar

import httpx
import pytest

from app.services import football_service as fs


class FakeResponse:
    def __init__(self, payload, status_code=200):
        self.status_code = status_code
        self._payload = payload

    def json(self):
        if isinstance(self._payload, Exception):
            raise self._payload
        return self._payload


class FakeClient:
    """Stands in for httpx.AsyncClient and records what was asked."""

    calls: ClassVar[list[tuple[str, dict]]] = []
    queue: ClassVar[list] = []

    def __init__(self, *_args, **_kwargs):
        pass

    async def __aenter__(self):
        return self

    async def __aexit__(self, *_exc):
        return False

    async def get(self, url, params=None, headers=None):
        FakeClient.calls.append((url, dict(params or {})))
        nxt = FakeClient.queue.pop(0)
        if isinstance(nxt, Exception):
            raise nxt
        return nxt


@pytest.fixture(autouse=True)
def fake_http(monkeypatch):
    FakeClient.calls = []
    FakeClient.queue = []
    monkeypatch.setattr(fs.httpx, "AsyncClient", FakeClient)
    monkeypatch.setattr(fs.settings, "api_football_key", "k" * 32)
    return FakeClient


def ok(response):
    return FakeResponse({"errors": [], "results": len(response), "response": response})


# --- the season, which is not optional -------------------------------------


@pytest.mark.asyncio
async def test_a_team_search_carries_the_season_derived_from_the_date(fake_http):
    fake_http.queue = [
        ok([{"team": {"id": 121, "name": "Palmeiras"}}]),  # /teams
        ok([{"fixture": {"id": 1492384}}]),  # /fixtures
    ]

    found = await fs.search_fixtures(team_name="Palmeiras", date="2026-09-20")

    assert len(found) == 1
    _url, params = fake_http.calls[-1]
    assert params["season"] == 2026, "the exact omission that hid the match"
    assert params["team"] == 121
    assert params["date"] == "2026-09-20"


@pytest.mark.asyncio
async def test_an_explicit_season_is_not_overwritten(fake_http):
    fake_http.queue = [ok([])]

    await fs.search_fixtures(league_id=71, date="2026-01-10", season=2025)

    assert fake_http.calls[-1][1]["season"] == 2025


@pytest.mark.asyncio
async def test_a_date_alone_needs_no_season(fake_http):
    """``date`` on its own is a legal combination and returns the whole day."""
    fake_http.queue = [ok([{"fixture": {"id": 1}}, {"fixture": {"id": 2}}])]

    await fs.search_fixtures(date="2026-09-20")

    assert "season" not in fake_http.calls[-1][1]


def test_the_season_of_a_brazilian_date_is_its_year():
    assert fs.season_for("2026-09-20") == 2026
    assert fs.season_for("2026-01-05") == 2026


# --- the reason, which must survive ----------------------------------------


@pytest.mark.asyncio
async def test_the_api_s_own_complaint_reaches_the_caller(fake_http):
    """The exact payload that cost an evening."""
    fake_http.queue = [
        ok([{"team": {"id": 121}}]),
        FakeResponse(
            {
                "errors": {"season": "The Season field is required."},
                "results": 0,
                "response": [],
            }
        ),
    ]

    with pytest.raises(fs.FootballApiError) as raised:
        await fs.search_fixtures(team_name="Palmeiras", date="2026-09-20")

    assert "The Season field is required." in raised.value.reason
    assert raised.value.status_code == 200, "the trap: it refuses with a 200"


@pytest.mark.asyncio
async def test_a_plan_that_does_not_cover_the_season_is_not_an_empty_result(fake_http):
    fake_http.queue = [
        FakeResponse({"errors": {"plan": "Free plans do not have access."}})
    ]

    with pytest.raises(fs.FootballApiError) as raised:
        await fs.search_fixtures(date="2026-09-20")

    assert "Free plans" in raised.value.reason


@pytest.mark.asyncio
async def test_a_non_200_says_which_one(fake_http):
    fake_http.queue = [FakeResponse({}, status_code=499)]

    with pytest.raises(fs.FootballApiError) as raised:
        await fs.search_fixtures(date="2026-09-20")

    assert raised.value.status_code == 499
    assert "499" in raised.value.reason


@pytest.mark.asyncio
async def test_a_network_failure_is_not_a_match_that_does_not_exist(fake_http):
    fake_http.queue = [httpx.ConnectError("no route")]

    with pytest.raises(fs.FootballApiError) as raised:
        await fs.search_fixtures(date="2026-09-20")

    assert "API-Football" in raised.value.reason


@pytest.mark.asyncio
async def test_a_team_nobody_has_heard_of_does_not_return_the_whole_day(fake_http):
    """A typo used to fall through to ``date`` alone — 1151 fixtures."""
    fake_http.queue = [ok([])]

    with pytest.raises(fs.FootballApiError) as raised:
        await fs.search_fixtures(team_name="Palmeirras", date="2026-09-20")

    assert "Palmeirras" in raised.value.reason
    assert len(fake_http.calls) == 1, "it must not go on to ask for the day"


# --- and a real empty answer stays empty ------------------------------------


@pytest.mark.asyncio
async def test_a_genuine_zero_is_still_a_zero(fake_http):
    """The one case where "Nenhum jogo com esses dados" is the truth."""
    fake_http.queue = [ok([{"team": {"id": 121}}]), ok([])]

    assert await fs.search_fixtures(team_name="Palmeiras", date="2026-06-01") == []
