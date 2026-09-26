from datetime import UTC, datetime, timedelta

from fastapi import Depends, HTTPException, Request, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.core.database import get_db

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/login")

ALGORITHM = "HS256"
# One hour (#34, 22/09). It was 24 hours with nothing to renew it, so every
# account was signed out daily. The session's length now lives in the refresh
# token — 90 days from last use — and this one only has to be short enough
# that a revoked session actually stops working soon after.
ACCESS_TOKEN_EXPIRE_MINUTES = 60


def create_access_token(data: dict, expires_delta: timedelta | None = None) -> str:
    to_encode = data.copy()
    expire = datetime.now(UTC) + (
        expires_delta or timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    )
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, settings.secret_key, algorithm=ALGORITHM)


def decode_access_token(token: str) -> dict:
    try:
        return jwt.decode(token, settings.secret_key, algorithms=[ALGORITHM])
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token inválido ou expirado",
            headers={"WWW-Authenticate": "Bearer"},
        ) from None


async def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: AsyncSession = Depends(get_db),
):
    payload = decode_access_token(token)
    user_id: str | None = payload.get("sub")
    if user_id is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Token inválido"
        )

    from app.models.user import User

    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Usuário não encontrado"
        )
    return user


async def require_admin(user=Depends(get_current_user)):
    """The signed-in person, if they operate the platform.

    Decided by `admin_emails` in the settings, never by anything the client
    sends. A 403 rather than a 404: the thing exists, and the honest answer
    to "may I?" is "not with this account", which the site then says in
    those words instead of pretending the page is empty.
    """
    if not settings.is_admin(user.email):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Esta ação não está disponível para a sua conta.",
        )
    return user


class ConsentRequired(HTTPException):
    """A 403 that names the purpose the person has not granted.

    Its body is `{"detail", "code": "consent_required", "purpose"}` (contract
    of 26/09) — the handler in `main.py` writes it — so a client can open the
    consent screen on exactly that purpose instead of retrying silently.
    """

    def __init__(self, purpose: str):
        from app.services.consents import REQUIRED_SENTENCES

        super().__init__(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=REQUIRED_SENTENCES.get(
                purpose, "Essa ação precisa da sua autorização antes."
            ),
        )
        self.purpose = purpose

    def body(self) -> dict:
        return {
            "detail": self.detail,
            "code": "consent_required",
            "purpose": self.purpose,
        }


def require_consent(purpose: str):
    """A dependency: the signed-in person, if `purpose` is granted right now.

    Checked on the server on every request, never trusted from the client —
    the app's own switch is a convenience, the row in `consents` is the
    permission.
    """
    from app.services.consents import PURPOSES

    if purpose not in PURPOSES:
        raise ValueError(f"unknown consent purpose: {purpose}")

    async def dependency(
        user=Depends(get_current_user),
        db: AsyncSession = Depends(get_db),
    ):
        from app.services import consents

        if not await consents.active(db, user.id, purpose):
            raise ConsentRequired(purpose)
        return user

    # Named after the purpose so a router test can read which consent a route
    # asks for, the way it reads `require_admin`.
    dependency.__name__ = f"require_consent_{purpose}"
    return dependency


def client_of(request: Request | None) -> str | None:
    """The `X-Tumtum-Client` header — `android/<versionCode>` or `web/<commit>`.

    Optional: a request without it is still served, and the consent row
    simply records that nobody said which client it was.
    """
    if request is None:
        return None
    value = (request.headers.get("x-tumtum-client") or "").strip()
    return value[:80] or None
