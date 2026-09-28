"""`users.username` and `signup_codes.username`: the @ with one owner (28/09).

**Adds columns to existing tables — `create_all` cannot apply this.** Run
`alembic upgrade head` on Railway (see `alembic/README-migrations.md`); the
startup catch-up (`app/core/schema_catchup.py`) adds both and the index
meanwhile, idempotently.

Until 28/09 the @ lived only on the phone. Now it is unique on its
lower-case form (`ix_users_username_lower`); accounts made before it hold
none and are given one at their next sign-in.

Revision ID: 026
Revises: 025
Create Date: 2026-09-28
"""

from alembic import op

revision = "026"
down_revision = "025"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(20)")
    op.execute(
        "ALTER TABLE signup_codes ADD COLUMN IF NOT EXISTS username VARCHAR(20)"
    )
    op.execute(
        "CREATE UNIQUE INDEX IF NOT EXISTS ix_users_username_lower "
        "ON users (lower(username))"
    )


def downgrade() -> None:
    op.execute("DROP INDEX IF EXISTS ix_users_username_lower")
    op.execute("ALTER TABLE signup_codes DROP COLUMN IF EXISTS username")
    op.execute("ALTER TABLE users DROP COLUMN IF EXISTS username")
