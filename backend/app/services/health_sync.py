"""Health data sync service.

Handles fetching HR data from external providers (Apple HealthKit, Google Health Connect,
Garmin, Fitbit) and ingesting it into the database.

Phase 0: The frontend collects data via on-device APIs (HealthKit / Health Connect)
and sends it to our backend. This service processes and stores that data.
Future phases will add server-side OAuth sync for Garmin and Fitbit.
"""

import uuid
from datetime import datetime, timezone

import httpx
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.wearable_connection import WearableConnection
from app.models.hr_session import HRSession
from app.models.hr_data import HRData


async def sync_health_data(
    db: AsyncSession,
    user_id: uuid.UUID,
    connection: WearableConnection,
    start_time: datetime,
    end_time: datetime,
) -> int:
    """Sync HR data from a wearable provider.

    For Phase 0, this processes data that was already sent from the client device.
    For providers with REST APIs (Google Fit, Fitbit, Garmin), fetches data server-side.

    Returns the number of records synced.
    """
    provider = connection.provider

    if provider == "apple_health":
        # Apple HealthKit data is pushed from the device — no server-side fetch
        # Data arrives via POST /api/health/sessions from the iOS/PWA client
        return 0

    if provider == "google_fit":
        return await _sync_google_fit(db, user_id, connection, start_time, end_time)

    if provider == "garmin":
        return await _sync_garmin(db, user_id, connection, start_time, end_time)

    if provider == "fitbit":
        return await _sync_fitbit(db, user_id, connection, start_time, end_time)

    return 0


async def _sync_google_fit(
    db: AsyncSession,
    user_id: uuid.UUID,
    connection: WearableConnection,
    start_time: datetime,
    end_time: datetime,
) -> int:
    """Fetch heart rate data from Google Health Connect REST API."""
    # Google Fit REST API: aggregate heart rate data
    start_ms = int(start_time.timestamp() * 1000)
    end_ms = int(end_time.timestamp() * 1000)

    async with httpx.AsyncClient() as client:
        response = await client.post(
            "https://www.googleapis.com/fitness/v1/users/me/dataset:aggregate",
            headers={"Authorization": f"Bearer {connection.access_token}"},
            json={
                "aggregateBy": [
                    {"dataTypeName": "com.google.heart_rate.bpm"}
                ],
                "bucketByTime": {"durationMillis": 5000},  # 5-second buckets
                "startTimeMillis": start_ms,
                "endTimeMillis": end_ms,
            },
        )

    if response.status_code == 401:
        connection.status = "expired"
        return 0

    if response.status_code != 200:
        return 0

    data = response.json()
    data_points = _parse_google_fit_response(data)

    if not data_points:
        return 0

    return await _ingest_data_points(db, user_id, connection, start_time, end_time, data_points)


async def _sync_garmin(
    db: AsyncSession,
    user_id: uuid.UUID,
    connection: WearableConnection,
    start_time: datetime,
    end_time: datetime,
) -> int:
    """Fetch heart rate data from Garmin Connect API. (Placeholder for Phase 1)"""
    # Garmin uses push-based webhooks — will be implemented in Phase 1
    return 0


async def _sync_fitbit(
    db: AsyncSession,
    user_id: uuid.UUID,
    connection: WearableConnection,
    start_time: datetime,
    end_time: datetime,
) -> int:
    """Fetch heart rate data from Fitbit Web API. (Placeholder for Phase 1)"""
    # Fitbit intraday HR requires special API access — will be implemented in Phase 1
    return 0


def _parse_google_fit_response(data: dict) -> list[dict]:
    """Parse Google Fit aggregate response into a list of {time, bpm} dicts."""
    points = []
    for bucket in data.get("bucket", []):
        start_ms = int(bucket.get("startTimeMillis", 0))
        for dataset in bucket.get("dataset", []):
            for point in dataset.get("point", []):
                for value in point.get("value", []):
                    bpm = value.get("fpVal")
                    if bpm and 30 <= bpm <= 250:
                        points.append({
                            "time": datetime.fromtimestamp(start_ms / 1000, tz=timezone.utc),
                            "bpm": round(bpm),
                        })
    return points


async def _ingest_data_points(
    db: AsyncSession,
    user_id: uuid.UUID,
    connection: WearableConnection,
    start_time: datetime,
    end_time: datetime,
    data_points: list[dict],
) -> int:
    """Create an HR session and insert data points."""
    bpm_values = [dp["bpm"] for dp in data_points]

    session = HRSession(
        user_id=user_id,
        start_time=start_time,
        end_time=end_time,
        avg_bpm=round(sum(bpm_values) / len(bpm_values)),
        max_bpm=max(bpm_values),
        min_bpm=min(bpm_values),
        source_device=connection.provider,
    )
    db.add(session)
    await db.flush()

    hr_records = [
        HRData(
            time=dp["time"],
            session_id=session.id,
            bpm=dp["bpm"],
            source=connection.provider,
        )
        for dp in data_points
    ]
    db.add_all(hr_records)

    connection.last_sync_at = datetime.now(timezone.utc)
    return len(hr_records)
