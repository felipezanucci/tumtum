"""Initial schema — users, wearables, events, HR sessions, HR data

Revision ID: 001
Revises: None
Create Date: 2026-04-02

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID, JSONB

revision: str = "001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # Users
    op.create_table(
        "users",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("email", sa.String(255), unique=True, nullable=False, index=True),
        sa.Column("name", sa.String(255), nullable=False),
        sa.Column("avatar_url", sa.String(500)),
        sa.Column("auth_provider", sa.String(50), nullable=False),
        sa.Column("auth_provider_id", sa.String(255)),
        sa.Column("hashed_password", sa.String(255)),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()")),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()")),
    )

    # Wearable connections
    op.create_table(
        "wearable_connections",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("user_id", UUID(as_uuid=True), sa.ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True),
        sa.Column("provider", sa.String(50), nullable=False),
        sa.Column("access_token", sa.String(1000)),
        sa.Column("refresh_token", sa.String(1000)),
        sa.Column("last_sync_at", sa.DateTime(timezone=True)),
        sa.Column("status", sa.String(20), nullable=False, server_default="active"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()")),
    )

    # Events
    op.create_table(
        "events",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("name", sa.String(255), nullable=False),
        sa.Column("subtitle", sa.String(255)),
        sa.Column("venue", sa.String(255)),
        sa.Column("city", sa.String(100)),
        sa.Column("country", sa.String(100)),
        sa.Column("date", sa.Date, nullable=False),
        sa.Column("start_time", sa.Time(timezone=True)),
        sa.Column("end_time", sa.Time(timezone=True)),
        sa.Column("event_type", sa.String(50), nullable=False),
        sa.Column("external_id", sa.String(255)),
        sa.Column("cover_image_url", sa.String(500)),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()")),
    )

    # Event timeline
    op.create_table(
        "event_timeline",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("event_id", UUID(as_uuid=True), sa.ForeignKey("events.id", ondelete="CASCADE"), nullable=False, index=True),
        sa.Column("timestamp", sa.DateTime(timezone=True), nullable=False),
        sa.Column("label", sa.String(255), nullable=False),
        sa.Column("entry_type", sa.String(50), nullable=False),
        sa.Column("metadata", JSONB),
    )

    # HR sessions
    op.create_table(
        "hr_sessions",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("user_id", UUID(as_uuid=True), sa.ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True),
        sa.Column("event_id", UUID(as_uuid=True), sa.ForeignKey("events.id", ondelete="SET NULL"), index=True),
        sa.Column("start_time", sa.DateTime(timezone=True), nullable=False),
        sa.Column("end_time", sa.DateTime(timezone=True), nullable=False),
        sa.Column("avg_bpm", sa.Integer),
        sa.Column("max_bpm", sa.Integer),
        sa.Column("min_bpm", sa.Integer),
        sa.Column("data_quality_score", sa.Integer),
        sa.Column("source_device", sa.String(100)),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()")),
    )

    # HR data points (TimescaleDB hypertable)
    op.create_table(
        "hr_data",
        sa.Column("time", sa.DateTime(timezone=True), nullable=False),
        sa.Column("session_id", UUID(as_uuid=True), sa.ForeignKey("hr_sessions.id", ondelete="CASCADE"), nullable=False),
        sa.Column("bpm", sa.SmallInteger, nullable=False),
        sa.Column("rr_interval_ms", sa.SmallInteger),
        sa.Column("motion_level", sa.SmallInteger),
        sa.Column("source", sa.String(50)),
        sa.PrimaryKeyConstraint("time", "session_id"),
    )
    op.create_index("ix_hr_data_session_id", "hr_data", ["session_id"])

    # Convert hr_data to TimescaleDB hypertable (1-day chunks) if available
    op.execute("""
        DO $$
        BEGIN
            IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'timescaledb') THEN
                PERFORM create_hypertable('hr_data', 'time', chunk_time_interval => INTERVAL '1 day');
            END IF;
        END $$;
    """)


def downgrade() -> None:
    op.drop_table("hr_data")
    op.drop_table("hr_sessions")
    op.drop_table("event_timeline")
    op.drop_table("events")
    op.drop_table("wearable_connections")
    op.drop_table("users")
