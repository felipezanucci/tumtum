from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.auth import router as auth_router
from app.api.health import router as health_router
from app.api.events import router as events_router
from app.api.experience import router as experience_router

app = FastAPI(title="Tumtum API", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router)
app.include_router(health_router)
app.include_router(events_router)
app.include_router(experience_router)


@app.get("/")
async def root():
    return {"message": "Tumtum API"}


@app.get("/health")
async def healthcheck():
    return {"status": "ok"}
