"""A card is public only once it was shared (audit AL-3).

**Adds a column to an existing table — `create_all` cannot apply this.** Run
`alembic upgrade head` on Railway (see `alembic/README-migrations.md`); the
startup catch-up adds it meanwhile, idempotently.

Every existing card starts unpublished, so every link sent before 26/09
answers 404 until its owner shares it again. That is deliberate: nobody
chose to publish those under a rule that said so. If a card with a recorded
share should stay public, the README gives the statement that republishes
exactly those.

Revision ID: 017
Revises: 016
Create Date: 2026-09-26
"""

from alembic import op

revision = "017"
down_revision = "016"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute(
        "ALTER TABLE cards ADD COLUMN IF NOT EXISTS published_at TIMESTAMP WITH TIME ZONE"
    )


def downgrade() -> None:
    op.execute("ALTER TABLE cards DROP COLUMN IF EXISTS published_at")
