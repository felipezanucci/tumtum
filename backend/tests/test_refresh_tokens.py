"""A session lasts as long as the device is used (#34, 2026-09-22).

The old credential was a 24-hour access token with nothing to renew it, so
every account was signed out daily — while the app still showed the person's
name and avatar, which Felipe read as the app contradicting itself. These
tests hold the rules of the replacement: rotation, a sliding 90 days, and a
reused token taken as the evidence of a copy that it is.
"""

import uuid
from datetime import UTC, datetime, timedelta

from app.core.auth import ACCESS_TOKEN_EXPIRE_MINUTES
from app.main import app
from app.models.refresh_token import RefreshToken
from app.services import refresh_tokens as rt

NOW = datetime(2026, 9, 22, 22, 0, tzinfo=UTC)


def row(**kw) -> RefreshToken:
    base = dict(
        user_id=uuid.uuid4(),
        family_id=uuid.uuid4(),
        token_hash="x" * 64,
        expires_at=NOW + timedelta(days=30),
        revoked_at=None,
    )
    base.update(kw)
    return RefreshToken(**base)


def test_a_live_token_renews():
    assert rt.verdict(row(), NOW) == "ok"


def test_a_token_nobody_issued_does_not():
    assert rt.verdict(None, NOW) == "unknown"


def test_ninety_days_untouched_is_the_end():
    assert rt.verdict(row(expires_at=NOW - timedelta(seconds=1)), NOW) == "expired"


def test_a_rotated_token_presented_again_is_a_copy():
    """Reuse wins over expiry: a spent token is evidence whatever its age."""
    spent = row(
        revoked_at=NOW - timedelta(hours=1),
        revoke_reason=rt.ROTATED,
        expires_at=NOW - timedelta(days=1),
    )
    assert rt.verdict(spent, NOW) == "reused"


def test_a_lost_answer_can_be_asked_for_again():
    """Stadium cellular: the server rotated, the phone never heard."""
    just_rotated = row(revoked_at=NOW - timedelta(seconds=20), revoke_reason=rt.ROTATED)
    assert rt.verdict(just_rotated, NOW, child_unused=True) == "retry"


def test_but_not_once_the_new_token_has_been_used():
    """If the child was spent, whoever holds the parent is holding a copy."""
    just_rotated = row(revoked_at=NOW - timedelta(seconds=20), revoke_reason=rt.ROTATED)
    assert rt.verdict(just_rotated, NOW, child_unused=False) == "reused"


def test_and_not_after_the_grace():
    late = row(
        revoked_at=NOW - rt.RETRY_GRACE - timedelta(seconds=1),
        revoke_reason=rt.ROTATED,
    )
    assert rt.verdict(late, NOW, child_unused=True) == "reused"


def test_a_signed_out_token_stays_signed_out():
    """ "Sair" means out — no grace applies to a logout or a reset."""
    for reason in (rt.LOGOUT, rt.RESET, rt.REUSE):
        gone = row(revoked_at=NOW - timedelta(seconds=5), revoke_reason=reason)
        assert rt.verdict(gone, NOW, child_unused=True) == "revoked"


def test_a_naive_expiry_from_the_database_is_read_as_utc():
    naive = row(expires_at=(NOW + timedelta(days=1)).replace(tzinfo=None))
    assert rt.verdict(naive, NOW) == "ok"


def test_the_window_slides_ninety_days_from_each_use():
    assert rt.REFRESH_TOKEN_DAYS == 90
    assert rt.expiry_from(NOW) == NOW + timedelta(days=90)


def test_the_access_token_is_short_now_that_something_renews_it():
    assert ACCESS_TOKEN_EXPIRE_MINUTES <= 60


def test_refresh_and_logout_exist_and_need_no_access_token():
    """A device whose hour ran out must still be able to renew or sign out."""
    wanted = {("POST", "/api/auth/refresh"), ("POST", "/api/auth/logout")}
    for route in app.routes:
        key = next(
            (
                (m, route.path)
                for m in getattr(route, "methods", set()) or set()
                if (m, getattr(route, "path", "")) in wanted
            ),
            None,
        )
        if key is None:
            continue
        wanted.discard(key)
        names = {getattr(d.call, "__name__", "") for d in route.dependant.dependencies}
        assert "get_current_user" not in names, key
    assert wanted == set(), f"missing: {wanted}"
