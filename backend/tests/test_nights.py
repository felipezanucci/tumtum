"""A night on the server: kept only with consent, only when it fits its event,
deletable by its owner, and counted in the crowd only with consent (26/09).
"""

import uuid
from datetime import UTC, datetime, time, timedelta

import pytest
from fastapi import HTTPException
from sqlalchemy import func, select

from app.api.experience import analyze_session
from app.api.feed import get_crowd, was_there
from app.api.health import create_hr_session, delete_hr_session
from app.config import settings
from app.models.card import Card, Share
from app.models.event_post import EventPost, EventPostReaction
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.moderation import PostReport
from app.models.peak import Peak
from app.models.privacy import DataAccessLog
from app.schemas.health import HRDataPointInput, HRSessionCreateRequest
from app.services.event_window import event_bounds
from tests.conftest import AT, add_event, add_night, add_user, grant, make_request


def _body(event=None, start=AT, minutes=30, **point_extra):
    return HRSessionCreateRequest(
        start_time=start,
        end_time=start + timedelta(minutes=minutes),
        source_device="Polar H10",
        event_id=event.id if event else None,
        data_points=[
            HRDataPointInput(time=start + timedelta(seconds=i), bpm=90, **point_extra)
            for i in range(5)
        ],
    )


async def _count(db, model, *where) -> int:
    query = select(func.count()).select_from(model)
    for condition in where:
        query = query.where(condition)
    return (await db.execute(query)).scalar_one()


# --- the event window ---


@pytest.mark.asyncio
async def test_a_night_during_the_event_is_kept(memdb):
    user = await add_user(memdb)
    event = await add_event(memdb, start=time(21, 0), end=time(23, 30))
    night = await create_hr_session(_body(event), user, memdb)
    assert night.event_id == event.id


@pytest.mark.asyncio
async def test_a_night_hours_away_from_the_event_is_refused(memdb):
    user = await add_user(memdb)
    event = await add_event(memdb, start=time(21, 0), end=time(23, 30))
    morning = datetime(2026, 10, 2, 12, 0, tzinfo=UTC)  # 09h00 in São Paulo
    with pytest.raises(HTTPException) as refused:
        await create_hr_session(_body(event, start=morning), user, memdb)
    assert refused.value.status_code == 422
    assert refused.value.detail == "Essa noite não bate com o horário do evento."


@pytest.mark.asyncio
async def test_two_hours_of_slack_either_side(memdb):
    user = await add_user(memdb)
    event = await add_event(memdb, start=time(21, 0), end=time(23, 0))
    # Ends at 19h05 local, inside the two hours before 21h00.
    early = datetime(2026, 10, 2, 21, 35, tzinfo=UTC)
    assert await create_hr_session(_body(event, start=early), user, memdb)


def test_an_event_that_ends_after_midnight_ends_the_next_day():
    from app.models.event import Event

    event = Event(
        date=datetime(2026, 10, 2).date(), start_time=time(22), end_time=time(3)
    )
    start, end = event_bounds(event)
    assert end - start == timedelta(hours=5)


@pytest.mark.asyncio
async def test_an_unknown_event_is_a_404(memdb):
    user = await add_user(memdb)
    body = _body()
    body.event_id = uuid.uuid4()
    with pytest.raises(HTTPException) as refused:
        await create_hr_session(body, user, memdb)
    assert refused.value.status_code == 404


@pytest.mark.asyncio
async def test_rr_and_motion_are_neither_accepted_nor_kept(memdb):
    user = await add_user(memdb)
    body = _body(rr_interval_ms=800, motion_level=3)  # an old client's upload
    assert not hasattr(body.data_points[0], "rr_interval_ms")
    night = await create_hr_session(body, user, memdb)
    stored = (
        (await memdb.execute(select(HRData).where(HRData.session_id == night.id)))
        .scalars()
        .all()
    )
    assert stored and all(
        p.rr_interval_ms is None and p.motion_level is None for p in stored
    )


# --- analysis starts the retention clock ---


@pytest.mark.asyncio
async def test_analysing_stamps_analyzed_at_and_logs_the_access(memdb):
    user = await add_user(memdb)
    night = await add_night(memdb, user)
    assert night.analyzed_at is None
    night_id = night.id
    memdb.expire(night)  # SQLite hands times back naive; read both from it
    await analyze_session(night_id, make_request(), user, memdb)
    assert night.analyzed_at is not None
    row = (await memdb.execute(select(DataAccessLog))).scalar_one()
    assert (row.resource, row.action, row.ip) == (
        "experience",
        "analyze",
        "203.0.113.9",
    )
    assert row.subject_user_id == user.id


# --- "Apagar esta noite" ---


async def _full_night(db, user, event):
    night = await add_night(db, user, event)
    peak = Peak(
        id=uuid.uuid4(),
        session_id=night.id,
        timestamp=AT,
        bpm=120,
        duration_seconds=30,
        magnitude=5.0,
        rank=1,
    )
    db.add(peak)
    await db.flush()
    card = Card(
        id=uuid.uuid4(),
        user_id=user.id,
        session_id=night.id,
        peak_id=peak.id,
        card_type="solo",
        status="ready",
    )
    db.add(card)
    await db.flush()
    db.add(Share(card_id=card.id, platform="link"))
    post = EventPost(
        id=uuid.uuid4(),
        event_id=event.id,
        user_id=user.id,
        session_id=night.id,
        bpm=120,
        moment_at=AT,
    )
    db.add(post)
    await db.flush()
    db.add(EventPostReaction(post_id=post.id, user_id=user.id))
    db.add(PostReport(post_id=post.id, reporter_id=user.id, reason="other"))
    await db.flush()
    return night, card


@pytest.mark.asyncio
async def test_deleting_a_night_takes_everything_made_from_it(memdb, fake_redis):
    ana = await add_user(memdb, "Ana")
    bia = await add_user(memdb, "Bia")
    event = await add_event(memdb)
    night, card = await _full_night(memdb, ana, event)
    other = await add_night(memdb, bia, event)

    await delete_hr_session(night.id, make_request(), ana, memdb)

    assert await _count(memdb, HRSession, HRSession.id == night.id) == 0
    assert await _count(memdb, HRData, HRData.session_id == night.id) == 0
    for model in (Peak, Card, Share, EventPost, EventPostReaction, PostReport):
        assert await _count(memdb, model) == 0, model.__tablename__
    assert f"card:image:{card.id}" in fake_redis.deleted
    assert f"card:image:{card.id}:og" in fake_redis.deleted
    # Somebody else's night at the same event is untouched.
    assert await _count(memdb, HRData, HRData.session_id == other.id) == 20


@pytest.mark.asyncio
async def test_somebody_elses_night_is_a_404(memdb, fake_redis):
    ana = await add_user(memdb, "Ana")
    bia = await add_user(memdb, "Bia")
    night = await add_night(memdb, bia)
    with pytest.raises(HTTPException) as refused:
        await delete_hr_session(night.id, make_request(), ana, memdb)
    assert refused.value.status_code == 404
    assert await _count(memdb, HRSession) == 1


# --- the crowd counts only consenting, measured nights ---


async def _crowd_night(db, name, event, consent=True, source="Polar H10"):
    user = await add_user(db, name)
    if consent:
        await grant(db, user, "crowd_stats")
    night = await add_night(db, user, event, source=source, readings=1)
    db.add(
        Peak(
            session_id=night.id,
            timestamp=AT,
            bpm=130,
            duration_seconds=20,
            magnitude=4.0,
            rank=1,
        )
    )
    await db.flush()
    return user


@pytest.mark.asyncio
async def test_the_crowd_counts_only_consenting_measured_nights(memdb, monkeypatch):
    event = await add_event(memdb)
    viewer = await _crowd_night(memdb, "Ana", event)
    await _crowd_night(memdb, "Bia", event)
    await _crowd_night(memdb, "Cris", event)
    await _crowd_night(memdb, "Duda", event, consent=False)
    await _crowd_night(memdb, "Edu", event, source="Tumtum Demo (simulado)")

    monkeypatch.setattr(settings, "crowd_min_nights", 3)
    crowd = await get_crowd(event.id, viewer, memdb)
    assert crowd.enough is True
    assert crowd.measured_nights == 3

    monkeypatch.setattr(settings, "crowd_min_nights", 4)
    crowd = await get_crowd(event.id, viewer, memdb)
    assert crowd.enough is False
    assert crowd.measured_nights is None  # how few is not published either
    assert crowd.moments == [] and crowd.top is None


@pytest.mark.asyncio
async def test_a_simulated_night_is_not_proof_of_having_been_there(memdb):
    event = await add_event(memdb)
    user = await add_user(memdb)
    await add_night(memdb, user, event, source="Tumtum Demo (simulado)")
    assert await was_there(memdb, user.id, event.id) is False
    await add_night(memdb, user, event)
    assert await was_there(memdb, user.id, event.id) is True


# --- the pilot wipe ---


@pytest.mark.asyncio
async def test_the_pilot_wipe_counts_first_and_deletes_only_with_yes(
    memdb, fake_redis, monkeypatch
):
    import importlib.util
    from pathlib import Path

    from app.core import database

    spec = importlib.util.spec_from_file_location(
        "purge_event_data",
        Path(__file__).parent.parent / "scripts" / "purge_event_data.py",
    )
    script = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(script)
    monkeypatch.setattr(database, "async_session", memdb.maker)

    event = await add_event(memdb)
    other_event = await add_event(memdb, name="Outro")
    ana = await add_user(memdb, "Ana")
    _night, card = await _full_night(memdb, ana, event)
    elsewhere = await add_night(memdb, ana, other_event)
    event_id, card_id, elsewhere_id = event.id, card.id, elsewhere.id
    await memdb.commit()

    counts = await script.purge(event_id, really=False)
    assert (counts["nights"], counts["readings"], counts["cards"], counts["posts"]) == (
        1,
        20,
        1,
        1,
    )
    assert await _count(memdb, HRSession) == 2  # nothing deleted yet

    await script.purge(event_id, really=True)
    memdb.expire_all()
    assert await _count(memdb, HRSession, HRSession.event_id == event_id) == 0
    assert await _count(memdb, EventPost) == 0
    assert await _count(memdb, HRSession, HRSession.id == elsewhere_id) == 1
    assert f"card:image:{card_id}" in fake_redis.deleted
