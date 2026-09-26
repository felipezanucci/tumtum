"""Who read whose heart-rate data, and every API request (audit AL-8).

Two new tables, deliberately without foreign keys — a log the database can
cascade away is not a log. `create_all` creates them without Alembic.

Revision ID: 021
Revises: 020
Create Date: 2026-09-26
"""

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision = "021"
down_revision = "020"
branch_labels = None
depends_on = None

UUID = postgresql.UUID(as_uuid=True)


def upgrade() -> None:
    op.create_table(
        "data_access_log",
        sa.Column(
            "id", UUID, primary_key=True, server_default=sa.text("gen_random_uuid()")
        ),
        sa.Column(
            "at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.text("now()"),
        ),
        sa.Column("actor_user_id", UUID, nullable=True),
        sa.Column("subject_user_id", UUID, nullable=True),
        sa.Column("resource", sa.String(40), nullable=False),
        sa.Column("resource_id", sa.String(64), nullable=True),
        sa.Column("action", sa.String(20), nullable=False),
        sa.Column("ip", sa.String(64), nullable=True),
    )
    op.create_index("ix_data_access_log_at", "data_access_log", ["at"])
    op.create_index(
        "ix_data_access_log_actor_user_id", "data_access_log", ["actor_user_id"]
    )
    op.create_index(
        "ix_data_access_log_subject_user_id", "data_access_log", ["subject_user_id"]
    )

    op.create_table(
        "access_log",
        sa.Column(
            "id", UUID, primary_key=True, server_default=sa.text("gen_random_uuid()")
        ),
        sa.Column(
            "at",
            sa.DateTime(timezone=True),
            nullable=False,
            server_default=sa.text("now()"),
        ),
        sa.Column("method", sa.String(10), nullable=False),
        sa.Column("path", sa.String(500), nullable=False),
        sa.Column("status", sa.SmallInteger(), nullable=False),
        sa.Column("ip", sa.String(64), nullable=True),
        sa.Column("user_id", UUID, nullable=True),
    )
    op.create_index("ix_access_log_at", "access_log", ["at"])
    op.create_index("ix_access_log_user_id", "access_log", ["user_id"])


def downgrade() -> None:
    op.drop_table("access_log")
    op.drop_table("data_access_log")
