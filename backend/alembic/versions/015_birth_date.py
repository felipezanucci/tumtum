"""Birth date on accounts and pending sign-ups; the sign-up's consent (CR-4).

**Adds columns to existing tables — `create_all` cannot apply this.** Run
`alembic upgrade head` on Railway (see `alembic/README-migrations.md`). Until
then the startup catch-up (`app/core/schema_catchup.py`) adds the same
columns with `ADD COLUMN IF NOT EXISTS`, and this migration uses the same
form so that running it afterwards is a no-op, not an error.

- `users.birth_date` — null for accounts made before 26/09 (asked once).
- `signup_codes.birth_date`, `consent_text_version`, `read_heart_rate` —
  what the account and its consent rows are born with on confirmation.

Revision ID: 015
Revises: 014
Create Date: 2026-09-26
"""

from alembic import op

revision = "015"
down_revision = "014"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS birth_date DATE")
    op.execute("ALTER TABLE signup_codes ADD COLUMN IF NOT EXISTS birth_date DATE")
    op.execute(
        "ALTER TABLE signup_codes "
        "ADD COLUMN IF NOT EXISTS consent_text_version VARCHAR(20)"
    )
    op.execute(
        "ALTER TABLE signup_codes "
        "ADD COLUMN IF NOT EXISTS read_heart_rate BOOLEAN NOT NULL DEFAULT false"
    )


def downgrade() -> None:
    op.execute("ALTER TABLE signup_codes DROP COLUMN IF EXISTS read_heart_rate")
    op.execute("ALTER TABLE signup_codes DROP COLUMN IF EXISTS consent_text_version")
    op.execute("ALTER TABLE signup_codes DROP COLUMN IF EXISTS birth_date")
    op.execute("ALTER TABLE users DROP COLUMN IF EXISTS birth_date")
