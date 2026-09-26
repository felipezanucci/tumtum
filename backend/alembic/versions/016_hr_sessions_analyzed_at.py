"""When a night's moments were detected — the start of its raw-series clock.

**Adds a column to an existing table — `create_all` cannot apply this.** Run
`alembic upgrade head` on Railway (see `alembic/README-migrations.md`); the
startup catch-up adds it meanwhile, idempotently.

The maintenance loop deletes a night's `hr_data` `RAW_READINGS_RETENTION_DAYS`
after `analyzed_at` (audit AL-9). Nights analysed before 26/09 have none and
are therefore not purged; backfilling it is a decision about deleting old
raw series, left to a person (the README gives the statement).

Revision ID: 016
Revises: 015
Create Date: 2026-09-26
"""

from alembic import op

revision = "016"
down_revision = "015"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute(
        "ALTER TABLE hr_sessions "
        "ADD COLUMN IF NOT EXISTS analyzed_at TIMESTAMP WITH TIME ZONE"
    )


def downgrade() -> None:
    op.execute("ALTER TABLE hr_sessions DROP COLUMN IF EXISTS analyzed_at")
