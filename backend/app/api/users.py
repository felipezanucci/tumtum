from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.user import User
from app.models.hr_session import HRSession
from app.models.card import Card
from app.schemas.user import UserProfileResponse, UserUpdateRequest, PublicProfileResponse

router = APIRouter(prefix="/api/users", tags=["users"])


async def _get_user_stats(db: AsyncSession, user_id) -> dict:
    """Fetch aggregate stats for a user."""
    sessions_result = await db.execute(
        select(func.count()).select_from(HRSession).where(HRSession.user_id == user_id)
    )
    total_sessions = sessions_result.scalar() or 0

    events_result = await db.execute(
        select(func.count(func.distinct(HRSession.event_id)))
        .select_from(HRSession)
        .where(HRSession.user_id == user_id, HRSession.event_id.isnot(None))
    )
    total_events = events_result.scalar() or 0

    cards_result = await db.execute(
        select(func.count()).select_from(Card).where(Card.user_id == user_id)
    )
    total_cards = cards_result.scalar() or 0

    max_bpm_result = await db.execute(
        select(func.max(HRSession.max_bpm)).where(HRSession.user_id == user_id)
    )
    highest_bpm = max_bpm_result.scalar()

    return {
        "total_sessions": total_sessions,
        "total_events": total_events,
        "total_cards": total_cards,
        "highest_bpm": highest_bpm,
    }


@router.get("/me", response_model=UserProfileResponse)
async def get_profile(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    stats = await _get_user_stats(db, user.id)
    return UserProfileResponse(
        id=user.id,
        email=user.email,
        name=user.name,
        avatar_url=user.avatar_url,
        auth_provider=user.auth_provider,
        created_at=user.created_at,
        **stats,
    )


@router.patch("/me", response_model=UserProfileResponse)
async def update_profile(
    body: UserUpdateRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    if body.name is not None:
        user.name = body.name
    if body.avatar_url is not None:
        user.avatar_url = body.avatar_url
    await db.flush()

    stats = await _get_user_stats(db, user.id)
    return UserProfileResponse(
        id=user.id,
        email=user.email,
        name=user.name,
        avatar_url=user.avatar_url,
        auth_provider=user.auth_provider,
        created_at=user.created_at,
        **stats,
    )


@router.get("/{user_id}", response_model=PublicProfileResponse)
async def get_public_profile(
    user_id: str,
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Usuário não encontrado")

    stats = await _get_user_stats(db, user.id)
    return PublicProfileResponse(
        name=user.name,
        avatar_url=user.avatar_url,
        created_at=user.created_at,
        **stats,
    )
