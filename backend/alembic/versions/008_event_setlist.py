"""The operator's setlist for a show.

A concert has no API that says which song was playing at 22h12, so a person
taps and the tap is the measurement. This table holds the order pasted before
the show and the instant each song really started.

Revision ID: 008
Revises: 007
Create Date: 2026-09-22
"""

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision = "008"
down_revision = "007"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.create_table(
        "event_setlist",
        sa.Column(
            "id",
            postgresql.UUID(as_uuid=True),
            primary_key=True,
            server_default=sa.text("gen_random_uuid()"),
        ),
        sa.Column(
            "event_id",
            postgresql.UUID(as_uuid=True),
            sa.ForeignKey("events.id"),
            nullable=False,
        ),
        sa.Column("position", sa.Integer(), nullable=False),
        sa.Column("title", sa.String(length=255), nullable=False),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=True),
        sa.UniqueConstraint("event_id", "position", name="uq_event_setlist_position"),
    )
    op.create_index("ix_event_setlist_event_id", "event_setlist", ["event_id"])


def downgrade() -> None:
    op.drop_index("ix_event_setlist_event_id", table_name="event_setlist")
    op.drop_table("event_setlist")
