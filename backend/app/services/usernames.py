"""The @ of an account: one owner, chosen once (28/09).

Until 28/09 the @ lived only on the phone, checked against four names
written into the app, so two accounts could hold the same one and the
screen said "disponível" to every name. It is now a column with a unique
index on its lower-case form, checked while the person types
(`GET /api/auth/username/{name}`), again when the sign-up code is asked
for, and a last time when the account is made.

What a name may be: 3 to 20 characters, lower-case letters, digits and
underscore. Only an account holds an @: a sign-up still waiting for its code
does not, because the person correcting a mistyped address would then be
told their own name "já tem dono" (found by the site's stream, 28/09). Two
people typing the same free name at once both get a code, and the second to
confirm is told at that moment. A handful of names are the platform's and
never anybody's.
"""

import re
import uuid

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.user import User

MIN_LENGTH = 3
MAX_LENGTH = 20
_PATTERN = re.compile(r"^[a-z0-9_]+$")

# The platform's own words, and the ones that would read as speaking for it.
RESERVED = frozenset(
    {
        "admin",
        "administrador",
        "ajuda",
        "api",
        "encarregado",
        "equipe",
        "oi",
        "operador",
        "privacidade",
        "suporte",
        "tumtum",
        "tumtumoficial",
    }
)

TOO_SHORT = f"O @ precisa de pelo menos {MIN_LENGTH} letras ou números."
TOO_LONG = f"O @ pode ter até {MAX_LENGTH} caracteres."
BAD_CHARS = "O @ só aceita letras sem acento, números e _."
RESERVED_NAME = "Esse @ é da TumTum."
TAKEN = "Esse @ já tem dono. Tenta outro."


def clean(raw: str) -> str:
    """The name as stored: no @ in front, no spaces around, lower case."""
    return raw.strip().lstrip("@").strip().lower()


def problem(name: str) -> str | None:
    """Why `name` (already cleaned) cannot be an @, or None."""
    if len(name) < MIN_LENGTH:
        return TOO_SHORT
    if len(name) > MAX_LENGTH:
        return TOO_LONG
    if not _PATTERN.match(name):
        return BAD_CHARS
    if name in RESERVED:
        return RESERVED_NAME
    return None


async def taken(
    db: AsyncSession,
    name: str,
    *,
    except_user_id: uuid.UUID | None = None,
) -> bool:
    """Whether an account already holds it, whatever the case."""
    owner = select(User.id).where(func.lower(User.username) == name)
    if except_user_id is not None:
        owner = owner.where(User.id != except_user_id)
    return (await db.execute(owner.limit(1))).first() is not None


async def free_from(db: AsyncSession, seed: str) -> str:
    """A free @ made from a name or an address, for a client that sent none.

    The site's sign-up asks for one since 28/09; a build from before it does
    not, and its account still gets an @ nobody else has.
    """
    base = re.sub(r"[^a-z0-9_]", "", clean(seed.split("@")[0]))[: MAX_LENGTH - 4]
    if len(base) < MIN_LENGTH:
        base = (base + "fa")[:MIN_LENGTH].ljust(MIN_LENGTH, "x")
    candidate = base
    for n in range(2, 10_000):
        if candidate not in RESERVED and not await taken(db, candidate):
            return candidate
        candidate = f"{base}{n}"
    return f"{base}{uuid.uuid4().hex[:4]}"
