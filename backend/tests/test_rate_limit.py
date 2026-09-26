"""How often one address, one e-mail or one account may knock (26/09).

Through the real app — routing, the dependency, the 429 and its
`Retry-After` — with the count in a fake Redis, or in memory when Redis
cannot be reached. The autouse fixture in `conftest.py` turns limits off for
every other test; each test here turns them back on.
"""

import pytest

from app.config import settings
from app.core import ratelimit
from app.core import redis as redis_module
from tests.conftest import BrokenRedis, add_user

PASSWORD = "segredo123"


def _from(ip: str) -> dict:
    # One entry: what Railway's edge appended (trusted_proxy_hops = 1).
    return {"X-Forwarded-For": ip}


async def _login(client, email, password, ip):
    return await client.post(
        "/api/auth/login",
        json={"email": email, "password": password},
        headers=_from(ip),
    )


@pytest.fixture
def limits_on(monkeypatch):
    import bcrypt

    from app.api import auth

    # Dozens of logins per test: bcrypt at its cheapest cost, for speed only.
    cheap = bcrypt.gensalt(4)
    monkeypatch.setattr(bcrypt, "gensalt", lambda *_a, **_k: cheap)
    monkeypatch.setattr(auth, "_DUMMY_HASH", auth.hash_password("dummy"))
    monkeypatch.setattr(settings, "rate_limit_enabled", True)
    monkeypatch.setattr(settings, "rate_limit_multiplier", 1.0)
    monkeypatch.setattr(settings, "trusted_proxy_hops", 1)
    ratelimit.reset()


@pytest.mark.asyncio
async def test_the_eleventh_login_in_the_window_is_refused(
    memdb, api, fake_redis, limits_on
):
    await add_user(memdb, "Ana", password=PASSWORD)
    client = api()
    for _ in range(10):
        wrong = await _login(client, "ana@x.cc", "errada", "203.0.113.9")
        assert wrong.status_code == 401

    # Even the right password: the count is of attempts, not of failures.
    eleventh = await _login(client, "ana@x.cc", PASSWORD, "203.0.113.9")
    assert eleventh.status_code == 429
    assert eleventh.json()["detail"] == ratelimit.TOO_MANY
    assert 1 <= int(eleventh.headers["retry-after"]) <= 900

    # Another address is its own count…
    elsewhere = await _login(client, "ana@x.cc", PASSWORD, "198.51.100.4")
    assert elsewhere.status_code == 200
    # …and so is another account from the same address.
    other = await _login(client, "bia@x.cc", "errada", "203.0.113.9")
    assert other.status_code == 401


@pytest.mark.asyncio
async def test_no_email_reaches_redis(memdb, api, fake_redis, limits_on):
    """A limit per e-mail must not leave the e-mails in the cache."""
    client = api()
    await _login(client, "ana@x.cc", "errada", "203.0.113.9")
    keys = [k for k in fake_redis.store if k.startswith("ratelimit:")]
    assert keys
    assert not any("ana" in k or "203.0.113.9" in k for k in keys)
    assert all(fake_redis.expiries[k] == 900 for k in keys)


@pytest.mark.asyncio
async def test_a_redis_outage_counts_in_memory_instead(
    memdb, api, monkeypatch, limits_on
):
    monkeypatch.setattr(redis_module, "redis_client", BrokenRedis())
    await add_user(memdb, "Ana", password=PASSWORD)
    client = api()
    first = await _login(client, "ana@x.cc", PASSWORD, "203.0.113.9")
    assert first.status_code == 200  # an outage locks nobody out
    for _ in range(9):
        await _login(client, "ana@x.cc", "errada", "203.0.113.9")
    refused = await _login(client, "ana@x.cc", PASSWORD, "203.0.113.9")
    assert refused.status_code == 429


@pytest.mark.asyncio
async def test_a_forged_forwarded_address_does_not_reset_the_count(
    memdb, api, fake_redis, limits_on
):
    """The first X-Forwarded-For entry is the client's; ours is the last."""
    client = api()
    for i in range(10):
        await _login(client, "ana@x.cc", "errada", f"10.0.0.{i}, 203.0.113.9")
    forged = await _login(client, "ana@x.cc", "errada", "10.9.9.9, 203.0.113.9")
    assert forged.status_code == 429


@pytest.mark.asyncio
async def test_reset_links_are_three_an_hour_per_address(
    memdb, api, fake_redis, limits_on
):
    client = api()
    for i in range(3):
        sent = await client.post(
            "/api/auth/forgot-password",
            json={"email": "Ana@x.cc"},
            headers=_from(f"198.51.100.{i}"),
        )
        assert sent.status_code == 200
    fourth = await client.post(
        "/api/auth/forgot-password",
        json={"email": "ana@x.cc"},  # the same inbox, whatever the case
        headers=_from("198.51.100.9"),
    )
    assert fourth.status_code == 429


@pytest.mark.asyncio
async def test_the_multiplier_scales_every_limit(
    memdb, api, fake_redis, limits_on, monkeypatch
):
    monkeypatch.setattr(settings, "rate_limit_multiplier", 2.0)
    client = api()
    for _ in range(20):
        answer = await _login(client, "ana@x.cc", "errada", "203.0.113.9")
        assert answer.status_code == 401
    assert (await _login(client, "ana@x.cc", "x", "203.0.113.9")).status_code == 429


@pytest.mark.asyncio
async def test_switched_off_it_counts_nothing(
    memdb, api, fake_redis, limits_on, monkeypatch
):
    monkeypatch.setattr(settings, "rate_limit_enabled", False)
    client = api()
    for _ in range(12):
        answer = await _login(client, "ana@x.cc", "errada", "203.0.113.9")
        assert answer.status_code == 401
    assert not any(k.startswith("ratelimit:") for k in fake_redis.store)


# --- the site: counted by what it asks about, never by Vercel's address ---

WEB = {"X-Tumtum-Client": "web/site"}


@pytest.mark.asyncio
async def test_the_site_counts_logins_by_email_whatever_the_address(
    memdb, api, fake_redis, limits_on
):
    await add_user(memdb, "Ana", password=PASSWORD)
    client = api()

    async def web_login(email, password, ip):
        return await client.post(
            "/api/auth/login",
            json={"email": email, "password": password},
            headers={**WEB, **_from(ip)},
        )

    # Ten guesses spread over ten addresses still count against the account…
    for i in range(10):
        assert (
            await web_login("ana@x.cc", "errada", f"76.76.21.{i}")
        ).status_code == 401
    refused = await web_login("Ana@x.cc", PASSWORD, "76.76.21.99")
    assert refused.status_code == 429
    # …while another person on the site, from the same Vercel address, is fine.
    assert (await web_login("bia@x.cc", "errada", "76.76.21.0")).status_code == 401
    # The app keeps its own count by address and e-mail.
    assert (
        await _login(client, "ana@x.cc", PASSWORD, "203.0.113.9")
    ).status_code == 200


@pytest.mark.asyncio
async def test_the_site_is_never_counted_by_address():
    from tests.conftest import make_request

    per_address = ratelimit.site_or_app(None, ratelimit.by_ip)
    site = make_request("76.76.21.21")
    site.scope["headers"].append((b"x-tumtum-client", b"web/site"))
    assert await per_address(site) is None
    assert await per_address(make_request("76.76.21.21")) == "app:76.76.21.21"


@pytest.mark.asyncio
async def test_the_site_counts_refreshes_by_the_token_spent(
    memdb, api, fake_redis, limits_on
):
    client = api()

    async def web_refresh(token, ip):
        return await client.post(
            "/api/auth/refresh",
            json={"refresh_token": token},
            headers={**WEB, **_from(ip)},
        )

    for i in range(60):
        assert (await web_refresh("t" * 40, f"76.76.21.{i}")).status_code == 401
    assert (await web_refresh("t" * 40, "76.76.21.200")).status_code == 429
    assert (await web_refresh("u" * 40, "76.76.21.200")).status_code == 401
    # Nothing to spend at all: counted by address, 60 an hour.
    for _ in range(60):
        empty = await client.post(
            "/api/auth/refresh", json={}, headers={**WEB, **_from("1.1.1.1")}
        )
        assert empty.status_code == 401
    empty = await client.post(
        "/api/auth/refresh", json={}, headers={**WEB, **_from("1.1.1.1")}
    )
    assert empty.status_code == 429
    # No token ever reaches Redis in the clear.
    assert not any("t" * 40 in k for k in fake_redis.store)
