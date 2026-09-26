"""What the server refuses to do at startup, and what it never mounts."""

import pytest

from app.config import Settings


def _settings(**kw) -> Settings:
    return Settings(_env_file=None, **kw)


@pytest.mark.parametrize(
    "key",
    [
        "your-secret-key",
        "dev-secret-key-change-in-production",
        "change-me-to-a-random-secret-key",
        "test-secret-key",
        "",
        "short-but-random-Xy7",
    ],
)
def test_a_weak_secret_key_stops_the_server(key, monkeypatch):
    monkeypatch.delenv("ALLOW_WEAK_SECRET_KEY", raising=False)
    assert _settings(secret_key=key, allow_weak_secret_key=False).secret_key_problem()


def test_a_long_random_key_starts():
    assert (
        _settings(
            secret_key="k" * 16 + "Zq93-xY_" * 3, allow_weak_secret_key=False
        ).secret_key_problem()
        is None
    )


def test_the_local_stack_may_say_the_key_is_weak_on_purpose():
    assert (
        _settings(
            secret_key="test-secret-key", allow_weak_secret_key=True
        ).secret_key_problem()
        is None
    )


@pytest.mark.asyncio
async def test_the_lifespan_refuses_to_start(monkeypatch):
    from app import main
    from app.config import settings

    monkeypatch.setattr(settings, "secret_key", "your-secret-key")
    monkeypatch.setattr(settings, "allow_weak_secret_key", False)
    with pytest.raises(RuntimeError, match="Refusing to start"):
        async with main.lifespan(main.app):
            pass


def test_the_demo_router_is_absent_in_production():
    from fastapi import FastAPI

    from app.main import include_routers

    def paths(env):
        target = FastAPI()
        include_routers(target, env)
        return {getattr(r, "path", "") for r in target.routes}

    assert not any(p.startswith("/api/demo") for p in paths("production"))
    assert "/api/demo/simulate/{event_id}" in paths("development")


def test_simulating_a_night_is_the_operators():
    from tests.test_operator_only_routes import guards

    assert "require_admin" in (guards("POST", "/api/demo/simulate/{event_id}") or set())


def test_sentry_reports_carry_no_body_cookie_or_token():
    from app.main import scrub_sentry_event

    event = {
        "request": {
            "url": "https://api.tumtum.cc/api/health/sessions",
            "data": {"data_points": [{"bpm": 187}]},
            "cookies": {"s": "1"},
            "headers": {"Authorization": "Bearer abc", "User-Agent": "okhttp"},
        }
    }
    scrubbed = scrub_sentry_event(event)["request"]
    assert "data" not in scrubbed and "cookies" not in scrubbed
    assert scrubbed["headers"] == {"User-Agent": "okhttp"}


def test_the_schema_catch_up_only_ever_adds():
    from app.core.schema_catchup import statements

    added = statements()
    assert added and all("ADD COLUMN IF NOT EXISTS" in s for s in added)
    assert not any("DROP" in s for s in added)
    joined = " ".join(added)
    for column in (
        "users ADD COLUMN IF NOT EXISTS birth_date",
        "hr_sessions ADD COLUMN IF NOT EXISTS analyzed_at",
        "cards ADD COLUMN IF NOT EXISTS published_at",
    ):
        assert column in joined


def test_wearable_connections_no_longer_hold_provider_tokens():
    from app.models.wearable_connection import WearableConnection
    from app.schemas.health import WearableConnectRequest

    assert "access_token" not in WearableConnection.__table__.columns
    assert "refresh_token" not in WearableConnection.__table__.columns
    assert "access_token" not in WearableConnectRequest.model_fields
