from redis.asyncio import Redis

from app.config import settings

# Timeouts, because the rate limiter asks Redis on every login: without them
# a Redis that hangs, rather than refuses, would hang the login with it. Two
# seconds is far above a healthy round trip on Railway's private network.
_TIMEOUTS = {"socket_connect_timeout": 2, "socket_timeout": 2}

# Text: the rate limiter's counters and anything else that is a string.
redis_client = Redis.from_url(settings.redis_url, decode_responses=True, **_TIMEOUTS)

# Bytes: the card images (`card:image:*`). Until 26/09 they went through the
# client above, whose `get` tries to decode a PNG as UTF-8, fails, and the
# card routes swallowed the error — so the cache was written on every request
# and never once served, and every `/image` drew the card again.
redis_bytes = Redis.from_url(settings.redis_url, decode_responses=False, **_TIMEOUTS)


async def get_redis() -> Redis:
    return redis_client
