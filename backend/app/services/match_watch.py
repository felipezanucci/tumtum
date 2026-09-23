"""Watching a match live, so its two whistles are measured without a tap (#52).

API-Football's ``periods.first`` and ``periods.second`` were meant to be the
measured anchor for a match's two clocks. Tried on a finished Série A match
on 23/09 (fixture 1492384), both came back **null** — the feed does not keep
them. Felipe's call the same day was the second path: watch the match while
it is played. The fixture's ``status.short`` changes from ``NS`` to ``1H`` at
the kick-off and from ``HT`` to ``2H`` at the restart; polled every fifteen
seconds, each change pins its whistle inside a fifteen-second window, and
the correlator's own window is sixty.

What this writes is the same thing an operator's tap writes — a ``kickoff``
or ``second_half`` entry on the event's timeline — with its own source, so
the timeline says who measured it. The operator's tap stays as the fallback
and, where both exist, the earlier one wins (see
[football_service.anchors_from_timeline]).

Three rules, all of them about not claiming more than was seen:

- **A change is only a whistle if it was seen happen.** If the server starts
  watching with the match already in ``1H`` — a deploy mid-match — it does
  not know when the half began, and records nothing for it.
- **A window too wide is not a measurement.** Two polls more than
  ``MAX_WINDOW_SEC`` apart (a restart, an outage) bound nothing useful; the
  change is noted and not written.
- **The quota is a budget.** The Pro plan is 7,500 requests a day; the
  watcher keeps under ``DAILY_BUDGET`` and leaves the rest to the operator's
  own searches. A match costs about 700.

The state lives in memory, because the backend runs as one process and the
only durable output — the timeline entries — is written to the database the
moment it exists. A restart loses the in-between state and nothing measured.
"""

import asyncio
import logging
import uuid
from dataclasses import dataclass, field
from datetime import UTC, date, datetime, timedelta
from zoneinfo import ZoneInfo

from app.config import settings
from app.services import football_service
from app.services.football_service import KICKOFF, SECOND_HALF

log = logging.getLogger(__name__)

POLL_SEC = 15
# Start watching this long before the scheduled kick-off (kick-offs are
# sometimes early by a minute; the pre-match polls also establish "NS").
LEAD = timedelta(minutes=10)
# Give up on a match this long after its scheduled kick-off, whatever state
# it is in — extra time and penalties fit well inside.
TAIL = timedelta(hours=4)
MAX_WINDOW_SEC = 90
DAILY_BUDGET = 5000

LIVE_SOURCE = "api-football-live"

PRE_MATCH = {"TBD", "NS"}
FINAL = {"FT", "AET", "PEN", "PST", "CANC", "ABD", "AWD", "WO"}

LABELS = {KICKOFF: "Apito inicial", SECOND_HALF: "2º tempo"}


@dataclass
class Watch:
    """One match being watched, and everything it has seen so far."""

    event_id: uuid.UUID
    fixture_id: int
    scheduled: datetime
    status: str | None = None
    seen_at: datetime | None = None
    kickoff_at: datetime | None = None
    second_half_at: datetime | None = None
    finished: bool = False
    rebuilt: bool = False
    polls: int = 0
    notes: list[str] = field(default_factory=list)

    def in_window(self, now: datetime) -> bool:
        return self.scheduled - LEAD <= now <= self.scheduled + TAIL


@dataclass(frozen=True)
class Whistle:
    """A measured half start: the midpoint of the two polls that bound it."""

    entry_type: str
    at: datetime
    window_sec: int


def observe(watch: Watch, status: str, now: datetime) -> Whistle | None:
    """Feed one poll to a watch; return a whistle if this poll pinned one."""
    before, before_at = watch.status, watch.seen_at
    watch.status, watch.seen_at = status, now
    watch.polls += 1

    if status in FINAL:
        watch.finished = True
        return None
    if before is None:
        if status not in PRE_MATCH:
            watch.notes.append(
                f"Comecei a olhar com o jogo já em {status}: o que já tinha "
                "começado fica com o toque do operador."
            )
        return None
    if before == status or before_at is None:
        return None

    if before in PRE_MATCH and status == "1H":
        entry_type = KICKOFF
    elif before == "HT" and status == "2H":
        entry_type = SECOND_HALF
    else:
        return None

    window = int((now - before_at).total_seconds())
    if window > MAX_WINDOW_SEC:
        watch.notes.append(
            f"{LABELS[entry_type]}: passei {window} s sem olhar, largo demais "
            "pra chamar de medido. Fica com o toque do operador."
        )
        return None
    whistle = Whistle(entry_type, before_at + (now - before_at) / 2, window)
    if entry_type == KICKOFF:
        watch.kickoff_at = whistle.at
    else:
        watch.second_half_at = whistle.at
    return whistle


def whistle_entry(whistle: Whistle) -> dict:
    """The timeline row a whistle becomes — what a tap would write, sourced."""
    return {
        "timestamp": whistle.at,
        "label": LABELS[whistle.entry_type],
        "entry_type": whistle.entry_type,
        "metadata": {
            "source": LIVE_SOURCE,
            "clock_source": football_service.CLOCK_LIVE,
            "anchored": True,
            "window_sec": whistle.window_sec,
        },
    }


def fixture_id_of(external_id: str | None) -> int | None:
    prefix = f"{football_service.SOURCE}:"
    if not external_id or not external_id.startswith(prefix):
        return None
    try:
        return int(external_id[len(prefix) :])
    except ValueError:
        return None


class Watcher:
    """The loop: which matches are on, one poll each per tick, and the budget."""

    def __init__(self) -> None:
        self.watches: dict[uuid.UUID, Watch] = {}
        self.spent: dict[date, int] = {}
        self.last_error: str | None = None

    def spent_today(self, now: datetime) -> int:
        return self.spent.get(now.date(), 0)

    def _spend(self, now: datetime) -> bool:
        if self.spent_today(now) >= DAILY_BUDGET:
            return False
        self.spent[now.date()] = self.spent_today(now) + 1
        return True

    async def run_forever(self) -> None:
        while True:
            try:
                await self.tick(datetime.now(UTC))
                self.last_error = None
            except asyncio.CancelledError:
                raise
            except Exception as exc:  # one bad tick must not end the watch
                self.last_error = str(exc)[:300]
                log.exception("match watch tick failed")
            await asyncio.sleep(POLL_SEC)

    async def tick(self, now: datetime) -> None:
        from app.core.database import async_session

        async with async_session() as db:
            await self._discover(db, now)
            for watch in list(self.watches.values()):
                if watch.finished and watch.rebuilt:
                    continue
                if not watch.in_window(now):
                    continue
                await self._poll(db, watch, now)
            await db.commit()

    async def _discover(self, db, now: datetime) -> None:
        """Pick up matches attached to an event whose date is today or yesterday."""
        from sqlalchemy import select

        from app.models.event import Event

        today = now.astimezone(ZoneInfo(settings.display_timezone)).date()
        rows = await db.execute(
            select(Event.id, Event.external_id).where(
                Event.date.in_([today, today - timedelta(days=1)]),
                Event.external_id.like(f"{football_service.SOURCE}:%"),
            )
        )
        for event_id, external_id in rows.all():
            fixture_id = fixture_id_of(external_id)
            known = self.watches.get(event_id)
            if fixture_id is None or (known and known.fixture_id == fixture_id):
                continue
            if not self._spend(now):
                return
            fixture = await football_service.get_fixture(fixture_id)
            if fixture:
                self.watches[event_id] = Watch(
                    event_id=event_id,
                    fixture_id=fixture_id,
                    scheduled=football_service.scheduled_kickoff(fixture),
                )

    async def _poll(self, db, watch: Watch, now: datetime) -> None:
        if not watch.finished:
            if not self._spend(now):
                if "orçamento" not in " ".join(watch.notes):
                    watch.notes.append(
                        "Parei de olhar: o orçamento de consultas do dia acabou."
                    )
                return
            fixture = await football_service.get_fixture(watch.fixture_id)
            status = ((fixture or {}).get("fixture", {}).get("status") or {}).get(
                "short"
            )
            if not status:
                return
            whistle = observe(watch, status, now)
            if whistle is not None:
                await _write(db, watch.event_id, whistle)
        if watch.finished and not watch.rebuilt:
            # The whistle is final: lay the goals on the measured clock.
            from app.api.events import rebuild_football_timeline

            await rebuild_football_timeline(db, watch.event_id, watch.fixture_id)
            watch.rebuilt = True

    def status_for(self, event_id: uuid.UUID) -> dict:
        watch = self.watches.get(event_id)
        if watch is None:
            return {"state": "idle"}
        now = datetime.now(UTC)
        if watch.finished:
            state = "done"
        elif watch.in_window(now):
            state = "watching"
        elif now < watch.scheduled - LEAD:
            state = "waiting"
        else:
            state = "done"
        return {
            "state": state,
            "status": watch.status,
            "scheduled": watch.scheduled,
            "last_polled_at": watch.seen_at,
            "kickoff_at": watch.kickoff_at,
            "second_half_at": watch.second_half_at,
            "polls": watch.polls,
            "notes": list(watch.notes),
            "spent_today": self.spent_today(now),
            "budget": DAILY_BUDGET,
            "last_error": self.last_error,
        }


async def _write(db, event_id: uuid.UUID, whistle: Whistle) -> None:
    from app.models.event_timeline import EventTimeline

    entry = whistle_entry(whistle)
    db.add(
        EventTimeline(
            event_id=event_id,
            timestamp=entry["timestamp"],
            label=entry["label"],
            entry_type=entry["entry_type"],
            metadata_=entry["metadata"],
        )
    )
    await db.flush()


watcher = Watcher()
