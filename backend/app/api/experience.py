import uuid

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.user import User
from app.models.hr_session import HRSession
from app.models.hr_data import HRData
from app.models.event import Event
from app.models.event_timeline import EventTimeline
from app.models.peak import Peak
from app.schemas.event import PeakResponse, ExperienceResponse, HRSessionSummary, TimelineEntryResponse
from app.services.peak_detection import detect_peaks
from app.services.event_correlator import correlate_peaks_to_timeline

router = APIRouter(prefix="/api/experience", tags=["experience"])


@router.post("/{session_id}/analyze", response_model=list[PeakResponse])
async def analyze_session(
    session_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Run peak detection on an HR session and store results."""
    # Fetch session
    result = await db.execute(
        select(HRSession).where(HRSession.id == session_id, HRSession.user_id == user.id)
    )
    session = result.scalar_one_or_none()
    if not session:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Sessão não encontrada")

    # Fetch HR data points
    data_result = await db.execute(
        select(HRData).where(HRData.session_id == session_id).order_by(HRData.time)
    )
    data_points = data_result.scalars().all()
    if len(data_points) < 10:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="Dados insuficientes para análise")

    # Fetch event timeline if session is linked to an event
    timeline_entries = []
    if session.event_id:
        tl_result = await db.execute(
            select(EventTimeline)
            .where(EventTimeline.event_id == session.event_id)
            .order_by(EventTimeline.timestamp)
        )
        timeline_entries = tl_result.scalars().all()

    # Run peak detection
    hr_data = [{"time": dp.time, "bpm": dp.bpm} for dp in data_points]
    tl_data = [{"time": e.timestamp, "label": e.label, "id": e.id} for e in timeline_entries]
    detected_peaks = detect_peaks(hr_data, tl_data)

    # Correlate peaks to timeline
    if tl_data:
        detected_peaks = correlate_peaks_to_timeline(detected_peaks, tl_data)

    # Delete old peaks for this session and store new ones
    old_peaks = await db.execute(select(Peak).where(Peak.session_id == session_id))
    for old in old_peaks.scalars().all():
        await db.delete(old)

    peak_models = []
    for i, p in enumerate(detected_peaks):
        peak = Peak(
            session_id=session_id,
            timestamp=p["timestamp"],
            bpm=p["bpm"],
            duration_seconds=p["duration_seconds"],
            magnitude=p["magnitude"],
            timeline_entry_id=p.get("timeline_entry_id"),
            rank=i + 1,
        )
        db.add(peak)
        peak_models.append(peak)

    await db.flush()

    # Build response with matched labels
    responses = []
    tl_map = {str(e.id): e.label for e in timeline_entries}
    for peak in peak_models:
        resp = PeakResponse.model_validate(peak)
        if peak.timeline_entry_id:
            resp.matched_label = tl_map.get(str(peak.timeline_entry_id))
        responses.append(resp)

    return responses


@router.get("/{session_id}", response_model=ExperienceResponse)
async def get_experience(
    session_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Get full experience data: session summary, peaks, and timeline."""
    result = await db.execute(
        select(HRSession).where(HRSession.id == session_id, HRSession.user_id == user.id)
    )
    session = result.scalar_one_or_none()
    if not session:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Sessão não encontrada")

    # Fetch peaks
    peaks_result = await db.execute(
        select(Peak).where(Peak.session_id == session_id).order_by(Peak.rank)
    )
    peaks = peaks_result.scalars().all()

    # Fetch timeline
    timeline_entries = []
    tl_map = {}
    if session.event_id:
        tl_result = await db.execute(
            select(EventTimeline)
            .where(EventTimeline.event_id == session.event_id)
            .order_by(EventTimeline.timestamp)
        )
        timeline_entries = tl_result.scalars().all()
        tl_map = {str(e.id): e.label for e in timeline_entries}

    peak_responses = []
    for peak in peaks:
        resp = PeakResponse.model_validate(peak)
        if peak.timeline_entry_id:
            resp.matched_label = tl_map.get(str(peak.timeline_entry_id))
        peak_responses.append(resp)

    return ExperienceResponse(
        session=HRSessionSummary.model_validate(session),
        peaks=peak_responses,
        timeline=[TimelineEntryResponse.model_validate(e) for e in timeline_entries],
    )
