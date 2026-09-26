# Migrations the deployed server does not apply by itself

**Read this before deploying the LGPD remediation (26/09).**

The API never runs Alembic (decision log, open item 16). On startup it calls
`Base.metadata.create_all`, which creates missing **tables** and never changes
an existing one. Since 26/09 it also runs `app/core/schema_catchup.py`, which
adds — only adds, idempotently — the columns of 015, 016 and 017, because the
models map them and every query on `users`, `hr_sessions` and `cards` would
fail without them.

## Migrations since 007 that `create_all` cannot apply

| rev | what | applied by startup? |
|---|---|---|
| 007 | `events.start_time`/`end_time` lose their timezone (type change) | **no** — dormant; the schema coercion covers it |
| 015 | `users.birth_date`; `signup_codes.birth_date`, `consent_text_version`, `read_heart_rate` | yes, by the catch-up |
| 016 | `hr_sessions.analyzed_at` | yes, by the catch-up |
| 017 | `cards.published_at` | yes, by the catch-up |
| 022 | drop `wearable_connections.access_token`/`refresh_token` | **no** — destructive, needs a person |

(008–014 and 018–021 only create tables: `create_all` handles them. 005,
before this range, added `waitlist_entries.first_name`/`last_name` and is in
the same situation as 007.)

## The command

Railway's database has never had an `alembic_version` table, so a plain
`alembic upgrade head` would start at 001 and fail on tables that already
exist. Tell Alembic where the schema really is, then run only what is
missing. From `backend/`, with `DATABASE_URL` set to Railway's **public**
Postgres URL in the `postgresql+asyncpg://` form, and `DATABASE_SSL=true`:

```bash
export DATABASE_URL='postgresql+asyncpg://…'   # Railway → Postgres → Connect → public URL
export DATABASE_SSL=true
alembic current                 # expect no revision: there is no version table yet

# 1. Tables 001–006 exist (create_all made them). Check 005 by hand first:
#    \d waitlist_entries  → are first_name and last_name there?
alembic stamp 006               # if they are
#   (if not: alembic stamp 004 && alembic upgrade 005 && alembic stamp 006)

# 2. 007, the type change. Check: \d events → start_time "time with time zone"?
alembic upgrade 007             # if it still has the timezone (the likely case)
#   (if it is already "time without time zone": alembic stamp 007)

# 3. 008–021 only create tables (create_all made them) or add the columns
#    the startup catch-up already added.
alembic stamp 021

# 4. 022 drops the unused wearable tokens.
alembic upgrade head
alembic current                 # → 022 (head)
```

From then on the version table is honest, and a future migration is one
`alembic upgrade head`.

## Optional, a person's decision

- **Start the raw-series clock for nights analysed before 26/09** (their
  readings will be deleted on the next maintenance pass if older than 30 days):
  `UPDATE hr_sessions SET analyzed_at = created_at WHERE analyzed_at IS NULL AND EXISTS (SELECT 1 FROM peaks WHERE peaks.session_id = hr_sessions.id);`
- **Keep public the cards that were already shared** (017 leaves every
  existing card unpublished):
  `UPDATE cards SET published_at = (SELECT min(shared_at) FROM shares WHERE shares.card_id = cards.id) WHERE published_at IS NULL AND EXISTS (SELECT 1 FROM shares WHERE shares.card_id = cards.id);`
