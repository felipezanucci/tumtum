"""The consent ledger: legal basis, scope and proof on every row (v1.1 §4.2).

**Adds columns to an existing table — `create_all` cannot apply this.** The
startup catch-up (`app/core/schema_catchup.py`) runs the same statements, so
on Railway nothing is manual; running this afterwards is a no-op.

- `legal_basis`: `consent_art11` for the heart-rate purposes, `consent_art7`
  for marketing, `contract_art7` for the terms (the row records acceptance
  of the contract; its basis is the contract's execution, not consent).
- `scope`: one fixed line per purpose saying what the grant covers.
- `proof`: SHA-256 hex of `"{purpose}:{text_version}"` — the fingerprint of
  the text shown, whose words live in the clients' strings and in
  `docs/consent-texts.md`.

Rows written before this get all three from their purpose and text version,
then `legal_basis` and `proof` become NOT NULL: a ledger row that cannot say
what it stands on is not proof of anything.

The maps below are frozen copies of `services.consents.LEGAL_BASES` and
`SCOPES` as of 26/09 — a migration describes the past, so it must not change
when the service does. `tests/test_consents.py` checks they still agree.

Revision ID: 023
Revises: 022
Create Date: 2026-09-26
"""

from alembic import op

revision = "023"
down_revision = "022"
branch_labels = None
depends_on = None

LEGAL_BASES = {
    "terms": "contract_art7",
    "read_heart_rate": "consent_art11",
    "keep_night": "consent_art11",
    "crowd_stats": "consent_art11",
    "artist_compare": "consent_art11",
    "improve_detection": "consent_art11",
    "marketing": "consent_art7",
}

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


def _quote(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def _case(mapping: dict[str, str], default: str) -> str:
    """`CASE purpose WHEN … END` — `default` is already SQL."""
    whens = " ".join(
        f"WHEN {_quote(purpose)} THEN {_quote(value)}"
        for purpose, value in mapping.items()
    )
    return f"CASE purpose {whens} ELSE {default} END"


def upgrade() -> None:
    op.execute("ALTER TABLE consents ADD COLUMN IF NOT EXISTS legal_basis VARCHAR(40)")
    op.execute("ALTER TABLE consents ADD COLUMN IF NOT EXISTS scope VARCHAR(200)")
    op.execute("ALTER TABLE consents ADD COLUMN IF NOT EXISTS proof VARCHAR(64)")
    # An unknown purpose cannot exist (the service refuses it); if one did,
    # the strictest basis is the honest label for it.
    op.execute(
        "UPDATE consents SET legal_basis = "
        f"{_case(LEGAL_BASES, _quote('consent_art11'))} WHERE legal_basis IS NULL"
    )
    op.execute(
        f"UPDATE consents SET scope = {_case(SCOPES, 'NULL')} WHERE scope IS NULL"
    )
    op.execute(
        "UPDATE consents SET proof = encode(sha256(convert_to("
        "purpose || ':' || text_version, 'UTF8')), 'hex') WHERE proof IS NULL"
    )
    op.execute("ALTER TABLE consents ALTER COLUMN legal_basis SET NOT NULL")
    op.execute("ALTER TABLE consents ALTER COLUMN proof SET NOT NULL")


def downgrade() -> None:
    op.execute("ALTER TABLE consents DROP COLUMN IF EXISTS proof")
    op.execute("ALTER TABLE consents DROP COLUMN IF EXISTS scope")
    op.execute("ALTER TABLE consents DROP COLUMN IF EXISTS legal_basis")
