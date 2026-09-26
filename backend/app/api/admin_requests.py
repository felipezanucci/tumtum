"""The operator's queue of data-subject requests (LGPD art. 18).

A request opened in the app or on the site lands here with its deadline —
15 days from opening — and the operator answers it. Reading the queue is a
read of personal data about each person in it, so it is written to
`data_access_log` like any other.
"""

import uuid
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import require_admin
from app.core.database import get_db
from app.models.privacy import DataSubjectRequest
from app.models.user import User
from app.schemas.privacy import AdminDataSubjectRequest, DataSubjectRequestUpdate
from app.services.access_log import record_access

router = APIRouter(prefix="/api/admin/requests", tags=["admin"])


def _admin_view(
    row: DataSubjectRequest, person: User | None
) -> AdminDataSubjectRequest:
    return AdminDataSubjectRequest(
        id=row.id,
        kind=row.kind,
        message=row.message,
        status=row.status,
        opened_at=row.opened_at,
        due_at=row.due_at,
        answered_at=row.answered_at,
        answer=row.answer,
        user_id=row.user_id,
        user_email=person.email if person else None,
        user_name=person.name if person else None,
    )


@router.get("", response_model=list[AdminDataSubjectRequest])
async def list_requests(
    request: Request,
    admin: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    """Every request, the open ones first and the most urgent first among them."""
    rows = (
        await db.execute(
            select(DataSubjectRequest, User)
            .join(User, User.id == DataSubjectRequest.user_id, isouter=True)
            .order_by(DataSubjectRequest.due_at)
        )
    ).all()
    rows.sort(key=lambda pair: pair[0].status != "open")
    for row, _person in rows:
        await record_access(
            db, admin, row.user_id, "data_subject_request", row.id, "read", request
        )
    return [_admin_view(row, person) for row, person in rows]


@router.patch("/{request_id}", response_model=AdminDataSubjectRequest)
async def answer_request(
    request_id: uuid.UUID,
    body: DataSubjectRequestUpdate,
    request: Request,
    admin: User = Depends(require_admin),
    db: AsyncSession = Depends(get_db),
):
    row = await db.get(DataSubjectRequest, request_id)
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Pedido não encontrado"
        )
    if body.answer is not None:
        row.answer = body.answer.strip() or None
    row.status = body.status
    # Answered is a fact with a date; reopening a request clears it, so the
    # deadline reads as still running.
    if body.status == "open":
        row.answered_at = None
    elif row.answered_at is None:
        row.answered_at = datetime.now(UTC)
        row.answered_by = admin.id
    await db.flush()
    await record_access(
        db, admin, row.user_id, "data_subject_request", row.id, "answer", request
    )
    person = await db.get(User, row.user_id)
    return _admin_view(row, person)
