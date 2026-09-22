"""What writes the event's truth is an operator act, and the router says so.

Events are TumTum's (product rule, 21/09), and so is what happened at one.
The **event timeline** is the shared script the correlator reads for everybody
who was there: one row saying "Yellow às 22h12" names that moment on every
fan's card at that show. It is not a place for a note about one person's own
night.

Until 22/09 `POST /events/{id}/timeline` took any signed-in account (item 47),
which was wrong twice: a fan's typed label landed on strangers' cards, and
what they meant privately was readable by everyone else at the event. The fan
keeps naming their own moment — on their phone, where it stays.

These tests read the router rather than a docstring, so the guard cannot be
lost to a refactor without a red build. They are deliberately asymmetric:
reading the timeline is open, writing it is not.
"""

from app.core.auth import require_admin
from app.main import app

# Every route that writes, rebuilds or reveals the operator's side of an
# event. A new one belongs here the day it is written.
OPERATOR_ONLY = {
    ("POST", "/api/events"),
    ("POST", "/api/events/{event_id}"),
    ("POST", "/api/events/{event_id}/timeline"),
    ("DELETE", "/api/events/{event_id}/timeline/{entry_id}"),
    ("POST", "/api/events/{event_id}/timeline/football"),
    ("GET", "/api/events/{event_id}/setlist"),
    ("PUT", "/api/events/{event_id}/setlist"),
    ("POST", "/api/events/{event_id}/setlist/start"),
    ("GET", "/api/events/sources/football"),
    # The moderation queue: what was reported, and the decision on it (#36).
    ("GET", "/api/admin/reports"),
    ("POST", "/api/admin/reports/{post_id}"),
}

# What a fan may do without operating the platform.
OPEN_TO_ANY_READER = {
    ("GET", "/api/events"),
    ("GET", "/api/events/{event_id}"),
    ("GET", "/api/events/{event_id}/timeline"),
}


def guards(method: str, path: str) -> set[str] | None:
    """The dependency names on a route, or None when there is no such route."""
    for route in app.routes:
        if getattr(route, "path", None) != path:
            continue
        if method not in (getattr(route, "methods", None) or set()):
            continue
        dependant = getattr(route, "dependant", None)
        if dependant is None:
            return set()
        return {
            getattr(d.call, "__name__", str(d.call)) for d in dependant.dependencies
        }
    return None


def test_every_operator_route_exists_and_requires_an_operator():
    missing = [f"{m} {p}" for m, p in OPERATOR_ONLY if guards(m, p) is None]
    assert missing == [], f"route gone or renamed: {missing}"

    unguarded = [
        f"{m} {p}"
        for m, p in OPERATOR_ONLY
        if require_admin.__name__ not in (guards(m, p) or set())
    ]
    assert unguarded == [], f"anyone signed in can reach: {unguarded}"


def test_writing_the_timeline_is_not_open_to_any_signed_in_account():
    """The regression item 47 names, on its own so a failure points at it."""
    assert require_admin.__name__ in guards("POST", "/api/events/{event_id}/timeline")


def test_reading_an_event_stays_open():
    """A fan browsing what is on tonight is not an operator act."""
    for method, path in OPEN_TO_ANY_READER:
        found = guards(method, path)
        assert found is not None, f"route gone: {method} {path}"
        assert require_admin.__name__ not in found, f"fans locked out of {path}"


def test_the_setlist_source_that_could_not_be_licensed_is_gone():
    """Setlist.fm was deleted 22/09 (item 49); nothing may quietly restore it."""
    paths = {getattr(r, "path", "") for r in app.routes}
    assert "/api/events/sources/setlist" not in paths
    assert "/api/events/{event_id}/timeline/setlist" not in paths


# --- the event feed: a different gate, and it must not be the admin one ---
#
# The feed is for the people who were at the event (Felipe, 22/09). That is
# neither "anyone signed in" nor "operators": it is proved by an hr_sessions
# row, and `require_attendance` is the only thing that proves it.

ATTENDANCE_ONLY = {
    ("GET", "/api/events/{event_id}/feed"),
    ("POST", "/api/events/{event_id}/feed"),
    ("POST", "/api/events/{event_id}/feed/{post_id}/senti"),
    ("GET", "/api/events/{event_id}/crowd"),
    # Report and block (#36): only somebody who can see a post can act on it.
    ("POST", "/api/events/{event_id}/feed/{post_id}/report"),
    ("POST", "/api/events/{event_id}/feed/{post_id}/block"),
}


def test_reading_posting_and_reacting_all_need_you_to_have_been_there():
    from app.api.feed import require_attendance

    missing = [f"{m} {p}" for m, p in ATTENDANCE_ONLY if guards(m, p) is None]
    assert missing == [], f"route gone or renamed: {missing}"

    ungated = [
        f"{m} {p}"
        for m, p in ATTENDANCE_ONLY
        if require_attendance.__name__ not in (guards(m, p) or set())
    ]
    assert ungated == [], f"open to anyone signed in: {ungated}"


def test_taking_your_own_post_down_is_not_gated_on_attendance():
    """The undo must outlive the night — consent is only real while it does."""
    from app.api.feed import require_attendance

    found = guards("DELETE", "/api/events/{event_id}/feed/{post_id}")
    assert found is not None, "the undo is gone"
    assert require_attendance.__name__ not in found


def test_the_feed_is_not_an_operator_surface():
    """Being staff is not being at the match."""
    for method, path in ATTENDANCE_ONLY:
        assert require_admin.__name__ not in (guards(method, path) or set())
