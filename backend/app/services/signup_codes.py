"""Proving an e-mail before an account exists (#64, Felipe 24/09).

Test 7 made an account with `teste@teste.com`. No validator can refuse that:
the address is well-formed, and whether a mailbox exists behind it is
something only a message can find out. So an account is born in two steps —
a 6-digit code goes to the address, and the account exists only once the
code comes back. The address is then one the person can read, which is also
what makes "esqueci minha senha" and "apague minha conta" reach them.

Kept apart from the endpoints, like `password_reset`, so the rules can be
read — and tested — without a server or a mailbox.
"""

import hashlib
import hmac
import secrets
from datetime import UTC, datetime, timedelta

CODE_DIGITS = 6

# Long enough to switch to the mail app, find the message (or the spam
# folder) and come back; short enough that a code left in an inbox stops
# being a key quickly.
CODE_TTL = timedelta(minutes=15)

# A million codes and five guesses: one chance in 200,000 of a lucky guess,
# after which the code is dead and a new one has to be asked for.
MAX_ATTEMPTS = 5

# Between two codes to the same address. A tap on "reenviar" that arrives
# before the first mail does would otherwise kill the code already on its way.
RESEND_AFTER = timedelta(seconds=60)

# Codes to one address in an hour. The form takes any address, so without a
# ceiling it is a way to fill a stranger's inbox with our mail.
MAX_CODES_PER_HOUR = 5

# A sign-up nobody confirms keeps an address, a name and a password hash of
# somebody who has no account. After a day it is forgotten.
KEEP_UNCONFIRMED = timedelta(hours=24)


def generate_code() -> str:
    """Six digits from the system's secure source, leading zeros kept."""
    return f"{secrets.randbelow(10**CODE_DIGITS):0{CODE_DIGITS}d}"


def email_key(email: str) -> str:
    """The address as it is compared: `Felipe@` and `felipe@` are one inbox."""
    return email.strip().lower()


def hash_code(email: str, code: str, secret: str) -> str:
    """What the table holds instead of the code.

    A keyed hash, and bound to the address. A plain SHA-256 of six digits is
    reversed by trying all million of them, so the server's secret goes in:
    reading the table without it tells nobody any code.
    """
    message = f"{email_key(email)}:{code}".encode()
    return hmac.new(secret.encode(), message, hashlib.sha256).hexdigest()


def matches(stored_hash: str, email: str, code: str, secret: str) -> bool:
    return hmac.compare_digest(stored_hash, hash_code(email, code, secret))


def clean_code(raw: str) -> str | None:
    """The six digits someone typed, forgiving the spaces and dashes a mail
    client or a paste can add — or None when it is not six digits."""
    compact = raw.strip().replace(" ", "").replace("-", "")
    # ASCII digits only: `str.isdigit` also says yes to "１２３４５６", which
    # would then be counted as a wrong guess instead of refused as a typo.
    if len(compact) == CODE_DIGITS and all(c in "0123456789" for c in compact):
        return compact
    return None


def _aware(moment: datetime) -> datetime:
    # A database can hand a timestamp back naive; comparing naive with aware
    # raises, which would turn a polite refusal into a 500.
    return moment if moment.tzinfo else moment.replace(tzinfo=UTC)


def expiry_from(now: datetime) -> datetime:
    return now + CODE_TTL


def is_open(
    expires_at: datetime, used_at: datetime | None, attempts: int, now: datetime
) -> bool:
    """A code is good until it is used, replaced, guessed wrong too often, or old."""
    if used_at is not None or attempts >= MAX_ATTEMPTS:
        return False
    return now < _aware(expires_at)


def seconds_until_resend(last_sent_at: datetime | None, now: datetime) -> int:
    """How long before another code may go to this address; 0 means now."""
    if last_sent_at is None:
        return 0
    wait = _aware(last_sent_at) + RESEND_AFTER - now
    return max(0, int(-(-wait.total_seconds() // 1)))  # rounded up


def over_hourly_cap(sent_at: list[datetime], now: datetime) -> bool:
    """Whether this address already got its codes for the hour."""
    since = now - timedelta(hours=1)
    return sum(1 for moment in sent_at if _aware(moment) > since) >= MAX_CODES_PER_HOUR


def attempts_left_message(attempts: int) -> str:
    """What a wrong code answers, counting down honestly."""
    left = MAX_ATTEMPTS - attempts
    if left <= 0:
        return "Código errado de novo. Pede um código novo."
    tries = "falta 1 tentativa" if left == 1 else f"faltam {left} tentativas"
    return f"Código errado. Confere o e-mail e tenta de novo — {tries}."
