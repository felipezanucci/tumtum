"""The columns `create_all` cannot add, added at startup where it is safe.

The deployed server has never run Alembic (decision log, open item 16):
startup calls `Base.metadata.create_all`, which creates missing *tables* and
never touches an existing one. Until 26/09 the project lived with that by
never adding a column to an old table. The LGPD remediation cannot — a birth
date belongs on `users`, a publication date on `cards` — and a mapped column
missing from the database is not a missing feature, it is every query on
that table failing: `users` is loaded on every authenticated request, with
its `hr_sessions` alongside.

So this runs, after `create_all`, the *additive* part of migrations 015–017
and 023, each statement idempotent (`ADD COLUMN IF NOT EXISTS`, PostgreSQL
≥ 9.6). 023 (the consent ledger) also needs its new columns filled for the
rows already there before they can be NOT NULL; `backfills()` does that —
each `UPDATE` touches only rows still NULL, and `SET NOT NULL` on a column
that already is one is a no-op, so every startup may run it again.
Nothing here drops, renames or retypes anything; the destructive steps
(022: dropping the wearable tokens) wait for a person to run Alembic. The
migrations themselves use the same `IF NOT EXISTS`, so running them after
this has run is a no-op rather than an error. See
`alembic/README-migrations.md`.
"""

from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncConnection

# (table, column, definition) — in the order of the migrations that own them.
ADDITIVE_COLUMNS = (
    ("users", "birth_date", "DATE"),  # 015
    ("signup_codes", "birth_date", "DATE"),  # 015
    ("signup_codes", "consent_text_version", "VARCHAR(20)"),  # 015
    ("signup_codes", "read_heart_rate", "BOOLEAN NOT NULL DEFAULT false"),  # 015
    ("hr_sessions", "analyzed_at", "TIMESTAMP WITH TIME ZONE"),  # 016
    ("cards", "published_at", "TIMESTAMP WITH TIME ZONE"),  # 017
    # 023 — added nullable, filled by `backfills()`, then made NOT NULL there.
    ("consents", "legal_basis", "VARCHAR(40)"),
    ("consents", "scope", "VARCHAR(200)"),
    ("consents", "proof", "VARCHAR(64)"),
)


def statements() -> list[str]:
    return [
        f"ALTER TABLE {table} ADD COLUMN IF NOT EXISTS {column} {definition}"
        for table, column, definition in ADDITIVE_COLUMNS
    ]


def _quote(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def _case(mapping: dict[str, str], default: str) -> str:
    whens = " ".join(
        f"WHEN {_quote(purpose)} THEN {_quote(value)}"
        for purpose, value in mapping.items()
    )
    return f"CASE purpose {whens} ELSE {default} END"


def backfills() -> list[str]:
    """023: give the consent rows written before it their ledger fields.

    The basis and the scope follow the purpose; the proof is the same
    SHA-256 `services.consents.proof_of` computes, done by Postgres (≥ 11).
    Only then are `legal_basis` and `proof` made NOT NULL.
    """
    from app.services.consents import LEGAL_BASES, SCOPES

    return [
        "UPDATE consents SET legal_basis = "
        f"{_case(LEGAL_BASES, _quote('consent_art11'))} WHERE legal_basis IS NULL",
        f"UPDATE consents SET scope = {_case(SCOPES, 'NULL')} WHERE scope IS NULL",
        "UPDATE consents SET proof = encode(sha256(convert_to("
        "purpose || ':' || text_version, 'UTF8')), 'hex') WHERE proof IS NULL",
        "ALTER TABLE consents ALTER COLUMN legal_basis SET NOT NULL",
        "ALTER TABLE consents ALTER COLUMN proof SET NOT NULL",
    ]


async def catch_up(conn: AsyncConnection) -> None:
    """Add the 26/09 columns if they are missing. PostgreSQL only.

    Anything else (the tests' SQLite) got every column from `create_all`,
    because its tables were created from today's models.
    """
    if conn.dialect.name != "postgresql":
        return
    for statement in statements() + backfills():
        await conn.execute(text(statement))
