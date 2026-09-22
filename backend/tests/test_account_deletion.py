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


def test_every_table_that_points_at_a_person_is_deleted_with_them():
    """Read the schema, not a list somebody remembered to update.

    The feed shipped on 22/09 with four tables pointing at users and nights,
    and none of them here: deleting an account that had ever posted was
    refused by the database. This walks every foreign key instead.
    """
    import app.main  # noqa: F401 — registers every model
    from app.core.database import Base

    owned = {"users", "hr_sessions", "cards", "event_posts"}
    pointing = set()
    changed = True
    while changed:
        changed = False
        for table in Base.metadata.tables.values():
            for fk in table.foreign_keys:
                if fk.column.table.name in owned and table.name not in owned | pointing:
                    if table.name in {"events", "event_timeline", "event_setlist"}:
                        continue
                    pointing.add(table.name)
                    changed = True
        owned |= pointing

    missing = sorted((owned - {"events"}) - set(DELETION_ORDER))
    assert missing == [], f"not deleted with the account: {missing}"


def test_the_feed_goes_before_the_nights_and_the_people_it_points_at():
    assert _before("post_reports", "event_posts")
    assert _before("event_post_reactions", "event_posts")
    assert _before("event_posts", "hr_sessions")
    assert _before("user_blocks", "users")
    assert _before("refresh_tokens", "users")
