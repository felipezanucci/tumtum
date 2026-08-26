import traceback
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware

from app.api.auth import router as auth_router
from app.api.cards import router as cards_router
from app.api.demo import router as demo_router
from app.api.events import router as events_router
from app.api.experience import router as experience_router
from app.api.health import router as health_router
from app.api.users import router as users_router
from app.api.waitlist import router as waitlist_router
from app.config import settings

# Sentry error tracking
if settings.sentry_dsn:
    import sentry_sdk

    sentry_sdk.init(
        dsn=settings.sentry_dsn,
        environment=settings.environment,
        traces_sample_rate=0.2,
    )


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Run migrations on startup using SQLAlchemy directly
    from app.core.database import Base, engine
    from app.models.card import Card, Share  # noqa: F401
    from app.models.event import Event  # noqa: F401
    from app.models.event_timeline import EventTimeline  # noqa: F401
    from app.models.hr_data import HRData  # noqa: F401
    from app.models.hr_session import HRSession  # noqa: F401
    from app.models.password_reset_token import (  # noqa: F401
        PasswordResetToken,
    )
    from app.models.peak import Peak  # noqa: F401

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
    yield


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


# Added before CORSMiddleware so CORS ends up outermost: Starlette treats the
# most recently added middleware as the outer one.
app.add_middleware(CatchUnhandledErrors)

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
)

app.include_router(auth_router)
app.include_router(health_router)
app.include_router(events_router)
app.include_router(experience_router)
app.include_router(cards_router)
app.include_router(users_router)
app.include_router(demo_router)
app.include_router(waitlist_router)


@app.get("/")
async def root():
    return {"message": "Tumtum API"}


@app.get("/health")
async def healthcheck():
    return {"status": "ok"}
