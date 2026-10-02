"""The person's consents, one per purpose (LGPD audit, CR-1).

`GET` is where every client learns what it may do; `PUT` is the only way a
consent changes. Both answer with the full picture — all seven purposes —
so a screen never has to merge a partial answer into what it remembers.
"""

from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import client_of, get_current_user
from app.core.database import get_db
from app.models.user import User
from app.schemas.consent import ConsentEntry, ConsentsResponse, ConsentsUpdate
from app.services import consents

router = APIRouter(prefix="/api/consents", tags=["consents"])

TERMS_NOT_A_SWITCH = "Os Termos não se desligam. Pra retirar o aceite, apague a conta."


async def _response(db: AsyncSession, user: User) -> ConsentsResponse:
    return ConsentsResponse(
        text_version=consents.CONSENT_TEXT_VERSION,
        consents=[ConsentEntry.of(s) for s in await consents.snapshot(db, user.id)],
    )


@router.get("", response_model=ConsentsResponse)
async def get_consents(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    return await _response(db, user)


@router.put("", response_model=ConsentsResponse)
async def put_consents(
    body: ConsentsUpdate,
    request: Request,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Grant or revoke the purposes in the body, and only those.

    The text version is the one the person was shown, recorded as sent: a
    grant proves consent to the words on that screen, not to whatever this
    server holds today.
    """
    # The Terms are a contract, not a consent (services/consents.py, the
    # legal bases): they are withdrawn by deleting the account, and the site
    # never offered the switch. The app did, until 02/10.
    if body.purposes.get("terms") is False:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, detail=TERMS_NOT_A_SWITCH
        )
    await consents.set_many(
        db,
        user.id,
        body.purposes,
        text_version=body.text_version,
        means=body.means,
        client=client_of(request),
    )
    return await _response(db, user)
