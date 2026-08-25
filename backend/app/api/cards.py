import uuid

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import Response
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.card import Card, Share
from app.models.hr_session import HRSession
from app.models.peak import Peak
from app.models.user import User
from app.schemas.card import (
    CardCreateRequest,
    CardResponse,
    PublicCardResponse,
    ShareRequest,
    ShareResponse,
)
from app.services.card_generator import generate_moment_card

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
        select(HRSession).where(
            HRSession.id == body.session_id, HRSession.user_id == user.id
        )
    )
    session = result.scalar_one_or_none()
    if not session:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Sessão não encontrada"
        )

    # Fetch peak if specified
    peak = None
    matched_label = None
    if body.peak_id:
        peak_result = await db.execute(
            select(Peak).where(
                Peak.id == body.peak_id, Peak.session_id == body.session_id
            )
        )
        peak = peak_result.scalar_one_or_none()

    # If no specific peak, use the top-ranked one
    if not peak:
        peak_result = await db.execute(
            select(Peak)
            .where(Peak.session_id == body.session_id)
            .order_by(Peak.rank)
            .limit(1)
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

    # Card 01 carries no chart, so the session's readings are not loaded here.
    # They were: every point of a four-hour capture, fetched to be discarded.
    # Card 03 ("Minha noite") will need them, and can fetch them then.

    # Generate card image
    peak_bpm = peak.bpm if peak else (session.max_bpm or 100)
    event_name = "Evento"
    event_date = session.start_time.strftime("%d/%m/%Y")

    # Try to get event name
    if session.event_id:
        from app.models.event import Event

        event_result = await db.execute(
            select(Event).where(Event.id == session.event_id)
        )
        event = event_result.scalar_one_or_none()
        if event:
            event_name = event.name
            event_date = event.date.strftime("%d/%m/%Y")

    moment_time = peak.timestamp.strftime("%Hh%M") if peak else None

    try:
        image_bytes = generate_moment_card(
            user_name=user.name,
            event_name=event_name,
            event_date=event_date,
            peak_bpm=peak_bpm,
            moment_label=matched_label,
            moment_time=moment_time,
            format=body.format,
        )
    except Exception as e:
        import traceback

        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Erro ao gerar card: {e!s}") from e

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
            "avg_bpm": session.avg_bpm or 0,
            "max_bpm": session.max_bpm or 0,
            "event_name": event_name,
            "event_date": event_date,
            "matched_label": matched_label,
            "moment_time": moment_time,
            "user_name": user.name,
            "image_size": len(image_bytes),
        },
    )
    db.add(card)
    await db.flush()

    # Store image bytes in Redis for serving (MVP approach)
    try:
        from app.core.redis import redis_client

        await redis_client.set(
            f"card:image:{card.id}", image_bytes, ex=86400 * 7
        )  # 7 days TTL
        card.image_url = f"/api/cards/{card.id}/image"
        await db.flush()
    except Exception as e:
        print(f"Redis cache warning (card image): {e}")

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
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Card não encontrado"
        )
    return card


@router.get("/{card_id}/public", response_model=PublicCardResponse)
async def get_public_card(card_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    """Read a shared card without signing in.

    Sharing a card is an explicit act, and the link carries an unguessable id.
    This returns strictly what the image already shows to whoever opens it —
    no owner id, no session, no other reading. Anything beyond that would leak
    health data the person did not choose to publish.
    """
    result = await db.execute(select(Card).where(Card.id == card_id))
    card = result.scalar_one_or_none()
    if not card:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Card não encontrado"
        )
    meta = card.metadata_ or {}
    return PublicCardResponse(
        id=card.id,
        event_name=meta.get("event_name", "Evento"),
        event_date=meta.get("event_date", ""),
        peak_bpm=meta.get("peak_bpm", 0),
        moment_label=meta.get("matched_label"),
        moment_time=meta.get("moment_time"),
        user_name=meta.get("user_name", "alguém"),
    )


@router.get("/{card_id}/image")
async def get_card_image(
    card_id: uuid.UUID,
    format: str | None = None,
    db: AsyncSession = Depends(get_db),
):
    """Serve the card image, public so a shared link can render a preview.

    `format=og` returns the landscape variant built for link previews. The
    stored 9:16 card is what people post; a preview slot crops it, so the two
    are cached separately rather than one standing in for the other.
    """
    cache_key = f"card:image:{card_id}" + (f":{format}" if format else "")
    try:
        from app.core.redis import redis_client

        image_bytes = await redis_client.get(cache_key)
        if image_bytes:
            return Response(content=image_bytes, media_type="image/png")
    except Exception:
        pass

    # Regenerate the image from card metadata
    result = await db.execute(select(Card).where(Card.id == card_id))
    card = result.scalar_one_or_none()
    if not card:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Card não encontrado"
        )

    meta = card.metadata_ or {}
    image_bytes = generate_moment_card(
        user_name=meta.get("user_name", "alguém"),
        event_name=meta.get("event_name", "Evento"),
        event_date=meta.get("event_date", ""),
        peak_bpm=meta.get("peak_bpm", 100),
        moment_label=meta.get("matched_label"),
        moment_time=meta.get("moment_time"),
        format=format or meta.get("format", "story"),
    )
    try:
        from app.core.redis import redis_client

        await redis_client.set(cache_key, image_bytes, ex=86400 * 7)
    except Exception:
        pass
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
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Card não encontrado"
        )
    await db.delete(card)


# --- Shares ---


@router.post(
    "/{card_id}/share",
    response_model=ShareResponse,
    status_code=status.HTTP_201_CREATED,
)
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
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Card não encontrado"
        )

    share = Share(card_id=card_id, platform=body.platform)
    db.add(share)
    await db.flush()
    return share
