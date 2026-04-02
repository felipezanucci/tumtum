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
    # Run migrations on startup
    from alembic.config import Config
    from alembic import command
    alembic_cfg = Config("alembic.ini")
    alembic_cfg.set_main_option("sqlalchemy.url", settings.database_url)
    try:
        command.upgrade(alembic_cfg, "head")
        print("Database migrations applied successfully")
    except Exception as e:
        print(f"Migration warning: {e}")
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
