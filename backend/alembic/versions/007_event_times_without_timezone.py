"""Drop the timezone from the event's start and end times.

Revision ID: 007
Revises: 006
Create Date: 2026-08-27

`events.start_time` and `events.end_time` were created as
`TIME WITH TIME ZONE`. That type takes an offset but carries no date, so it
cannot account for a daylight rule and cannot say what "22:00" means anywhere
— which is why Postgres's own documentation calls its usefulness
questionable. What an event has is a wall-clock hour on a given date, and that
is a plain `time`.

It also broke in practice, not just in principle: asyncpg encodes `timetz` as
`obj.tzinfo.utcoffset(None)`, so writing the naive `datetime.time` that comes
from parsing "22:00:00" raised inside the driver and reached the browser as a
bare "Erro interno do servidor". Found on 2026-08-27 trying to put the
Realness Festival's hours on its event.

**This migration is dormant in production.** The deployed app does not run
Alembic — `app/main.py` calls `Base.metadata.create_all` on startup, which
creates missing tables and never alters an existing column. So the running fix
is the coercion in `app/schemas/event.py`; this migration is what makes the
schema right for anybody who does run migrations, and what makes that
coercion a no-op when they do.

The cast keeps the wall-clock digits and discards the offset, which is all
that was ever displayed: the frontend reads these values as HH:MM.
"""

import sqlalchemy as sa
from alembic import op

revision = "007"
down_revision = "006"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.alter_column(
        "events",
        "start_time",
        existing_type=sa.Time(timezone=True),
        type_=sa.Time(),
        existing_nullable=True,
        postgresql_using="start_time::time",
    )
    op.alter_column(
        "events",
        "end_time",
        existing_type=sa.Time(timezone=True),
        type_=sa.Time(),
        existing_nullable=True,
        postgresql_using="end_time::time",
    )


def downgrade() -> None:
    op.alter_column(
        "events",
        "start_time",
        existing_type=sa.Time(),
        type_=sa.Time(timezone=True),
        existing_nullable=True,
        postgresql_using="start_time::timetz",
    )
    op.alter_column(
        "events",
        "end_time",
        existing_type=sa.Time(),
        type_=sa.Time(timezone=True),
        existing_nullable=True,
        postgresql_using="end_time::timetz",
    )
