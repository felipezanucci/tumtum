"""`deletion_tombstones`: what keeps a deleted account deleted after a restore.

Two hashes and a date per deleted account (v1.1 opinion, §11) — see
`app.models.privacy.DeletionTombstone` for why hashes, and
`app.services.tombstones` for the sweep that reads them.

A new table: `create_all` creates it at startup without Alembic, so this uses
`IF NOT EXISTS` throughout and is a no-op on a database that already has it.

Revision ID: 024
Revises: 023
Create Date: 2026-09-26
"""

from alembic import op

revision = "024"
down_revision = "023"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute(
        """
        CREATE TABLE IF NOT EXISTS deletion_tombstones (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            subject_key VARCHAR(64) NOT NULL,
            email_key VARCHAR(64) NOT NULL,
            deleted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
        )
        """
    )
    # The names `create_all` gives them, so either path leaves one index each.
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_deletion_tombstones_subject_key "
        "ON deletion_tombstones (subject_key)"
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_deletion_tombstones_email_key "
        "ON deletion_tombstones (email_key)"
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_deletion_tombstones_deleted_at "
        "ON deletion_tombstones (deleted_at)"
    )


def downgrade() -> None:
    op.execute("DROP TABLE IF EXISTS deletion_tombstones")
