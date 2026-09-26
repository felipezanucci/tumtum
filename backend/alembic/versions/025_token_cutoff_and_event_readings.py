"""`users.tokens_valid_after` and `hr_sessions.event_readings` (security review).

**Adds columns to existing tables — `create_all` cannot apply this.** Run
`alembic upgrade head` on Railway (see `alembic/README-migrations.md`); the
startup catch-up (`app/core/schema_catchup.py`) adds both meanwhile,
idempotently, and fills `event_readings` the same way.

- `users.tokens_valid_after`: access tokens issued before it are refused. Set
  by a password reset, an e-mail change and an account deletion, so a stolen
  access token dies with the credential it was issued under instead of
  living out its hour.
- `hr_sessions.event_readings`: how many of a night's readings fall inside
  its event's window, counted at upload. A night is attendance — the feed,
  the crowd — only with `ATTENDANCE_MIN_READINGS` of them. It is a count and
  not a query because the raw readings are deleted a week after analysis,
  and attendance must outlive them.

Nights already carrying an event get the count of **all** their readings
still stored: an approximation (the window is a wall-clock rule in the
display timezone, not something to redo in SQL), generous to the few test
nights that exist, and zero for a night whose readings were already purged —
which then no longer opens its feed.

Revision ID: 025
Revises: 024
Create Date: 2026-09-26
"""

from alembic import op

revision = "025"
down_revision = "024"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute(
        "ALTER TABLE users "
        "ADD COLUMN IF NOT EXISTS tokens_valid_after TIMESTAMP WITH TIME ZONE"
    )
    op.execute(
        "ALTER TABLE hr_sessions ADD COLUMN IF NOT EXISTS event_readings INTEGER"
    )
    op.execute(
        "UPDATE hr_sessions SET event_readings = ("
        "SELECT count(*) FROM hr_data WHERE hr_data.session_id = hr_sessions.id"
        ") WHERE event_readings IS NULL AND event_id IS NOT NULL"
    )


def downgrade() -> None:
    op.execute("ALTER TABLE hr_sessions DROP COLUMN IF EXISTS event_readings")
    op.execute("ALTER TABLE users DROP COLUMN IF EXISTS tokens_valid_after")
