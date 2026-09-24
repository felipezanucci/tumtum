"""The rules of the sign-up code (#64, 24/09), without a server or a mailbox."""

from datetime import UTC, datetime, timedelta

from app.services import signup_codes as codes

NOW = datetime(2026, 9, 24, 20, 0, tzinfo=UTC)
SECRET = "test-secret"


def test_a_code_is_six_digits_and_keeps_its_leading_zeros():
    for _ in range(500):
        code = codes.generate_code()
        assert len(code) == 6 and code.isdigit()


def test_codes_vary():
    assert len({codes.generate_code() for _ in range(200)}) > 150


def test_the_table_never_holds_the_code():
    code = codes.generate_code()
    assert code not in codes.hash_code("ana@x.cc", code, SECRET)


def test_the_hash_needs_the_server_secret():
    """Six digits are a million guesses; without the key, the table gives none away."""
    assert codes.hash_code("ana@x.cc", "123456", SECRET) != codes.hash_code(
        "ana@x.cc", "123456", "another-secret"
    )


def test_a_code_belongs_to_its_address():
    assert codes.hash_code("ana@x.cc", "123456", SECRET) != codes.hash_code(
        "bia@x.cc", "123456", SECRET
    )


def test_the_address_is_compared_without_case_or_spaces():
    h = codes.hash_code("Ana@X.cc", "123456", SECRET)
    assert codes.matches(h, " ana@x.cc ", "123456", SECRET)
    assert not codes.matches(h, "ana@x.cc", "123457", SECRET)


def test_what_a_person_types_is_forgiven_its_spaces_and_dashes():
    assert codes.clean_code(" 123 456 ") == "123456"
    assert codes.clean_code("123-456") == "123456"
    assert codes.clean_code("012345") == "012345"


def test_anything_but_six_digits_is_not_a_code():
    for raw in ("", "12345", "1234567", "12345a", "１２３４５６", "abcdef"):
        assert codes.clean_code(raw) is None, raw


def test_a_fresh_code_is_open():
    assert codes.is_open(codes.expiry_from(NOW), None, 0, NOW)


def test_a_code_dies_at_fifteen_minutes():
    assert codes.CODE_TTL == timedelta(minutes=15)
    expires = codes.expiry_from(NOW)
    assert not codes.is_open(expires, None, 0, expires)


def test_a_spent_code_is_closed():
    assert not codes.is_open(codes.expiry_from(NOW), NOW, 0, NOW)


def test_five_wrong_guesses_close_it():
    assert codes.MAX_ATTEMPTS == 5
    assert codes.is_open(codes.expiry_from(NOW), None, 4, NOW)
    assert not codes.is_open(codes.expiry_from(NOW), None, 5, NOW)


def test_a_naive_expiry_does_not_raise():
    naive = codes.expiry_from(NOW).replace(tzinfo=None)
    assert codes.is_open(naive, None, 0, NOW)


def test_another_code_waits_a_minute():
    assert codes.seconds_until_resend(None, NOW) == 0
    assert codes.seconds_until_resend(NOW, NOW) == 60
    assert codes.seconds_until_resend(NOW - timedelta(seconds=59.5), NOW) == 1
    assert codes.seconds_until_resend(NOW - timedelta(seconds=60), NOW) == 0


def test_an_address_gets_five_codes_an_hour():
    four = [NOW - timedelta(minutes=m) for m in (5, 15, 25, 35)]
    assert not codes.over_hourly_cap(four, NOW)
    assert codes.over_hourly_cap([*four, NOW - timedelta(minutes=45)], NOW)
    # Older than an hour does not count.
    assert not codes.over_hourly_cap([*four, NOW - timedelta(minutes=61)], NOW)


def test_a_wrong_code_counts_down_in_words():
    assert "faltam 4 tentativas" in codes.attempts_left_message(1)
    assert "falta 1 tentativa" in codes.attempts_left_message(4)
    assert "Pede um código novo" in codes.attempts_left_message(5)
