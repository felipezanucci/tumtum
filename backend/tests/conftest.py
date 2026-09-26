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
    """Enough of redis.asyncio for the card cache: get, set, delete."""

    def __init__(self):
        self.store: dict[str, bytes] = {}
        self.deleted: list[str] = []

    async def get(self, key):
        return self.store.get(key)

    async def set(self, key, value, ex=None):
        self.store[key] = value

    async def delete(self, *keys):
        self.deleted.extend(keys)
        for key in keys:
            self.store.pop(key, None)
        return len(keys)


@pytest.fixture
def fake_redis(monkeypatch):
    from app.core import redis as redis_module

    fake = FakeRedis()
    monkeypatch.setattr(redis_module, "redis_client", fake)
    return fake


def make_request(ip_chain: str | None = "203.0.113.9, 10.0.0.1", client=None):
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
    from app.models.hr_data import HRData
    from app.models.hr_session import HRSession

    night = HRSession(
        id=uuid.uuid4(),
        user_id=user.id,
        event_id=event.id if event else None,
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
