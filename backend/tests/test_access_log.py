"""Who read whose data, and every API request (AL-8)."""

import uuid

import pytest
from fastapi import FastAPI
from httpx import ASGITransport, AsyncClient
from sqlalchemy import select

from app.core import database
from app.core.auth import create_access_token
from app.main import AccessLogMiddleware
from app.models.privacy import AccessLog, DataAccessLog
from app.services.access_log import ip_of, record_access
from tests.conftest import add_user, make_request


def test_the_ip_is_the_first_forwarded_hop():
    assert ip_of(make_request("203.0.113.9, 10.0.0.1")) == "203.0.113.9"
    assert ip_of(make_request(None, client=("198.51.100.4", 1))) == "198.51.100.4"
    assert ip_of(None) is None


@pytest.mark.asyncio
async def test_a_read_is_recorded_without_any_bpm(memdb):
    user = await add_user(memdb)
    night_id = uuid.uuid4()
    await record_access(
        memdb, user, user.id, "hr_session", night_id, "read", make_request()
    )
    row = (await memdb.execute(select(DataAccessLog))).scalar_one()
    assert (row.actor_user_id, row.subject_user_id) == (user.id, user.id)
    assert (row.resource, row.resource_id, row.action) == (
        "hr_session",
        str(night_id),
        "read",
    )
    assert row.ip == "203.0.113.9"
    assert not any("bpm" in column.name for column in DataAccessLog.__table__.columns)


@pytest.mark.asyncio
async def test_the_middleware_logs_api_requests_and_not_the_healthcheck(
    memdb, monkeypatch
):
    monkeypatch.setattr(database, "async_session", memdb.maker)

    tiny = FastAPI()
    tiny.add_middleware(AccessLogMiddleware)

    @tiny.get("/api/ping")
    async def ping():
        return {"ok": True}

    @tiny.get("/health")
    async def health():
        return {"status": "ok"}

    user_id = uuid.uuid4()
    token = create_access_token({"sub": str(user_id)})
    async with AsyncClient(
        transport=ASGITransport(app=tiny), base_url="http://t"
    ) as client:
        await client.get(
            "/api/ping",
            headers={
                "Authorization": f"Bearer {token}",
                "X-Forwarded-For": "203.0.113.7",
            },
        )
        await client.get("/api/missing")
        await client.get("/health")

    rows = (
        (await memdb.execute(select(AccessLog).order_by(AccessLog.at))).scalars().all()
    )
    assert [(r.path, r.status) for r in rows] == [
        ("/api/ping", 200),
        ("/api/missing", 404),
    ]
    assert rows[0].user_id == user_id and rows[0].ip == "203.0.113.7"
    assert rows[1].user_id is None


@pytest.mark.asyncio
async def test_a_logging_failure_never_costs_the_answer(monkeypatch):
    def broken():
        raise RuntimeError("database down")

    monkeypatch.setattr(database, "async_session", broken)
    tiny = FastAPI()
    tiny.add_middleware(AccessLogMiddleware)

    @tiny.get("/api/ping")
    async def ping():
        return {"ok": True}

    async with AsyncClient(
        transport=ASGITransport(app=tiny), base_url="http://t"
    ) as client:
        response = await client.get("/api/ping")
    assert response.status_code == 200
