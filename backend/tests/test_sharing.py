""" "Ver compartilhamentos" (v1.1 §7) and the no-store rule for personal data
(v1.1 §18), end to end through the real app."""

import uuid
from datetime import UTC, datetime

import pytest
from sqlalchemy import select

from app.main import cache_headers
from app.models.card import Card, Share
from app.models.event_post import EventPost, EventPostReaction
from app.models.event_series import EventSeries, SeriesPost
from app.models.privacy import DataAccessLog
from app.services import card_cache
from app.services.operators import NEVER_SHARED_WITH, OPERATORS
from tests.conftest import AT, add_event, add_night, add_user


async def _card(db, user, night, published=True):
    card = Card(
        id=uuid.uuid4(),
        user_id=user.id,
        session_id=night.id,
        card_type="solo",
        status="ready",
        metadata_={"event_name": "Show", "event_date": "02/10/2026", "peak_bpm": 150},
        published_at=datetime.now(UTC) if published else None,
    )
    db.add(card)
    await db.flush()
    return card


async def _post(db, user, event, night, deleted=False):
    post = EventPost(
        id=uuid.uuid4(),
        event_id=event.id,
        user_id=user.id,
        session_id=night.id,
        bpm=150,
        moment_at=AT,
        deleted_at=datetime.now(UTC) if deleted else None,
    )
    db.add(post)
    await db.flush()
    return post


# --- sharing ---


@pytest.mark.asyncio
async def test_the_operators_are_listed_and_no_partner_is(memdb, api):
    ana = await add_user(memdb)

    answer = await api(ana).get("/api/users/me/sharing")

    assert answer.status_code == 200
    body = answer.json()
    names = [o["name"] for o in body["operators"]]
    assert names == [o.name for o in OPERATORS]
    for expected in ("Railway", "Redis no Railway", "Vercel", "Resend", "Sentry"):
        assert expected in names
    football = next(o for o in body["operators"] if o["name"] == "API-Football")
    assert football["role"] == "nenhum dado pessoal"
    assert body["never"] == list(NEVER_SHARED_WITH)
    listed = " ".join(names).lower()
    for partner in body["never"]:
        assert partner not in listed
    assert body["anpd"]["url"].startswith("https://www.gov.br/anpd/")
    assert (body["published_cards"], body["feed_posts"], body["shares"]) == (
        [],
        [],
        [],
    )


@pytest.mark.asyncio
async def test_a_published_card_is_listed_with_its_url_and_a_private_one_is_not(
    memdb, api
):
    from app.config import settings

    ana = await add_user(memdb)
    event = await add_event(memdb, name="Coldplay")
    night = await add_night(memdb, ana, event)
    public = await _card(memdb, ana, night)
    private = await _card(memdb, ana, night, published=False)
    memdb.add(Share(card_id=public.id, platform="whatsapp"))
    bia = await add_user(memdb, "Bia")
    await _card(memdb, bia, await add_night(memdb, bia, event))  # not hers
    await memdb.flush()

    body = (await api(ana).get("/api/users/me/sharing")).json()

    (card,) = body["published_cards"]
    assert card["id"] == str(public.id)
    assert card["event_name"] == "Coldplay"
    assert card["public_url"] == f"{settings.site_url.rstrip('/')}/cards/{public.id}"
    assert str(private.id) not in str(body)
    (share,) = body["shares"]
    assert (share["card_id"], share["platform"]) == (str(public.id), "whatsapp")


@pytest.mark.asyncio
async def test_feed_posts_say_their_audience_and_a_deleted_one_is_gone(memdb, api):
    ana = await add_user(memdb)
    bia = await add_user(memdb, "Bia")
    event = await add_event(memdb, name="Eras")
    night = await add_night(memdb, ana, event)
    to_event = await _post(memdb, ana, event, night)
    to_tour = await _post(memdb, ana, event, night)
    taken_down = await _post(memdb, ana, event, night, deleted=True)
    series = EventSeries(id=uuid.uuid4(), name="The Eras Tour")
    memdb.add(series)
    await memdb.flush()
    memdb.add_all(
        [
            SeriesPost(post_id=to_tour.id, series_id=series.id),
            EventPostReaction(post_id=to_tour.id, user_id=bia.id),
        ]
    )
    await memdb.flush()

    body = (await api(ana).get("/api/users/me/sharing")).json()

    posts = {p["id"]: p for p in body["feed_posts"]}
    assert set(posts) == {str(to_event.id), str(to_tour.id)}
    assert str(taken_down.id) not in posts
    assert posts[str(to_event.id)]["audience"] == "evento"
    assert posts[str(to_tour.id)]["audience"] == "turnê"
    assert posts[str(to_tour.id)]["reactions"] == 1
    assert posts[str(to_event.id)]["reactions"] == 0
    for post in posts.values():
        assert post["event_id"] == str(event.id)
        assert post["event_name"] == "Eras"


@pytest.mark.asyncio
async def test_reading_the_sharing_screen_is_logged(memdb, api):
    ana = await add_user(memdb)
    await api(ana).get("/api/users/me/sharing")
    rows = (
        (
            await memdb.execute(
                select(DataAccessLog).where(DataAccessLog.resource == "sharing")
            )
        )
        .scalars()
        .all()
    )
    assert [(r.subject_user_id, r.action) for r in rows] == [(ana.id, "read")]


@pytest.mark.asyncio
async def test_signed_out_there_is_no_sharing_screen(api):
    assert (await api().get("/api/users/me/sharing")).status_code == 401


# --- no cache for personal data ---


@pytest.mark.asyncio
async def test_personal_data_is_never_stored_by_a_cache(memdb, api):
    ana = await add_user(memdb)
    answer = await api(ana).get("/api/users/me")
    assert answer.status_code == 200
    assert answer.headers["cache-control"] == "no-store"
    assert answer.headers["pragma"] == "no-cache"
    # Refusals too: a 401 is still an answer about somebody.
    refused = await api().get("/api/users/me")
    assert refused.headers["cache-control"] == "no-store"


@pytest.mark.asyncio
async def test_a_card_image_may_be_cached_and_its_404_may_not(memdb, api, fake_redis):
    ana = await add_user(memdb)
    night = await add_night(memdb, ana)
    public = await _card(memdb, ana, night)
    private = await _card(memdb, ana, night, published=False)
    for card in (public, private):
        fake_redis.store[card_cache.cache_key(card.id)] = b"\x89PNG"

    image = await api().get(f"/api/cards/{public.id}/image")
    assert image.status_code == 200
    assert image.headers["cache-control"] == "public, max-age=3600"
    assert "pragma" not in image.headers

    preview = await api(ana).get(f"/api/cards/{private.id}/preview")
    assert preview.status_code == 200
    assert preview.headers["cache-control"] == "private, max-age=3600"

    unpublished = await api().get(f"/api/cards/{private.id}/image")
    assert unpublished.status_code == 404
    assert unpublished.headers["cache-control"] == "no-store"


@pytest.mark.asyncio
async def test_the_healthcheck_carries_no_cache_rule(api):
    answer = await api().get("/health")
    assert answer.status_code == 200
    assert "cache-control" not in answer.headers


def test_only_the_two_image_routes_are_exceptions():
    card = uuid.uuid4()
    assert cache_headers("GET", f"/api/cards/{card}", 200)["Cache-Control"] == (
        "no-store"
    )
    assert cache_headers("GET", f"/api/cards/{card}/public", 200)["Cache-Control"] == (
        "no-store"
    )
    assert cache_headers("DELETE", f"/api/cards/{card}/image", 200)[
        "Cache-Control"
    ] == ("no-store")
    assert cache_headers("GET", "/", 200) == {}
