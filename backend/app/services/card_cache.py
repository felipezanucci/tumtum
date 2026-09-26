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

    The known variants go first, in one call. Then every other key under
    `card:image:{id}` is found with `SCAN` and deleted too: a variant added
    later, or one written while `format` still accepted any string, must not
    outlive the card. SCAN rather than KEYS, which blocks Redis while it
    walks the whole keyspace.

    Redis being down must not keep somebody's account or night alive: the
    rows go regardless, and since `/image` now reads the database first, a
    key that survives an outage serves nobody and expires within the week.
    """
    ids = [str(cid) for cid in card_ids]
    keys = keys_of(ids)
    if not keys:
        return
    try:
        from app.core import redis as redis_module

        client = redis_module.redis_bytes
        await client.delete(*keys)
        for cid in ids:
            stray = [key async for key in client.scan_iter(f"card:image:{cid}*")]
            if stray:
                await client.delete(*stray)
    except Exception as error:  # an outage must not block a deletion
        log.warning("card cache: could not forget %d cards: %s", len(ids), error)
