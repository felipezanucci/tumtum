"""Add first_name and last_name to waitlist_entries

Revision ID: 005
Revises: 004
Create Date: 2026-08-26

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = "005"
down_revision: Union[str, None] = "004"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # Nullable on purpose: entries collected before these columns existed have
    # no name, and the alternative to nullable is inventing one.
    op.add_column("waitlist_entries", sa.Column("first_name", sa.String(100), nullable=True))
    op.add_column("waitlist_entries", sa.Column("last_name", sa.String(100), nullable=True))


def downgrade() -> None:
    op.drop_column("waitlist_entries", "last_name")
    op.drop_column("waitlist_entries", "first_name")
