"""Add peaks table

Revision ID: 002
Revises: 001
Create Date: 2026-04-02

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID

revision: str = "002"
down_revision: Union[str, None] = "001"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "peaks",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("session_id", UUID(as_uuid=True), sa.ForeignKey("hr_sessions.id", ondelete="CASCADE"), nullable=False, index=True),
        sa.Column("timestamp", sa.DateTime(timezone=True), nullable=False),
        sa.Column("bpm", sa.Integer, nullable=False),
        sa.Column("duration_seconds", sa.Integer, nullable=False),
        sa.Column("magnitude", sa.Float, nullable=False),
        sa.Column("timeline_entry_id", UUID(as_uuid=True), sa.ForeignKey("event_timeline.id", ondelete="SET NULL")),
        sa.Column("rank", sa.SmallInteger),
    )


def downgrade() -> None:
    op.drop_table("peaks")
