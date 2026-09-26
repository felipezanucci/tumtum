""" "Ver compartilhamentos": everything that left the person's hands (v1.1 §7).

The right to know with whom the controller shared the data (LGPD art. 18,
VII) has two halves here. The operators are the same for everybody and come
from `services/operators.py`, the one list the documents cite. What the
person published is theirs alone: the cards anyone with the link can open,
the posts a feed shows (to the event, or to the whole tour when they chose
it — the `series_posts` row is that choice), and the networks they sent a
card to. A card that is not published, or a post they took down, is not
shared and does not appear: the screen answers "who can see this now".
"""

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.models.card import Card, Share
from app.models.event import Event
from app.models.event_post import EventPost, EventPostReaction
from app.models.event_series import SeriesPost
from app.models.hr_session import HRSession
from app.models.user import User
from app.schemas.privacy import (
    SharingAuthority,
    SharingFeedPost,
    SharingOperator,
    SharingPublishedCard,
    SharingResponse,
    SharingShare,
)
from app.services.operators import NEVER_SHARED_WITH, OPERATORS

ANPD = SharingAuthority(
    url="https://www.gov.br/anpd/pt-br/canais_atendimento/cidadao-titular-de-dados",
    note=(
        "Canal da ANPD para o titular de dados, quando a resposta da TumTum "
        "não resolver."
    ),
)

# What a card or post says when its event cannot be read — never an empty
# name on a screen that is meant to be exact.
UNKNOWN_EVENT = "Evento"


def public_url(card_id) -> str:
    return f"{settings.site_url.rstrip('/')}/cards/{card_id}"


async def collect(db: AsyncSession, user: User) -> SharingResponse:
    card_rows = (
        await db.execute(
            select(Card, Event.name)
            .join(HRSession, HRSession.id == Card.session_id, isouter=True)
            .join(Event, Event.id == HRSession.event_id, isouter=True)
            .where(Card.user_id == user.id, Card.published_at.is_not(None))
            .order_by(Card.published_at.desc())
        )
    ).all()
    published_cards = [
        SharingPublishedCard(
            id=card.id,
            event_name=event_name
            or (card.metadata_ or {}).get("event_name")
            or UNKNOWN_EVENT,
            published_at=card.published_at,
            public_url=public_url(card.id),
        )
        for card, event_name in card_rows
    ]

    reactions = (
        select(func.count())
        .select_from(EventPostReaction)
        .where(EventPostReaction.post_id == EventPost.id)
        .scalar_subquery()
    )
    post_rows = (
        await db.execute(
            select(EventPost, Event.name, SeriesPost.post_id, reactions)
            .join(Event, Event.id == EventPost.event_id, isouter=True)
            .join(SeriesPost, SeriesPost.post_id == EventPost.id, isouter=True)
            .where(EventPost.user_id == user.id, EventPost.deleted_at.is_(None))
            .order_by(EventPost.created_at.desc())
        )
    ).all()
    feed_posts = [
        SharingFeedPost(
            id=post.id,
            event_id=post.event_id,
            event_name=event_name or UNKNOWN_EVENT,
            created_at=post.created_at,
            audience="turnê" if in_series is not None else "evento",
            reactions=count or 0,
        )
        for post, event_name, in_series, count in post_rows
    ]

    shares = (
        await db.execute(
            select(Share)
            .join(Card, Card.id == Share.card_id)
            .where(Card.user_id == user.id)
            .order_by(Share.shared_at.desc())
        )
    ).scalars()

    return SharingResponse(
        operators=[
            SharingOperator(
                name=o.name, role=o.role, what=o.what, why=o.why, where=o.where
            )
            for o in OPERATORS
        ],
        never=list(NEVER_SHARED_WITH),
        published_cards=published_cards,
        feed_posts=feed_posts,
        shares=[
            SharingShare(card_id=s.card_id, platform=s.platform, shared_at=s.shared_at)
            for s in shares
        ],
        anpd=ANPD,
    )
