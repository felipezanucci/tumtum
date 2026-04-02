"""Add cards and shares tables

Revision ID: 003
Revises: 002
Create Date: 2026-04-02

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID, JSONB

revision: str = "003"
down_revision: Union[str, None] = "002"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "cards",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("user_id", UUID(as_uuid=True), sa.ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True),
        sa.Column("session_id", UUID(as_uuid=True), sa.ForeignKey("hr_sessions.id", ondelete="CASCADE"), nullable=False, index=True),
        sa.Column("peak_id", UUID(as_uuid=True), sa.ForeignKey("peaks.id", ondelete="SET NULL")),
        sa.Column("card_type", sa.String(20), nullable=False),
        sa.Column("image_url", sa.String(500)),
        sa.Column("video_url", sa.String(500)),
        sa.Column("metadata", JSONB),
        sa.Column("status", sa.String(20), nullable=False, server_default="pending"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()")),
    )

    op.create_table(
        "shares",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("card_id", UUID(as_uuid=True), sa.ForeignKey("cards.id", ondelete="CASCADE"), nullable=False, index=True),
        sa.Column("platform", sa.String(50), nullable=False),
        sa.Column("shared_at", sa.DateTime(timezone=True), server_default=sa.text("now()")),
    )


def downgrade() -> None:
    op.drop_table("shares")
    op.drop_table("cards")
