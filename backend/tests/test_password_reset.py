from datetime import UTC, datetime, timedelta

from app.services.password_reset import (
    RESET_TOKEN_TTL,
    expiry_from,
    generate_token,
    hash_token,
    is_usable,
)

NOW = datetime(2026, 8, 26, 12, 0, tzinfo=UTC)


def test_every_token_is_different():
    assert len({generate_token() for _ in range(200)}) == 200


def test_a_token_is_long_enough_to_be_unguessable():
    # 32 random bytes, urlsafe-encoded. The exact length is incidental; what
    # matters is that it is nowhere near brute-forceable.
    assert len(generate_token()) >= 40


def test_the_hash_hides_the_token():
    token = generate_token()
    assert token not in hash_token(token)


def test_the_same_token_always_hashes_the_same():
    # Otherwise the lookup on redemption could never find the row.
    token = generate_token()
    assert hash_token(token) == hash_token(token)


def test_different_tokens_hash_differently():
    assert hash_token("a") != hash_token("b")


def test_a_fresh_token_is_usable():
    assert is_usable(expiry_from(NOW), None, NOW) is True


def test_a_token_expires():
    expires = expiry_from(NOW)
    assert is_usable(expires, None, expires + timedelta(seconds=1)) is False


def test_a_token_is_dead_the_moment_it_expires():
    expires = expiry_from(NOW)
    assert is_usable(expires, None, expires) is False


def test_a_spent_token_cannot_be_spent_again():
    # Single use is what stops a link left in an inbox from staying a key for
    # the rest of its lifetime.
    assert is_usable(expiry_from(NOW), NOW, NOW) is False


def test_a_spent_token_stays_dead_even_before_expiry():
    fresh = expiry_from(NOW)
    assert is_usable(fresh, NOW, NOW + timedelta(minutes=1)) is False


def test_a_naive_expiry_does_not_raise():
    # Rows written before timezone handling settled can come back naive.
    # Comparing naive to aware raises, which would turn an expired token into
    # a 500 instead of a polite refusal.
    naive = expiry_from(NOW).replace(tzinfo=None)
    assert is_usable(naive, None, NOW) is True


def test_the_window_is_half_an_hour():
    # Long enough to walk to a laptop, short enough that a link in an inbox
    # stops being a key quickly. Change this deliberately, not by accident.
    assert RESET_TOKEN_TTL.total_seconds() == 30 * 60
