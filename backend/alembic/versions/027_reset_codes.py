"""`password_reset_tokens.code_hash` and `.attempts`: a reset in the app (02/10).

**Adds columns to an existing table — `create_all` cannot apply this.** Run
`alembic upgrade head` on Railway (see `alembic/README-migrations.md`); the
startup catch-up (`app/core/schema_catchup.py`) adds both meanwhile,
idempotently.

The "esqueci a senha" mail carried only a link to the site, so a person in
the app had to leave it for a browser. The same mail now carries a 6-digit
code the app takes back; the table keeps a keyed hash of it and the count of
wrong guesses.

Revision ID: 027
Revises: 026
Create Date: 2026-10-02
"""

from alembic import op

revision = "027"
down_revision = "026"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute(
        "ALTER TABLE password_reset_tokens "
        "ADD COLUMN IF NOT EXISTS code_hash VARCHAR(64)"
    )
    op.execute(
        "ALTER TABLE password_reset_tokens "
        "ADD COLUMN IF NOT EXISTS attempts INTEGER NOT NULL DEFAULT 0"
    )


def downgrade() -> None:
    op.execute("ALTER TABLE password_reset_tokens DROP COLUMN IF EXISTS attempts")
    op.execute("ALTER TABLE password_reset_tokens DROP COLUMN IF EXISTS code_hash")
