"""Account deletion removes children before parents.

Three foreign keys carry no ON DELETE clause, so the database refuses a
delete in the wrong order instead of cascading it. The order is the
correctness; this pins it.
"""

from app.services.account_deletion import DELETION_ORDER


def _before(child: str, parent: str) -> bool:
    return DELETION_ORDER.index(child) < DELETION_ORDER.index(parent)


def test_children_go_before_their_parents():
    assert _before("shares", "cards")
    assert _before("cards", "hr_sessions")
    assert _before("cards", "peaks")  # cards point at peaks
    assert _before("hr_data", "hr_sessions")
    assert _before("peaks", "hr_sessions")
    assert _before("hr_sessions", "users")
    assert _before("wearable_connections", "users")
    assert _before("password_reset_tokens", "users")
    assert _before("cards", "users")


def test_the_user_goes_last_and_events_are_not_touched():
    assert DELETION_ORDER[-1] == "users"
    assert "events" not in DELETION_ORDER
    assert "event_timeline" not in DELETION_ORDER
