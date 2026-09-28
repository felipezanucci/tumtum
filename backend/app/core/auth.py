import uuid
from datetime import UTC, datetime, timedelta

import jwt
from fastapi import Depends, HTTPException, Request, status
from fastapi.security import OAuth2PasswordBearer
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
    """A signed token for `data["sub"]`, with its expiry and its issue time.

    `iat` is what lets a password reset, an e-mail change or a deletion end
    the access tokens already out there (`users.tokens_valid_after`): the
    refresh tokens were revoked by those since #34, but an access token
    stolen a minute before a reset kept working for the rest of its hour.
    """
    to_encode = data.copy()
    now = datetime.now(UTC)
    expire = now + (expires_delta or timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES))
    to_encode.update({"exp": expire, "iat": now})
    return jwt.encode(to_encode, settings.secret_key, algorithm=ALGORITHM)


def revoke_access_tokens(user, now: datetime | None = None) -> None:
    """Every access token this account was issued until now stops working.

    Stored to the whole second, because `iat` is a whole second: a token
    signed in the same request, just after — the reset that signs the person
    straight back in — must not be refused by its own reset.
    """
    moment = now or datetime.now(UTC)
    user.tokens_valid_after = moment.replace(microsecond=0)


def _issued_before(payload: dict, valid_after: datetime | None) -> bool:
    if valid_after is None:
        return False
    if valid_after.tzinfo is None:  # SQLite hands it back naive; it is UTC
        valid_after = valid_after.replace(tzinfo=UTC)
    issued = payload.get("iat")
    # A token with no `iat` was signed before 26/09, before any cut-off.
    if not isinstance(issued, int | float):
        return True
    return issued < valid_after.timestamp()


def decode_access_token(token: str) -> dict:
    try:
        return jwt.decode(token, settings.secret_key, algorithms=[ALGORITHM])
    except jwt.PyJWTError:
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
    try:
        user_id = uuid.UUID(str(payload.get("sub")))
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Token inválido"
        ) from None

    from app.models.user import User

    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Usuário não encontrado"
        )
    # Issued before the account's last reset, e-mail change or deletion: the
    # same answer as an expired token, so the client renews or signs in.
    if _issued_before(payload, user.tokens_valid_after):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token inválido ou expirado",
            headers={"WWW-Authenticate": "Bearer"},
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


def require_consents(*purposes: str):
    """A dependency: the signed-in person, if every one of `purposes` is granted.

    Checked in the order given, and the 403 names the first one missing, so
    the app opens the consent screen on the purpose the act needs first
    (28/09: a night was recorded and offered for upload on an account that
    had never granted `read_heart_rate` — `keep_night` alone was checked).
    """
    from app.services.consents import PURPOSES

    for purpose in purposes:
        if purpose not in PURPOSES:
            raise ValueError(f"unknown consent purpose: {purpose}")

    async def dependency(
        user=Depends(get_current_user),
        db: AsyncSession = Depends(get_db),
    ):
        from app.services import consents

        for purpose in purposes:
            if not await consents.active(db, user.id, purpose):
                raise ConsentRequired(purpose)
        return user

    dependency.__name__ = "require_consents_" + "_".join(purposes)
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


def is_web_client(request: Request | None) -> bool:
    """Whether the request says it comes from the site (`web/...`).

    The site's refresh token lives in an httpOnly cookie instead of the body
    (legal opinion v1.1, §18). The header doubles as the cookie's CSRF guard:
    a custom header makes the browser ask CORS first, so a page on another
    origin cannot make a request that spends or clears the cookie.
    """
    client = client_of(request)
    return bool(client and client.lower().startswith("web/"))
