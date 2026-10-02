"""The app's "esqueci a senha" (02/10): a 6-digit code in the reset mail.

Felipe, on b227, forgot his password in the app and found only a sentence
sending him to the site. The mail now carries a code the app takes back with
the new password; these hold the rules end to end, through the router.
"""

import re

import pytest

from app.api import auth as auth_api
from app.api.auth import RESET_CODE_REFUSED
from tests.conftest import add_user


@pytest.fixture
def mailbox(monkeypatch):
    sent: list[dict] = []

    async def fake_send(*, to, subject, html, text):
        sent.append({"to": to, "subject": subject, "html": html, "text": text})

    monkeypatch.setattr(auth_api, "send_email", fake_send)
    return sent


def _code_in(mail: dict) -> str:
    return re.search(r"\b(\d{6})\b", mail["text"]).group(1)


async def _forgot(client, email="ana@x.cc"):
    answer = await client.post("/api/auth/forgot-password", json={"email": email})
    assert answer.status_code == 200
    return answer


def _wrong(code: str) -> str:
    return f"{(int(code) + 1) % 1_000_000:06d}"


@pytest.mark.asyncio
async def test_the_mail_carries_a_code_and_the_link_and_the_table_neither(
    memdb, api, mailbox
):
    from sqlalchemy import select

    from app.models.password_reset_token import PasswordResetToken

    await add_user(memdb, "Ana", email="ana@x.cc", password="segredo123")
    await _forgot(api())

    mail = mailbox[-1]
    code = _code_in(mail)
    assert code in mail["subject"]
    assert "/redefinir-senha?token=" in mail["text"]
    row = (await memdb.execute(select(PasswordResetToken))).scalar_one()
    assert row.code_hash and code not in row.code_hash
    assert row.attempts == 0


@pytest.mark.asyncio
async def test_the_code_sets_the_password_and_signs_in(memdb, api, mailbox):
    await add_user(memdb, "Ana", email="ana@x.cc", password="segredo123")
    client = api()
    await _forgot(client)

    answer = await client.post(
        "/api/auth/reset-password/code",
        json={
            "email": "ANA@x.cc",
            "code": _code_in(mailbox[-1]),
            "password": "nova-senha",
        },
    )
    assert answer.status_code == 200
    assert answer.json()["access_token"]

    old = await client.post(
        "/api/auth/login", json={"email": "ana@x.cc", "password": "segredo123"}
    )
    new = await client.post(
        "/api/auth/login", json={"email": "ana@x.cc", "password": "nova-senha"}
    )
    assert old.status_code == 401 and new.status_code == 200


@pytest.mark.asyncio
async def test_a_code_works_once(memdb, api, mailbox):
    await add_user(memdb, "Ana", email="ana@x.cc", password="segredo123")
    client = api()
    await _forgot(client)
    code = _code_in(mailbox[-1])
    body = {"email": "ana@x.cc", "code": code, "password": "nova-senha"}

    assert (
        await client.post("/api/auth/reset-password/code", json=body)
    ).status_code == 200
    again = await client.post(
        "/api/auth/reset-password/code", json={**body, "password": "outra-senha"}
    )
    assert again.status_code == 400
    assert again.json()["detail"] == RESET_CODE_REFUSED


@pytest.mark.asyncio
async def test_five_wrong_guesses_kill_the_code(memdb, api, mailbox):
    await add_user(memdb, "Ana", email="ana@x.cc", password="segredo123")
    client = api()
    await _forgot(client)
    code = _code_in(mailbox[-1])

    for _ in range(5):
        wrong = await client.post(
            "/api/auth/reset-password/code",
            json={"email": "ana@x.cc", "code": _wrong(code), "password": "nova-senha"},
        )
        assert wrong.status_code == 400
    right = await client.post(
        "/api/auth/reset-password/code",
        json={"email": "ana@x.cc", "code": code, "password": "nova-senha"},
    )
    assert right.status_code == 400


@pytest.mark.asyncio
async def test_an_unknown_address_gets_the_same_sentence_as_a_wrong_code(
    memdb, api, mailbox
):
    await add_user(memdb, "Ana", email="ana@x.cc", password="segredo123")
    client = api()
    await _forgot(client)
    code = _code_in(mailbox[-1])

    nobody = await client.post(
        "/api/auth/reset-password/code",
        json={"email": "ninguem@x.cc", "code": code, "password": "nova-senha"},
    )
    wrong = await client.post(
        "/api/auth/reset-password/code",
        json={"email": "ana@x.cc", "code": _wrong(code), "password": "nova-senha"},
    )
    assert nobody.status_code == wrong.status_code == 400
    assert nobody.json()["detail"] == wrong.json()["detail"] == RESET_CODE_REFUSED


@pytest.mark.asyncio
async def test_only_the_latest_code_counts(memdb, api, mailbox):
    import asyncio

    await add_user(memdb, "Ana", email="ana@x.cc", password="segredo123")
    client = api()
    await _forgot(client)
    first = _code_in(mailbox[-1])
    await asyncio.sleep(0.01)
    await _forgot(client)
    second = _code_in(mailbox[-1])
    if first == second:
        pytest.skip("the two random codes happened to be equal")

    stale = await client.post(
        "/api/auth/reset-password/code",
        json={"email": "ana@x.cc", "code": first, "password": "nova-senha"},
    )
    fresh = await client.post(
        "/api/auth/reset-password/code",
        json={"email": "ana@x.cc", "code": second, "password": "nova-senha"},
    )
    assert stale.status_code == 400 and fresh.status_code == 200


@pytest.mark.asyncio
async def test_a_code_that_is_not_six_digits_is_said_so(memdb, api, mailbox):
    answer = await api().post(
        "/api/auth/reset-password/code",
        json={"email": "ana@x.cc", "code": "12345", "password": "nova-senha"},
    )
    assert answer.status_code == 400
    assert "6 números" in answer.json()["detail"]


def test_the_columns_are_added_at_startup():
    from app.core.schema_catchup import statements

    joined = " ".join(statements())
    assert "password_reset_tokens ADD COLUMN IF NOT EXISTS code_hash" in joined
    assert "password_reset_tokens ADD COLUMN IF NOT EXISTS attempts" in joined
