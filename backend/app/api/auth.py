import html
import traceback
from datetime import UTC, datetime, timedelta

import bcrypt
import httpx
from fastapi import APIRouter, Depends, HTTPException, Request, status
from sqlalchemy import delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.core.auth import client_of, create_access_token, get_current_user
from app.core.database import get_db
from app.models.password_reset_token import PasswordResetToken
from app.models.signup_code import SignupCode
from app.models.user import User
from app.schemas.auth import (
    ForgotPasswordRequest,
    LoginRequest,
    MessageResponse,
    RefreshRequest,
    ResetPasswordRequest,
    SignupConfirmRequest,
    SignupStarted,
    SignupStartRequest,
    TokenResponse,
    UserResponse,
)
from app.services import consents, refresh_tokens
from app.services import signup_codes as codes
from app.services.age import UNDER_AGE, is_adult, today_local
from app.services.email import EmailNotConfigured, send_email
from app.services.password_reset import (
    expiry_from,
    generate_token,
    hash_token,
    is_usable,
)

router = APIRouter(prefix="/api/auth", tags=["auth"])


async def _signed_in(db: AsyncSession, user_id, family_id=None) -> TokenResponse:
    """An access token and the refresh token that renews it."""
    return TokenResponse(
        access_token=create_access_token({"sub": str(user_id)}),
        refresh_token=await refresh_tokens.issue(db, user_id, family_id=family_id),
    )


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")


def verify_password(password: str, hashed: str) -> bool:
    return bcrypt.checkpw(password.encode("utf-8"), hashed.encode("utf-8"))


# The old one-step sign-up, retired by #64 (24/09). It answers every caller
# — an app installed before the code existed, or anyone with curl — with a
# sentence instead of an account, because left open it would be the way
# around the code.
SIGNUP_RETIRED = (
    "Pra criar uma conta agora a gente manda um código pro seu e-mail. "
    "Atualiza o app (ou crie pelo tumtum.cc) e tenta de novo."
)

CODE_GONE = "Esse código não vale mais. Pede um novo."
TERMS_REQUIRED = "Você precisa aceitar os Termos e a Política de Privacidade."
BIRTH_DATE_REQUIRED = "Coloca sua data de nascimento."
CODE_NOT_SENT = "Não deu pra mandar o código agora. Tenta de novo em alguns minutos."


@router.post("/register", status_code=status.HTTP_410_GONE)
async def register():
    raise HTTPException(status_code=status.HTTP_410_GONE, detail=SIGNUP_RETIRED)


async def _has_account(db: AsyncSession, key: str) -> bool:
    # Addresses were stored as typed (open item 11), so an account made as
    # "Felipe@" must still stop a second one as "felipe@".
    found = await db.execute(select(User.id).where(func.lower(User.email) == key))
    return found.first() is not None


def _code_mail(code: str) -> tuple[str, str, str]:
    """Subject, HTML and text of the code's e-mail.

    No name in it, on purpose: the form takes any address, and whatever it
    lets a stranger write would land, under our name, in somebody else's
    inbox. The only thing that varies is six digits we chose.
    """
    minutes = int(codes.CODE_TTL.total_seconds() // 60)
    subject = f"{code} é seu código da TumTum"
    html = (
        "<p>Seu código pra criar a conta na TumTum:</p>"
        f'<p style="font-size:28px;font-weight:700;letter-spacing:6px">{code}</p>'
        f"<p>Ele vale por {minutes} minutos.</p>"
        "<p>Se não foi você que pediu, é só ignorar este e-mail — "
        "nenhuma conta é criada sem o código.</p>"
    )
    text = (
        f"Seu código pra criar a conta na TumTum: {code}\n\n"
        f"Ele vale por {minutes} minutos.\n\n"
        "Se não foi você que pediu, é só ignorar este e-mail — "
        "nenhuma conta é criada sem o código."
    )
    return subject, html, text


@router.post(
    "/register/start",
    response_model=SignupStarted,
    status_code=status.HTTP_202_ACCEPTED,
)
async def register_start(body: SignupStartRequest, db: AsyncSession = Depends(get_db)):
    """Step one of an account (#64): send a code to the address, create nothing.

    The account is created by `register/confirm`, and only when the code comes
    back — so an address nobody reads never becomes an account.
    """
    now = datetime.now(UTC)
    key = codes.email_key(body.email)
    name = body.name.strip()
    if not name:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, detail="Coloca seu nome."
        )
    # Adults only, and only with the terms accepted (LGPD audit, CR-1 and
    # CR-4). Checked before any mail leaves: a code for an account that can
    # never exist is a mail nobody should get.
    if body.birth_date is None:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=BIRTH_DATE_REQUIRED,
        )
    if not is_adult(body.birth_date, today_local()):
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail=UNDER_AGE
        )
    if not body.terms_accepted:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail=TERMS_REQUIRED
        )
    if await _has_account(db, key):
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT, detail="Email já cadastrado"
        )

    # A sign-up nobody confirmed is somebody's address, name and password
    # hash with no account attached. After a day, anyone's goes.
    await db.execute(
        delete(SignupCode).where(SignupCode.created_at < now - codes.KEEP_UNCONFIRMED)
    )

    recent = (
        (
            await db.execute(
                select(SignupCode)
                .where(
                    SignupCode.email_key == key,
                    SignupCode.created_at > now - timedelta(hours=1),
                )
                .order_by(SignupCode.created_at.desc())
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
            detail="Já mandamos muitos códigos pra esse e-mail. Tenta de novo daqui a uma hora.",
        )

    code = codes.generate_code()
    subject, html, text = _code_mail(code)
    # The mail goes first. If it cannot leave, nothing is kept: no code that
    # nobody received, and no wait imposed before trying again.
    try:
        await send_email(to=body.email.strip(), subject=subject, html=html, text=text)
    except (EmailNotConfigured, httpx.HTTPError) as error:
        traceback.print_exception(error)
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=CODE_NOT_SENT
        ) from None

    # Only the newest code works: a mail that arrives late cannot revive one
    # the person already asked to replace.
    still_open = await db.execute(
        select(SignupCode).where(
            SignupCode.email_key == key, SignupCode.used_at.is_(None)
        )
    )
    for older in still_open.scalars().all():
        older.used_at = now

    db.add(
        SignupCode(
            email=body.email.strip(),
            email_key=key,
            name=name,
            hashed_password=hash_password(body.password),
            birth_date=body.birth_date,
            consent_text_version=(
                body.consent_text_version or consents.CONSENT_TEXT_VERSION
            ),
            read_heart_rate=body.read_heart_rate,
            code_hash=codes.hash_code(key, code, settings.secret_key),
            expires_at=codes.expiry_from(now),
            created_at=now,
        )
    )
    await db.flush()

    return SignupStarted(
        email=body.email.strip(),
        expires_in_seconds=int(codes.CODE_TTL.total_seconds()),
        resend_after_seconds=int(codes.RESEND_AFTER.total_seconds()),
    )


@router.post(
    "/register/confirm",
    response_model=TokenResponse,
    status_code=status.HTTP_201_CREATED,
)
async def register_confirm(
    body: SignupConfirmRequest,
    db: AsyncSession = Depends(get_db),
    request: Request = None,
):
    """Step two: the code came back, so the address is real — make the account.

    The account is born with its birth date and with the consent the person
    gave on the sign-up screen: `terms` always (the checkbox), and
    `read_heart_rate` when that screen carried it. Both dated when they were
    given — the moment the code was asked for — not when it came back.
    """
    now = datetime.now(UTC)
    key = codes.email_key(body.email)
    code = codes.clean_code(body.code)
    if code is None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, detail="O código tem 6 números."
        )

    pending = (
        await db.execute(
            select(SignupCode)
            .where(SignupCode.email_key == key, SignupCode.used_at.is_(None))
            .order_by(SignupCode.created_at.desc())
            .limit(1)
        )
    ).scalar_one_or_none()
    if pending is None or not codes.is_open(
        pending.expires_at, pending.used_at, pending.attempts, now
    ):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=CODE_GONE)

    if not codes.matches(pending.code_hash, key, code, settings.secret_key):
        pending.attempts += 1
        if pending.attempts >= codes.MAX_ATTEMPTS:
            pending.used_at = now
        # Committed before it is raised, or the request's rollback would undo
        # the count and the five guesses would be infinite.
        await db.commit()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=codes.attempts_left_message(pending.attempts),
        )

    pending.used_at = now
    if await _has_account(db, key):
        # Two sign-ups raced, and the other one won.
        await db.commit()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT, detail="Email já cadastrado"
        )

    user = User(
        email=pending.email,
        name=pending.name,
        auth_provider="email",
        hashed_password=pending.hashed_password,
        birth_date=pending.birth_date,
    )
    db.add(user)
    await db.flush()

    given = {"terms": True}
    if pending.read_heart_rate:
        given["read_heart_rate"] = True
    await consents.set_many(
        db,
        user.id,
        given,
        text_version=pending.consent_text_version or consents.CONSENT_TEXT_VERSION,
        means="checkbox",
        client=client_of(request),
        now=codes._aware(pending.created_at) if pending.created_at else now,
    )

    return await _signed_in(db, user.id)


@router.post("/login", response_model=TokenResponse)
async def login(body: LoginRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.email == body.email))
    user = result.scalar_one_or_none()

    if (
        not user
        or not user.hashed_password
        or not verify_password(body.password, user.hashed_password)
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Email ou senha incorretos"
        )

    return await _signed_in(db, user.id)


@router.post("/refresh", response_model=TokenResponse)
async def refresh(body: RefreshRequest, db: AsyncSession = Depends(get_db)):
    """Renew a session without a password — and rotate the token that did it.

    The refusal is one sentence whatever the cause, and it is **committed
    before it is raised**: a reused token revokes its whole family, and the
    request's own rollback must not quietly undo that.
    """
    try:
        user_id, nxt = await refresh_tokens.rotate(db, body.refresh_token)
    except refresh_tokens.RefreshRefused:
        await db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Sua sessão terminou. Entre de novo.",
        ) from None
    return TokenResponse(
        access_token=create_access_token({"sub": str(user_id)}),
        refresh_token=nxt,
    )


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
async def logout(body: RefreshRequest, db: AsyncSession = Depends(get_db)):
    """ "Sair" means out: this device's refresh chain is revoked.

    No access token required, so a device whose hour has run out can still
    sign itself out properly; knowing the refresh token is the proof.
    """
    await refresh_tokens.revoke(db, body.refresh_token)


@router.get("/me", response_model=UserResponse)
async def me(user: User = Depends(get_current_user)):
    response = UserResponse.model_validate(user)
    response.is_admin = settings.is_admin(user.email)
    return response


# The same words whichever way it goes. Saying "esse e-mail não está
# cadastrado" would turn this form into a tool for finding out who has an
# account — free of charge, at any volume, from anyone.
RESET_SENT = (
    "Se esse e-mail tiver uma conta, o link para criar uma nova senha "
    "acabou de sair. Confere sua caixa de entrada e o spam."
)


@router.post("/forgot-password", response_model=MessageResponse)
async def forgot_password(
    body: ForgotPasswordRequest,
    db: AsyncSession = Depends(get_db),
):
    """Start a password reset, and admit nothing about who has an account."""
    email = body.email.strip().lower()

    # Addresses were stored without normalisation, so a case-insensitive
    # comparison is the only way to find an account registered as "Felipe@".
    # Fixing the column is queued; until then, matching loosely here is what
    # keeps the feature from failing for exactly the people who need it.
    result = await db.execute(select(User).where(func.lower(User.email) == email))
    user = result.scalar_one_or_none()

    if user is not None:
        token = generate_token()
        now = datetime.now(UTC)
        db.add(
            PasswordResetToken(
                user_id=user.id,
                token_hash=hash_token(token),
                expires_at=expiry_from(now),
            )
        )
        await db.flush()

        link = f"{settings.site_url}/redefinir-senha?token={token}"
        # The name as the person typed it once, which can end in a space: the
        # first reset that ever left (24/09) greeted "Felipe Zanucci ." — and
        # escaped for the HTML part, since it is text a person chose.
        name = user.name.strip()
        try:
            await send_email(
                to=user.email,
                subject="Criar uma nova senha na TumTum",
                html=(
                    f"<p>Oi, {html.escape(name)}.</p>"
                    f"<p>Alguém pediu uma nova senha para a sua conta na TumTum. "
                    f"Se foi você, o link abaixo vale por 30 minutos:</p>"
                    f'<p><a href="{link}">Criar uma nova senha</a></p>'
                    f"<p>Se não foi você, pode ignorar esta mensagem — "
                    f"sua senha continua a mesma.</p>"
                ),
                text=(
                    f"Oi, {name}.\n\n"
                    f"Alguém pediu uma nova senha para a sua conta na TumTum. "
                    f"Se foi você, abra este link nos próximos 30 minutos:\n\n"
                    f"{link}\n\n"
                    f"Se não foi você, pode ignorar esta mensagem — "
                    f"sua senha continua a mesma."
                ),
            )
        except (EmailNotConfigured, httpx.HTTPError) as error:
            # The person is told the same thing either way, so a failure here
            # would otherwise vanish completely. It has to reach the logs, or
            # "não recebi o e-mail" becomes unanswerable.
            traceback.print_exception(error)

    return MessageResponse(message=RESET_SENT)


@router.post("/reset-password", response_model=TokenResponse)
async def reset_password(
    body: ResetPasswordRequest,
    db: AsyncSession = Depends(get_db),
):
    """Spend the token, set the password, and sign the person in."""
    now = datetime.now(UTC)
    result = await db.execute(
        select(PasswordResetToken).where(
            PasswordResetToken.token_hash == hash_token(body.token)
        )
    )
    reset = result.scalar_one_or_none()

    if reset is None or not is_usable(reset.expires_at, reset.used_at, now):
        # One message for missing, expired and already-spent alike: telling
        # them apart tells a stranger which guesses were close.
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Esse link não vale mais. Peça um novo para criar sua senha.",
        )

    user = await db.get(User, reset.user_id)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Esse link não vale mais. Peça um novo para criar sua senha.",
        )

    user.hashed_password = hash_password(body.password)
    reset.used_at = now

    # Every other outstanding link for this account dies with it. Someone
    # resetting because they fear a break-in should not leave a spare key in
    # an inbox they no longer control.
    others = await db.execute(
        select(PasswordResetToken).where(
            PasswordResetToken.user_id == user.id,
            PasswordResetToken.used_at.is_(None),
        )
    )
    for outstanding in others.scalars().all():
        outstanding.used_at = now

    # Every signed-in device goes too (#34). A reset is what somebody does
    # when they fear a break-in, and a stolen refresh token would otherwise
    # outlive the password it was issued under by up to 90 days.
    await refresh_tokens.revoke_all(db, user.id)

    # Signing them straight in: they just proved control of the mailbox and
    # chose a password. A login form here would only ask them to type it again.
    return await _signed_in(db, user.id)
