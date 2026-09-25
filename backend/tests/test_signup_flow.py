"""An account exists only once its e-mail is proved (#64, Felipe 24/09).

Test 7 made an account with `teste@teste.com`. These run the two sign-up
steps against a real (in-memory) database with the mail captured, because
the rules that matter live in the queries: only the newest code works, a
wrong code is counted even though the request fails, and nothing is kept
when the mail could not leave.
"""

import re
import uuid
from datetime import UTC, timedelta

import pytest
import pytest_asyncio
from fastapi import HTTPException
from sqlalchemy import func, select
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.ext.asyncio import async_sessionmaker, create_async_engine
from sqlalchemy.ext.compiler import compiles

import app.main  # noqa: F401 — registers every model
from app.api import auth as auth_api
from app.api.auth import register, register_confirm, register_start
from app.core.database import Base
from app.models.signup_code import SignupCode
from app.models.user import User
from app.schemas.auth import SignupConfirmRequest, SignupStartRequest
from app.services.email import EmailNotConfigured


@compiles(UUID, "sqlite")
def _uuid_on_sqlite(_type, _compiler, **_kw):
    return "CHAR(32)"


@compiles(JSONB, "sqlite")
def _jsonb_on_sqlite(_type, _compiler, **_kw):
    return "TEXT"


@pytest_asyncio.fixture
async def db():
    engine = create_async_engine("sqlite+aiosqlite://")
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    async with async_sessionmaker(engine, expire_on_commit=False)() as session:
        yield session
    await engine.dispose()


@pytest.fixture
def mailbox(monkeypatch):
    """Every mail the server tried to send, instead of sending it."""
    sent: list[dict] = []

    async def fake_send(*, to, subject, html, text):
        sent.append({"to": to, "subject": subject, "html": html, "text": text})

    monkeypatch.setattr(auth_api, "send_email", fake_send)
    return sent


def _code_in(mail: dict) -> str:
    return re.search(r"\b(\d{6})\b", mail["text"]).group(1)


async def _start(db, email="ana@x.cc", name="Ana", password="segredo123"):
    body = SignupStartRequest(email=email, name=name, password=password)
    return await register_start(body, db)


async def _confirm(db, code, email="ana@x.cc"):
    return await register_confirm(SignupConfirmRequest(email=email, code=code), db)


async def _users(db) -> int:
    return (await db.execute(select(func.count()).select_from(User))).scalar_one()


async def _pending(db) -> list[SignupCode]:
    return (await db.execute(select(SignupCode))).scalars().all()


async def _age(db, seconds: int):
    """Move every pending code back in time, as if the clock had run."""
    for row in await _pending(db):
        row.created_at = row.created_at.replace(tzinfo=UTC) - timedelta(seconds=seconds)
        row.expires_at = row.expires_at.replace(tzinfo=UTC) - timedelta(seconds=seconds)
    await db.flush()


@pytest.mark.asyncio
async def test_starting_sends_a_code_and_creates_no_account(db, mailbox):
    started = await _start(db)
    assert started.email == "ana@x.cc"
    assert len(mailbox) == 1 and mailbox[0]["to"] == "ana@x.cc"
    code = _code_in(mailbox[0])
    assert code in mailbox[0]["subject"]
    assert await _users(db) == 0
    # The code itself is nowhere in the table.
    (row,) = await _pending(db)
    assert code not in (row.code_hash, row.hashed_password, row.email, row.name)


@pytest.mark.asyncio
async def test_the_right_code_makes_the_account_and_signs_in(db, mailbox):
    await _start(db, email="Ana@X.cc")
    tokens = await _confirm(db, _code_in(mailbox[0]), email="ana@x.cc")
    assert tokens.access_token and tokens.refresh_token
    user = (await db.execute(select(User))).scalar_one()
    # Stored the way it is signed into (open item 11): as typed, except that
    # EmailStr lowercases the domain, as it did for the old one-step sign-up.
    assert (user.email, user.name, user.auth_provider) == ("Ana@x.cc", "Ana", "email")
    assert user.hashed_password and "segredo123" not in user.hashed_password


@pytest.mark.asyncio
async def test_a_code_works_once(db, mailbox):
    await _start(db)
    code = _code_in(mailbox[0])
    await _confirm(db, code)
    with pytest.raises(HTTPException) as refused:
        await _confirm(db, code)
    assert refused.value.status_code == 400


@pytest.mark.asyncio
async def test_a_wrong_code_counts_down_and_the_fifth_kills_it(db, mailbox):
    await _start(db)
    right = _code_in(mailbox[0])
    wrong = "000000" if right != "000000" else "111111"
    for attempt in range(1, 6):
        with pytest.raises(HTTPException) as refused:
            await _confirm(db, wrong)
        assert refused.value.status_code == 400
        if attempt == 1:
            assert "faltam 4 tentativas" in refused.value.detail
    # The count survived each failed request, and the right code is now dead.
    with pytest.raises(HTTPException) as refused:
        await _confirm(db, right)
    assert refused.value.detail == auth_api.CODE_GONE
    assert await _users(db) == 0


@pytest.mark.asyncio
async def test_a_code_left_fifteen_minutes_is_refused(db, mailbox):
    await _start(db)
    await _age(db, 15 * 60)
    with pytest.raises(HTTPException) as refused:
        await _confirm(db, _code_in(mailbox[0]))
    assert refused.value.detail == auth_api.CODE_GONE


@pytest.mark.asyncio
async def test_a_second_code_waits_a_minute(db, mailbox):
    await _start(db)
    with pytest.raises(HTTPException) as refused:
        await _start(db)
    assert refused.value.status_code == 429
    assert len(mailbox) == 1


@pytest.mark.asyncio
async def test_only_the_newest_code_works(db, mailbox):
    await _start(db)
    await _age(db, 61)
    await _start(db)
    first, second = _code_in(mailbox[0]), _code_in(mailbox[1])
    if first != second:
        with pytest.raises(HTTPException):
            await _confirm(db, first)
    tokens = await _confirm(db, second)
    assert tokens.access_token


@pytest.mark.asyncio
async def test_an_address_gets_five_codes_an_hour(db, mailbox):
    for _ in range(5):
        await _start(db)
        await _age(db, 61)
    with pytest.raises(HTTPException) as refused:
        await _start(db)
    assert refused.value.status_code == 429
    assert len(mailbox) == 5


@pytest.mark.asyncio
async def test_an_address_with_an_account_gets_no_code(db, mailbox):
    db.add(User(id=uuid.uuid4(), email="Ana@x.cc", name="Ana", auth_provider="email"))
    await db.flush()
    with pytest.raises(HTTPException) as refused:
        await _start(db, email="ana@x.cc")
    assert refused.value.status_code == 409
    assert mailbox == []


@pytest.mark.asyncio
async def test_when_the_mail_cannot_leave_nothing_is_kept(db, monkeypatch):
    async def no_mail(**_kw):
        raise EmailNotConfigured("RESEND_API_KEY is not set")

    monkeypatch.setattr(auth_api, "send_email", no_mail)
    with pytest.raises(HTTPException) as refused:
        await _start(db)
    assert refused.value.status_code == 503
    assert refused.value.detail == auth_api.CODE_NOT_SENT
    assert await _pending(db) == []


@pytest.mark.asyncio
async def test_a_sign_up_nobody_confirms_is_forgotten_after_a_day(db, mailbox):
    await _start(db, email="bia@x.cc")
    await _age(db, 25 * 3600)
    await _start(db, email="cris@x.cc")
    assert [row.email for row in await _pending(db)] == ["cris@x.cc"]


@pytest.mark.asyncio
async def test_the_mail_carries_no_text_a_stranger_chose(db, mailbox):
    """The form takes any address; the name typed in it never reaches that inbox."""
    await _start(db, name="Clique aqui: http://golpe.example")
    assert "golpe" not in mailbox[0]["html"] + mailbox[0]["text"] + mailbox[0]["subject"]


@pytest.mark.asyncio
async def test_a_code_that_is_not_six_digits_is_said_so(db, mailbox):
    await _start(db)
    with pytest.raises(HTTPException) as refused:
        await _confirm(db, "12345")
    assert refused.value.detail == "O código tem 6 números."


@pytest.mark.asyncio
async def test_the_old_one_step_sign_up_is_closed():
    """Left open, it would be the way around the code."""
    with pytest.raises(HTTPException) as refused:
        await register()
    assert refused.value.status_code == 410
    assert "código" in refused.value.detail


@pytest.mark.asyncio
async def test_the_reset_mail_greets_the_name_trimmed_and_escaped(db, mailbox):
    """The first reset that ever left (24/09) said "Oi, Felipe Zanucci ." —
    the name as typed once, trailing space and all."""
    from app.api.auth import forgot_password
    from app.schemas.auth import ForgotPasswordRequest

    db.add(
        User(id=uuid.uuid4(), email="felipe@x.cc", name="Felipe <b>Z</b> ", auth_provider="email")
    )
    await db.flush()
    await forgot_password(ForgotPasswordRequest(email="felipe@x.cc"), db)
    (mail,) = mailbox
    assert mail["text"].startswith("Oi, Felipe <b>Z</b>.")
    assert "Oi, Felipe &lt;b&gt;Z&lt;/b&gt;.</p>" in mail["html"]
