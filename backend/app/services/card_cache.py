"""The Redis copies of a card's image, and forgetting them.

A card's PNG is cached for seven days under `card:image:{id}` (and one key
per variant: the link-preview `:og`, and the explicit `:story`/`:feed`).
Until 26/09 nothing removed them, so a deleted card — or a deleted account's
card — kept being served for a week, while the privacy page said it left
"na hora" (LGPD audit, AL-3). Every path that deletes a card, a night or an
account now calls `forget`.
"""

import logging
import uuid
from collections.abc import Iterable

log = logging.getLogger(__name__)

TTL_SECONDS = 86400 * 7

VARIANTS = ("", ":og", ":story", ":feed")


def cache_key(card_id: uuid.UUID | str, variant: str | None = None) -> str:
    return f"card:image:{card_id}" + (f":{variant}" if variant else "")


def keys_of(card_ids: Iterable[uuid.UUID | str]) -> list[str]:
    return [f"card:image:{cid}{suffix}" for cid in card_ids for suffix in VARIANTS]


async def forget(card_ids: Iterable[uuid.UUID | str]) -> None:
    """Delete every cached image of these cards. Never raises.

    Redis being down must not keep somebody's account or night alive: the
    rows go regardless, and since `/image` now reads the database first, a
    key that survives an outage serves nobody and expires within the week.
    """
    keys = keys_of(card_ids)
    if not keys:
        return
    try:
        from app.core.redis import redis_client

        await redis_client.delete(*keys)
    except Exception as error:  # an outage must not block a deletion
        log.warning("card cache: could not forget %d keys: %s", len(keys), error)
