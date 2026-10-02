"""Consent per purpose: the one legal basis for heart-rate data (LGPD art. 11).

The legal opinion behind the 26/09 audit (CR-1) leaves no alternative: a
heartbeat is sensitive health data, and the only basis that fits a product
people use for fun is specific, highlighted consent — one per purpose,
recorded, revocable. This module is the record.

**Append-only.** A grant is a row; revoking stamps `revoked_at` on the active
row; granting again inserts a new one. The newest row of a purpose is its
state, every row together is the proof. Nothing here ever updates a
`granted_at` or deletes a row — only account deletion does, because then
there is nobody left to prove anything to.

The purposes and the text version are constants shared, literally, with the
Android app and the site (contract of 26/09). A new text version is a new
string here *and* there, never a silent edit of the words.

**The ledger (v1.1 opinion, §4.2).** Each row also says on what legal basis
it stands (`legal_basis`), what it covers in one plain sentence (`scope`),
and which text it was given under (`proof`: the SHA-256 of
`"{purpose}:{text_version}"`). The words themselves are not in the database:
each version's full text lives in the clients' string resources and, for
the record, in `docs/consent-texts.md`. The hash names a version, not the
words, so it ties a row to exactly one section there only as long as a
version's words are never edited in place — which is the rule.
"""

import hashlib
import uuid
from collections.abc import Mapping
from dataclasses import dataclass
from datetime import UTC, datetime

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.consent import Consent
from app.services import consent_text

# The words live in shared/consent/consent-text.json (02/10) and are
# generated into consent_text.py; the version comes from there.
CONSENT_TEXT_VERSION = consent_text.CONSENT_TEXT_VERSION

PURPOSES = (
    "terms",
    "read_heart_rate",
    "keep_night",
    "crowd_stats",
    "artist_compare",
    "improve_detection",
    "marketing",
)

MEANS = ("tap", "checkbox", "button", "form")

# The legal basis each purpose's row stands on. Heart-rate uses are sensitive
# data, so consent under art. 11; marketing e-mail is ordinary personal data,
# consent under art. 7. `terms` is different in kind: the row records that
# the person accepted the contract, and the basis for what the contract needs
# is its execution (art. 7, V) — calling it "consent" would promise a
# withdrawal that only deleting the account can honour.
LEGAL_BASES = {
    "terms": "contract_art7",
    "read_heart_rate": "consent_art11",
    "keep_night": "consent_art11",
    "crowd_stats": "consent_art11",
    "artist_compare": "consent_art11",
    "improve_detection": "consent_art11",
    "marketing": "consent_art7",
}

# What each grant covers, in one line a person (or the ANPD) reads in the
# export without opening the app. Fixed per purpose: the words the person
# agreed to are the versioned text, this is its label in the ledger.
SCOPES = {
    "terms": "Termos de uso e Política de Privacidade da conta, enquanto ela existir",
    "read_heart_rate": (
        "leitura da batida do relógio ou sensor, só na janela do evento, "
        "e detecção dos momentos"
    ),
    "keep_night": "série, momentos e cards da noite no servidor, enquanto ativo",
    "crowd_stats": (
        "noite sem nome na conta coletiva do evento (A galera), só em faixas "
        "e com gente suficiente"
    ),
    "artist_compare": "comparação da batida com a do artista ou atleta que topar",
    "improve_detection": "uso das noites para melhorar a detecção de momentos",
    "marketing": "e-mails da TumTum sobre eventos e novidades",
}


def proof_of(purpose: str, text_version: str) -> str:
    """The fingerprint of the text a grant was given under (64 hex chars).

    Postgres computes the same for old rows in the catch-up and in 023:
    `encode(sha256(convert_to(purpose || ':' || text_version, 'UTF8')), 'hex')`.
    """
    return hashlib.sha256(f"{purpose}:{text_version}".encode()).hexdigest()


# What the 403 says when an action needs a purpose the person has not
# granted. The screen opens the consent for that purpose; the sentence is
# what a client that cannot do that shows instead.
REQUIRED_SENTENCES = {
    "terms": "Pra continuar, você precisa aceitar os Termos e a Política de Privacidade.",
    "read_heart_rate": "Pra gravar a noite, a TumTum precisa da sua autorização pra ler o batimento.",
    "keep_night": "Pra guardar a noite na TumTum, você precisa autorizar isso antes.",
    "crowd_stats": "Pra entrar na estatística da galera, você precisa autorizar isso antes.",
    "artist_compare": "Pra comparar com o artista, você precisa autorizar isso antes.",
    "improve_detection": "Pra usar sua noite no detector, você precisa autorizar isso antes.",
    "marketing": "Pra receber nossos e-mails, você precisa autorizar isso antes.",
}


@dataclass(frozen=True)
class ConsentState:
    """Where one purpose stands for one person."""

    purpose: str
    granted: bool
    granted_at: datetime | None
    revoked_at: datetime | None
    text_version: str | None
    legal_basis: str | None = None
    scope: str | None = None
    proof: str | None = None


async def _latest(
    db: AsyncSession, user_id: uuid.UUID, purposes=PURPOSES
) -> dict[str, Consent]:
    """The newest row of each purpose this person ever answered."""
    rows = (
        (
            await db.execute(
                select(Consent)
                .where(Consent.user_id == user_id, Consent.purpose.in_(purposes))
                .order_by(Consent.granted_at, Consent.created_at)
            )
        )
        .scalars()
        .all()
    )
    # At most one row per purpose is active (set_many keeps it so), and when
    # there is one it is the state. Otherwise the newest revoked row is.
    latest: dict[str, Consent] = {}
    for row in rows:  # ascending, so a later row replaces an earlier one
        current = latest.get(row.purpose)
        if current is not None and current.revoked_at is None:
            continue
        latest[row.purpose] = row
    return latest


async def active(db: AsyncSession, user_id: uuid.UUID, purpose: str) -> bool:
    """Whether this purpose is granted right now."""
    row = (await _latest(db, user_id, (purpose,))).get(purpose)
    return row is not None and row.revoked_at is None


def active_consent_users(purpose: str):
    """A subquery of the user ids with this purpose granted right now.

    For filtering in SQL (the crowd counts only consenting nights). A grant
    is active when its row has no `revoked_at`; a person who revoked and
    granted again has one such row, the newest, so the rule needs no
    "latest row" window.
    """
    return select(Consent.user_id).where(
        Consent.purpose == purpose, Consent.revoked_at.is_(None)
    )


async def set_many(
    db: AsyncSession,
    user_id: uuid.UUID,
    purposes: Mapping[str, bool],
    *,
    text_version: str,
    means: str,
    client: str | None = None,
    now: datetime | None = None,
) -> None:
    """Grant or revoke each purpose given; leave every other purpose alone.

    Granting what is already granted under the same text is a no-op, not a
    second row. Granting it under a newer text closes the old row and opens
    one for the words the person is looking at now. Revoking what was never
    granted records nothing: there is nothing to withdraw.
    """
    unknown = sorted(set(purposes) - set(PURPOSES))
    if unknown:
        raise ValueError(f"unknown purposes: {unknown}")
    now = now or datetime.now(UTC)
    latest = await _latest(db, user_id, tuple(purposes))
    for purpose, granted in purposes.items():
        row = latest.get(purpose)
        is_active = row is not None and row.revoked_at is None
        if granted:
            if is_active and row.text_version == text_version:
                continue
            if is_active:
                row.revoked_at = now
            db.add(
                Consent(
                    user_id=user_id,
                    purpose=purpose,
                    text_version=text_version,
                    legal_basis=LEGAL_BASES[purpose],
                    scope=SCOPES[purpose],
                    proof=proof_of(purpose, text_version),
                    granted_at=now,
                    means=means,
                    client=(client or None) and client[:80],
                    created_at=now,
                )
            )
        elif is_active:
            row.revoked_at = now
    await db.flush()


async def snapshot(db: AsyncSession, user_id: uuid.UUID) -> list[ConsentState]:
    """Every purpose, in the contract's order, granted or not.

    The basis and the scope belong to the purpose, so they are there even for
    one never answered; the proof belongs to a row, so it is there only when
    the person gave one.
    """
    latest = await _latest(db, user_id)
    out = []
    for purpose in PURPOSES:
        row = latest.get(purpose)
        out.append(
            ConsentState(
                purpose=purpose,
                granted=row is not None and row.revoked_at is None,
                granted_at=row.granted_at if row else None,
                revoked_at=row.revoked_at if row else None,
                text_version=row.text_version if row else None,
                legal_basis=row.legal_basis if row else LEGAL_BASES[purpose],
                scope=row.scope if row else SCOPES[purpose],
                proof=row.proof if row else None,
            )
        )
    return out


async def history(db: AsyncSession, user_id: uuid.UUID) -> list[Consent]:
    """Every row, oldest first — what the person's own data export shows."""
    return list(
        (
            await db.execute(
                select(Consent)
                .where(Consent.user_id == user_id)
                .order_by(Consent.granted_at, Consent.created_at)
            )
        )
        .scalars()
        .all()
    )
