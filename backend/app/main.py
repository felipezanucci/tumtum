from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.api.auth import router as auth_router
from app.api.health import router as health_router
from app.api.events import router as events_router
from app.api.experience import router as experience_router
from app.api.cards import router as cards_router
from app.api.users import router as users_router

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
    from sqlalchemy import text
    from app.core.database import engine, Base

    # Import all models so they register with Base.metadata
    from app.models.user import User  # noqa: F401
    from app.models.wearable_connection import WearableConnection  # noqa: F401
    from app.models.event import Event  # noqa: F401
    from app.models.event_timeline import EventTimeline  # noqa: F401
    from app.models.hr_session import HRSession  # noqa: F401
    from app.models.hr_data import HRData  # noqa: F401
    from app.models.peak import Peak  # noqa: F401
    from app.models.card import Card, Share  # noqa: F401

    try:
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
        print("Database tables created successfully")
    except Exception as e:
        print(f"Database setup warning: {e}")
    yield


app = FastAPI(title="Tumtum API", version="0.1.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "https://tumtum.vercel.app",
        "https://tumtum-eight.vercel.app",
    ],
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


@app.get("/")
async def root():
    return {"message": "Tumtum API"}


@app.get("/health")
async def healthcheck():
    return {"status": "ok"}
