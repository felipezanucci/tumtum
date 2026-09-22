"""The level above one night: tour, club, championship (#33).

Three new tables and no change to an existing one, because the deployed
server creates tables at startup but never alters them (open item 16).

Revision ID: 012
Revises: 011
Create Date: 2026-09-22
"""

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision = "012"
down_revision = "011"
branch_labels = None
depends_on = None

UUID = postgresql.UUID(as_uuid=True)


def upgrade() -> None:
    op.create_table(
        "event_series",
        sa.Column(
            "id", UUID, primary_key=True, server_default=sa.text("gen_random_uuid()")
        ),
        sa.Column("name", sa.String(255), nullable=False),
        sa.Column("kind", sa.String(16), nullable=False, server_default="tour"),
        sa.Column(
            "created_at", sa.DateTime(timezone=True), server_default=sa.text("now()")
        ),
    )
    op.create_table(
        "event_series_members",
        sa.Column("event_id", UUID, sa.ForeignKey("events.id"), primary_key=True),
        sa.Column("series_id", UUID, sa.ForeignKey("event_series.id"), nullable=False),
    )
    op.create_index(
        "ix_event_series_members_series_id", "event_series_members", ["series_id"]
    )
    op.create_table(
        "series_posts",
        sa.Column("post_id", UUID, sa.ForeignKey("event_posts.id"), primary_key=True),
        sa.Column("series_id", UUID, sa.ForeignKey("event_series.id"), nullable=False),
        sa.Column(
            "created_at", sa.DateTime(timezone=True), server_default=sa.text("now()")
        ),
    )
    op.create_index("ix_series_posts_series_id", "series_posts", ["series_id"])


def downgrade() -> None:
    op.drop_table("series_posts")
    op.drop_table("event_series_members")
    op.drop_table("event_series")
