"""Drop the provider tokens nobody used from `wearable_connections`.

**Alters an existing table — `create_all` cannot apply this, and the startup
catch-up deliberately does not either** (it only ever adds). Run `alembic
upgrade head` on Railway (see `alembic/README-migrations.md`). Until then
the columns sit there unmapped and, being nullable, harmless: the model no
longer reads or writes them.

`access_token` and `refresh_token` were accepted from clients and stored in
plain text; nothing on the server ever used them. An idle credential to
somebody's health account is a liability with no feature attached.

The downgrade restores the columns empty — what they held is gone.

Revision ID: 022
Revises: 021
Create Date: 2026-09-26
"""

from alembic import op

revision = "022"
down_revision = "021"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute("ALTER TABLE wearable_connections DROP COLUMN IF EXISTS access_token")
    op.execute("ALTER TABLE wearable_connections DROP COLUMN IF EXISTS refresh_token")


def downgrade() -> None:
    op.execute(
        "ALTER TABLE wearable_connections "
        "ADD COLUMN IF NOT EXISTS access_token VARCHAR(1000)"
    )
    op.execute(
        "ALTER TABLE wearable_connections "
        "ADD COLUMN IF NOT EXISTS refresh_token VARCHAR(1000)"
    )
