"""The live watch of a match (#52): whistles measured without a tap.

API-Football's periods came back null on a finished Série A match (23/09),
so the whistle is caught live instead — the status changing from NS to 1H,
and from HT to 2H, between two polls fifteen seconds apart. What these pin
is that the watch never records more than it saw.
"""

import uuid
from datetime import UTC, datetime, timedelta

import pytest

from app.services import football_service
from app.services.football_service import (
    CLOCK_LIVE,
    CLOCK_SCHEDULE,
    CLOCK_TAP,
    KICKOFF,
    SECOND_HALF,
    anchor_sources_from_timeline,
    parse_fixture_to_timeline,
)
from app.services.match_watch import (
    DAILY_BUDGET,
    LIVE_SOURCE,
    Watch,
    Watcher,
    fixture_id_of,
    observe,
    whistle_entry,
)

KICK = datetime(2026, 10, 4, 19, 0, tzinfo=UTC)


def _watch() -> Watch:
    return Watch(event_id=uuid.uuid4(), fixture_id=1, scheduled=KICK)


def _poll(watch: Watch, status: str, at: datetime):
    return observe(watch, status, at)


def test_the_kickoff_is_the_midpoint_of_the_two_polls_that_bound_it():
    w = _watch()
    assert _poll(w, "NS", KICK) is None
    whistle = _poll(w, "1H", KICK + timedelta(seconds=15))
    assert whistle is not None
    assert whistle.entry_type == KICKOFF
    assert whistle.at == KICK + timedelta(seconds=7.5)
    assert whistle.window_sec == 15


def test_the_restart_is_caught_from_half_time():
    w = _watch()
    _poll(w, "NS", KICK)
    _poll(w, "1H", KICK + timedelta(seconds=15))
    _poll(w, "HT", KICK + timedelta(minutes=48))
    restart = KICK + timedelta(minutes=66)
    _poll(w, "HT", restart)
    whistle = _poll(w, "2H", restart + timedelta(seconds=15))
    assert whistle is not None and whistle.entry_type == SECOND_HALF
    assert w.second_half_at == restart + timedelta(seconds=7.5)


def test_a_match_already_running_when_the_watch_starts_records_nothing():
    """A deploy mid-match: the watch does not know when the half began."""
    w = _watch()
    assert _poll(w, "1H", KICK + timedelta(minutes=20)) is None
    assert w.kickoff_at is None
    assert w.notes  # and it says so, rather than staying silent


def test_a_gap_too_wide_is_not_called_a_measurement():
    w = _watch()
    _poll(w, "NS", KICK)
    assert _poll(w, "1H", KICK + timedelta(minutes=5)) is None
    assert w.kickoff_at is None
    assert any("largo demais" in n for n in w.notes)


def test_a_jump_that_skips_a_whistle_records_nothing():
    w = _watch()
    _poll(w, "NS", KICK)
    assert _poll(w, "HT", KICK + timedelta(seconds=15)) is None


def test_the_final_whistle_ends_the_watch():
    w = _watch()
    _poll(w, "2H", KICK)
    _poll(w, "FT", KICK + timedelta(seconds=15))
    assert w.finished


def test_the_entry_says_who_measured_it():
    w = _watch()
    _poll(w, "NS", KICK)
    entry = whistle_entry(_poll(w, "1H", KICK + timedelta(seconds=15)))
    assert entry["entry_type"] == KICKOFF
    assert entry["metadata"]["source"] == LIVE_SOURCE
    assert entry["metadata"]["clock_source"] == CLOCK_LIVE
    assert entry["metadata"]["anchored"] is True


def test_the_budget_stops_the_watch_before_the_quota_does():
    watcher = Watcher()
    now = KICK
    for _ in range(DAILY_BUDGET):
        assert watcher._spend(now)
    assert not watcher._spend(now)
    # A new day, a new budget.
    assert watcher._spend(now + timedelta(days=1))


def test_only_an_api_football_id_is_watched():
    assert fixture_id_of("api-football:1492384") == 1492384
    assert fixture_id_of("setlist:9") is None
    assert fixture_id_of(None) is None
    assert fixture_id_of("api-football:abc") is None


# --- the anchors reaching the goals ---


def test_a_live_whistle_is_read_back_as_live():
    live = {"timestamp": KICK, "entry_type": KICKOFF, "source": LIVE_SOURCE}
    tap = {"timestamp": KICK + timedelta(minutes=64), "entry_type": SECOND_HALF}
    assert anchor_sources_from_timeline([live, tap]) == (CLOCK_LIVE, CLOCK_TAP)


def test_the_earlier_of_a_tap_and_a_live_whistle_decides_the_source():
    tap = {"timestamp": KICK - timedelta(seconds=5), "entry_type": KICKOFF}
    live = {"timestamp": KICK, "entry_type": KICKOFF, "source": LIVE_SOURCE}
    assert anchor_sources_from_timeline([live, tap])[0] == CLOCK_TAP


def test_goals_laid_on_live_whistles_are_anchored_and_not_duplicated():
    fixture = {
        "fixture": {"date": KICK.isoformat(), "periods": {"first": None}},
        "teams": {"home": {"name": "Corinthians"}, "away": {"name": "Palmeiras"}},
    }
    goal = {
        "time": {"elapsed": 60, "extra": None},
        "type": "Goal",
        "detail": "Normal Goal",
        "team": {"name": "Corinthians"},
        "player": {"name": "Yuri Alberto"},
    }
    kickoff = KICK + timedelta(minutes=3)
    restart = KICK + timedelta(minutes=66)
    entries = parse_fixture_to_timeline(
        fixture,
        [goal],
        kickoff_at=kickoff,
        second_half_at=restart,
        kickoff_source=CLOCK_LIVE,
        second_half_source=CLOCK_LIVE,
    )
    kinds = [e["entry_type"] for e in entries]
    # The live whistles are already on the timeline; no second copy.
    assert KICKOFF not in kinds and SECOND_HALF not in kinds
    scored = next(e for e in entries if e["entry_type"] == "goal")
    assert scored["timestamp"] == restart + timedelta(minutes=15)
    assert scored["metadata"]["anchored"] is True
    assert scored["metadata"]["clock_source"] == CLOCK_LIVE


def test_without_any_measurement_the_schedule_still_says_so():
    fixture = {"fixture": {"date": KICK.isoformat()}, "teams": {}}
    entries = parse_fixture_to_timeline(fixture, [])
    kick = next(e for e in entries if e["entry_type"] == KICKOFF)
    assert kick["metadata"]["clock_source"] == CLOCK_SCHEDULE
    assert kick["metadata"]["source"] == football_service.SOURCE


# --- end to end: a simulated match through the real loop ---


@pytest.mark.asyncio
async def test_a_simulated_match_leaves_two_live_whistles_and_anchored_goals(
    monkeypatch,
):
    from sqlalchemy import select
    from sqlalchemy.dialects.postgresql import JSONB, UUID
    from sqlalchemy.ext.asyncio import async_sessionmaker, create_async_engine
    from sqlalchemy.ext.compiler import compiles

    import app.core.database as database
    import app.main  # noqa: F401 — registers every model
    from app.models.event import Event
    from app.models.event_timeline import EventTimeline

    compiles(UUID, "sqlite")(lambda *_a, **_k: "CHAR(32)")
    compiles(JSONB, "sqlite")(lambda *_a, **_k: "TEXT")
    engine = create_async_engine("sqlite+aiosqlite://")
    async with engine.begin() as conn:
        await conn.run_sync(database.Base.metadata.create_all)
    sessions = async_sessionmaker(engine, expire_on_commit=False)
    monkeypatch.setattr(database, "async_session", sessions)

    event_id = uuid.uuid4()
    async with sessions() as db:
        db.add(
            Event(
                id=event_id,
                name="Corinthians x Palmeiras",
                date=KICK.date(),
                event_type="sports",
                external_id="api-football:77",
            )
        )
        await db.commit()

    script = {"status": "NS"}

    async def fake_fixture(_fixture_id):
        return {
            "fixture": {
                "date": KICK.isoformat(),
                "status": {"short": script["status"]},
                "periods": {"first": None, "second": None},
            },
            "teams": {"home": {"name": "Corinthians"}, "away": {"name": "Palmeiras"}},
        }

    async def fake_events(_fixture_id):
        return [
            {
                "time": {"elapsed": 60, "extra": None},
                "type": "Goal",
                "detail": "Normal Goal",
                "team": {"name": "Corinthians"},
                "player": {"name": "Yuri Alberto"},
            }
        ]

    monkeypatch.setattr(football_service, "get_fixture", fake_fixture)
    monkeypatch.setattr(football_service, "get_fixture_events", fake_events)

    watcher = Watcher()
    t = KICK - timedelta(minutes=5)
    step = timedelta(seconds=15)
    for status, until in [
        ("NS", KICK + timedelta(minutes=2)),
        ("1H", KICK + timedelta(minutes=2, seconds=30)),
        ("HT", KICK + timedelta(minutes=68)),
        ("2H", KICK + timedelta(minutes=68, seconds=30)),
        ("FT", KICK + timedelta(minutes=69)),
    ]:
        script["status"] = status
        while t < until:
            await watcher.tick(t)
            t += step

    async with sessions() as db:
        rows = (
            (
                await db.execute(
                    select(EventTimeline).where(EventTimeline.event_id == event_id)
                )
            )
            .scalars()
            .all()
        )
    by_type = {}
    for row in rows:
        by_type.setdefault(row.entry_type, []).append(row)

    assert len(by_type[KICKOFF]) == 1 and len(by_type[SECOND_HALF]) == 1
    kickoff = by_type[KICKOFF][0]
    assert kickoff.metadata_["source"] == LIVE_SOURCE
    assert (
        abs(
            (
                kickoff.timestamp.replace(tzinfo=UTC) - (KICK + timedelta(minutes=2))
            ).total_seconds()
        )
        <= 15
    )
    goal = by_type["goal"][0]
    assert goal.metadata_["anchored"] is True
    assert goal.metadata_["clock_source"] == CLOCK_LIVE
    status = watcher.status_for(event_id)
    assert status["state"] == "done"
    await engine.dispose()
