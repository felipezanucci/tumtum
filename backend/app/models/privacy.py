"""The tables that keep the promises of the privacy policy (LGPD remediation).

- `deletion_log` — that an account was deleted, and when. Nothing else: the
  row exists so the count of deletions can be shown to the ANPD, and a row
  that said *whose* account would be the one thing deletion must not leave.
- `email_changes` — a new address waiting for its code, like `signup_codes`.
- `data_subject_requests` — the person's requests under art. 18, with the
  15-day deadline the law gives the controller to answer.
- `data_access_log` — who read whose heart-rate data, and when.
- `access_log` — every API request, kept 180 days (Marco Civil, art. 15).

The two logs carry no foreign keys on purpose: an operator's read of
somebody's data must stay on record after the operator's own account goes,
and a log that the database can cascade away is not a log.
"""

import uuid
from datetime import UTC, datetime

from sqlalchemy import DateTime, ForeignKey, Integer, SmallInteger, String, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


def _now() -> datetime:
    return datetime.now(UTC)


class DeletionLog(Base):
    """One deleted account. No identifier of any kind, by design."""

    __tablename__ = "deletion_log"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    deleted_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=_now, nullable=False
    )


class EmailChange(Base):
    """A signed-in person's new address, waiting for the code sent to it.

    The account keeps its old address until the code comes back, so a typo
    never locks anybody out of their own mailbox. `code_hash`, never the code
    — keyed and bound to the new address, exactly like `signup_codes`.
    """

    __tablename__ = "email_changes"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    new_email: Mapped[str] = mapped_column(String(255), nullable=False)
    email_key: Mapped[str] = mapped_column(String(255), nullable=False, index=True)
    code_hash: Mapped[str] = mapped_column(String(64), nullable=False)
    attempts: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    expires_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False
    )
    used_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=_now, index=True
    )


class DataSubjectRequest(Base):
    """A request under art. 18 LGPD: access, correction, deletion, and so on.

    `due_at` is fixed when the request is opened — 15 days — so a late answer
    is visible as late rather than recomputed away.
    """

    __tablename__ = "data_subject_requests"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    # access | portability | correction | deletion | revocation | other
    kind: Mapped[str] = mapped_column(String(20), nullable=False)
    message: Mapped[str] = mapped_column(Text, nullable=False, default="")
    # open | answered | closed
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="open")
    opened_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=_now, nullable=False
    )
    due_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    answered_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    answer: Mapped[str | None] = mapped_column(Text)
    # The operator who answered. Not a foreign key: see the module docstring.
    answered_by: Mapped[uuid.UUID | None] = mapped_column(UUID(as_uuid=True))


class DataAccessLog(Base):
    """One read (or deletion) of somebody's heart-rate data. Never a bpm."""

    __tablename__ = "data_access_log"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=_now, nullable=False, index=True
    )
    actor_user_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), index=True
    )
    subject_user_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), index=True
    )
    resource: Mapped[str] = mapped_column(String(40), nullable=False)
    resource_id: Mapped[str | None] = mapped_column(String(64))
    action: Mapped[str] = mapped_column(String(20), nullable=False)
    ip: Mapped[str | None] = mapped_column(String(64))


class AccessLog(Base):
    """One API request: who, from where, what, and how it ended. 180 days."""

    __tablename__ = "access_log"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=_now, nullable=False, index=True
    )
    method: Mapped[str] = mapped_column(String(10), nullable=False)
    path: Mapped[str] = mapped_column(String(500), nullable=False)
    status: Mapped[int] = mapped_column(SmallInteger, nullable=False)
    ip: Mapped[str | None] = mapped_column(String(64))
    user_id: Mapped[uuid.UUID | None] = mapped_column(UUID(as_uuid=True), index=True)
