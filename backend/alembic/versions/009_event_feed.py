"""The per-event feed: posts and the one reaction there is.

Only people with a measured night at the event may read, post or react —
that gate is an `hr_sessions` lookup, so it needs no table of its own.

Revision ID: 009
Revises: 008
Create Date: 2026-09-22
"""

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision = "009"
down_revision = "008"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.create_table(
        "event_posts",
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
        sa.Column(
            "user_id",
            postgresql.UUID(as_uuid=True),
            sa.ForeignKey("users.id"),
            nullable=False,
        ),
        sa.Column(
            "session_id",
            postgresql.UUID(as_uuid=True),
            sa.ForeignKey("hr_sessions.id"),
            nullable=True,
        ),
        sa.Column("bpm", sa.Integer(), nullable=False),
        sa.Column("moment_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("label", sa.String(length=255), nullable=True),
        sa.Column("quote", sa.String(length=280), nullable=True),
        sa.Column("skin", sa.String(length=20), nullable=False, server_default="BLACK"),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
    )
    op.create_index("ix_event_posts_event_id", "event_posts", ["event_id"])
    op.create_index("ix_event_posts_user_id", "event_posts", ["user_id"])
    op.create_index("ix_event_posts_session_id", "event_posts", ["session_id"])

    op.create_table(
        "event_post_reactions",
        sa.Column(
            "id",
            postgresql.UUID(as_uuid=True),
            primary_key=True,
            server_default=sa.text("gen_random_uuid()"),
        ),
        sa.Column(
            "post_id",
            postgresql.UUID(as_uuid=True),
            sa.ForeignKey("event_posts.id"),
            nullable=False,
        ),
        sa.Column(
            "user_id",
            postgresql.UUID(as_uuid=True),
            sa.ForeignKey("users.id"),
            nullable=False,
        ),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.UniqueConstraint("post_id", "user_id", name="uq_event_post_reaction"),
    )
    op.create_index(
        "ix_event_post_reactions_post_id", "event_post_reactions", ["post_id"]
    )
    op.create_index(
        "ix_event_post_reactions_user_id", "event_post_reactions", ["user_id"]
    )


def downgrade() -> None:
    op.drop_table("event_post_reactions")
    op.drop_table("event_posts")
