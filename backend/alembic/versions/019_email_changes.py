"""A new e-mail waits for its code here (art. 18, III — correction).

A new table — `create_all` creates it without Alembic.

Revision ID: 019
Revises: 018
Create Date: 2026-09-26
"""

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision = "019"
down_revision = "018"
branch_labels = None
depends_on = None

UUID = postgresql.UUID(as_uuid=True)


def upgrade() -> None:
    op.create_table(
        "email_changes",
        sa.Column(
            "id", UUID, primary_key=True, server_default=sa.text("gen_random_uuid()")
        ),
        sa.Column(
            "user_id",
            UUID,
            sa.ForeignKey("users.id", ondelete="CASCADE"),
            nullable=False,
        ),
        sa.Column("new_email", sa.String(255), nullable=False),
        sa.Column("email_key", sa.String(255), nullable=False),
        sa.Column("code_hash", sa.String(64), nullable=False),
        sa.Column("attempts", sa.Integer(), nullable=False, server_default="0"),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("used_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column(
            "created_at", sa.DateTime(timezone=True), server_default=sa.text("now()")
        ),
    )
    op.create_index("ix_email_changes_user_id", "email_changes", ["user_id"])
    op.create_index("ix_email_changes_email_key", "email_changes", ["email_key"])
    op.create_index("ix_email_changes_created_at", "email_changes", ["created_at"])


def downgrade() -> None:
    op.drop_table("email_changes")
