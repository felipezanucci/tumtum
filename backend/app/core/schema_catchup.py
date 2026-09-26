"""The columns `create_all` cannot add, added at startup where it is safe.

The deployed server has never run Alembic (decision log, open item 16):
startup calls `Base.metadata.create_all`, which creates missing *tables* and
never touches an existing one. Until 26/09 the project lived with that by
never adding a column to an old table. The LGPD remediation cannot — a birth
date belongs on `users`, a publication date on `cards` — and a mapped column
missing from the database is not a missing feature, it is every query on
that table failing: `users` is loaded on every authenticated request, with
its `hr_sessions` alongside.

So this runs, after `create_all`, the *additive* part of migrations 015–017,
each statement idempotent (`ADD COLUMN IF NOT EXISTS`, PostgreSQL ≥ 9.6).
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
)


def statements() -> list[str]:
    return [
        f"ALTER TABLE {table} ADD COLUMN IF NOT EXISTS {column} {definition}"
        for table, column, definition in ADDITIVE_COLUMNS
    ]


async def catch_up(conn: AsyncConnection) -> None:
    """Add the 26/09 columns if they are missing. PostgreSQL only.

    Anything else (the tests' SQLite) got every column from `create_all`,
    because its tables were created from today's models.
    """
    if conn.dialect.name != "postgresql":
        return
    for statement in statements():
        await conn.execute(text(statement))
