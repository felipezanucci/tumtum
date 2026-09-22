"""What a report and a block do (#36, item 55, 22/09).

Kept apart from the routes so the rules are testable on their own:

- a post with [HIDE_AT] distinct open reports leaves the feed until an
  operator decides — a crowd can take something down faster than a person
  can read an e-mail, and a wrongly hidden post comes back with one click;
- a block hides both people from each other.
"""

REASONS = ("abuse", "fake", "other")
HIDE_AT = 3


def hidden_by_reports(open_reports: int) -> bool:
    return open_reports >= HIDE_AT


def clean_reason(reason: str | None) -> str:
    return reason if reason in REASONS else "other"


def blocked_either_way(viewer_id, author_id, blocks: set[tuple]) -> bool:
    """Whether a block stands between these two, whichever of them made it."""
    return (viewer_id, author_id) in blocks or (author_id, viewer_id) in blocks
