"""Delete every night of one event, and everything made from them.

    cd backend && python scripts/purge_event_data.py <event_id> [--yes]

The pilot wipe (LGPD audit, CR-3): the participants of a pilot are promised
their data is deleted on a stated date, and this is the one command that
keeps that promise. For the event given it deletes every night (of every
account), its readings, its moments, the cards made from it with their
shares and cached images, and every feed post of the event with its
reactions, reports and tour-wide consent. The event and its timeline stay —
they are TumTum's, not anybody's data.

Without `--yes` it only counts what it would delete. It uses the app's own
engine, so it reads `DATABASE_URL` (and `DATABASE_SSL`) like the server.
"""

import argparse
import asyncio
import sys
import uuid
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))


async def purge(event_id: uuid.UUID, really: bool) -> dict:
    from sqlalchemy import func, select

    import app.main  # noqa: F401 — registers every model
    from app.core.database import async_session
    from app.models.card import Card
    from app.models.event import Event
    from app.models.event_post import EventPost
    from app.models.hr_data import HRData
    from app.models.hr_session import HRSession
    from app.services.night_deletion import delete_nights, delete_posts

    async with async_session() as db:
        event = await db.get(Event, event_id)
        if event is None:
            raise SystemExit(f"No event {event_id}.")
        nights = list(
            (
                await db.execute(
                    select(HRSession.id).where(HRSession.event_id == event_id)
                )
            )
            .scalars()
            .all()
        )
        posts = list(
            (
                await db.execute(
                    select(EventPost.id).where(EventPost.event_id == event_id)
                )
            )
            .scalars()
            .all()
        )
        counts = {
            "event": f"{event.name} ({event.date})",
            "nights": len(nights),
            "readings": (
                await db.execute(
                    select(func.count())
                    .select_from(HRData)
                    .where(HRData.session_id.in_(nights))
                )
            ).scalar_one()
            if nights
            else 0,
            "cards": (
                await db.execute(
                    select(func.count())
                    .select_from(Card)
                    .where(Card.session_id.in_(nights))
                )
            ).scalar_one()
            if nights
            else 0,
            "posts": len(posts),
        }
        if really:
            # Posts first: some may point at no night at all.
            await delete_posts(db, posts)
            await delete_nights(db, nights)
            await db.commit()
        return counts


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("event_id", type=uuid.UUID)
    parser.add_argument(
        "--yes", action="store_true", help="delete; without it, only count"
    )
    args = parser.parse_args()
    counts = asyncio.run(purge(args.event_id, args.yes))
    verb = "Deleted" if args.yes else "Would delete (run again with --yes)"
    print(f"{verb}: {counts}")


if __name__ == "__main__":
    main()
