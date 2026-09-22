"""The operator's side of moderation: the queue of reported posts (#36).

Item 55 asked for reports to land "where a human reads them". This is that
place — `/admin/denuncias` on the site reads it — and the only two decisions
an operator makes are the two a report asks for: keep it, or take it down.
"""

import uuid
from collections import defaultdict
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import require_admin
from app.core.database import get_db
from app.models.event import Event
from app.models.event_post import EventPost
from app.models.moderation import PostReport
from app.models.user import User
from app.schemas.feed import FeedAuthor, ReportedPost, ResolveReportRequest
from app.services import moderation

router = APIRouter(prefix="/api/admin/reports", tags=["moderation"])


@router.get("", response_model=list[ReportedPost])
async def open_reports(
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Every post with a report nobody has decided on yet, oldest first."""
    rows = (
        await db.execute(
            select(PostReport, EventPost, Event, User)
            .join(EventPost, EventPost.id == PostReport.post_id)
            .join(Event, Event.id == EventPost.event_id)
            .join(User, User.id == EventPost.user_id)
            .where(PostReport.resolved_at.is_(None))
            .order_by(PostReport.created_at)
        )
    ).all()

    grouped: dict[uuid.UUID, dict] = {}
    reasons: dict[uuid.UUID, dict[str, int]] = defaultdict(lambda: defaultdict(int))
    for report, post, event, author in rows:
        reasons[post.id][report.reason] += 1
        grouped.setdefault(
            post.id,
            {
                "post": post,
                "event": event,
                "author": author,
                "first": report.created_at,
            },
        )

    out = []
    for post_id, g in grouped.items():
        count = sum(reasons[post_id].values())
        post = g["post"]
        out.append(
            ReportedPost(
                post_id=post_id,
                event_id=g["event"].id,
                event_name=g["event"].name,
                author=FeedAuthor.of(g["author"]),
                bpm=post.bpm,
                moment_at=post.moment_at,
                label=post.label,
                quote=post.quote,
                reports=count,
                reasons=dict(reasons[post_id]),
                first_reported_at=g["first"],
                hidden=moderation.hidden_by_reports(count)
                or post.deleted_at is not None,
            )
        )
    return out


@router.post("/{post_id}", status_code=status.HTTP_204_NO_CONTENT)
async def resolve(
    post_id: uuid.UUID,
    body: ResolveReportRequest,
    _: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Keep the post (it comes back if the reports had hidden it) or take it down."""
    post = (
        await db.execute(select(EventPost).where(EventPost.id == post_id))
    ).scalar_one_or_none()
    if post is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Post não encontrado"
        )
    now = datetime.now(UTC)
    if body.action == "remove" and post.deleted_at is None:
        post.deleted_at = now
    reports = (
        await db.execute(
            select(PostReport).where(
                PostReport.post_id == post_id, PostReport.resolved_at.is_(None)
            )
        )
    ).scalars()
    for report in reports:
        report.resolved_at = now
        report.resolution = "removed" if body.action == "remove" else "kept"
    await db.flush()
