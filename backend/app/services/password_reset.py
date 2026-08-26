"""The pieces of password recovery that are not database or HTTP.

Kept apart from the endpoint so the security decisions can be read — and
tested — without a server or a mailbox.
"""

import hashlib
import secrets
from datetime import UTC, datetime, timedelta

# Half an hour. Long enough to walk to a laptop, short enough that a link left
# in an inbox, a browser history or a screenshot stops being a key quickly.
RESET_TOKEN_TTL = timedelta(minutes=30)

# 32 bytes of urlsafe randomness. Guessing one is not a thing anybody can do.
TOKEN_BYTES = 32


def generate_token() -> str:
    """The secret that travels in the email, and nowhere else."""
    return secrets.token_urlsafe(TOKEN_BYTES)


def hash_token(token: str) -> str:
    """What the database is allowed to hold.

    Storing the raw token would mean anyone who reads the table can take over
    any account with a pending reset — the same reason passwords are hashed.
    SHA-256 rather than bcrypt is right *here* and wrong for a password: this
    input is 32 bytes of entropy with a 30-minute life, so there is no
    dictionary to run against it, and the lookup happens on every attempt.
    """
    return hashlib.sha256(token.encode("utf-8")).hexdigest()


def expiry_from(now: datetime) -> datetime:
    return now + RESET_TOKEN_TTL


def is_usable(expires_at: datetime, used_at: datetime | None, now: datetime) -> bool:
    """A token is good once, briefly, and never again.

    Single use matters as much as expiry: without it, a link sitting in an
    inbox stays a working key to the account for its whole lifetime, and
    "I already reset it" would not close that window.
    """
    if used_at is not None:
        return False
    # Rows written before timezone handling settled can come back naive.
    # Comparing a naive datetime to an aware one raises, which would turn an
    # expired token into a 500 instead of a polite refusal.
    if expires_at.tzinfo is None:
        expires_at = expires_at.replace(tzinfo=UTC)
    return now < expires_at
