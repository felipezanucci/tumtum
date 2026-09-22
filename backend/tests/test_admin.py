"""Who operates the platform is a server setting, never a claim from a client.

Events are TumTum's (product rule, 21/09): creating one, correcting one,
attaching a match or a setlist to one are operator acts. The operator is an
account named in ``admin_emails`` — or in the older ``waitlist_admin_emails``,
so one setting on Railway is enough — and nobody else, however signed in.
"""

from types import SimpleNamespace

import pytest
from fastapi import HTTPException

from app.config import Settings
from app.core import auth


def settings_with(**values) -> Settings:
    # ``_env_file=None`` so a developer's own .env cannot leak into the test.
    return Settings(_env_file=None, **values)


def test_nobody_is_admin_by_default():
    s = settings_with()
    assert s.admins == set()
    assert not s.is_admin("felipe@tumtum.cc")


def test_admin_emails_are_trimmed_and_case_insensitive():
    s = settings_with(admin_emails=" Felipe@TumTum.cc , ops@tumtum.cc ")
    assert s.is_admin("felipe@tumtum.cc")
    assert s.is_admin("OPS@tumtum.cc")
    assert not s.is_admin("fan@example.com")


def test_the_waitlist_admins_operate_events_too():
    s = settings_with(waitlist_admin_emails="felipe@tumtum.cc")
    assert s.is_admin("felipe@tumtum.cc")
    assert s.waitlist_admins == {"felipe@tumtum.cc"}


@pytest.mark.asyncio
async def test_require_admin_refuses_a_signed_in_fan_with_403(monkeypatch):
    monkeypatch.setattr(auth, "settings", settings_with(admin_emails="ops@tumtum.cc"))
    with pytest.raises(HTTPException) as refused:
        await auth.require_admin(SimpleNamespace(email="fan@example.com"))
    assert refused.value.status_code == 403


@pytest.mark.asyncio
async def test_require_admin_lets_the_operator_through(monkeypatch):
    monkeypatch.setattr(auth, "settings", settings_with(admin_emails="ops@tumtum.cc"))
    user = SimpleNamespace(email="Ops@tumtum.cc")
    assert await auth.require_admin(user) is user
