"""How often one address, one e-mail or one account may knock (26/09).

Before this nothing stopped a script from trying ten thousand passwords on
one account, asking for a reset link every second for somebody else's
address, or making the server draw the same card image in a loop. The
security review found each of those; this is the one mechanism for all of
them.

A **fixed window**: every limit counts hits in the current slice of
`window_seconds` and forgets them when the slice ends. Cruder than a sliding
window — a burst straddling the boundary can reach twice the limit — and
enough for what it is for, which is making guessing slow, not metering.

Counted in **Redis** (`INCR` + `EXPIRE`), so every worker shares the count.
When Redis cannot be reached the count falls back to this process's memory,
said once in the log: an outage of the cache must never lock anybody out of
their account, and a per-process count is still a limit.

The keys are hashed before they reach Redis — a limit per e-mail must not
leave a list of the e-mails that tried to sign in lying in the cache.

**The site is never counted by address.** Its calls to `/api/auth/*` come
through the rewrite on tumtum.cc, so every one of them reaches this server
from Vercel's few addresses: a count per IP would be one count for every
person using the site, and the first busy hour would lock all of them out.
So for a request that says `X-Tumtum-Client: web/...` the auth routes count
by what the request is *about* — the lowercased e-mail (login, reset,
sign-up) or the hash of the refresh token it spends — and never by where it
came from. The app, which reaches Railway directly, keeps the per-address
counts (`by_ip_and_email`, `by_ip`).

The header is the client's word, and that is fine here. Claiming to be the
site only moves a caller onto the per-e-mail count, which is global — ten
guesses per quarter hour at an account however many addresses they come
from — so it is stricter for a guesser, not looser. Leaving the header out
gives exactly what any app client has had since 26/09. The cost is known and
accepted: anybody can spend an address's ten site logins and make that
person wait fifteen minutes on the site (the app is not affected); a
lockout of minutes is the price of making the password unguessable in
practice.
"""

import hashlib
import logging
import math
import time
import uuid
from collections.abc import Awaitable, Callable

from fastapi import HTTPException, Request, status

from app.config import settings
from app.services.access_log import ip_of, user_id_from_authorization

log = logging.getLogger(__name__)

TOO_MANY = "Muitas tentativas. Espera um pouco e tenta de novo."

KeyFn = Callable[[Request], Awaitable[str | None]]

# The fallback's count: key -> (hits, the instant its window ends).
_local: dict[str, tuple[int, float]] = {}
_LOCAL_MAX_KEYS = 50_000
_warned = False


def reset() -> None:
    """Forget every in-process count. For tests."""
    global _warned
    _local.clear()
    _warned = False


# --- what a limit is counted by ---


async def by_ip(request: Request) -> str | None:
    """The client's address, as `ip_of` reads it (the hop our edge wrote)."""
    return ip_of(request)


async def _email_of(request: Request) -> str | None:
    """The `email` of a JSON body, lowercased — or None.

    FastAPI has already read and cached the body by the time a dependency
    runs, so this reads nothing twice. A body that is not JSON, or has no
    e-mail, is simply not counted by e-mail; the route refuses it anyway.
    """
    try:
        body = await request.json()
    except Exception:
        return None
    email = body.get("email") if isinstance(body, dict) else None
    if not isinstance(email, str) or not email.strip():
        return None
    return email.strip().lower()


async def by_email(request: Request) -> str | None:
    return await _email_of(request)


async def by_ip_and_email(request: Request) -> str | None:
    email = await _email_of(request)
    ip = ip_of(request)
    if email is None or ip is None:
        return None
    return f"{ip}|{email}"


async def by_refresh_token(request: Request) -> str | None:
    """The refresh token being spent — the body's, else the site's cookie.

    Hashed here already, so not even the in-memory fallback holds a live
    credential. A request carrying no token at all is counted by address:
    it can do nothing but fail, and the only thing to stop is volume.
    """
    from app.api.auth import REFRESH_COOKIE

    token = None
    try:
        body = await request.json()
        if isinstance(body, dict) and isinstance(body.get("refresh_token"), str):
            token = body["refresh_token"].strip() or None
    except Exception:
        token = None
    if token is None:
        token = (request.cookies.get(REFRESH_COOKIE) or "").strip() or None
    if token is None:
        ip = ip_of(request)
        return f"ip:{ip}" if ip else None
    return "token:" + hashlib.sha256(token.encode()).hexdigest()


def site_or_app(site: KeyFn | None, app: KeyFn | None) -> KeyFn:
    """One key for the site's requests and another for everybody else's.

    `None` on either side means that kind of client is not counted by this
    limit at all. The two never share a count: the key says which it was.
    """
    from app.core.auth import is_web_client

    async def key(request: Request) -> str | None:
        web = is_web_client(request)
        chosen = site if web else app
        if chosen is None:
            return None
        found = await chosen(request)
        return None if found is None else f"{'site' if web else 'app'}:{found}"

    key.__name__ = (
        f"site_{site.__name__ if site else 'none'}"
        f"_app_{app.__name__ if app else 'none'}"
    )
    return key


async def by_user(request: Request) -> str | None:
    """The account the bearer token names, decoded without the database.

    A request with no valid token is counted by address instead: it is
    refused with a 401 by the route, but the refusal must not be free.
    """
    user_id = user_id_from_authorization(request.headers.get("authorization"))
    if isinstance(user_id, uuid.UUID):
        return f"user:{user_id}"
    ip = ip_of(request)
    return f"ip:{ip}" if ip else None


# --- the count ---


def _hashed(name: str, key: str) -> str:
    digest = hashlib.sha256(f"{name}|{key}".encode()).hexdigest()[:32]
    return f"ratelimit:{name}:{digest}"


async def _count_in_redis(key: str, window_seconds: int) -> int:
    from app.core import redis as redis_module

    client = redis_module.redis_client
    hits = await client.incr(key)
    # Every time, not only on the first hit: an EXPIRE lost after the first
    # INCR would otherwise leave a counter that never resets.
    await client.expire(key, window_seconds)
    return int(hits)


def _count_locally(key: str, window_end: float, now: float) -> int:
    if len(_local) > _LOCAL_MAX_KEYS:
        for stale in [k for k, (_, end) in _local.items() if end <= now]:
            _local.pop(stale, None)
    hits, end = _local.get(key, (0, window_end))
    if end <= now:
        hits, end = 0, window_end
    hits += 1
    _local[key] = (hits, end)
    return hits


async def hit(name: str, key: str, max_hits: int, window_seconds: int) -> None:
    """Count one hit against `key`; raise the 429 once it is over `max_hits`."""
    global _warned
    now = time.time()
    window = int(now // window_seconds)
    window_end = (window + 1) * window_seconds
    full_key = f"{_hashed(name, key)}:{window}"
    try:
        hits = await _count_in_redis(full_key, window_seconds)
    except Exception as error:
        if not _warned:
            _warned = True
            log.warning("rate limit: Redis unavailable, counting in memory: %s", error)
        hits = _count_locally(full_key, window_end, now)

    allowed = max(1, math.ceil(max_hits * settings.rate_limit_multiplier))
    if hits > allowed:
        retry_after = max(1, math.ceil(window_end - now))
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail=TOO_MANY,
            headers={"Retry-After": str(retry_after)},
        )


def limit(key_fn: KeyFn, max_hits: int, window_seconds: int, name: str | None = None):
    """A FastAPI dependency: at most `max_hits` per `window_seconds` per key.

    `key_fn` reads the key from the request (`by_ip`, `by_email`,
    `by_ip_and_email`, `by_user`); a request it finds no key in is not
    counted by this limit. `name` separates two limits that share a key
    function on the same route; by default it is the key function's.
    """
    label = name or key_fn.__name__

    async def dependency(request: Request) -> None:
        if not settings.rate_limit_enabled:
            return
        key = await key_fn(request)
        if key is None:
            return
        # The route's template, not the URL: `/api/cards/{card_id}/image` is
        # one limit per address, not one per card.
        route = getattr(request.scope.get("route"), "path", request.url.path)
        await hit(f"{route}:{label}", key, max_hits, window_seconds)

    dependency.__name__ = f"rate_limit_{label}_{max_hits}_per_{window_seconds}s"
    return dependency
