import traceback
import uuid
from datetime import UTC, datetime, timedelta

import httpx
from fastapi import APIRouter, Depends, HTTPException, Request, Response, status
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.card import Card
from app.models.hr_session import HRSession
from app.models.moderation import UserBlock
from app.models.privacy import DataSubjectRequest, EmailChange
from app.models.user import User
from app.schemas.feed import BlockedPerson, FeedAuthor
from app.schemas.privacy import (
    DataSubjectRequestCreate,
    DataSubjectRequestResponse,
    MyDataResponse,
)
from app.schemas.user import (
    DeleteAccountRequest,
    EmailChangeConfirm,
    EmailChangeRequest,
    EmailChangeStarted,
    PublicProfileResponse,
    UserProfileResponse,
    UserUpdateRequest,
)
from app.services import refresh_tokens, subject_data
from app.services import signup_codes as codes
from app.services.access_log import record_access
from app.services.account_deletion import delete_account
from app.services.age import UNDER_AGE, is_adult, today_local
from app.services.email import EmailNotConfigured, send_email

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


async def _profile(db: AsyncSession, user: User) -> UserProfileResponse:
    stats = await _get_user_stats(db, user.id)
    return UserProfileResponse(
        id=user.id,
        email=user.email,
        name=user.name,
        avatar_url=user.avatar_url,
        auth_provider=user.auth_provider,
        created_at=user.created_at,
        birth_date=user.birth_date,
        **stats,
    )


@router.get("/me", response_model=UserProfileResponse)
async def get_profile(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    return await _profile(db, user)


@router.patch("/me", response_model=UserProfileResponse)
async def update_profile(
    body: UserUpdateRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    # The birth date is given once. Accounts made before 26/09 have none and
    # are asked for it by the gate; after that it is fixed, because a date
    # that can be edited freely proves nothing about age.
    if body.birth_date is not None and body.birth_date != user.birth_date:
        if user.birth_date is not None:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Sua data de nascimento já está registrada.",
            )
        if not is_adult(body.birth_date, today_local()):
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail=UNDER_AGE
            )
        user.birth_date = body.birth_date
    if body.name is not None:
        user.name = body.name
    if body.avatar_url is not None:
        user.avatar_url = body.avatar_url
    await db.flush()
    return await _profile(db, user)


def _password_matches(user: User, password: str) -> bool:
    from app.api.auth import verify_password

    return bool(user.hashed_password) and verify_password(
        password, user.hashed_password
    )


WRONG_PASSWORD = "Senha incorreta."


# Delete the account and everything it owns: the privacy page's promise, kept
# by code instead of by hand (decision log, item 32). Irreversible; the app
# asks first and wipes the phone only after this answered 204.
#
# A POST with the password since 26/09 (LGPD remediation): a phone left
# unlocked on a table, or a stolen access token, must not be enough to erase
# somebody's nights forever. `DELETE /me` is gone rather than kept as a way
# around the password.
@router.post("/me/delete", status_code=status.HTTP_204_NO_CONTENT)
async def delete_profile(
    body: DeleteAccountRequest,
    request: Request,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> Response:
    if not _password_matches(user, body.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail=WRONG_PASSWORD
        )
    await delete_account(db, user)
    # No actor, no subject: the account is gone, and the one row this leaves
    # must not be a way to name who it was. `deletion_log` has the count.
    await record_access(db, None, None, "account", None, "delete", request)
    return Response(status_code=status.HTTP_204_NO_CONTENT)


# --- Data-subject rights (LGPD art. 18) ---


@router.get("/me/data", response_model=MyDataResponse)
async def my_data(
    request: Request,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Everything TumTum holds about the person, except the raw readings."""
    await record_access(db, user, user, "account_data", user.id, "read", request)
    return await subject_data.collect(db, user)


@router.get("/me/export")
async def my_export(
    request: Request,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> Response:
    """The same, with every reading, as a JSON file to keep (portability)."""
    await record_access(db, user, user, "export", user.id, "export", request)
    export = await subject_data.collect(db, user, with_readings=True)
    return Response(
        content=export.model_dump_json(),
        media_type="application/json",
        headers={"Content-Disposition": "attachment; filename=tumtum-export.json"},
    )


@router.get("/me/export.csv")
async def my_export_csv(
    request: Request,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
) -> Response:
    """Every reading as `session_id,time,bpm` — what a spreadsheet opens."""
    await record_access(db, user, user, "export", user.id, "export_csv", request)
    return Response(
        content=await subject_data.readings_csv(db, user.id),
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": "attachment; filename=tumtum-export.csv"},
    )


# The controller answers within 15 days (art. 19, II, for the complete
# statement; used for every kind, so no request waits longer than the
# longest the law allows).
REQUEST_DUE = timedelta(days=15)


@router.post(
    "/me/requests",
    response_model=DataSubjectRequestResponse,
    status_code=status.HTTP_201_CREATED,
)
async def open_request(
    body: DataSubjectRequestCreate,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    now = datetime.now(UTC)
    row = DataSubjectRequest(
        user_id=user.id,
        kind=body.kind,
        message=body.message.strip(),
        status="open",
        opened_at=now,
        due_at=now + REQUEST_DUE,
    )
    db.add(row)
    await db.flush()
    return row


@router.get("/me/requests", response_model=list[DataSubjectRequestResponse])
async def my_requests(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    rows = await db.execute(
        select(DataSubjectRequest)
        .where(DataSubjectRequest.user_id == user.id)
        .order_by(DataSubjectRequest.opened_at.desc())
    )
    return rows.scalars().all()


# --- Changing the e-mail (art. 18, III: correction) ---

EMAIL_TAKEN = "Esse e-mail já é de outra conta."
EMAIL_CODE_NOT_SENT = (
    "Não deu pra mandar o código agora. Tenta de novo em alguns minutos."
)


def _email_change_mail(code: str) -> tuple[str, str, str]:
    """Subject, HTML and text of the code sent to the new address."""
    minutes = int(codes.CODE_TTL.total_seconds() // 60)
    subject = f"{code} é seu código pra trocar o e-mail na TumTum"
    html = (
        "<p>Seu código pra usar este e-mail na sua conta da TumTum:</p>"
        f'<p style="font-size:28px;font-weight:700;letter-spacing:6px">{code}</p>'
        f"<p>Ele vale por {minutes} minutos.</p>"
        "<p>Se não foi você que pediu, é só ignorar este e-mail — "
        "nada muda sem o código.</p>"
    )
    text = (
        f"Seu código pra usar este e-mail na sua conta da TumTum: {code}\n\n"
        f"Ele vale por {minutes} minutos.\n\n"
        "Se não foi você que pediu, é só ignorar este e-mail — "
        "nada muda sem o código."
    )
    return subject, html, text


async def _address_taken(db: AsyncSession, key: str, user_id: uuid.UUID) -> bool:
    found = await db.execute(
        select(User.id).where(func.lower(User.email) == key, User.id != user_id)
    )
    return found.first() is not None


@router.post(
    "/me/email",
    response_model=EmailChangeStarted,
    status_code=status.HTTP_202_ACCEPTED,
)
async def start_email_change(
    body: EmailChangeRequest,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Send a code to the new address; change nothing until it comes back.

    The password is asked because the address is where "esqueci minha senha"
    goes: whoever changes it owns the account.
    """
    if not _password_matches(user, body.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail=WRONG_PASSWORD
        )
    now = datetime.now(UTC)
    new_email = body.email.strip()
    key = codes.email_key(new_email)
    if key == codes.email_key(user.email):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Esse já é o e-mail da sua conta.",
        )
    if await _address_taken(db, key, user.id):
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail=EMAIL_TAKEN)

    recent = (
        (
            await db.execute(
                select(EmailChange)
                .where(
                    EmailChange.user_id == user.id,
                    EmailChange.created_at > now - timedelta(hours=1),
                )
                .order_by(EmailChange.created_at.desc())
            )
        )
        .scalars()
        .all()
    )
    wait = codes.seconds_until_resend(recent[0].created_at if recent else None, now)
    if wait > 0:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail=f"O código acabou de sair. Espera {wait} segundos pra pedir outro.",
        )
    if codes.over_hourly_cap([row.created_at for row in recent], now):
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Já mandamos muitos códigos. Tenta de novo daqui a uma hora.",
        )

    code = codes.generate_code()
    subject, html, text = _email_change_mail(code)
    try:
        await send_email(to=new_email, subject=subject, html=html, text=text)
    except (EmailNotConfigured, httpx.HTTPError) as error:
        traceback.print_exception(error)
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=EMAIL_CODE_NOT_SENT,
        ) from None

    # Only the newest code works, as in sign-up.
    still_open = await db.execute(
        select(EmailChange).where(
            EmailChange.user_id == user.id, EmailChange.used_at.is_(None)
        )
    )
    for older in still_open.scalars().all():
        older.used_at = now

    db.add(
        EmailChange(
            user_id=user.id,
            new_email=new_email,
            email_key=key,
            code_hash=codes.hash_code(key, code, settings.secret_key),
            expires_at=codes.expiry_from(now),
            created_at=now,
        )
    )
    await db.flush()
    return EmailChangeStarted(
        email=new_email,
        expires_in_seconds=int(codes.CODE_TTL.total_seconds()),
        resend_after_seconds=int(codes.RESEND_AFTER.total_seconds()),
    )


@router.post("/me/email/confirm", response_model=UserProfileResponse)
async def confirm_email_change(
    body: EmailChangeConfirm,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """The code came back: the new address is the account's.

    Every refresh token of the account is revoked with it — the same reason a
    password reset does: a change of address is what somebody does when the
    old one is no longer theirs, and a device signed in under it should not
    carry on for 90 days.
    """
    now = datetime.now(UTC)
    code = codes.clean_code(body.code)
    if code is None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, detail="O código tem 6 números."
        )
    pending = (
        await db.execute(
            select(EmailChange)
            .where(EmailChange.user_id == user.id, EmailChange.used_at.is_(None))
            .order_by(EmailChange.created_at.desc())
            .limit(1)
        )
    ).scalar_one_or_none()
    gone = "Esse código não vale mais. Pede um novo."
    if pending is None or not codes.is_open(
        pending.expires_at, pending.used_at, pending.attempts, now
    ):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=gone)

    if not codes.matches(
        pending.code_hash, pending.email_key, code, settings.secret_key
    ):
        pending.attempts += 1
        if pending.attempts >= codes.MAX_ATTEMPTS:
            pending.used_at = now
        # Committed before raising, or the rollback would give back the guess.
        await db.commit()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=codes.attempts_left_message(pending.attempts),
        )

    pending.used_at = now
    if await _address_taken(db, pending.email_key, user.id):
        await db.commit()
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail=EMAIL_TAKEN)

    user.email = pending.new_email
    await refresh_tokens.revoke_all(db, user.id)
    await db.flush()
    return await _profile(db, user)


@router.get("/{user_id}", response_model=PublicProfileResponse)
async def get_public_profile(
    user_id: str,
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Usuário não encontrado"
        )

    stats = await _get_user_stats(db, user.id)
    return PublicProfileResponse(
        name=user.name,
        avatar_url=user.avatar_url,
        created_at=user.created_at,
        **stats,
    )


# --- The people this account blocked (#36, 22/09) ---
#
# A block is made from a post in the feed; this is where it is undone. Names
# only — the list shows who, never anything that finds them elsewhere.


@router.get("/me/blocks", response_model=list[BlockedPerson])
async def my_blocks(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    rows = await db.execute(
        select(UserBlock, User)
        .join(User, User.id == UserBlock.blocked_id)
        .where(UserBlock.blocker_id == user.id)
        .order_by(UserBlock.created_at.desc())
    )
    out = []
    for block, person in rows.all():
        who = FeedAuthor.of(person)
        out.append(
            BlockedPerson(
                id=block.id,
                name=who.name,
                initials=who.initials,
                created_at=block.created_at,
            )
        )
    return out


@router.delete("/me/blocks/{block_id}", status_code=status.HTTP_204_NO_CONTENT)
async def unblock(
    block_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    block = (
        await db.execute(
            select(UserBlock).where(
                UserBlock.id == block_id, UserBlock.blocker_id == user.id
            )
        )
    ).scalar_one_or_none()
    if block is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Bloqueio não encontrado"
        )
    await db.delete(block)
    await db.flush()
