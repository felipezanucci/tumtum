import uuid
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user, require_consents
from app.core.database import get_db
from app.models.event import Event
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.user import User
from app.models.wearable_connection import WearableConnection
from app.schemas.health import (
    END_BEFORE_START,
    MAX_NIGHT,
    NIGHT_TOO_LONG,
    HRSessionCreateRequest,
    HRSessionDetailResponse,
    HRSessionResponse,
    WearableConnectionResponse,
    WearableConnectRequest,
)
from app.services import data_quality
from app.services.access_log import record_access
from app.services.event_window import (
    NOT_THIS_EVENT,
    aware,
    event_window,
    night_fits,
)
from app.services.night_deletion import delete_nights

router = APIRouter(prefix="/api/health", tags=["health"])

# Keeping a night on the server is its own purpose (contract of 26/09) — and a
# night is made of readings, which needed `read_heart_rate` first (28/09: a
# night reached this endpoint from an account that had never granted it).
# Checked in that order, so the 403 names the consent the reading needed.
requires_night_consents = require_consents("read_heart_rate", "keep_night")


# --- Wearable Connections ---


@router.post(
    "/wearables",
    response_model=WearableConnectionResponse,
    status_code=status.HTTP_201_CREATED,
)
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

    connection = WearableConnection(user_id=user.id, provider=body.provider)
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
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Conexão não encontrada"
        )

    connection.status = "revoked"
    await db.flush()


# --- HR Data Ingestion ---


@router.post(
    "/sessions", response_model=HRSessionResponse, status_code=status.HTTP_201_CREATED
)
async def create_hr_session(
    body: HRSessionCreateRequest,
    user: User = Depends(requires_night_consents),
    db: AsyncSession = Depends(get_db),
):
    """Keep a night on the server — only with `read_heart_rate` and
    `keep_night` granted.

    The upload is the act the consent is for (LGPD audit, CR-2): without the
    row, the 403 names the purpose and the app opens that consent screen.
    """
    # A reversed or week-long span made the quality score and the card's
    # curve meaningless, and let one upload overlap every event of a week.
    start, end = aware(body.start_time), aware(body.end_time)
    if end <= start:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_CONTENT, detail=END_BEFORE_START
        )
    if end - start > MAX_NIGHT:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_CONTENT, detail=NIGHT_TOO_LONG
        )

    # An event id is a claim of having been there, and it opens that event's
    # feed. The night has to have happened when the event did.
    event = None
    if body.event_id is not None:
        event = await db.get(Event, body.event_id)
        if event is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado"
            )
        if not night_fits(event, body.start_time, body.end_time):
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_CONTENT,
                detail=NOT_THIS_EVENT,
            )

    # hr_data is keyed by (time, session_id), so two readings sharing a
    # timestamp would abort the whole insert. A client that rounds or replays
    # timestamps can produce those, and losing a four-hour capture to one
    # repeated instant is never the right trade: keep the first, drop the rest.
    seen: set[datetime] = set()
    unique_points = []
    for dp in body.data_points:
        if dp.time in seen:
            continue
        seen.add(dp.time)
        unique_points.append(dp)

    # Compute stats from data points
    bpm_values = [dp.bpm for dp in unique_points]
    avg_bpm = round(sum(bpm_values) / len(bpm_values)) if bpm_values else None
    max_bpm = max(bpm_values) if bpm_values else None
    min_bpm = min(bpm_values) if bpm_values else None

    # How much of the night we actually hold — continuity, not volume. The
    # reasoning, and the Realness capture that exposed the old formula, are
    # in app/services/data_quality.py.
    data_quality_score = data_quality.score(
        body.start_time, body.end_time, (dp.time for dp in unique_points)
    )

    # How much of the night was actually inside the event (`event_readings`):
    # overlapping its window is enough to be stored against it, but the feed
    # and the crowd ask for a minute of real readings there, not a claim.
    event_readings = None
    if event is not None:
        low, high = event_window(event)
        event_readings = sum(1 for dp in unique_points if low <= aware(dp.time) <= high)

    session = HRSession(
        user_id=user.id,
        event_id=body.event_id,
        event_readings=event_readings,
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
            source=dp.source,
        )
        for dp in unique_points
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
    request: Request,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(HRSession).where(
            HRSession.id == session_id, HRSession.user_id == user.id
        )
    )
    session = result.scalar_one_or_none()
    if not session:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Sessão não encontrada"
        )

    await record_access(db, user, user, "hr_session", session_id, "read", request)
    data_result = await db.execute(
        select(HRData).where(HRData.session_id == session_id).order_by(HRData.time)
    )
    session.data_points = data_result.scalars().all()
    return session


@router.delete("/sessions/{session_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_hr_session(
    session_id: uuid.UUID,
    request: Request,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """ "Apagar esta noite": the night and everything made from it (AL-9).

    Readings, moments, cards (with their shares and cached images) and the
    feed posts made from it go; the event stays. A night that is not the
    caller's is a 404, not a 403 — whether it exists is not theirs to learn.
    """
    owned = (
        await db.execute(
            select(HRSession.id).where(
                HRSession.id == session_id, HRSession.user_id == user.id
            )
        )
    ).scalar_one_or_none()
    if owned is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Sessão não encontrada"
        )
    await delete_nights(db, [session_id])
    await record_access(db, user, user, "hr_session", session_id, "delete", request)
