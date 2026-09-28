"""The security review of 26/09, one rule per test.

CORS by exact origin, the demo only where it belongs, card formats as a
closed list, attendance proved by readings, posted moments that must come
from the night, bounded uploads, access tokens that die with a reset, and
the smaller leaks (a public profile's counts, an operator mail's markup).
"""

import uuid
from datetime import UTC, datetime, timedelta

import pytest
from fastapi import HTTPException

from app.config import settings
from app.core.auth import create_access_token, get_current_user
from app.models.hr_session import HRSession
from tests.conftest import AT, add_event, add_night, add_user, grant, night_body

# --- A. CORS ---


def test_the_origins_are_exact_and_localhost_only_off_production(monkeypatch):
    monkeypatch.setattr(
        settings, "cors_extra_origins", " https://tumtum-eight.vercel.app/ ,"
    )
    monkeypatch.setattr(settings, "environment", "production")
    assert settings.cors_origins == [
        "https://tumtum.cc",
        "https://www.tumtum.cc",
        "https://tumtum-eight.vercel.app",
    ]
    monkeypatch.setattr(settings, "environment", "development")
    assert "http://localhost:3000" in settings.cors_origins


@pytest.mark.asyncio
async def test_a_lookalike_vercel_origin_is_not_allowed(api):
    client = api()
    evil = "https://tumtum-x-felipezanuccis-projects.vercel.app.evil.com"
    for origin, allowed in ((evil, False), ("https://tumtum.cc", True)):
        answer = await client.options(
            "/api/auth/login",
            headers={
                "Origin": origin,
                "Access-Control-Request-Method": "POST",
            },
        )
        assert (answer.headers.get("access-control-allow-origin") == origin) is (
            allowed
        )


def test_the_demo_is_mounted_only_on_development_and_test():
    from fastapi import FastAPI

    from app.main import include_routers

    def has_demo(env):
        target = FastAPI()
        include_routers(target, env)
        return any(
            getattr(r, "path", "").startswith("/api/demo") for r in target.routes
        )

    assert has_demo("development") and has_demo("test")
    for env in ("production", "staging", "", "Production"):
        assert not has_demo(env)


# --- C. card images ---


@pytest.mark.asyncio
async def test_a_card_format_outside_the_list_is_a_422(memdb, api):
    user = await add_user(memdb)
    client = api(user)
    card_id = uuid.uuid4()
    image = await client.get(f"/api/cards/{card_id}/image?format=../../x")
    preview = await client.get(f"/api/cards/{card_id}/preview?format=huge")
    assert image.status_code == 422 and preview.status_code == 422
    # A listed one gets as far as the card (which does not exist).
    assert (
        await client.get(f"/api/cards/{card_id}/image?format=og")
    ).status_code == 404


@pytest.mark.asyncio
async def test_forgetting_a_card_takes_every_variant_it_ever_had(fake_redis):
    from app.services import card_cache

    card_id, other = uuid.uuid4(), uuid.uuid4()
    for key in (
        f"card:image:{card_id}",
        f"card:image:{card_id}:og",
        f"card:image:{card_id}:whatever-was-typed",
        f"card:image:{other}",
    ):
        fake_redis.store[key] = b"PNG"
    await card_cache.forget([card_id])
    assert list(fake_redis.store) == [f"card:image:{other}"]


# --- E. attendance and posts ---


def _points(n: int, start=AT) -> dict:
    return night_body(start=start, bpms=tuple(90 + i % 50 for i in range(n)))


async def _uploaded(memdb, client, event, readings: int) -> HRSession:
    body = _points(readings)
    body["event_id"] = str(event.id)
    answer = await client.post("/api/health/sessions", json=body)
    assert answer.status_code == 201, answer.text
    return await memdb.get(HRSession, uuid.UUID(answer.json()["id"]))


@pytest.mark.asyncio
async def test_a_few_readings_at_the_event_do_not_open_its_feed(memdb, api):
    event = await add_event(memdb)
    ana = await add_user(memdb, "Ana")
    await grant(memdb, ana, "read_heart_rate")
    await grant(memdb, ana, "keep_night")
    client = api(ana)

    night = await _uploaded(memdb, client, event, readings=59)
    assert night.event_readings == 59
    assert (await client.get(f"/api/events/{event.id}/feed")).status_code == 403

    night = await _uploaded(memdb, client, event, readings=60)
    assert night.event_readings == 60
    assert (await client.get(f"/api/events/{event.id}/feed")).status_code == 200


@pytest.mark.asyncio
async def test_readings_outside_the_event_window_do_not_count(memdb, api):
    event = await add_event(memdb)  # 02/10, all day, ±2 h
    ana = await add_user(memdb, "Ana")
    await grant(memdb, ana, "read_heart_rate")
    await grant(memdb, ana, "keep_night")
    # From 01h59 on 03/10 (São Paulo) for two minutes; the window of an
    # all-day event closes at 02h00, so only the first minute and one second
    # count.
    start = datetime(2026, 10, 3, 4, 59, tzinfo=UTC)
    body = night_body(start=start, bpms=tuple(90 for _ in range(120)))
    body["event_id"] = str(event.id)
    answer = await api(ana).post("/api/health/sessions", json=body)
    assert answer.status_code == 201, answer.text
    night = await memdb.get(HRSession, uuid.UUID(answer.json()["id"]))
    assert night.event_readings == 61  # 04:59:00 … 05:00:00 UTC


@pytest.mark.asyncio
async def test_a_posted_moment_must_come_from_the_night(memdb, api):
    event = await add_event(memdb)
    ana = await add_user(memdb, "Ana")
    await grant(memdb, ana, "read_heart_rate")
    await grant(memdb, ana, "keep_night")
    client = api(ana)
    night = await _uploaded(memdb, client, event, readings=60)  # 90…139 bpm

    def post(bpm, at):
        return client.post(
            f"/api/events/{event.id}/feed",
            json={"session_id": str(night.id), "bpm": bpm, "moment_at": at.isoformat()},
        )

    too_high = await post(187, AT + timedelta(seconds=10))
    after = await post(120, AT + timedelta(hours=3))
    assert too_high.status_code == after.status_code == 422
    assert too_high.json()["detail"] == "Esse momento não bate com a sua noite."
    assert (await post(120, AT + timedelta(seconds=10))).status_code == 201


# --- F. bounds ---


@pytest.mark.asyncio
async def test_a_night_must_end_after_it_starts_and_last_at_most_twelve_hours(
    memdb, api
):
    ana = await add_user(memdb, "Ana")
    await grant(memdb, ana, "read_heart_rate")
    await grant(memdb, ana, "keep_night")
    client = api(ana)

    reversed_ = night_body()
    reversed_["end_time"] = (AT - timedelta(minutes=1)).isoformat()
    answer = await client.post("/api/health/sessions", json=reversed_)
    assert answer.status_code == 422
    assert (
        answer.json()["detail"]
        == "A noite termina antes de começar. Confere o horário."
    )

    too_long = night_body()
    too_long["end_time"] = (AT + timedelta(hours=12, seconds=1)).isoformat()
    answer = await client.post("/api/health/sessions", json=too_long)
    assert answer.status_code == 422
    assert answer.json()["detail"] == "Uma noite tem no máximo 12 horas."


def test_a_night_holds_at_most_thirty_thousand_readings():
    from pydantic import ValidationError

    from app.schemas.health import HRSessionCreateRequest

    point = {"time": AT.isoformat(), "bpm": 90}
    body = {
        "start_time": AT.isoformat(),
        "end_time": (AT + timedelta(hours=1)).isoformat(),
    }
    HRSessionCreateRequest(**body, data_points=[point] * 30_000)
    with pytest.raises(ValidationError):
        HRSessionCreateRequest(**body, data_points=[point] * 30_001)


@pytest.mark.asyncio
async def test_a_body_over_four_megabytes_is_a_413(api):
    client = api()
    declared = await client.post(
        "/api/waitlist",
        content=b"x" * (4 * 1024 * 1024 + 1),
        headers={"Content-Type": "application/json"},
    )
    assert declared.status_code == 413
    assert (
        declared.json()["detail"]
        == "Esse envio é grande demais. Tenta mandar em partes menores."
    )

    async def chunks():  # no Content-Length: counted as it streams
        for _ in range(5):
            yield b"x" * (1024 * 1024)

    streamed = await client.post(
        "/api/waitlist", content=chunks(), headers={"Content-Type": "application/json"}
    )
    assert streamed.status_code == 413


@pytest.mark.asyncio
async def test_an_avatar_is_an_https_address_or_nothing(memdb, api):
    ana = await add_user(memdb, "Ana")
    client = api(ana)
    for bad in (
        "javascript:alert(1)",
        "http://t.cc/a.png",
        "https://t.cc/" + "a" * 500,
    ):
        answer = await client.patch("/api/users/me", json={"avatar_url": bad})
        assert answer.status_code == 422, bad
    good = await client.patch(
        "/api/users/me", json={"avatar_url": "https://t.cc/a.png"}
    )
    assert good.status_code == 200
    assert good.json()["avatar_url"] == "https://t.cc/a.png"


# --- G. tokens, login, profile, operator mail ---


@pytest.mark.asyncio
async def test_a_reset_ends_the_access_tokens_issued_before_it(memdb, monkeypatch):
    from app.core import auth

    ana = await add_user(memdb, "Ana")
    before = datetime.now(UTC) - timedelta(minutes=5)

    class Clock(datetime):
        @classmethod
        def now(cls, tz=None):
            return before

    monkeypatch.setattr(auth, "datetime", Clock)
    old = create_access_token({"sub": str(ana.id)})
    monkeypatch.undo()

    assert (await get_current_user(old, memdb)).id == ana.id
    auth.revoke_access_tokens(ana)
    await memdb.flush()
    with pytest.raises(HTTPException) as refused:
        await get_current_user(old, memdb)
    assert refused.value.status_code == 401
    # One signed now, as the reset signs the person back in, still works.
    fresh = create_access_token({"sub": str(ana.id)})
    assert (await get_current_user(fresh, memdb)).id == ana.id


@pytest.mark.asyncio
async def test_a_token_from_before_iat_existed_dies_with_the_first_reset(memdb):
    import jwt

    from app.core import auth

    ana = await add_user(memdb, "Ana")
    legacy = jwt.encode(
        {"sub": str(ana.id), "exp": datetime.now(UTC) + timedelta(minutes=30)},
        settings.secret_key,
        algorithm="HS256",
    )
    assert (await get_current_user(legacy, memdb)).id == ana.id
    auth.revoke_access_tokens(ana)
    with pytest.raises(HTTPException):
        await get_current_user(legacy, memdb)


@pytest.mark.asyncio
async def test_resetting_the_password_sets_the_cut_off(memdb, api):
    from app.models.password_reset_token import PasswordResetToken
    from app.services.password_reset import expiry_from, hash_token

    ana = await add_user(memdb, "Ana", password="segredo123")
    memdb.add(
        PasswordResetToken(
            user_id=ana.id,
            token_hash=hash_token("t" * 40),
            expires_at=expiry_from(datetime.now(UTC)),
        )
    )
    await memdb.flush()
    answer = await api().post(
        "/api/auth/reset-password", json={"token": "t" * 40, "password": "nova-senha"}
    )
    assert answer.status_code == 200
    assert ana.tokens_valid_after is not None
    # The token it handed back is not refused by its own reset.
    assert (await get_current_user(answer.json()["access_token"], memdb)).id == ana.id


@pytest.mark.asyncio
async def test_login_ignores_the_case_of_the_address(memdb, api):
    await add_user(memdb, "Ana", email="ana@x.cc", password="segredo123")
    answer = await api().post(
        "/api/auth/login", json={"email": "ANA@X.cc", "password": "segredo123"}
    )
    assert answer.status_code == 200
    missing = await api().post(
        "/api/auth/login", json={"email": "ninguem@x.cc", "password": "segredo123"}
    )
    assert missing.status_code == 401
    assert missing.json()["detail"] == "Email ou senha incorretos"


def test_a_password_past_bcrypts_72_bytes_still_verifies():
    from app.api.auth import hash_password, verify_password

    long_one = "ç" * 64  # 128 bytes in UTF-8
    assert verify_password(long_one, hash_password(long_one))
    assert not verify_password("x", "not-a-hash")


@pytest.mark.asyncio
async def test_a_public_profile_counts_no_nights(memdb, api):
    ana = await add_user(memdb, "Ana")
    await add_night(memdb, ana)
    client = api()
    answer = await client.get(f"/api/users/{ana.id}")
    assert answer.status_code == 200
    assert set(answer.json()) == {"name", "avatar_url", "created_at", "total_cards"}
    assert (await client.get("/api/users/not-a-uuid")).status_code == 422


@pytest.mark.asyncio
async def test_the_operator_mail_escapes_the_event_name(memdb, monkeypatch):
    from app.api import feed
    from app.models.event_post import EventPost
    from app.services import operator_mail

    monkeypatch.setattr(settings, "admin_emails", "oi@tumtum.cc")
    sent = []

    async def capture(**mail):
        sent.append(mail)

    monkeypatch.setattr(operator_mail, "send_email", capture)
    event = await add_event(memdb, name='<img src=x onerror="alert(1)">')
    ana = await add_user(memdb, "Ana")
    night = await add_night(memdb, ana, event)
    post = EventPost(
        event_id=event.id, user_id=ana.id, session_id=night.id, bpm=100, moment_at=AT
    )
    memdb.add(post)
    await memdb.flush()
    await feed._tell_operators(memdb, post)
    (mail,) = sent
    assert "<img" not in mail["html"]
    assert "&lt;img src=x" in mail["html"]


@pytest.mark.asyncio
async def test_a_card_image_is_drawn_once_and_then_served_from_the_cache(
    memdb, fake_redis, monkeypatch
):
    from app.api import cards
    from app.models.card import Card

    drawn = []

    def draw(**kwargs):
        drawn.append(kwargs["format"])
        return b"\x89PNG-bytes-not-utf8-\xff"

    monkeypatch.setattr(cards, "generate_moment_card", draw)
    ana = await add_user(memdb, "Ana")
    night = await add_night(memdb, ana)
    card = Card(
        id=uuid.uuid4(),
        user_id=ana.id,
        session_id=night.id,
        card_type="solo",
        status="ready",
        metadata_={"event_name": "Show", "peak_bpm": 150},
        published_at=datetime.now(UTC),
    )
    memdb.add(card)
    await memdb.flush()

    first = await cards.get_card_image(card.id, "og", memdb)
    second = await cards.get_card_image(card.id, "og", memdb)
    assert first.body == second.body == b"\x89PNG-bytes-not-utf8-\xff"
    assert drawn == ["og"]


def test_the_card_cache_uses_a_client_that_keeps_bytes():
    from app.core import redis as redis_module

    assert (
        redis_module.redis_bytes.connection_pool.connection_kwargs.get(
            "decode_responses"
        )
        is False
    )


@pytest.mark.asyncio
async def test_a_long_password_set_by_a_reset_still_signs_in(memdb, api):
    from app.models.password_reset_token import PasswordResetToken
    from app.services.password_reset import expiry_from, hash_token

    ana = await add_user(memdb, "Ana", password="segredo123")
    memdb.add(
        PasswordResetToken(
            user_id=ana.id,
            token_hash=hash_token("r" * 40),
            expires_at=expiry_from(datetime.now(UTC)),
        )
    )
    await memdb.flush()
    long_one = "senha-comprida-" * 8  # 120 characters, past bcrypt's 72 bytes
    client = api()
    reset = await client.post(
        "/api/auth/reset-password", json={"token": "r" * 40, "password": long_one}
    )
    assert reset.status_code == 200
    login = await client.post(
        "/api/auth/login", json={"email": "ana@x.cc", "password": long_one}
    )
    assert login.status_code == 200
    too_long = await client.post(
        "/api/auth/login", json={"email": "ana@x.cc", "password": "x" * 129}
    )
    assert too_long.status_code == 422
