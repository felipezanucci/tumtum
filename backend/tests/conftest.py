"""Shared pieces for the LGPD-remediation tests (26/09).

The older test files each build their own in-memory database; these give the
new ones the same thing once — SQLite through aiosqlite, with the two
Postgres types compiled to what SQLite has — plus the fakes they share: a
Redis that remembers, a request with a forwarded address, and factories for
the rows a night is made of.
"""

import uuid
from datetime import UTC, date, datetime, timedelta

import pytest
import pytest_asyncio
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.ext.asyncio import async_sessionmaker, create_async_engine
from sqlalchemy.ext.compiler import compiles
from starlette.requests import Request

import app.main  # noqa: F401 — registers every model
from app.core.database import Base


@compiles(UUID, "sqlite")
def _uuid_on_sqlite(_type, _compiler, **_kw):
    return "CHAR(32)"


@compiles(JSONB, "sqlite")
def _jsonb_on_sqlite(_type, _compiler, **_kw):
    return "TEXT"


@pytest_asyncio.fixture
async def memdb():
    engine = create_async_engine("sqlite+aiosqlite://")
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    maker = async_sessionmaker(engine, expire_on_commit=False)
    async with maker() as session:
        session.maker = maker  # for code that opens its own session
        yield session
    await engine.dispose()


class FakeRedis:
    """Enough of redis.asyncio for the card cache and the rate limiter:
    get, set, delete, scan_iter, incr, expire."""

    def __init__(self):
        self.store: dict[str, bytes] = {}
        self.deleted: list[str] = []
        self.expiries: dict[str, int] = {}

    async def get(self, key):
        return self.store.get(key)

    async def set(self, key, value, ex=None):
        self.store[key] = value

    async def delete(self, *keys):
        self.deleted.extend(keys)
        for key in keys:
            self.store.pop(key, None)
        return len(keys)

    async def scan_iter(self, match=None):
        import fnmatch

        for key in list(self.store):
            if match is None or fnmatch.fnmatchcase(key, match):
                yield key

    async def incr(self, key):
        self.store[key] = int(self.store.get(key, 0)) + 1
        return self.store[key]

    async def expire(self, key, seconds):
        self.expiries[key] = seconds
        return True


class BrokenRedis:
    """A Redis that cannot be reached: every call raises."""

    def __getattr__(self, name):
        async def refuse(*_args, **_kwargs):
            raise ConnectionError("redis is down")

        return refuse


@pytest.fixture(autouse=True)
def no_rate_limit(monkeypatch):
    """Rate limits off in every test but the ones that switch them back on.

    Otherwise the count runs across tests — every login in the suite comes
    from the same test address — and CI's real Redis would carry it across
    files. `tests/test_rate_limit.py` turns it on, on a clean count.
    """
    from app.config import settings
    from app.core import ratelimit

    monkeypatch.setattr(settings, "rate_limit_enabled", False)
    ratelimit.reset()
    yield
    ratelimit.reset()


@pytest.fixture
def fake_redis(monkeypatch):
    from app.core import redis as redis_module

    fake = FakeRedis()
    # One fake behind both clients: the text one (rate limits) and the bytes
    # one (card images) talk to the same Redis in production too.
    monkeypatch.setattr(redis_module, "redis_client", fake)
    monkeypatch.setattr(redis_module, "redis_bytes", fake)
    return fake


def make_request(ip_chain: str | None = "198.51.100.66, 203.0.113.9", client=None):
    """A request as Railway hands it over: whatever the client wrote in
    `X-Forwarded-For`, then the address Railway's edge appended — the real
    one, 203.0.113.9 (services/access_log.py, `ip_of`)."""
    headers = []
    if ip_chain:
        headers.append((b"x-forwarded-for", ip_chain.encode()))
    return Request(
        {
            "type": "http",
            "method": "GET",
            "path": "/",
            "headers": headers,
            "client": client or ("10.0.0.1", 5000),
            "query_string": b"",
        }
    )


@pytest.fixture
def request_():
    return make_request()


AT = datetime(2026, 10, 3, 1, 0, tzinfo=UTC)  # 22h00 in São Paulo, 02/10


async def add_user(db, name="Ana", email=None, password=None, birth=date(1990, 5, 17)):
    from app.api.auth import hash_password
    from app.models.user import User

    user = User(
        id=uuid.uuid4(),
        email=email or f"{name.lower()}@x.cc",
        name=name,
        auth_provider="email",
        hashed_password=hash_password(password) if password else None,
        birth_date=birth,
    )
    db.add(user)
    await db.flush()
    return user


async def add_event(db, day=date(2026, 10, 2), start=None, end=None, name="Show"):
    from app.models.event import Event

    event = Event(
        id=uuid.uuid4(),
        name=name,
        city="São Paulo",
        date=day,
        start_time=start,
        end_time=end,
        event_type="concert",
    )
    db.add(event)
    await db.flush()
    return event


async def add_night(db, user, event=None, source="Polar H10", readings=20, at=AT):
    """A night of `readings` seconds from `at`, stored as the upload stores it.

    `event_readings` is counted the way `create_hr_session` counts it, so a
    night needs `settings.attendance_min_readings` (60) readings inside the
    event's window to count as having been there.
    """
    from app.models.hr_data import HRData
    from app.models.hr_session import HRSession
    from app.services.event_window import event_window

    event_readings = None
    if event is not None:
        low, high = event_window(event)
        event_readings = sum(
            1 for i in range(readings) if low <= at + timedelta(seconds=i) <= high
        )
    night = HRSession(
        id=uuid.uuid4(),
        user_id=user.id,
        event_id=event.id if event else None,
        event_readings=event_readings,
        start_time=at,
        end_time=at + timedelta(seconds=readings),
        avg_bpm=90,
        max_bpm=120,
        min_bpm=70,
        source_device=source,
    )
    db.add(night)
    await db.flush()
    db.add_all(
        HRData(time=at + timedelta(seconds=i), session_id=night.id, bpm=80 + i)
        for i in range(readings)
    )
    await db.flush()
    return night


async def grant(db, user, *purposes):
    from app.services import consents

    await consents.set_many(
        db,
        user.id,
        dict.fromkeys(purposes, True),
        text_version=consents.CONSENT_TEXT_VERSION,
        means="tap",
    )


@pytest_asyncio.fixture
async def api(memdb, monkeypatch):
    """The real app over HTTP, on the test database, signed in as anybody.

    For the rules that must hold end to end — through the router, the
    consent guard, the exception handler and the access-log middleware —
    rather than on a route function called by hand. `api(user)` gives a
    client whose requests carry that person's access token.
    """
    from fastapi import Depends
    from httpx import ASGITransport, AsyncClient

    from app.core import database
    from app.core.auth import (
        create_access_token,
        decode_access_token,
        get_current_user,
        oauth2_scheme,
    )
    from app.core.database import get_db
    from app.main import app
    from app.models.user import User

    async def the_test_db():
        yield memdb

    async def the_signed_in_user(token: str = Depends(oauth2_scheme)):
        # The real one, except that SQLite's UUID column wants a UUID where
        # asyncpg accepts the token's string.
        return await memdb.get(User, uuid.UUID(decode_access_token(token)["sub"]))

    monkeypatch.setattr(database, "async_session", memdb.maker)
    app.dependency_overrides[get_db] = the_test_db
    app.dependency_overrides[get_current_user] = the_signed_in_user
    clients = []

    def client_for(user=None):
        headers = {}
        if user is not None:
            token = create_access_token({"sub": str(user.id)})
            headers["Authorization"] = f"Bearer {token}"
        client = AsyncClient(
            transport=ASGITransport(app=app), base_url="http://t", headers=headers
        )
        clients.append(client)
        return client

    yield client_for
    for client in clients:
        await client.aclose()
    app.dependency_overrides.pop(get_db, None)
    app.dependency_overrides.pop(get_current_user, None)


def night_body(start=AT, bpms=(90, 91, 92, 93, 94)) -> dict:
    """What the app uploads for a night, as JSON."""
    return {
        "start_time": start.isoformat(),
        "end_time": (start + timedelta(minutes=30)).isoformat(),
        "source_device": "Polar H10",
        "data_points": [
            {"time": (start + timedelta(seconds=i)).isoformat(), "bpm": bpm}
            for i, bpm in enumerate(bpms)
        ],
    }
