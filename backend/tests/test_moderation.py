"""Report and block (#36, item 55, 22/09).

The feed shipped without either, and the decision log said so: a
crowd-sized moderation problem, still a blocker before the store listing
goes public. These tests hold what each one does.
"""

import uuid

from app.services import moderation

A, B, C = uuid.uuid4(), uuid.uuid4(), uuid.uuid4()


def test_a_block_hides_both_people_from_each_other():
    blocks = {(A, B)}
    assert moderation.blocked_either_way(A, B, blocks)
    assert moderation.blocked_either_way(B, A, blocks), "not a one-way window"
    assert not moderation.blocked_either_way(A, C, blocks)


def test_three_people_take_a_post_down_until_an_operator_looks():
    assert not moderation.hidden_by_reports(2)
    assert moderation.hidden_by_reports(3)


def test_a_reason_is_one_of_three_and_never_free_text():
    assert moderation.clean_reason("abuse") == "abuse"
    assert moderation.clean_reason("fake") == "fake"
    assert moderation.clean_reason("você é feio") == "other"
    assert moderation.clean_reason(None) == "other"
