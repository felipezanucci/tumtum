"""One feed per event — the tour's when there is one (#65, Felipe 23/09).

Tested on 22/09 as two feeds, the rolê and the tour above it, it read as
confusing, and the feeds became one: an event in a tour opens onto the
tour's feed, every date together, with the night as a filter.

What must not change with it is who sees a post. These run the real queries
against a real (in-memory) database, because the rule lives in SQL:

- a post is seen by the people at its own night;
- by everybody at any date of the tour only if its author said so;
- never by anybody else, however they ask.
"""

import uuid
from datetime import UTC, date, datetime

import pytest
import pytest_asyncio
from fastapi import HTTPException
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.ext.asyncio import async_sessionmaker, create_async_engine
from sqlalchemy.ext.compiler import compiles

import app.main  # noqa: F401 — registers every model
from app.api.feed import (
    block_author,
    delete_post,
    get_event_feed,
    post_moment,
    toggle_reaction,
)
from app.core.database import Base
from app.models.event import Event
from app.models.event_series import EventSeries, EventSeriesMember
from app.models.hr_session import HRSession
from app.models.user import User
from app.schemas.feed import FeedPostCreate


@compiles(UUID, "sqlite")
def _uuid_on_sqlite(_type, _compiler, **_kw):
    return "CHAR(32)"


@compiles(JSONB, "sqlite")
def _jsonb_on_sqlite(_type, _compiler, **_kw):
    return "TEXT"


AT = datetime(2026, 10, 26, 22, 30, tzinfo=UTC)


@pytest_asyncio.fixture
async def db():
    engine = create_async_engine("sqlite+aiosqlite://")
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    async with async_sessionmaker(engine, expire_on_commit=False)() as session:
        yield session
    await engine.dispose()


async def _person(db, name: str) -> User:
    user = User(id=uuid.uuid4(), email=f"{name}@x.cc", name=name, auth_provider="email")
    db.add(user)
    await db.flush()
    return user


async def _event(db, name: str, day: int, series: EventSeries | None = None) -> Event:
    event = Event(
        id=uuid.uuid4(),
        name=name,
        city="São Paulo",
        date=date(2026, 10, day),
        event_type="concert",
    )
    db.add(event)
    await db.flush()
    if series is not None:
        db.add(EventSeriesMember(event_id=event.id, series_id=series.id))
        await db.flush()
    return event


async def _night(db, user: User, event: Event) -> HRSession:
    night = HRSession(
        id=uuid.uuid4(), user_id=user.id, event_id=event.id, start_time=AT, end_time=AT
    )
    db.add(night)
    await db.flush()
    return night


async def _post(db, user, event, night, to_series: bool):
    body = FeedPostCreate(
        session_id=night.id, bpm=150, moment_at=AT, to_series=to_series
    )
    return await post_moment(event.id, body, user, db)


@pytest_asyncio.fixture
async def tour(db):
    """Two dates of one tour: Ana at São Paulo, Bia at Rio, Cris at both."""
    series = EventSeries(id=uuid.uuid4(), name="Rihanna no Brasil", kind="tour")
    db.add(series)
    await db.flush()
    t = {"db": db}
    t["sp"] = await _event(db, "Rihanna SP", 24, series)
    t["rio"] = await _event(db, "Rihanna Rio", 26, series)
    for name in ("ana", "bia", "cris", "dani"):
        t[name] = await _person(db, name.title())
    t["ana@sp"] = await _night(db, t["ana"], t["sp"])
    t["bia@rio"] = await _night(db, t["bia"], t["rio"])
    t["cris@sp"] = await _night(db, t["cris"], t["sp"])
    t["cris@rio"] = await _night(db, t["cris"], t["rio"])
    return t


async def _seen(t, who: str, door: str) -> set[uuid.UUID]:
    """The posts `who` sees in the feed opened from the event `door`."""
    feed = await get_event_feed(t[door].id, t[who], t["db"])
    return {p.id for p in feed.posts}


@pytest.mark.asyncio
async def test_a_post_shown_to_the_tour_reaches_the_other_date(tour):
    t = tour
    post = await _post(t["db"], t["ana"], t["sp"], t["ana@sp"], to_series=True)
    assert post.id in await _seen(t, "bia", "rio")


@pytest.mark.asyncio
async def test_a_post_kept_to_its_night_does_not_travel(tour):
    """The consent a post was given is the audience it keeps."""
    t = tour
    post = await _post(t["db"], t["ana"], t["sp"], t["ana@sp"], to_series=False)
    assert post.id not in await _seen(t, "bia", "rio")
    # The people of its own night still see it — through either door.
    assert post.id in await _seen(t, "cris", "sp")
    assert post.id in await _seen(t, "cris", "rio")


@pytest.mark.asyncio
async def test_the_feed_is_the_same_whichever_date_you_came_in_through(tour):
    t = tour
    await _post(t["db"], t["ana"], t["sp"], t["ana@sp"], to_series=True)
    await _post(t["db"], t["bia"], t["rio"], t["bia@rio"], to_series=True)
    assert await _seen(t, "cris", "sp") == await _seen(t, "cris", "rio")


@pytest.mark.asyncio
async def test_each_post_carries_its_own_night_for_the_filter(tour):
    t = tour
    await _post(t["db"], t["ana"], t["sp"], t["ana@sp"], to_series=True)
    feed = await get_event_feed(t["rio"].id, t["bia"], t["db"])
    assert feed.series is not None and feed.series.dates == 2
    assert [e.id for e in feed.events] == [t["sp"].id, t["rio"].id]
    assert feed.posts[0].event_id == t["sp"].id
    assert feed.posts[0].event_date == t["sp"].date


@pytest.mark.asyncio
async def test_nobody_without_a_night_gets_in(tour):
    t = tour
    with pytest.raises(HTTPException) as refused:
        from app.api.feed import require_attendance

        await require_attendance(t["sp"].id, t["dani"], t["db"])
    assert refused.value.status_code == 403
    assert "rolê" not in refused.value.detail


@pytest.mark.asyncio
async def test_senti_reaches_a_post_from_the_other_date(tour):
    t = tour
    post = await _post(t["db"], t["ana"], t["sp"], t["ana@sp"], to_series=True)
    felt = await toggle_reaction(t["rio"].id, post.id, t["bia"], t["db"])
    assert felt.reactions == 1 and felt.reacted_by_me
    assert felt.event_id == t["sp"].id


@pytest.mark.asyncio
async def test_a_tap_on_a_post_you_cannot_see_is_refused_as_if_it_did_not_exist(tour):
    t = tour
    post = await _post(t["db"], t["ana"], t["sp"], t["ana@sp"], to_series=False)
    with pytest.raises(HTTPException) as refused:
        await toggle_reaction(t["rio"].id, post.id, t["bia"], t["db"])
    assert refused.value.status_code == 404


@pytest.mark.asyncio
async def test_the_author_takes_it_down_through_either_door(tour):
    t = tour
    post = await _post(t["db"], t["cris"], t["sp"], t["cris@sp"], to_series=True)
    await delete_post(t["rio"].id, post.id, t["cris"], t["db"])
    assert post.id not in await _seen(t, "ana", "sp")


@pytest.mark.asyncio
async def test_an_empty_feed_knows_a_block_emptied_it(tour):
    """#63: "Ninguém mostrou nada ainda" was false — somebody had."""
    t = tour
    post = await _post(t["db"], t["ana"], t["sp"], t["ana@sp"], to_series=True)
    await block_author(t["rio"].id, post.id, t["bia"], t["db"])
    feed = await get_event_feed(t["rio"].id, t["bia"], t["db"])
    assert feed.posts == []
    assert feed.hidden_by_block == 1
    # And it hides both ways: Ana no longer sees Bia either.
    theirs = await _post(t["db"], t["bia"], t["rio"], t["bia@rio"], to_series=True)
    assert theirs.id not in await _seen(t, "ana", "sp")


@pytest.mark.asyncio
async def test_an_event_outside_any_tour_is_its_own_feed(db):
    show = await _event(db, "Show avulso", 10)
    ana, bia = await _person(db, "Ana"), await _person(db, "Bia")
    ana_night = await _night(db, ana, show)
    await _night(db, bia, show)
    post = await _post(db, ana, show, ana_night, to_series=True)
    feed = await get_event_feed(show.id, bia, db)
    assert feed.series is None
    assert [e.id for e in feed.events] == [show.id]
    assert [p.id for p in feed.posts] == [post.id]
