import uuid
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import Response
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.user import User
from app.models.hr_session import HRSession
from app.models.hr_data import HRData
from app.models.peak import Peak
from app.models.card import Card, Share
from app.schemas.card import CardCreateRequest, CardResponse, ShareRequest, ShareResponse
from app.services.card_generator import generate_solo_card

router = APIRouter(prefix="/api/cards", tags=["cards"])


@router.post("", response_model=CardResponse, status_code=status.HTTP_201_CREATED)
async def create_card(
    body: CardCreateRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Generate a share card for an HR session."""
    # Fetch session
    result = await db.execute(
        select(HRSession).where(HRSession.id == body.session_id, HRSession.user_id == user.id)
    )
    session = result.scalar_one_or_none()
    if not session:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Sessão não encontrada")

    # Fetch peak if specified
    peak = None
    matched_label = None
    if body.peak_id:
        peak_result = await db.execute(
            select(Peak).where(Peak.id == body.peak_id, Peak.session_id == body.session_id)
        )
        peak = peak_result.scalar_one_or_none()

    # If no specific peak, use the top-ranked one
    if not peak:
        peak_result = await db.execute(
            select(Peak).where(Peak.session_id == body.session_id).order_by(Peak.rank).limit(1)
        )
        peak = peak_result.scalar_one_or_none()

    # Get matched label from timeline entry
    if peak and peak.timeline_entry_id:
        from app.models.event_timeline import EventTimeline
        tl_result = await db.execute(
            select(EventTimeline).where(EventTimeline.id == peak.timeline_entry_id)
        )
        tl_entry = tl_result.scalar_one_or_none()
        if tl_entry:
            matched_label = tl_entry.label

    # Fetch HR data for mini curve
    data_result = await db.execute(
        select(HRData).where(HRData.session_id == body.session_id).order_by(HRData.time)
    )
    hr_data = [{"time": d.time, "bpm": d.bpm} for d in data_result.scalars().all()]

    # Generate card image
    peak_bpm = peak.bpm if peak else (session.max_bpm or 0)
    event_name = "Evento"
    event_date = session.start_time.strftime("%d/%m/%Y")

    # Try to get event name
    if session.event_id:
        from app.models.event import Event
        event_result = await db.execute(select(Event).where(Event.id == session.event_id))
        event = event_result.scalar_one_or_none()
        if event:
            event_name = event.name
            event_date = event.date.strftime("%d/%m/%Y")

    image_bytes = generate_solo_card(
        user_name=user.name,
        event_name=event_name,
        event_date=event_date,
        peak_bpm=peak_bpm,
        avg_bpm=session.avg_bpm or 0,
        max_bpm=session.max_bpm or 0,
        matched_label=matched_label,
        hr_data=hr_data,
        format=body.format,
    )

    # In production: upload to R2/S3 and store URL
    # For MVP: store as data URL placeholder, serve via /api/cards/{id}/image
    card = Card(
        user_id=user.id,
        session_id=body.session_id,
        peak_id=peak.id if peak else None,
        card_type=body.card_type,
        status="ready",
        metadata_={
            "format": body.format,
            "peak_bpm": peak_bpm,
            "event_name": event_name,
            "matched_label": matched_label,
            "image_size": len(image_bytes),
        },
    )
    db.add(card)
    await db.flush()

    # Store image bytes in Redis for serving (MVP approach)
    from app.core.redis import redis_client
    await redis_client.set(f"card:image:{card.id}", image_bytes, ex=86400 * 7)  # 7 days TTL

    card.image_url = f"/api/cards/{card.id}/image"
    await db.flush()

    return card


@router.get("", response_model=list[CardResponse])
async def list_cards(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Card).where(Card.user_id == user.id).order_by(Card.created_at.desc())
    )
    return result.scalars().all()


@router.get("/{card_id}", response_model=CardResponse)
async def get_card(
    card_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Card).where(Card.id == card_id, Card.user_id == user.id)
    )
    card = result.scalar_one_or_none()
    if not card:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Card não encontrado")
    return card


@router.get("/{card_id}/image")
async def get_card_image(card_id: uuid.UUID):
    """Serve the card image from Redis cache."""
    from app.core.redis import redis_client
    image_bytes = await redis_client.get(f"card:image:{card_id}")
    if not image_bytes:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Imagem não encontrada ou expirada")
    return Response(content=image_bytes, media_type="image/png")


@router.delete("/{card_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_card(
    card_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Card).where(Card.id == card_id, Card.user_id == user.id)
    )
    card = result.scalar_one_or_none()
    if not card:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Card não encontrado")
    await db.delete(card)


# --- Shares ---

@router.post("/{card_id}/share", response_model=ShareResponse, status_code=status.HTTP_201_CREATED)
async def track_share(
    card_id: uuid.UUID,
    body: ShareRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Track when a card is shared on a platform."""
    result = await db.execute(
        select(Card).where(Card.id == card_id, Card.user_id == user.id)
    )
    card = result.scalar_one_or_none()
    if not card:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Card não encontrado")

    share = Share(card_id=card_id, platform=body.platform)
    db.add(share)
    await db.flush()
    return share
