import traceback
from datetime import UTC, datetime

import bcrypt
import httpx
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.core.auth import create_access_token, get_current_user
from app.core.database import get_db
from app.models.password_reset_token import PasswordResetToken
from app.models.user import User
from app.schemas.auth import (
    ForgotPasswordRequest,
    LoginRequest,
    MessageResponse,
    RegisterRequest,
    ResetPasswordRequest,
    TokenResponse,
    UserResponse,
)
from app.services.email import EmailNotConfigured, send_email
from app.services.password_reset import (
    expiry_from,
    generate_token,
    hash_token,
    is_usable,
)

router = APIRouter(prefix="/api/auth", tags=["auth"])


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")


def verify_password(password: str, hashed: str) -> bool:
    return bcrypt.checkpw(password.encode("utf-8"), hashed.encode("utf-8"))


@router.post(
    "/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED
)
async def register(body: RegisterRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.email == body.email))
    if result.scalar_one_or_none():
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT, detail="Email já cadastrado"
        )

    user = User(
        email=body.email,
        name=body.name,
        auth_provider="email",
        hashed_password=hash_password(body.password),
    )
    db.add(user)
    await db.flush()

    token = create_access_token({"sub": str(user.id)})
    return TokenResponse(access_token=token)


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

    token = create_access_token({"sub": str(user.id)})
    return TokenResponse(access_token=token)


@router.get("/me", response_model=UserResponse)
async def me(user: User = Depends(get_current_user)):
    return user


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
        try:
            await send_email(
                to=user.email,
                subject="Criar uma nova senha na TumTum",
                html=(
                    f"<p>Oi, {user.name}.</p>"
                    f"<p>Alguém pediu uma nova senha para a sua conta na TumTum. "
                    f"Se foi você, o link abaixo vale por 30 minutos:</p>"
                    f'<p><a href="{link}">Criar uma nova senha</a></p>'
                    f"<p>Se não foi você, pode ignorar esta mensagem — "
                    f"sua senha continua a mesma.</p>"
                ),
                text=(
                    f"Oi, {user.name}.\n\n"
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

    await db.flush()

    # Signing them straight in: they just proved control of the mailbox and
    # chose a password. A login form here would only ask them to type it again.
    return TokenResponse(access_token=create_access_token({"sub": str(user.id)}))
