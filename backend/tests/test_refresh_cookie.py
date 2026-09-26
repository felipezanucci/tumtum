"""The site's refresh token lives in an httpOnly cookie (legal opinion v1.1, §18).

A 90-day credential in `localStorage` is readable by any script on the page.
For a request that says `X-Tumtum-Client: web/...` the server now hands the
refresh token over as the `tumtum_refresh` cookie and leaves it out of the
body; the Android app keeps receiving it in the body, unchanged. These run
through the real ASGI app — routing, the injected `Response`, the cookie
headers — against the in-memory database of `conftest.py`.
"""

from http.cookies import SimpleCookie

import httpx
import pytest
import pytest_asyncio
from sqlalchemy import select

from app.api.auth import REFRESH_COOKIE
from app.core import database
from app.core.database import get_db
from app.main import app
from app.models.refresh_token import RefreshToken
from tests.conftest import add_user

WEB = {"X-Tumtum-Client": "web/site"}
ANDROID = {"X-Tumtum-Client": "android/187"}
PASSWORD = "segredo123"


@pytest_asyncio.fixture
async def client(memdb, monkeypatch):
    async def the_memdb():
        yield memdb
        await memdb.commit()

    app.dependency_overrides[get_db] = the_memdb
    # The access log opens a session of its own; point it at the same memory.
    monkeypatch.setattr(database, "async_session", memdb.maker)
    transport = httpx.ASGITransport(app=app)
    async with httpx.AsyncClient(transport=transport, base_url="https://test") as c:
        yield c
    app.dependency_overrides.pop(get_db, None)


@pytest_asyncio.fixture
async def ana(memdb):
    user = await add_user(memdb, "Ana", password=PASSWORD)
    await memdb.commit()
    return user


def _cookie(response: httpx.Response):
    """The `tumtum_refresh` Set-Cookie of a response, parsed, or None."""
    for header in response.headers.get_list("set-cookie"):
        jar = SimpleCookie()
        jar.load(header)
        if REFRESH_COOKIE in jar:
            return jar[REFRESH_COOKIE]
    return None


async def _login(client, headers):
    response = await client.post(
        "/api/auth/login",
        json={"email": "ana@x.cc", "password": PASSWORD},
        headers=headers,
    )
    # httpx keeps cookies like a browser; each test says which one it sends.
    client.cookies.clear()
    return response


async def _refresh(client, headers, cookie=None, body=None):
    headers = dict(headers)
    if cookie is not None:
        headers["Cookie"] = f"{REFRESH_COOKIE}={cookie}"
    response = await client.post("/api/auth/refresh", json=body or {}, headers=headers)
    client.cookies.clear()
    return response


@pytest.mark.asyncio
async def test_the_site_gets_the_refresh_token_as_an_httponly_cookie(client, ana):
    response = await _login(client, WEB)
    assert response.status_code == 200
    assert response.json()["access_token"]
    # Not in the body: a script on the page must never see it.
    assert response.json()["refresh_token"] is None

    cookie = _cookie(response)
    assert cookie is not None and len(cookie.value) >= 16
    assert cookie["httponly"] is True
    assert cookie["secure"] is True
    # Lax: the site reaches /api/auth through its own rewrite, so the cookie
    # is first-party; None would hand it to any other site's POST as well.
    assert cookie["samesite"].lower() == "lax"
    assert cookie["path"] == "/api/auth"
    assert int(cookie["max-age"]) == 90 * 24 * 60 * 60


@pytest.mark.asyncio
async def test_the_cookie_renews_and_rotates(client, ana):
    first = _cookie(await _login(client, WEB)).value

    renewed = await _refresh(client, WEB, cookie=first)
    assert renewed.status_code == 200
    assert renewed.json()["access_token"]
    assert renewed.json()["refresh_token"] is None
    second = _cookie(renewed).value
    assert second != first

    # And the new cookie renews in its turn.
    again = await _refresh(client, WEB, cookie=second)
    assert again.status_code == 200


@pytest.mark.asyncio
async def test_a_browser_sends_the_cookie_back_by_itself(client, ana):
    """What `credentials: 'include'` does: the jar returns it to /api/auth."""
    await client.post(
        "/api/auth/login",
        json={"email": "ana@x.cc", "password": PASSWORD},
        headers=WEB,
    )
    renewed = await client.post("/api/auth/refresh", json={}, headers=WEB)
    assert renewed.status_code == 200
    client.cookies.clear()


@pytest.mark.asyncio
async def test_no_cookie_and_no_body_is_no_session(client, ana):
    await _login(client, WEB)
    response = await _refresh(client, WEB)
    assert response.status_code == 401
    assert response.json()["detail"] == "Sua sessão terminou. Entre de novo."


@pytest.mark.asyncio
async def test_the_cookie_counts_only_with_the_site_header(client, ana):
    """The custom header forces a CORS preflight — the cookie's CSRF guard."""
    token = _cookie(await _login(client, WEB)).value
    response = await _refresh(client, {}, cookie=token)
    assert response.status_code == 401


@pytest.mark.asyncio
async def test_a_legacy_body_token_moves_into_the_cookie(client, ana):
    """A site session from before the cookie sends its token once in the body."""
    android_token = (await _login(client, ANDROID)).json()["refresh_token"]
    response = await _refresh(client, WEB, body={"refresh_token": android_token})
    assert response.status_code == 200
    assert response.json()["refresh_token"] is None
    assert _cookie(response) is not None


@pytest.mark.asyncio
async def test_android_keeps_the_body_token_and_gets_no_cookie(client, ana):
    login = await _login(client, ANDROID)
    assert login.status_code == 200
    token = login.json()["refresh_token"]
    assert token
    assert _cookie(login) is None

    renewed = await _refresh(client, ANDROID, body={"refresh_token": token})
    assert renewed.status_code == 200
    assert renewed.json()["refresh_token"] not in (None, token)
    assert _cookie(renewed) is None


@pytest.mark.asyncio
async def test_logout_clears_the_cookie_and_revokes_its_family(client, ana, memdb):
    token = _cookie(await _login(client, WEB)).value

    out = await client.post(
        "/api/auth/logout",
        json={},
        headers={**WEB, "Cookie": f"{REFRESH_COOKIE}={token}"},
    )
    assert out.status_code == 204
    cleared = _cookie(out)
    assert cleared is not None
    assert cleared.value in ("", '""')
    assert int(cleared["max-age"]) == 0
    assert cleared["path"] == "/api/auth"

    rows = (await memdb.execute(select(RefreshToken))).scalars().all()
    assert rows and all(row.revoke_reason == "logout" for row in rows)
    assert (await _refresh(client, WEB, cookie=token)).status_code == 401


@pytest.mark.asyncio
async def test_android_logout_still_takes_the_body_token(client, ana, memdb):
    token = (await _login(client, ANDROID)).json()["refresh_token"]
    out = await client.post(
        "/api/auth/logout", json={"refresh_token": token}, headers=ANDROID
    )
    assert out.status_code == 204
    assert _cookie(out) is None
    row = (await memdb.execute(select(RefreshToken))).scalar_one()
    assert row.revoke_reason == "logout"
