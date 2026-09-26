import traceback
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware

from app.api.admin_requests import router as admin_requests_router
from app.api.auth import router as auth_router
from app.api.cards import router as cards_router
from app.api.consents import router as consents_router
from app.api.demo import router as demo_router
from app.api.events import router as events_router
from app.api.experience import router as experience_router
from app.api.feed import router as feed_router
from app.api.health import router as health_router
from app.api.moderation import router as moderation_router
from app.api.series import router as series_router
from app.api.users import router as users_router
from app.api.waitlist import router as waitlist_router
from app.config import settings
from app.core.auth import ConsentRequired


def scrub_sentry_event(event: dict, _hint: dict | None = None) -> dict:
    """Take out of an error report everything that could carry health data.

    A request body here is a night (`POST /api/health/sessions`) or a feed
    post with a bpm and a name; a cookie or an Authorization header is a
    session. None of it helps fix a bug, and all of it would sit with an
    operator the privacy policy names only as an error tracker (LGPD audit,
    AL-6). `send_default_pii=False` and `max_request_body_size="never"` should
    already keep them out; this is the belt to those braces.
    """
    request = event.get("request")
    if isinstance(request, dict):
        request.pop("data", None)
        request.pop("cookies", None)
        headers = request.get("headers")
        if isinstance(headers, dict):
            for name in list(headers):
                if name.lower() in {"authorization", "cookie"}:
                    headers.pop(name)
    return event


# Sentry error tracking
if settings.sentry_dsn:
    import sentry_sdk

    sentry_sdk.init(
        dsn=settings.sentry_dsn,
        environment=settings.environment,
        traces_sample_rate=0.2,
        send_default_pii=False,
        max_request_body_size="never",
        include_local_variables=False,
        before_send=scrub_sentry_event,
    )


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Refuse to serve with a key anybody can read (LGPD audit, CR-5). Raised,
    # not printed: a server that starts anyway is the failure this prevents.
    problem = settings.secret_key_problem()
    if problem:
        raise RuntimeError(
            f"Refusing to start: {problem}. Set SECRET_KEY to a random value of "
            "at least 32 characters (python -c 'import secrets; "
            "print(secrets.token_urlsafe(48))'), or ALLOW_WEAK_SECRET_KEY=true "
            "on a local stack."
        )

    # Run migrations on startup using SQLAlchemy directly
    from app.core.database import Base, engine
    from app.models.card import Card, Share  # noqa: F401
    from app.models.consent import Consent  # noqa: F401
    from app.models.event import Event  # noqa: F401
    from app.models.event_post import (  # noqa: F401
        EventPost,
        EventPostReaction,
    )
    from app.models.event_series import (  # noqa: F401
        EventSeries,
        EventSeriesMember,
        SeriesPost,
    )
    from app.models.event_setlist import EventSetlist  # noqa: F401
    from app.models.event_timeline import EventTimeline  # noqa: F401
    from app.models.hr_data import HRData  # noqa: F401
    from app.models.hr_session import HRSession  # noqa: F401
    from app.models.moderation import PostReport, UserBlock  # noqa: F401
    from app.models.password_reset_token import (  # noqa: F401
        PasswordResetToken,
    )
    from app.models.peak import Peak  # noqa: F401
    from app.models.privacy import (  # noqa: F401
        AccessLog,
        DataAccessLog,
        DataSubjectRequest,
        DeletionLog,
        EmailChange,
    )
    from app.models.refresh_token import RefreshToken  # noqa: F401
    from app.models.signup_code import SignupCode  # noqa: F401

    # Import all models so they register with Base.metadata
    from app.models.user import User  # noqa: F401
    from app.models.waitlist_entry import WaitlistEntry  # noqa: F401
    from app.models.wearable_connection import WearableConnection  # noqa: F401

    try:
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
        print("Database tables created successfully")
    except Exception as e:
        print(f"Database setup warning: {e}")

    # create_all never adds a column to a table that exists (open item 16),
    # and every model below now maps columns added on 26/09. Without them in
    # the database, loading any user fails. See core/schema_catchup.py.
    from app.core.schema_catchup import catch_up

    try:
        async with engine.begin() as conn:
            await catch_up(conn)
    except Exception as e:
        print(f"Schema catch-up warning: {e}")

    # The live watch of football matches (#52): one loop in this process,
    # only when there is a key to watch with.
    import asyncio

    from app.services.match_watch import watcher

    watch = (
        asyncio.create_task(watcher.run_forever())
        if settings.api_football_key and settings.environment != "test"
        else None
    )
    # Retention: raw readings, access log, spent codes and tokens — once a
    # day, in this process (services/maintenance.py).
    from app.services import maintenance

    upkeep = (
        asyncio.create_task(maintenance.run_forever())
        if settings.environment != "test"
        else None
    )
    yield
    for task in (watch, upkeep):
        if task is not None:
            task.cancel()


app = FastAPI(title="Tumtum API", version="0.1.0", lifespan=lifespan)

# Vercel gives every branch and every commit its own preview domain, so the
# fixed list cannot cover them. The pattern is scoped to this account's Vercel
# org ("-felipezanuccis-projects"), which keeps it from matching anyone else's
# deployments while letting branch previews reach the API.
VERCEL_PREVIEW_ORIGIN = (
    r"https://tumtum-[a-z0-9-]+-felipezanuccis-projects\.vercel\.app"
)


class CatchUnhandledErrors(BaseHTTPMiddleware):
    """Return unhandled errors as a normal response, from inside the CORS layer.

    Starlette's own 500 is produced outside every user middleware, so it carries
    no Access-Control-Allow-Origin. The browser then refuses the response and
    the fetch rejects, which the frontend can only report as "the server is
    unreachable" — a crash disguised as an outage. Trapping the exception here,
    inside CORSMiddleware, means the 500 travels back out through it and the
    client sees what actually went wrong.
    """

    async def dispatch(self, request: Request, call_next):
        try:
            return await call_next(request)
        except Exception:
            traceback.print_exc()
            return JSONResponse(
                status_code=500,
                content={"detail": "Erro interno do servidor."},
            )


class AccessLogMiddleware(BaseHTTPMiddleware):
    """One `access_log` row per `/api/*` request (Marco Civil, art. 15).

    Written after the response is decided, in a database session of its own
    so it never rides on — or rolls back with — the request's transaction.
    It must never cost anybody their answer: any failure to log is printed
    and swallowed. Kept 180 days; the maintenance loop purges the rest.
    """

    async def dispatch(self, request: Request, call_next):
        response = await call_next(request)
        path = request.url.path
        if path.startswith("/api/") and path != "/health":
            await self._write(request, response.status_code)
        return response

    @staticmethod
    async def _write(request: Request, status: int) -> None:
        from app.core import database
        from app.services.access_log import (
            ip_of,
            user_id_from_authorization,
            write_request,
        )

        try:
            async with database.async_session() as db:
                await write_request(
                    db,
                    method=request.method,
                    path=request.url.path,
                    status=status,
                    ip=ip_of(request),
                    user_id=user_id_from_authorization(
                        request.headers.get("authorization")
                    ),
                )
        except Exception as error:
            print(f"Access log warning: {error}")


# Added before CORSMiddleware so CORS ends up outermost: Starlette treats the
# most recently added middleware as the outer one. The access log sits
# outside the error trap, so a crash is logged with the 500 it became.
app.add_middleware(CatchUnhandledErrors)
app.add_middleware(AccessLogMiddleware)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "https://tumtum.cc",
        "https://www.tumtum.cc",
        "https://tumtum.vercel.app",
        "https://tumtum-eight.vercel.app",
    ],
    allow_origin_regex=VERCEL_PREVIEW_ORIGIN,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    # The browser may only read a download's file name if the server says so.
    expose_headers=["Content-Disposition"],
)


@app.exception_handler(ConsentRequired)
async def consent_required_handler(_request: Request, exc: ConsentRequired):
    """The 403 of a missing consent, with the purpose a client opens (contract)."""
    return JSONResponse(status_code=exc.status_code, content=exc.body())


def include_routers(target: FastAPI, environment: str) -> None:
    """Mount every router. The demo one only outside production.

    `/api/demo/*` seeds fake events and simulates nights. In production it
    is not mounted at all (LGPD audit, AL-10) — not guarded, absent — so no
    account, operator or not, can mix a synthetic night into real ones.
    """
    target.include_router(auth_router)
    target.include_router(consents_router)
    target.include_router(health_router)
    target.include_router(events_router)
    target.include_router(feed_router)
    target.include_router(moderation_router)
    target.include_router(admin_requests_router)
    target.include_router(series_router)
    target.include_router(experience_router)
    target.include_router(cards_router)
    target.include_router(users_router)
    if environment != "production":
        target.include_router(demo_router)
    target.include_router(waitlist_router)


include_routers(app, settings.environment)


@app.get("/")
async def root():
    return {"message": "Tumtum API"}


@app.get("/health")
async def healthcheck():
    return {"status": "ok"}
