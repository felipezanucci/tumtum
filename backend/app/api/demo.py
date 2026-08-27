"""Demo API — seed data and simulate experiences for testing.

These endpoints allow testing the full Tumtum flow without real wearable devices.
They should be disabled or restricted in production.
"""

import math
import random
import uuid
from datetime import date, datetime, time, timedelta, timezone

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user
from app.core.database import get_db
from app.models.event import Event
from app.models.event_timeline import EventTimeline
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.peak import Peak
from app.models.user import User
from app.schemas.event import (
    ExperienceResponse,
    HRDataPointBrief,
    HRSessionSummary,
    PeakResponse,
    TimelineEntryResponse,
    offset_aware,
)
from app.services.event_correlator import correlate_peaks_to_timeline
from app.services.peak_detection import detect_peaks

router = APIRouter(prefix="/api/demo", tags=["demo"])


# ── Seed data ─────────────────────────────────────────────────────────

SEED_EVENTS = [
    {
        "name": "Coldplay — Music of the Spheres",
        "subtitle": "World Tour 2025",
        "venue": "Allianz Parque",
        "city": "São Paulo",
        "country": "Brasil",
        "date": date(2025, 10, 18),
        "start_time": time(21, 0),
        "end_time": time(23, 30),
        "event_type": "concert",
        "cover_image_url": "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800&q=80",
        "timeline": [
            (0, "Higher Power", "song_start"),
            (5, "Adventure of a Lifetime", "song_start"),
            (10, "Paradise", "song_start"),
            (16, "The Scientist", "song_start"),
            (22, "Viva la Vida", "song_start"),
            (28, "Hymn for the Weekend", "song_start"),
            (34, "Let Somebody Go", "song_start"),
            (40, "Charlie Brown", "song_start"),
            (46, "Yellow", "song_start"),
            (52, "Human Heart", "song_start"),
            (58, "People of the Pride", "song_start"),
            (65, "Clocks", "song_start"),
            (72, "Infinity Sign", "song_start"),
            (78, "Something Just Like This", "song_start"),
            (85, "My Universe", "song_start"),
            (92, "A Sky Full of Stars", "song_start"),
            (100, "Sparks", "encore"),
            (106, "Fix You", "encore"),
        ],
    },
    {
        "name": "Corinthians x Palmeiras",
        "subtitle": "Campeonato Paulista 2025 — Final",
        "venue": "Neo Química Arena",
        "city": "São Paulo",
        "country": "Brasil",
        "date": date(2025, 4, 6),
        "start_time": time(16, 0),
        "end_time": time(17, 50),
        "event_type": "sports",
        "cover_image_url": "https://images.unsplash.com/photo-1489944440615-453fc2b6a9a9?w=800&q=80",
        "timeline": [
            (0, "Início do 1º Tempo", "highlight"),
            (12, "⚽ Gol! Yuri Alberto (Corinthians)", "goal"),
            (23, "Cartão amarelo — Zé Rafael", "highlight"),
            (38, "⚽ Gol! Raphael Veiga (Palmeiras)", "goal"),
            (45, "Intervalo", "halftime"),
            (55, "Início do 2º Tempo", "highlight"),
            (67, "⚽ Gol! Endrick (Palmeiras)", "goal"),
            (75, "Cartão vermelho — Fagner", "highlight"),
            (82, "⚽ Gol! Yuri Alberto (Corinthians)", "goal"),
            (88, "Pênalti!", "highlight"),
            (90, "⚽ GOL! Wesley — Corinthians vence!", "goal"),
        ],
    },
    {
        "name": "Lollapalooza Brasil 2025",
        "subtitle": "Palco Budweiser — Headliner: Arctic Monkeys",
        "venue": "Autódromo de Interlagos",
        "city": "São Paulo",
        "country": "Brasil",
        "date": date(2025, 3, 28),
        "start_time": time(19, 30),
        "end_time": time(21, 30),
        "event_type": "festival",
        "cover_image_url": "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=800&q=80",
        "timeline": [
            (0, "Do I Wanna Know?", "song_start"),
            (6, "Brianstorm", "song_start"),
            (11, "Snap Out of It", "song_start"),
            (17, "Crying Lightning", "song_start"),
            (23, "Why'd You Only Call Me When You're High?", "song_start"),
            (29, "Arabella", "song_start"),
            (35, "Fluorescent Adolescent", "song_start"),
            (42, "Body Paint", "song_start"),
            (48, "There'd Better Be a Mirrorball", "song_start"),
            (55, "505", "song_start"),
            (62, "I Bet You Look Good on the Dancefloor", "song_start"),
            (68, "R U Mine?", "song_start"),
        ],
    },
    {
        "name": "Anitta — Baile Funk Experience",
        "subtitle": "Turnê 2025",
        "venue": "Vibra São Paulo",
        "city": "São Paulo",
        "country": "Brasil",
        "date": date(2025, 8, 15),
        "start_time": time(21, 0),
        "end_time": time(23, 0),
        "event_type": "concert",
        "cover_image_url": "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800&q=80",
        "timeline": [
            (0, "Envolver", "song_start"),
            (5, "Vai Malandra", "song_start"),
            (10, "Boys Don't Cry", "song_start"),
            (16, "Funk Rave", "song_start"),
            (22, "Movimento da Sanfoninha", "song_start"),
            (28, "Bola Rebola", "song_start"),
            (34, "Show das Poderosas", "song_start"),
            (40, "Mil Veces", "song_start"),
            (46, "Medicina", "song_start"),
            (52, "Lobby", "song_start"),
            (58, "Terremoto", "song_start"),
            (64, "Bellaquita", "song_start"),
            (70, "Faking Love", "song_start"),
            (78, "Aceita", "encore"),
            (84, "Paradinha", "encore"),
        ],
    },
    {
        "name": "São Paulo FC x Flamengo",
        "subtitle": "Brasileirão 2025 — Rodada 12",
        "venue": "Morumbis",
        "city": "São Paulo",
        "country": "Brasil",
        "date": date(2025, 7, 20),
        "start_time": time(18, 30),
        "end_time": time(20, 20),
        "event_type": "sports",
        "cover_image_url": "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&q=80",
        "timeline": [
            (0, "Início do 1º Tempo", "highlight"),
            (15, "⚽ Gol! Luciano (São Paulo)", "goal"),
            (30, "Grande defesa de Rafael", "highlight"),
            (42, "⚽ Gol! Gabigol (Flamengo)", "goal"),
            (45, "Intervalo", "halftime"),
            (55, "Início do 2º Tempo", "highlight"),
            (63, "⚽ Gol! Calleri (São Paulo)", "goal"),
            (78, "⚽ Gol! Arrascaeta (Flamengo)", "goal"),
            (85, "Expulsão! Cartão vermelho", "highlight"),
            (90, "Fim de jogo — Empate 2x2", "highlight"),
        ],
    },
    {
        "name": "The Weeknd — After Hours til Dawn",
        "subtitle": "World Tour 2025",
        "venue": "Allianz Parque",
        "city": "São Paulo",
        "country": "Brasil",
        "date": date(2025, 11, 8),
        "start_time": time(21, 0),
        "end_time": time(23, 0),
        "event_type": "concert",
        "cover_image_url": "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=800&q=80",
        "timeline": [
            (0, "Alone Again", "song_start"),
            (5, "Gasoline", "song_start"),
            (10, "Sacrifice", "song_start"),
            (16, "How Do I Make You Love Me?", "song_start"),
            (22, "Can't Feel My Face", "song_start"),
            (28, "Starboy", "song_start"),
            (35, "I Feel It Coming", "song_start"),
            (42, "Die For You", "song_start"),
            (48, "The Hills", "song_start"),
            (55, "Often", "song_start"),
            (62, "Earned It", "song_start"),
            (68, "Save Your Tears", "song_start"),
            (75, "Take My Breath", "song_start"),
            (82, "Blinding Lights", "encore"),
            (90, "After Hours", "encore"),
        ],
    },
]


@router.post("/seed")
async def seed_database(db: AsyncSession = Depends(get_db)):
    """Populate the database with demo events and timelines."""
    # Check if events already exist
    result = await db.execute(select(Event).limit(1))
    if result.scalar_one_or_none():
        return {
            "message": "Database já possui eventos. Seed ignorado.",
            "events_created": 0,
        }

    created_events = []
    for ev_data in SEED_EVENTS:
        timeline_data = ev_data["timeline"]
        event_fields = {k: v for k, v in ev_data.items() if k != "timeline"}

        # The same timetz trap the API hits: a naive time cannot be encoded
        # into TIME WITH TIME ZONE, so seeding has never worked against a real
        # database either. See offset_aware in app/schemas/event.py.
        for key in ("start_time", "end_time"):
            event_fields[key] = offset_aware(event_fields.get(key))

        event = Event(**event_fields)
        db.add(event)
        await db.flush()

        # Create timeline entries
        base_dt = datetime.combine(
            ev_data["date"],
            ev_data["start_time"] or time(21, 0),
            tzinfo=timezone(timedelta(hours=-3)),
        )

        for minute_offset, label, entry_type in timeline_data:
            entry = EventTimeline(
                event_id=event.id,
                timestamp=base_dt + timedelta(minutes=minute_offset),
                label=label,
                entry_type=entry_type,
            )
            db.add(entry)

        created_events.append({"id": str(event.id), "name": ev_data["name"]})

    return {
        "message": f"{len(created_events)} eventos criados com sucesso!",
        "events": created_events,
    }


# ── Simulate experience ──────────────────────────────────────────────


def _generate_realistic_hr(
    start_time: datetime,
    duration_minutes: int,
    timeline_entries: list[dict],
    event_type: str,
) -> list[dict]:
    """Generate realistic heart rate data that creates peaks near timeline events.

    Args:
        start_time: When the event starts
        duration_minutes: Total duration
        timeline_entries: List of {"time": datetime, "label": str} for peak injection
        event_type: "concert", "sports", or "festival"

    Returns:
        List of {"time": datetime, "bpm": int} sampled every 5 seconds
    """
    interval_sec = 5
    total_points = (duration_minutes * 60) // interval_sec
    data = []

    # Baseline parameters based on event type
    if event_type == "sports":
        resting_bpm = 78
        excitement_base = 95
        max_excitement = 165
    elif event_type == "festival":
        resting_bpm = 82
        excitement_base = 100
        max_excitement = 155
    else:  # concert
        resting_bpm = 75
        excitement_base = 90
        max_excitement = 160

    # Build an excitement curve that peaks near timeline entries
    excitement_map = {}
    for entry in timeline_entries:
        entry_time = entry["time"]
        entry_type = entry.get("entry_type", "song_start")

        # Stronger peaks for goals, encores, and specific labels
        if entry_type == "goal":
            intensity = random.uniform(0.85, 1.0)
            duration_sec = random.randint(40, 90)
        elif entry_type == "encore":
            intensity = random.uniform(0.75, 0.95)
            duration_sec = random.randint(60, 120)
        elif entry_type == "halftime":
            intensity = random.uniform(0.1, 0.25)
            duration_sec = 120
        else:
            intensity = random.uniform(0.35, 0.8)
            duration_sec = random.randint(30, 90)

        excitement_map[entry_time] = (intensity, duration_sec)

    for i in range(total_points):
        t = start_time + timedelta(seconds=i * interval_sec)

        # Calculate excitement level from nearby timeline entries
        excitement = 0.0
        for entry_time, (intensity, dur) in excitement_map.items():
            delta_sec = (t - entry_time).total_seconds()
            # Ramp up before the peak, sustained during, decay after
            if -10 <= delta_sec <= dur:
                # Within peak zone
                # Fast ramp up (0 to peak in ~10s)
                if delta_sec < 0:
                    phase = (delta_sec + 10) / 10
                elif delta_sec < dur * 0.6:
                    phase = 1.0
                else:
                    phase = 1.0 - ((delta_sec - dur * 0.6) / (dur * 0.4))
                excitement = max(excitement, intensity * max(0, phase))
            elif dur < delta_sec < dur + 30:
                # Tail decay
                decay = 1.0 - ((delta_sec - dur) / 30)
                excitement = max(excitement, intensity * 0.3 * decay)

        # Gradual overall excitement increase as event progresses
        progress = i / total_points
        ambient = 0.15 * math.sin(progress * math.pi)  # peaks in the middle

        # Combine
        base = resting_bpm + (excitement_base - resting_bpm) * ambient
        peak_contribution = (max_excitement - base) * excitement

        bpm = base + peak_contribution

        # Add natural noise (heart rate variability)
        noise = random.gauss(0, 2.5)
        bpm = max(55, min(200, bpm + noise))

        data.append({"time": t, "bpm": round(bpm)})

    return data


@router.post("/simulate/{event_id}", response_model=ExperienceResponse)
async def simulate_experience(
    event_id: uuid.UUID,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Simulate an HR session for a given event with realistic data.

    Creates HR data, runs peak detection, and returns the full experience.
    """
    # Fetch event with timeline
    result = await db.execute(select(Event).where(Event.id == event_id))
    event = result.scalar_one_or_none()
    if not event:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Evento não encontrado"
        )

    tl_result = await db.execute(
        select(EventTimeline)
        .where(EventTimeline.event_id == event_id)
        .order_by(EventTimeline.timestamp)
    )
    timeline_entries = tl_result.scalars().all()

    # Calculate event duration
    start_dt = datetime.combine(
        event.date,
        event.start_time or time(21, 0),
        tzinfo=timezone(timedelta(hours=-3)),
    )
    end_dt = datetime.combine(
        event.date,
        event.end_time or time(23, 0),
        tzinfo=timezone(timedelta(hours=-3)),
    )
    duration_minutes = int((end_dt - start_dt).total_seconds() / 60)

    # Delete any existing session for this user+event (so we can re-simulate)
    old_sessions = await db.execute(
        select(HRSession).where(
            HRSession.user_id == user.id, HRSession.event_id == event_id
        )
    )
    for old_session in old_sessions.scalars().all():
        # Delete associated peaks
        old_peaks = await db.execute(
            select(Peak).where(Peak.session_id == old_session.id)
        )
        for p in old_peaks.scalars().all():
            await db.delete(p)
        # Delete associated HR data
        old_data = await db.execute(
            select(HRData).where(HRData.session_id == old_session.id)
        )
        for d in old_data.scalars().all():
            await db.delete(d)
        await db.delete(old_session)
    await db.flush()

    # Generate realistic HR data
    tl_for_gen = [
        {"time": e.timestamp, "label": e.label, "entry_type": e.entry_type}
        for e in timeline_entries
    ]
    hr_data = _generate_realistic_hr(
        start_dt, duration_minutes, tl_for_gen, event.event_type
    )

    # Compute stats
    bpm_values = [d["bpm"] for d in hr_data]
    avg_bpm = round(sum(bpm_values) / len(bpm_values))
    max_bpm = max(bpm_values)
    min_bpm = min(bpm_values)

    # Create session
    session = HRSession(
        user_id=user.id,
        event_id=event_id,
        start_time=start_dt,
        end_time=end_dt,
        avg_bpm=avg_bpm,
        max_bpm=max_bpm,
        min_bpm=min_bpm,
        data_quality_score=95,
        source_device="Tumtum Demo (simulado)",
    )
    db.add(session)
    await db.flush()

    # Bulk insert HR data points
    data_points = [
        HRData(
            time=d["time"],
            session_id=session.id,
            bpm=d["bpm"],
            source="demo",
        )
        for d in hr_data
    ]
    db.add_all(data_points)
    await db.flush()

    # Run peak detection
    tl_data = [
        {"time": e.timestamp, "label": e.label, "id": e.id} for e in timeline_entries
    ]
    detected_peaks = detect_peaks(hr_data, tl_data)

    # Correlate peaks to timeline
    if tl_data:
        detected_peaks = correlate_peaks_to_timeline(detected_peaks, tl_data)

    # Store peaks
    peak_models = []
    tl_map = {str(e.id): e.label for e in timeline_entries}
    for i, p in enumerate(detected_peaks):
        peak = Peak(
            session_id=session.id,
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

    # Build response
    peak_responses = []
    for peak in peak_models:
        resp = PeakResponse.model_validate(peak)
        if peak.timeline_entry_id:
            resp.matched_label = tl_map.get(str(peak.timeline_entry_id))
        peak_responses.append(resp)

    return ExperienceResponse(
        session=HRSessionSummary.model_validate(session),
        peaks=peak_responses,
        timeline=[TimelineEntryResponse.model_validate(e) for e in timeline_entries],
        hr_data=[HRDataPointBrief(time=d["time"], bpm=d["bpm"]) for d in hr_data],
    )
