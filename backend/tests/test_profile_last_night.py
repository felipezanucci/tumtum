"""The profile says what recorded the last night (02/10).

The site's profile showed "Dispositivos conectados — Nenhum dispositivo
conectado" over an account with thirty strap nights, because nothing ever
writes `wearable_connections`. The profile now carries the last night kept
on the server and its `source_device`, and the page says that instead.
"""

from datetime import UTC, datetime, timedelta

import pytest

from app.models.hr_session import HRSession
from tests.conftest import add_user


@pytest.mark.asyncio
async def test_the_profile_names_the_last_night_and_what_recorded_it(memdb, api):
    ana = await add_user(memdb, "Ana")
    client = api(ana)
    empty = await client.get("/api/users/me")
    assert empty.status_code == 200
    assert empty.json()["last_night_at"] is None
    assert empty.json()["last_night_source"] is None

    older = datetime(2026, 9, 20, 22, 0, tzinfo=UTC)
    newer = datetime(2026, 9, 28, 20, 0, tzinfo=UTC)
    for start, device in ((older, "Polar H10"), (newer, "Galaxy Watch")):
        memdb.add(
            HRSession(
                user_id=ana.id,
                start_time=start,
                end_time=start + timedelta(hours=2),
                source_device=device,
            )
        )
    await memdb.flush()

    answer = await client.get("/api/users/me")
    assert answer.json()["last_night_source"] == "Galaxy Watch"
    assert answer.json()["last_night_at"].startswith("2026-09-28T20:00")
