from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.user import User
from app.models.waitlist_entry import WaitlistEntry
from app.schemas.waitlist import (
    WaitlistCountResponse,
    WaitlistEntryResponse,
    WaitlistJoinRequest,
    WaitlistJoinResponse,
)
from app.services.waitlist import normalize_email

router = APIRouter(prefix="/api/waitlist", tags=["waitlist"])


@router.post("", response_model=WaitlistJoinResponse, status_code=status.HTTP_201_CREATED)
async def join_waitlist(
    body: WaitlistJoinRequest,
    db: AsyncSession = Depends(get_db),
):
    """Put an email on the list. Public — there is no account yet.

    Signing up twice is not an error. Someone who submits again wanted to be on
    the list, and they are; telling them "esse e-mail já está cadastrado" in red
    would be the app being unhelpful about a situation where nothing went wrong.
    So the repeat returns 201 with `already_joined`, and the page says something
    warmer.

    The address is normalised first — see `normalize_email` for why, and for
    what it deliberately does not do.
    """
    email = normalize_email(body.email)

    existing = await db.execute(
        select(WaitlistEntry).where(WaitlistEntry.email == email)
    )
    if existing.scalar_one_or_none() is not None:
        return WaitlistJoinResponse(email=email, already_joined=True)

    entry = WaitlistEntry(email=email, source=body.source)
    db.add(entry)
    try:
        await db.flush()
    except IntegrityError:
        # Two submissions racing each other. The unique index did its job;
        # from the person's side the outcome is the same as above.
        await db.rollback()
        return WaitlistJoinResponse(email=email, already_joined=True)

    return WaitlistJoinResponse(email=email, already_joined=False)


@router.get("/count", response_model=WaitlistCountResponse)
async def waitlist_count(db: AsyncSession = Depends(get_db)):
    """How many people are on the list. Public, and deliberately only a number.

    A count is not contact details, and it is the one figure worth showing
    without signing in.
    """
    total = await db.scalar(select(func.count()).select_from(WaitlistEntry))
    return WaitlistCountResponse(total=total or 0)


@router.get("", response_model=list[WaitlistEntryResponse])
async def list_waitlist(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """The list itself, for the people named in `waitlist_admin_emails`.

    Being signed in is not enough. These are other people's addresses, given
    for one stated purpose, and every account on the platform is not a
    reasonable audience for them. With the setting empty this endpoint is
    closed to everyone, which is the right default for a table that fills up
    before anyone remembers to configure it.
    """
    if user.email.lower() not in settings.waitlist_admins:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Esta lista não está disponível para a sua conta.",
        )

    result = await db.execute(
        select(WaitlistEntry).order_by(WaitlistEntry.created_at.desc())
    )
    return list(result.scalars().all())
