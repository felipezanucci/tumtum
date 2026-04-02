import uuid
from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.user import User
from app.models.wearable_connection import WearableConnection
from app.models.hr_session import HRSession
from app.models.hr_data import HRData
from app.schemas.health import (
    WearableConnectRequest,
    WearableConnectionResponse,
    HRSessionCreateRequest,
    HRSessionResponse,
    HRSessionDetailResponse,
    SyncRequest,
    SyncStatusResponse,
)

router = APIRouter(prefix="/api/health", tags=["health"])


# --- Wearable Connections ---

@router.post("/wearables", response_model=WearableConnectionResponse, status_code=status.HTTP_201_CREATED)
async def connect_wearable(
    body: WearableConnectRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    # Check if user already has an active connection for this provider
    result = await db.execute(
        select(WearableConnection).where(
            WearableConnection.user_id == user.id,
            WearableConnection.provider == body.provider,
            WearableConnection.status == "active",
        )
    )
    existing = result.scalar_one_or_none()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"Conexão ativa com {body.provider} já existe",
        )

    connection = WearableConnection(
        user_id=user.id,
        provider=body.provider,
        access_token=body.access_token,
        refresh_token=body.refresh_token,
    )
    db.add(connection)
    await db.flush()
    return connection


@router.get("/wearables", response_model=list[WearableConnectionResponse])
async def list_wearables(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(WearableConnection)
        .where(WearableConnection.user_id == user.id)
        .order_by(WearableConnection.created_at.desc())
    )
    return result.scalars().all()


@router.delete("/wearables/{connection_id}", status_code=status.HTTP_204_NO_CONTENT)
async def disconnect_wearable(
    connection_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(WearableConnection).where(
            WearableConnection.id == connection_id,
            WearableConnection.user_id == user.id,
        )
    )
    connection = result.scalar_one_or_none()
    if not connection:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Conexão não encontrada")

    connection.status = "revoked"
    await db.flush()


# --- HR Data Ingestion ---

@router.post("/sessions", response_model=HRSessionResponse, status_code=status.HTTP_201_CREATED)
async def create_hr_session(
    body: HRSessionCreateRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    # Compute stats from data points
    bpm_values = [dp.bpm for dp in body.data_points]
    avg_bpm = round(sum(bpm_values) / len(bpm_values)) if bpm_values else None
    max_bpm = max(bpm_values) if bpm_values else None
    min_bpm = min(bpm_values) if bpm_values else None

    # Data quality: based on coverage (points per minute) and variance
    duration_minutes = (body.end_time - body.start_time).total_seconds() / 60
    expected_points = duration_minutes * 12  # ~1 reading per 5 seconds
    coverage = min(len(bpm_values) / expected_points, 1.0) if expected_points > 0 else 0
    data_quality_score = round(coverage * 100)

    session = HRSession(
        user_id=user.id,
        event_id=body.event_id,
        start_time=body.start_time,
        end_time=body.end_time,
        avg_bpm=avg_bpm,
        max_bpm=max_bpm,
        min_bpm=min_bpm,
        data_quality_score=data_quality_score,
        source_device=body.source_device,
    )
    db.add(session)
    await db.flush()

    # Bulk insert HR data points
    data_points = [
        HRData(
            time=dp.time,
            session_id=session.id,
            bpm=dp.bpm,
            rr_interval_ms=dp.rr_interval_ms,
            motion_level=dp.motion_level,
            source=dp.source,
        )
        for dp in body.data_points
    ]
    db.add_all(data_points)
    await db.flush()

    return session


@router.get("/sessions", response_model=list[HRSessionResponse])
async def list_hr_sessions(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(HRSession)
        .where(HRSession.user_id == user.id)
        .order_by(HRSession.start_time.desc())
    )
    return result.scalars().all()


@router.get("/sessions/{session_id}", response_model=HRSessionDetailResponse)
async def get_hr_session(
    session_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(HRSession).where(HRSession.id == session_id, HRSession.user_id == user.id)
    )
    session = result.scalar_one_or_none()
    if not session:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Sessão não encontrada")

    data_result = await db.execute(
        select(HRData).where(HRData.session_id == session_id).order_by(HRData.time)
    )
    session.data_points = data_result.scalars().all()
    return session


# --- Sync ---

@router.post("/sync", response_model=SyncStatusResponse)
async def trigger_sync(
    body: SyncRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(WearableConnection).where(
            WearableConnection.id == body.connection_id,
            WearableConnection.user_id == user.id,
            WearableConnection.status == "active",
        )
    )
    connection = result.scalar_one_or_none()
    if not connection:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Conexão não encontrada ou inativa")

    # Dispatch async sync task
    from app.services.health_sync import sync_health_data

    records_synced = await sync_health_data(
        db=db,
        user_id=user.id,
        connection=connection,
        start_time=body.start_time,
        end_time=body.end_time,
    )

    connection.last_sync_at = datetime.now(timezone.utc)
    await db.flush()

    return SyncStatusResponse(
        connection_id=connection.id,
        status="completed",
        records_synced=records_synced,
        last_sync_at=connection.last_sync_at,
    )
