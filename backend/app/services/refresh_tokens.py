"""Sessions that last as long as the device is used (#34, 2026-09-22).

The rules, each small enough to test on its own:

- a refresh token lives **90 days from its last use** — a sliding window, so
  a fan who opens the app once a season is never asked for a password;
- every use **rotates** it: the old one is revoked, a new one is issued;
- a token presented after it was rotated is a **reuse**, which means a copy
  exists somewhere, so its whole family is revoked;
- **except an honest retry.** Stadium cellular loses responses: the server
  rotates, the answer never arrives, and the phone asks again with the token
  it still holds. Treated as theft, that would sign a fan out in the middle
  of the one place the product exists for. So for [RETRY_GRACE] after a
  rotation, and only while the token it produced has never been used, the
  old token may ask again — the unused child is revoked and a fresh one
  issued in its place. Once the child has been used, the old token is a copy
  by definition;
- signing out revokes the family, so "Sair" means out.
"""

import uuid
from datetime import UTC, datetime, timedelta

from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.refresh_token import RefreshToken
from app.services.password_reset import generate_token, hash_token

REFRESH_TOKEN_DAYS = 90
RETRY_GRACE = timedelta(minutes=2)

ROTATED = "rotated"
LOGOUT = "logout"
RESET = "reset"
REUSE = "reuse"


class RefreshRefused(Exception):
    """The token cannot renew a session. ``reason`` is for logs and tests."""

    def __init__(self, reason: str):
        super().__init__(reason)
        self.reason = reason


def expiry_from(now: datetime) -> datetime:
    return now + timedelta(days=REFRESH_TOKEN_DAYS)


def _aware(moment: datetime) -> datetime:
    return moment if moment.tzinfo else moment.replace(tzinfo=UTC)


def verdict(row: RefreshToken | None, now: datetime, child_unused: bool = False) -> str:
    """What a presented token is.

    ``ok`` · ``unknown`` · ``expired`` · ``revoked`` (signed out, reset, or its
    family already killed) · ``retry`` (an honest repeat of a rotation whose
    answer was lost) · ``reused`` (a copy). Pure, so the rules are tested
    without a database. Reuse is judged before expiry on purpose: a rotated
    token is evidence of a copy whatever its age.
    """
    if row is None:
        return "unknown"
    if row.revoked_at is not None:
        if row.revoke_reason != ROTATED:
            return "revoked"
        within = now - _aware(row.revoked_at) <= RETRY_GRACE
        return "retry" if within and child_unused else "reused"
    if _aware(row.expires_at) <= now:
        return "expired"
    return "ok"


async def issue(
    db: AsyncSession,
    user_id: uuid.UUID,
    family_id: uuid.UUID | None = None,
    parent_id: uuid.UUID | None = None,
) -> str:
    """A new refresh token for ``user_id``. Returns the raw token, once."""
    raw = generate_token()
    now = datetime.now(UTC)
    db.add(
        RefreshToken(
            user_id=user_id,
            family_id=family_id or uuid.uuid4(),
            parent_id=parent_id,
            token_hash=hash_token(raw),
            expires_at=expiry_from(now),
            created_at=now,
        )
    )
    await db.flush()
    return raw


async def _revoke(db: AsyncSession, where, reason: str, now: datetime) -> None:
    await db.execute(
        update(RefreshToken)
        .where(where, RefreshToken.revoked_at.is_(None))
        .values(revoked_at=now, revoke_reason=reason)
    )


async def _find(db: AsyncSession, raw: str) -> RefreshToken | None:
    return (
        await db.execute(
            select(RefreshToken).where(RefreshToken.token_hash == hash_token(raw))
        )
    ).scalar_one_or_none()


async def rotate(db: AsyncSession, raw: str) -> tuple[uuid.UUID, str]:
    """Spend ``raw`` and return ``(user_id, the next raw token)``.

    Raises [RefreshRefused] when the token cannot renew. A reuse revokes the
    family before raising; the caller commits that, so the refusal sticks.
    """
    now = datetime.now(UTC)
    row = await _find(db, raw)

    child = None
    if row is not None and row.revoke_reason == ROTATED:
        child = (
            await db.execute(
                select(RefreshToken).where(
                    RefreshToken.parent_id == row.id,
                    RefreshToken.revoked_at.is_(None),
                )
            )
        ).scalar_one_or_none()

    found = verdict(row, now, child_unused=child is not None)
    if found == "reused":
        await _revoke(db, RefreshToken.family_id == row.family_id, REUSE, now)
        await db.flush()
        raise RefreshRefused("reused")
    if found == "retry":
        # The phone never received the child; it goes, and a new one takes
        # its place. The family stays alive.
        child.revoked_at = now
        child.revoke_reason = ROTATED
        nxt = await issue(db, row.user_id, family_id=row.family_id, parent_id=row.id)
        return row.user_id, nxt
    if found != "ok":
        raise RefreshRefused(found)

    row.revoked_at = now
    row.revoke_reason = ROTATED
    nxt = await issue(db, row.user_id, family_id=row.family_id, parent_id=row.id)
    return row.user_id, nxt


async def revoke(db: AsyncSession, raw: str) -> None:
    """Sign a device out: its whole family, whatever state the token is in."""
    row = await _find(db, raw)
    if row is not None:
        await _revoke(
            db, RefreshToken.family_id == row.family_id, LOGOUT, datetime.now(UTC)
        )
        await db.flush()


async def revoke_all(db: AsyncSession, user_id: uuid.UUID) -> None:
    """Every device, every family — for a password reset."""
    await _revoke(db, RefreshToken.user_id == user_id, RESET, datetime.now(UTC))
    await db.flush()
