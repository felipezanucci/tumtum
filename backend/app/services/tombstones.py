"""Keeping a deleted account deleted after a backup is restored (v1.1 §11).

`delete_account` writes a `deletion_tombstones` row — two hashes, no name —
right before the `users` row goes. `sweep` reads them back and deletes, again,
whatever a restore brought back:

- every account whose id hashes to a tombstone's `subject_key`;
- every account whose address matches a tombstone's `email_key` **and that
  already existed when that deletion happened** (`created_at` at or before
  it). The guard is the difference between cleaning a restore and deleting
  somebody who came back: a person who deletes their account and signs up
  again with the same address next month has an account created *after*
  the tombstone, and it is theirs;
- the sign-up codes, e-mail-change codes and waitlist entries of a
  tombstoned address, under the same guard.

It runs once at startup (a restore is followed by a deploy or a restart) and
in the daily maintenance pass. It is O(users + codes + waitlist): each row's
address is hashed in Python, because the key is an HMAC under `SECRET_KEY`
and the database has no business holding that key. At pilot size that is a
few thousand hashes a day.

The tombstones must survive the restore themselves: a backup holds only
those older than it. `alembic/README-migrations.md` ("Restoring a backup")
carries the live table across; a copy kept outside the database, for when
the live one is lost, does not exist yet.

If `SECRET_KEY` is ever rotated, the `email_key` of older tombstones stops
matching; the `subject_key` does not depend on the key and still finds every
restored account. The address-only rows (codes, waitlist) are the part that
loses its net, and those are short-lived or re-asked for anyway.
"""

import hashlib
import hmac
import uuid
from datetime import UTC, datetime

from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.models.privacy import DeletionTombstone, EmailChange
from app.models.signup_code import SignupCode
from app.models.user import User
from app.models.waitlist_entry import WaitlistEntry
from app.services import signup_codes

BATCH = 500


def subject_fingerprint(user_id: uuid.UUID | str) -> str:
    """SHA-256 of the account id. Unkeyed: a random UUID cannot be guessed."""
    return hashlib.sha256(str(user_id).encode()).hexdigest()


def email_fingerprint(email: str) -> str:
    """HMAC-SHA256 of the address as it is compared (`signup_codes.email_key`),
    keyed with `SECRET_KEY` so a list of addresses cannot be hashed against it."""
    return hmac.new(
        settings.secret_key.encode(),
        signup_codes.email_key(email).encode(),
        hashlib.sha256,
    ).hexdigest()


def _aware(moment: datetime | None) -> datetime | None:
    # SQLite (the tests) hands timestamps back naive; they are UTC.
    if moment is None:
        return None
    return moment if moment.tzinfo else moment.replace(tzinfo=UTC)


def _existed_by(created_at: datetime | None, deleted_at: datetime) -> bool:
    """Whether a row was already there when the deletion happened.

    A row with no creation date is treated as old: every table here fills it
    on insert, so a missing one can only come from before the column did.
    """
    created = _aware(created_at)
    return created is None or created <= deleted_at


async def record(db: AsyncSession, user: User) -> None:
    """The tombstone of this account, unless it already has one.

    An account a restore brought back and `sweep` deleted again has the
    tombstone of its first deletion; a second would only extend nothing and
    count the same person twice.
    """
    key = subject_fingerprint(user.id)
    exists = await db.execute(
        select(DeletionTombstone.id)
        .where(DeletionTombstone.subject_key == key)
        .limit(1)
    )
    if exists.first() is not None:
        return
    db.add(
        DeletionTombstone(
            subject_key=key,
            email_key=email_fingerprint(user.email),
            deleted_at=datetime.now(UTC),
        )
    )


async def _tombstones(db: AsyncSession) -> tuple[set[str], dict[str, datetime]]:
    """Every subject key, and each e-mail key with its latest deletion."""
    rows = await db.execute(
        select(
            DeletionTombstone.subject_key,
            DeletionTombstone.email_key,
            DeletionTombstone.deleted_at,
        )
    )
    subjects: set[str] = set()
    emails: dict[str, datetime] = {}
    for subject_key, email_key, deleted_at in rows.all():
        subjects.add(subject_key)
        when = _aware(deleted_at)
        if email_key not in emails or when > emails[email_key]:
            emails[email_key] = when
    return subjects, emails


def _address_tombstoned(
    emails: dict[str, datetime], address: str, created_at: datetime | None
) -> bool:
    deleted_at = emails.get(email_fingerprint(address))
    return deleted_at is not None and _existed_by(created_at, deleted_at)


async def _restored_users(
    db: AsyncSession, subjects: set[str], emails: dict[str, datetime]
) -> list[uuid.UUID]:
    hits: list[uuid.UUID] = []
    last_id = None
    while True:
        query = select(User.id, User.email, User.created_at).order_by(User.id)
        if last_id is not None:
            query = query.where(User.id > last_id)
        batch = (await db.execute(query.limit(BATCH))).all()
        if not batch:
            return hits
        for user_id, email, created_at in batch:
            if subject_fingerprint(user_id) in subjects or _address_tombstoned(
                emails, email, created_at
            ):
                hits.append(user_id)
        last_id = batch[-1][0]


async def _delete_matching(
    db: AsyncSession, model, address_of, emails: dict[str, datetime]
) -> int:
    rows = (await db.execute(select(model))).scalars().all()
    ids = [
        row.id
        for row in rows
        if _address_tombstoned(emails, address_of(row), row.created_at)
    ]
    if ids:
        await db.execute(delete(model).where(model.id.in_(ids)))
    return len(ids)


async def sweep(db: AsyncSession) -> int:
    """Delete what a restored backup brought back. Returns the accounts removed.

    The caller commits. Each account goes through `delete_account`, the same
    path a person's own deletion takes, so a restored account leaves exactly
    what a deleted one does.
    """
    from app.services.account_deletion import delete_account

    subjects, emails = await _tombstones(db)
    if not subjects:
        return 0

    removed = 0
    for user_id in await _restored_users(db, subjects, emails):
        user = await db.get(User, user_id)
        if user is not None:
            await delete_account(db, user)
            removed += 1

    await _delete_matching(db, SignupCode, lambda row: row.email, emails)
    await _delete_matching(db, EmailChange, lambda row: row.new_email, emails)
    await _delete_matching(db, WaitlistEntry, lambda row: row.email, emails)
    await db.flush()
    return removed
