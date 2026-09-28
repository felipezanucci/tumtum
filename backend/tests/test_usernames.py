# ruff: noqa: F811 — the sign-up fixtures are imported and then requested by name
"""The @ with one owner (28/09).

Until 28/09 it lived only on the phone, checked against four names written
into the app: two accounts could hold the same @, and every name was
"disponível". These pin the contract the app and the site now rely on.
"""

import pytest
from fastapi import HTTPException
from sqlalchemy import select

from app.api.auth import check_username
from app.api.users import update_profile
from app.models.user import User
from app.schemas.user import UserUpdateRequest
from app.services import usernames
from tests.conftest import add_user
from tests.test_signup_flow import (  # noqa: F401 — fixtures
    _code_in,
    _confirm,
    _start,
    db,
    mailbox,
)


def test_what_an_at_may_be():
    assert usernames.clean("  @FeZanu ") == "fezanu"
    assert usernames.problem("fe") == usernames.TOO_SHORT
    assert usernames.problem("a" * 21) == usernames.TOO_LONG
    assert usernames.problem("josé") == usernames.BAD_CHARS
    assert usernames.problem("fe.zanu") == usernames.BAD_CHARS
    assert usernames.problem("tumtum") == usernames.RESERVED_NAME
    assert usernames.problem("fe_zanu2") is None


@pytest.mark.asyncio
async def test_the_check_says_taken_whatever_the_case(db, mailbox):
    assert (await check_username("fezanu", db)).available is True
    await _start(db, username="FeZanu")
    await _confirm(db, _code_in(mailbox[0]))
    answer = await check_username("@FEZANU", db)
    assert (answer.available, answer.reason) == (False, usernames.TAKEN)


@pytest.mark.asyncio
async def test_a_second_account_cannot_take_an_at_already_held(db, mailbox):
    """The b220 finding: @fezanu twice, and "disponível" both times."""
    await _start(db, email="ana@x.cc", username="fezanu")
    await _confirm(db, _code_in(mailbox[0]), email="ana@x.cc")
    with pytest.raises(HTTPException) as refused:
        await _start(db, email="bia@x.cc", name="Bia", username="fezanu")
    assert (refused.value.status_code, refused.value.detail) == (409, usernames.TAKEN)


@pytest.mark.asyncio
async def test_only_an_account_holds_an_at_and_the_second_to_confirm_is_told(
    db, mailbox
):
    """A pending code holds nothing: correcting a mistyped address must not
    turn the person's own @ into "já tem dono" (site stream, 28/09). Two
    people with the same free @ both get a code; the first to confirm keeps it."""
    await _start(db, email="ana@x.cc", username="fezanu")
    assert (await check_username("fezanu", db)).available is True
    await _start(db, email="ana.certo@x.cc", username="fezanu")  # the corrected address
    await _confirm(db, _code_in(mailbox[1]), email="ana.certo@x.cc")
    with pytest.raises(HTTPException) as late:
        await _confirm(db, _code_in(mailbox[0]), email="ana@x.cc")
    assert (late.value.status_code, late.value.detail) == (409, usernames.TAKEN)


@pytest.mark.asyncio
async def test_a_client_that_sends_none_still_gets_a_free_one(db, mailbox):
    await _start(db, email="ana@x.cc", name="Ana Paula")
    await _confirm(db, _code_in(mailbox[0]), email="ana@x.cc")
    await _start(db, email="ana2@x.cc", name="Ana Paula")
    await _confirm(db, _code_in(mailbox[1]), email="ana2@x.cc")
    names = sorted((await db.execute(select(User.username))).scalars().all())
    assert names == ["anapaula", "anapaula2"]


@pytest.mark.asyncio
async def test_an_old_account_chooses_once_and_it_is_fixed(db):
    user = await add_user(db)
    other = await add_user(db, "Bia", email="bia@x.cc")
    other.username = "fezanu"
    await db.flush()
    with pytest.raises(HTTPException) as refused:
        await update_profile(UserUpdateRequest(username="FEZANU"), user, db)
    assert refused.value.status_code == 409
    profile = await update_profile(UserUpdateRequest(username="@anazinha"), user, db)
    assert profile.username == "anazinha"
    with pytest.raises(HTTPException) as fixed:
        await update_profile(UserUpdateRequest(username="outra"), user, db)
    assert fixed.value.status_code == 409
    # Sending the same one again is not a change.
    assert (
        await update_profile(UserUpdateRequest(username="anazinha"), user, db)
    ).username == "anazinha"
