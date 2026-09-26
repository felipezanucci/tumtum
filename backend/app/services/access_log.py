"""Who read whose heart-rate data, and every request the API answered.

Two logs, two purposes (LGPD audit, AL-8):

- **`data_access_log`** answers the question a data subject — or the ANPD —
  may ask: *who looked at my data, and when?* It is written by the routes
  that read a night, its moments, its cards, an export, or an operator's
  queue, and by an account deletion. It names the resource and the action,
  never a bpm: a log of health-data reads must not become a second copy of
  the health data.
- **`access_log`** is the Marco Civil's (art. 15) record of application
  access: every `/api/*` request with its time, method, path, status, IP and
  account. It is written by `AccessLogMiddleware` and purged after
  `ACCESS_LOG_RETENTION` by the maintenance loop.
"""

import logging
import uuid
from datetime import timedelta

from fastapi import Request
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.privacy import AccessLog, DataAccessLog

log = logging.getLogger(__name__)

# Six months: the Marco Civil's minimum for an application provider, and the
# figure the privacy policy states.
ACCESS_LOG_RETENTION = timedelta(days=180)


def ip_of(request: Request | None) -> str | None:
    """The client's address: the first hop of `X-Forwarded-For` behind a proxy.

    Railway and Vercel sit in front of the API, so `request.client.host` is
    the proxy's address and says nothing about who asked. The first entry of
    `X-Forwarded-For` is the one the edge saw.
    """
    if request is None:
        return None
    forwarded = request.headers.get("x-forwarded-for")
    if forwarded:
        first = forwarded.split(",")[0].strip()
        if first:
            return first[:64]
    client = request.client
    return client.host[:64] if client and client.host else None


def _user_id(who) -> uuid.UUID | None:
    if who is None:
        return None
    if isinstance(who, uuid.UUID):
        return who
    if isinstance(who, str):
        return uuid.UUID(who)
    return who.id


async def record_access(
    db: AsyncSession,
    actor,
    subject,
    resource: str,
    resource_id,
    action: str,
    request: Request | None = None,
) -> None:
    """Add one row to `data_access_log`, in the request's own transaction.

    `actor` and `subject` are a `User`, a user id, or None. The row commits
    with the read it records — a read that failed and rolled back was not a
    read. Logging must never break the request, so a failure here is
    reported and swallowed.
    """
    try:
        db.add(
            DataAccessLog(
                actor_user_id=_user_id(actor),
                subject_user_id=_user_id(subject),
                resource=resource,
                resource_id=str(resource_id) if resource_id is not None else None,
                action=action,
                ip=ip_of(request),
            )
        )
        await db.flush()
    except Exception as error:  # the log must not cost the person their answer
        log.warning(
            "data access log: could not record %s %s: %s", action, resource, error
        )


def user_id_from_authorization(header: str | None) -> uuid.UUID | None:
    """The account a bearer token names, without a database round trip.

    For the access log only: an invalid or expired token is simply logged as
    anonymous, and the route itself still decides whether to serve it.
    """
    if not header or not header.lower().startswith("bearer "):
        return None
    try:
        from app.core.auth import decode_access_token

        sub = decode_access_token(header.split(" ", 1)[1].strip()).get("sub")
        return uuid.UUID(str(sub)) if sub else None
    except Exception:
        return None


async def write_request(
    db: AsyncSession,
    *,
    method: str,
    path: str,
    status: int,
    ip: str | None,
    user_id: uuid.UUID | None,
) -> None:
    db.add(
        AccessLog(
            method=method[:10],
            path=path[:500],
            status=status,
            ip=ip,
            user_id=user_id,
        )
    )
    await db.commit()
