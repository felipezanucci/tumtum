"""Everything TumTum holds about one person, gathered for them (LGPD art. 18).

Access (II) and portability (V) are one query plan: `collect` reads every
table that points at the person and returns it in the shapes of
`schemas/privacy.py`. The readings are the only part left out unless asked
for — a four-hour night is ~14,000 of them, which belongs in a download, not
in a page the profile opens.
"""

import csv
import io
import uuid

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.card import Card, Share
from app.models.event import Event
from app.models.event_post import EventPost, EventPostReaction
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.moderation import PostReport, UserBlock
from app.models.peak import Peak
from app.models.privacy import DataSubjectRequest
from app.models.user import User
from app.schemas.consent import ConsentHistoryEntry
from app.schemas.feed import FeedAuthor
from app.schemas.privacy import (
    DataBlock,
    DataCard,
    DataEvent,
    DataPeak,
    DataPoint,
    DataPost,
    DataReaction,
    DataReport,
    DataSession,
    DataShare,
    DataSubjectRequestResponse,
    DataUser,
    ExportSession,
    MyDataResponse,
    MyExportResponse,
)
from app.services import consents


async def _all(db: AsyncSession, query) -> list:
    return list((await db.execute(query)).scalars().all())


async def collect(
    db: AsyncSession, user: User, with_readings: bool = False
) -> MyDataResponse | MyExportResponse:
    sessions = await _all(
        db,
        select(HRSession)
        .where(HRSession.user_id == user.id)
        .order_by(HRSession.start_time),
    )
    session_ids = [s.id for s in sessions]
    event_ids = {s.event_id for s in sessions if s.event_id}
    events = (
        {e.id: e for e in await _all(db, select(Event).where(Event.id.in_(event_ids)))}
        if event_ids
        else {}
    )

    readings: dict[uuid.UUID, list[DataPoint]] = {}
    if with_readings and session_ids:
        rows = await db.execute(
            select(HRData.session_id, HRData.time, HRData.bpm)
            .where(HRData.session_id.in_(session_ids))
            .order_by(HRData.session_id, HRData.time)
        )
        for session_id, time, bpm in rows.all():
            readings.setdefault(session_id, []).append(DataPoint(time=time, bpm=bpm))

    session_cls = ExportSession if with_readings else DataSession
    out_sessions = []
    for s in sessions:
        event = events.get(s.event_id) if s.event_id else None
        fields = {
            "id": s.id,
            "event": DataEvent(id=event.id, name=event.name, date=event.date)
            if event
            else None,
            "start_time": s.start_time,
            "end_time": s.end_time,
            "avg_bpm": s.avg_bpm,
            "max_bpm": s.max_bpm,
            "min_bpm": s.min_bpm,
            "data_quality_score": s.data_quality_score,
            "source_device": s.source_device,
            "created_at": s.created_at,
            "analyzed_at": s.analyzed_at,
        }
        if with_readings:
            fields["data_points"] = readings.get(s.id, [])
        out_sessions.append(session_cls(**fields))

    peaks = (
        await _all(
            db,
            select(Peak)
            .where(Peak.session_id.in_(session_ids))
            .order_by(Peak.session_id, Peak.rank),
        )
        if session_ids
        else []
    )
    cards = await _all(
        db, select(Card).where(Card.user_id == user.id).order_by(Card.created_at)
    )
    card_ids = [c.id for c in cards]
    shares = (
        await _all(
            db,
            select(Share).where(Share.card_id.in_(card_ids)).order_by(Share.shared_at),
        )
        if card_ids
        else []
    )
    posts = await _all(
        db,
        select(EventPost)
        .where(EventPost.user_id == user.id)
        .order_by(EventPost.created_at),
    )
    reactions = await _all(
        db,
        select(EventPostReaction)
        .where(EventPostReaction.user_id == user.id)
        .order_by(EventPostReaction.created_at),
    )
    block_rows = (
        await db.execute(
            select(UserBlock, User)
            .join(User, User.id == UserBlock.blocked_id)
            .where(UserBlock.blocker_id == user.id)
            .order_by(UserBlock.created_at)
        )
    ).all()
    reports = await _all(
        db,
        select(PostReport)
        .where(PostReport.reporter_id == user.id)
        .order_by(PostReport.created_at),
    )
    requests = await _all(
        db,
        select(DataSubjectRequest)
        .where(DataSubjectRequest.user_id == user.id)
        .order_by(DataSubjectRequest.opened_at),
    )

    response_cls = MyExportResponse if with_readings else MyDataResponse
    return response_cls(
        user=DataUser.model_validate(user),
        birth_date=user.birth_date,
        consents=[
            ConsentHistoryEntry(
                purpose=c.purpose,
                granted=c.revoked_at is None,
                text_version=c.text_version,
                granted_at=c.granted_at,
                revoked_at=c.revoked_at,
                means=c.means,
                client=c.client,
            )
            for c in await consents.history(db, user.id)
        ],
        sessions=out_sessions,
        peaks=[DataPeak.model_validate(p) for p in peaks],
        cards=[
            DataCard(
                id=c.id,
                session_id=c.session_id,
                peak_id=c.peak_id,
                card_type=c.card_type,
                status=c.status,
                created_at=c.created_at,
                published_at=c.published_at,
                metadata=c.metadata_,
            )
            for c in cards
        ],
        posts=[DataPost.model_validate(p) for p in posts],
        reactions=[DataReaction.model_validate(r) for r in reactions],
        shares=[DataShare.model_validate(s) for s in shares],
        blocks=[
            DataBlock(
                id=block.id,
                blocked_name=FeedAuthor.of(person).name,
                created_at=block.created_at,
            )
            for block, person in block_rows
        ],
        reports_filed=[DataReport.model_validate(r) for r in reports],
        requests=[DataSubjectRequestResponse.model_validate(r) for r in requests],
    )


async def readings_csv(db: AsyncSession, user_id: uuid.UUID) -> str:
    """`session_id,time,bpm` for every reading of the person.

    Built whole rather than streamed: a streamed body would outlive the
    request's database session on newer FastAPI releases, and a person's
    nights are tens of thousands of lines, not millions.
    """
    rows = await db.execute(
        select(HRData.session_id, HRData.time, HRData.bpm)
        .join(HRSession, HRSession.id == HRData.session_id)
        .where(HRSession.user_id == user_id)
        .order_by(HRSession.start_time, HRData.session_id, HRData.time)
    )
    buffer = io.StringIO()
    writer = csv.writer(buffer)
    writer.writerow(["session_id", "time", "bpm"])
    for session_id, time, bpm in rows.all():
        writer.writerow([str(session_id), time.isoformat(), bpm])
    return buffer.getvalue()
