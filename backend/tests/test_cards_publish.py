"""A card is private until it is shared, and gone when it is deleted (AL-3).

Until 26/09 every card was public by URL from the moment it was made, the
image was served from Redis before the database was asked, and deleting a
card left its image cached for a week.
"""

import uuid

import pytest
from fastapi import HTTPException
from sqlalchemy import func, select

from app.api.cards import (
    create_card,
    delete_card,
    get_card_image,
    get_public_card,
    track_share,
    unpublish_card,
)
from app.models.card import Card, Share
from app.schemas.card import CardCreateRequest, ShareRequest
from tests.conftest import add_night, add_user


async def _card(db, user, night, **meta):
    card = Card(
        id=uuid.uuid4(),
        user_id=user.id,
        session_id=night.id,
        card_type="solo",
        status="ready",
        metadata_={
            "event_name": "Show",
            "event_date": "02/10/2026",
            "peak_bpm": 150,
            **meta,
        },
    )
    db.add(card)
    await db.flush()
    return card


@pytest.mark.asyncio
async def test_a_card_nobody_shared_is_not_public(memdb, fake_redis):
    user = await add_user(memdb)
    card = await _card(memdb, user, await add_night(memdb, user))
    for read in (get_public_card(card.id, memdb), get_card_image(card.id, None, memdb)):
        with pytest.raises(HTTPException) as refused:
            await read
        assert refused.value.status_code == 404


@pytest.mark.asyncio
async def test_the_database_is_asked_before_the_cache(memdb, fake_redis):
    """A cached image of an unshared card is never served."""
    user = await add_user(memdb)
    card = await _card(memdb, user, await add_night(memdb, user))
    fake_redis.store[f"card:image:{card.id}"] = b"PNG-bytes"
    with pytest.raises(HTTPException) as refused:
        await get_card_image(card.id, None, memdb)
    assert refused.value.status_code == 404


@pytest.mark.asyncio
async def test_sharing_publishes_and_unpublishing_takes_it_back(memdb, fake_redis):
    user = await add_user(memdb)
    card = await _card(memdb, user, await add_night(memdb, user))

    await track_share(card.id, ShareRequest(platform="whatsapp"), user, memdb)
    assert card.published_at is not None
    assert (await get_public_card(card.id, memdb)).peak_bpm == 150
    fake_redis.store[f"card:image:{card.id}"] = b"PNG-bytes"
    image = await get_card_image(card.id, None, memdb)
    assert image.body == b"PNG-bytes"

    answer = await unpublish_card(card.id, user, memdb)
    assert answer.published_at is None
    assert f"card:image:{card.id}" not in fake_redis.store
    with pytest.raises(HTTPException):
        await get_public_card(card.id, memdb)


@pytest.mark.asyncio
async def test_the_name_is_read_from_the_account_not_from_the_card(memdb, fake_redis):
    user = await add_user(memdb, "Ana")
    card = await _card(
        memdb, user, await add_night(memdb, user), user_name="Nome antigo"
    )
    await track_share(card.id, ShareRequest(platform="link"), user, memdb)
    user.name = "Ana Souza"
    await memdb.flush()
    assert (await get_public_card(card.id, memdb)).user_name == "Ana Souza"


@pytest.mark.asyncio
async def test_only_the_owner_unpublishes(memdb, fake_redis):
    ana = await add_user(memdb, "Ana")
    bia = await add_user(memdb, "Bia")
    card = await _card(memdb, ana, await add_night(memdb, ana))
    with pytest.raises(HTTPException) as refused:
        await unpublish_card(card.id, bia, memdb)
    assert refused.value.status_code == 404


@pytest.mark.asyncio
async def test_deleting_a_card_forgets_its_cached_images(memdb, fake_redis):
    user = await add_user(memdb)
    card = await _card(memdb, user, await add_night(memdb, user))
    await track_share(card.id, ShareRequest(platform="link"), user, memdb)
    fake_redis.store[f"card:image:{card.id}"] = b"x"
    fake_redis.store[f"card:image:{card.id}:og"] = b"y"
    await delete_card(card.id, user, memdb)
    assert fake_redis.store == {}
    left = (await memdb.execute(select(func.count()).select_from(Share))).scalar_one()
    assert left == 0


@pytest.mark.asyncio
async def test_a_new_card_does_not_copy_the_name_into_its_metadata(memdb, fake_redis):
    user = await add_user(memdb)
    user_id, night_id = user.id, (await add_night(memdb, user)).id
    memdb.expire_all()  # SQLite hands times back naive; read them all from it
    user = await memdb.get(type(user), user_id)
    card = await create_card(CardCreateRequest(session_id=night_id), user, memdb)
    assert "user_name" not in card.metadata_
    assert card.published_at is None


@pytest.mark.asyncio
async def test_the_owner_sees_an_unpublished_card_and_nobody_else_does(
    memdb, fake_redis
):
    from app.api.cards import get_card_preview

    ana = await add_user(memdb, "Ana")
    bia = await add_user(memdb, "Bia")
    card = await _card(memdb, ana, await add_night(memdb, ana))
    preview = await get_card_preview(card.id, None, ana, memdb)
    assert preview.media_type == "image/png"
    assert preview.body[:8] == b"\x89PNG\r\n\x1a\n"
    with pytest.raises(HTTPException) as refused:
        await get_card_preview(card.id, None, bia, memdb)
    assert refused.value.status_code == 404
