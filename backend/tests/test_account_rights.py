"""The person's rights over their account (LGPD art. 18), against a real
(in-memory) database: see it all, take it all, correct the e-mail, ask, and
delete — with the password, leaving nothing that names them.
"""

import json
import re
import uuid
from datetime import date, timedelta

import pytest
from fastapi import HTTPException
from sqlalchemy import func, select

from app.api import users as users_api
from app.api.admin_requests import answer_request, list_requests
from app.api.users import (
    confirm_email_change,
    delete_profile,
    my_data,
    my_export,
    my_export_csv,
    my_requests,
    open_request,
    start_email_change,
    update_profile,
)
from app.main import app
from app.models.card import Card
from app.models.consent import Consent
from app.models.hr_data import HRData
from app.models.hr_session import HRSession
from app.models.privacy import (
    DataAccessLog,
    DataSubjectRequest,
    DeletionLog,
    EmailChange,
)
from app.models.refresh_token import RefreshToken
from app.models.signup_code import SignupCode
from app.models.user import User
from app.models.waitlist_entry import WaitlistEntry
from app.schemas.privacy import DataSubjectRequestCreate, DataSubjectRequestUpdate
from app.schemas.user import (
    DeleteAccountRequest,
    EmailChangeConfirm,
    EmailChangeRequest,
    UserUpdateRequest,
)
from app.services import refresh_tokens
from app.services.age import UNDER_AGE, is_adult
from tests.conftest import add_night, add_user, grant, make_request


async def _count(db, model, *where) -> int:
    query = select(func.count()).select_from(model)
    for condition in where:
        query = query.where(condition)
    return (await db.execute(query)).scalar_one()


@pytest.fixture
def mailbox(monkeypatch):
    sent: list[dict] = []

    async def fake_send(*, to, subject, html, text):
        sent.append({"to": to, "subject": subject, "text": text})

    monkeypatch.setattr(users_api, "send_email", fake_send)
    return sent


# --- age ---


def test_eighteen_on_the_birthday_itself():
    assert is_adult(date(2008, 9, 26), date(2026, 9, 26))
    assert not is_adult(date(2008, 9, 27), date(2026, 9, 26))
    assert not is_adult(date(2030, 1, 1), date(2026, 9, 26))


@pytest.mark.asyncio
async def test_the_birth_date_is_given_once(memdb):
    user = await add_user(memdb, birth=None)
    profile = await update_profile(
        UserUpdateRequest(birth_date=date(1990, 1, 2)), user, memdb
    )
    assert profile.birth_date == date(1990, 1, 2)
    with pytest.raises(HTTPException) as refused:
        await update_profile(
            UserUpdateRequest(birth_date=date(1980, 1, 2)), user, memdb
        )
    assert refused.value.status_code == 409


@pytest.mark.asyncio
async def test_a_minor_cannot_fill_the_gate(memdb):
    user = await add_user(memdb, birth=None)
    with pytest.raises(HTTPException) as refused:
        await update_profile(
            UserUpdateRequest(birth_date=date(2015, 1, 1)), user, memdb
        )
    assert (refused.value.status_code, refused.value.detail) == (422, UNDER_AGE)
    assert user.birth_date is None


# --- deletion ---


def test_the_old_delete_without_a_password_is_gone():
    routes = {
        (m, r.path) for r in app.routes for m in (getattr(r, "methods", None) or ())
    }
    assert ("DELETE", "/api/users/me") not in routes
    assert ("POST", "/api/users/me/delete") in routes


@pytest.mark.asyncio
async def test_a_wrong_password_deletes_nothing(memdb, fake_redis):
    user = await add_user(memdb, password="segredo123")
    with pytest.raises(HTTPException) as refused:
        await delete_profile(
            DeleteAccountRequest(password="errada"), make_request(), user, memdb
        )
    assert refused.value.status_code == 401
    assert await _count(memdb, User) == 1


@pytest.mark.asyncio
async def test_deletion_takes_everything_around_the_account_and_leaves_no_name(
    memdb, fake_redis
):
    from datetime import UTC, datetime

    user = await add_user(memdb, "Ana", email="Ana@x.cc", password="segredo123")
    night = await add_night(memdb, user)
    card = Card(id=uuid.uuid4(), user_id=user.id, session_id=night.id, card_type="solo")
    memdb.add(card)
    await grant(memdb, user, "terms", "keep_night")
    now = datetime.now(UTC)
    memdb.add_all(
        [
            DataSubjectRequest(user_id=user.id, kind="access", due_at=now),
            DataAccessLog(subject_user_id=user.id, resource="card", action="read"),
            EmailChange(
                user_id=user.id,
                new_email="nova@x.cc",
                email_key="nova@x.cc",
                code_hash="h",
                expires_at=now,
            ),
            SignupCode(
                email="ana@x.cc",
                email_key="ana@x.cc",
                name="Ana",
                hashed_password="h",
                code_hash="h",
                expires_at=now,
            ),
            WaitlistEntry(email="ANA@x.cc"),
        ]
    )
    await memdb.flush()
    fake_redis.store[f"card:image:{card.id}"] = b"png"

    await delete_profile(
        DeleteAccountRequest(password="segredo123"), make_request(), user, memdb
    )

    for model in (
        User,
        HRSession,
        HRData,
        Card,
        Consent,
        DataSubjectRequest,
        EmailChange,
        SignupCode,
        WaitlistEntry,
    ):
        assert await _count(memdb, model) == 0, model.__tablename__
    assert fake_redis.store == {}
    assert await _count(memdb, DeletionLog) == 1
    # What is left of the deletion in the access log names nobody.
    (log,) = (await memdb.execute(select(DataAccessLog))).scalars().all()
    assert (log.resource, log.action) == ("account", "delete")
    assert log.actor_user_id is None and log.subject_user_id is None


# --- access and portability ---


@pytest.mark.asyncio
async def test_my_data_is_everything_but_the_readings(memdb):
    user = await add_user(memdb)
    await add_night(memdb, user)
    await grant(memdb, user, "terms")
    data = await my_data(make_request(), user, memdb)
    assert data.user.email == user.email
    assert data.birth_date == user.birth_date
    assert [c.purpose for c in data.consents] == ["terms"]
    assert len(data.sessions) == 1
    assert "data_points" not in data.sessions[0].model_dump()
    assert await _count(memdb, DataAccessLog, DataAccessLog.action == "read") == 1


@pytest.mark.asyncio
async def test_the_export_carries_every_reading_as_a_file(memdb):
    user = await add_user(memdb)
    await add_night(memdb, user, readings=12)
    response = await my_export(make_request(), user, memdb)
    assert response.headers["content-disposition"] == (
        "attachment; filename=tumtum-export.json"
    )
    body = json.loads(response.body)
    assert len(body["sessions"][0]["data_points"]) == 12
    assert set(body) >= {"user", "consents", "peaks", "cards", "posts", "requests"}


@pytest.mark.asyncio
async def test_the_csv_is_session_time_bpm(memdb):
    user = await add_user(memdb)
    night = await add_night(memdb, user, readings=3)
    other = await add_user(memdb, "Bia")
    await add_night(memdb, other, readings=5)
    response = await my_export_csv(make_request(), user, memdb)
    lines = response.body.decode().strip().splitlines()
    assert lines[0] == "session_id,time,bpm"
    assert len(lines) == 4 and all(line.startswith(str(night.id)) for line in lines[1:])


# --- requests ---


@pytest.mark.asyncio
async def test_a_request_is_open_with_a_fifteen_day_deadline(memdb):
    user = await add_user(memdb)
    made = await open_request(
        DataSubjectRequestCreate(kind="correction", message=" meu nome "), user, memdb
    )
    assert made.status == "open" and made.message == "meu nome"
    assert made.due_at - made.opened_at == timedelta(days=15)
    assert made.answered_at is None and made.answer is None
    assert [r.id for r in await my_requests(user, memdb)] == [made.id]


@pytest.mark.asyncio
async def test_the_operator_answers_and_the_read_is_logged(memdb):
    user = await add_user(memdb)
    admin = await add_user(memdb, "Op")
    made = await open_request(DataSubjectRequestCreate(kind="access"), user, memdb)
    queue = await list_requests(make_request(), admin, memdb)
    assert [(r.id, r.user_email) for r in queue] == [(made.id, user.email)]
    answered = await answer_request(
        made.id,
        DataSubjectRequestUpdate(status="answered", answer="Mandamos por e-mail."),
        make_request(),
        admin,
        memdb,
    )
    assert answered.status == "answered" and answered.answered_at is not None
    logged = (await memdb.execute(select(DataAccessLog))).scalars().all()
    assert {(r.actor_user_id, r.subject_user_id) for r in logged} == {
        (admin.id, user.id)
    }


# --- changing the e-mail ---


def _code(mail) -> str:
    return re.search(r"\b(\d{6})\b", mail["text"]).group(1)


@pytest.mark.asyncio
async def test_changing_the_email_needs_the_password_and_the_code(memdb, mailbox):
    user = await add_user(memdb, email="ana@x.cc", password="segredo123")
    await refresh_tokens.issue(memdb, user.id)

    with pytest.raises(HTTPException) as refused:
        await start_email_change(
            EmailChangeRequest(email="nova@x.cc", password="x"), user, memdb
        )
    assert refused.value.status_code == 401
    assert mailbox == []

    started = await start_email_change(
        EmailChangeRequest(email="nova@x.cc", password="segredo123"), user, memdb
    )
    assert started.email == "nova@x.cc"
    assert mailbox[0]["to"] == "nova@x.cc"
    assert user.email == "ana@x.cc"  # nothing changes before the code comes back

    profile = await confirm_email_change(
        EmailChangeConfirm(code=_code(mailbox[0])), user, memdb
    )
    assert profile.email == "nova@x.cc"
    live = await _count(memdb, RefreshToken, RefreshToken.revoked_at.is_(None))
    assert live == 0  # every device signs in again


@pytest.mark.asyncio
async def test_an_address_of_another_account_is_refused(memdb, mailbox):
    await add_user(memdb, "Bia", email="Bia@x.cc")
    user = await add_user(memdb, email="ana@x.cc", password="segredo123")
    with pytest.raises(HTTPException) as refused:
        await start_email_change(
            EmailChangeRequest(email="bia@x.cc", password="segredo123"), user, memdb
        )
    assert refused.value.status_code == 409
    assert mailbox == []


@pytest.mark.asyncio
async def test_a_wrong_email_code_counts_down(memdb, mailbox):
    user = await add_user(memdb, email="ana@x.cc", password="segredo123")
    await start_email_change(
        EmailChangeRequest(email="nova@x.cc", password="segredo123"), user, memdb
    )
    wrong = "000000" if _code(mailbox[0]) != "000000" else "111111"
    with pytest.raises(HTTPException) as refused:
        await confirm_email_change(EmailChangeConfirm(code=wrong), user, memdb)
    assert "faltam 4 tentativas" in refused.value.detail
    assert user.email == "ana@x.cc"
