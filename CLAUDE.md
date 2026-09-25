# CLAUDE.md — Tumtum Project

## What is Tumtum

Tumtum is a live entertainment technology platform that captures how people feel during their most exciting moments — concerts, sports matches, festivals — by monitoring their heart rate and correlating it with the event timeline. Users can collect, relive, and share those emotional highlights on social media.

The core product loop is: wear a device (or connect existing wearable) → attend event → see your HR curve synced to the event moments → generate a shareable card → post on social media.

Future feature: artists and athletes can also share their heart rate, enabling fans to compare their heartbeat with their favorite performer ("I was 78% in sync with Chris Martin during A Sky Full of Stars").

## Continuity — read this first in a new session

This project is built across many chat sessions, and the memory between them
is **`docs/decision-log.md`**. Before doing anything substantial, read it:
the "Where things stand" table at the top is the current state, the "Open
items" list is what is pending, and the dated entries carry the reasoning
behind every decision — including the failures, which are the expensive part
to relearn.

Standing instruction from the founder: **keep recording**. Every significant
decision, defect, measurement or reversal goes into the decision log, newest
first, in the same style — what was decided, why, and what it cost to learn.
The log is the index and the reasoning; deep detail lives in the documents it
links.

The other durable documents:

| file | what it holds |
|---|---|
| `docs/decision-log.md` | **The memory.** Start here |
| `docs/path-2-roadmap.md` | The fans'-own-watches strategy, phase by phase |
| `docs/jstyle-v8-evaluation.md` | Everything learned about the J-Style bands |
| `shared/brand/README.md` | Brand implementation details beyond this file |
| `docs/handoff-2026-08-26.md` | Session handoff: state, config, traps, and what was still Felipe's to do two days before the festival |
| `docs/handoff-2026-09-18.md` | **Session handoff, 18/09**: the day Etapas 0–4 closed and the app reached the Play internal testing track — the twelve PRs, the two bugs that mattered, every Play Console answer and why, and what is next |
| `docs/handoff-2026-09-24.md` | **Session handoff, 24/09**: the 23/09 test round, sharing to five networks proved in a hand on b187, where each portal's review stands (Snap and TikTok in review, IDs to swap on approval), the Play Console answers, the traps, and what is pending |
| `docs/health-connect-plan.md` | The next phase: reading the fans' own watches, its stages, its estimate, and the sampling risk that decides it |
| `docs/wear-os-plan.md` | The Wear OS app costed honestly: what it actually buys (guaranteed 1 Hz, not "live"), and why it waits on a measurement |
| `docs/one-app-plan.md` | **Two Android apps became one on 18/09**: `cc.tumtum.app` (the designed experience, branch `app`) absorbs the proven pipeline of `cc.tumtum.capture`, in five staged gates |
| `docs/pilot-event-options.md` | **The events that can carry the pilot** after 25/09 was lost: the São Paulo calendar shortlisted against this project's own constraints, and football evaluated honestly against a concert |
| `docs/design-brief.md` | **Self-contained brand + product handoff for design tools and outside collaborators.** Paste it whole before asking for design work |
| `docs/naming-moments-plan.md` | **Como um momento ganha nome sem ninguém tocar, 22/09**: o detector já acha sozinho, a marca só nomeia; futebol resolvido com o intervalo e duas âncoras, show sem solução pelo setlist (que não tem horário), e os três caminhos com seus custos |
| `docs/app-psychology-principles.md` | **What makes an app feel great, 19/09**: the sourced catalogue of behavioural principles, the benchmark of thirty apps with their published numbers, TumTum's loop audited screen by screen against both, and the ranked list of what to borrow, what to refuse, and what the pilot can measure |

One working rule the log records, learned four times: push everything first,
open the pull request last, and anything pushed after a PR is merged gets a
new PR — never an edit to a merged one. The fourth time proved that knowing
the rule is not what fixes it: **do not open the PR until the work is finished
and pushed**, and if one is already open with more commits coming, say so in
its description so it does not get merged early. The fifth time (18/09) was
the assistant pushing to a branch whose PR Felipe had already merged and
sending that PR's link as "the merge": **before sending a merge link, read
the PR's state from GitHub — `merged: false`, head SHA equal to the
branch's — and never from memory.** The sixth time (24/09) the state was read
and was `open`; a minute of work later the push landed one minute after
Felipe's merge. **Read it again as the last step before every push to a PR's
branch** — he merges within minutes of a link.

**Always end a piece of work by giving Felipe the merge link, and the APK as
a direct download.** Standing instruction from 2026-09-01, extended 22/09.
The APK link must **start the download by itself** — no clicking through a
page, no GitHub login, no `.zip` to unpack. A workflow artifact can never be
that (GitHub requires a session for it and always wraps it in a zip), so the
link to send is the release asset, which this public repository serves to
anyone:

```
https://github.com/felipezanucci/tumtum/releases/download/app-b<N>/tumtum-1.0-b<N>.apk
```

`<N>` is `100 + ` the run number of `build-app.yml`, and every build publishes
its own release (branch builds as prereleases). Never send an
`actions/runs/.../artifacts/...` URL again. He merges everything himself — nothing reaches
tumtum.cc or Railway without it — so a finished task that ends without the
URL leaves him hunting for it. Close with the link, its verified CI state,
and what the merge puts live. And the bug class to watch for, found
twelve times in two days: **the app stating something false about its own
state** — a control with no feedback, a stale display, a message describing
the wrong condition. None break anything, none show up in tests, all surface
only in a real person's hands.

Three more on 21/09, the first day b140 was in Felipe's hands, all the
same shape: a permission step skipped correctly but silently (read as
forgotten), an upload whose steps ran behind an animation and whose result
sat below the fold (read as "never went up"), and a repeated tap whose only
answer was a changed word (read as ignored). **A step the app skips, a
state it reaches, a tap it declines — each is said, where the eye already
is.**

The twelfth was the worst and generalises the rest: **an empty state is a
claim.** `/events` had no error handling on load, so a refused request left the
list empty and the page announced "Nenhum evento encontrado" — then offered to
seed demo data over the real event. Any list that can fail to load must tell
"nothing there" apart from "I could not ask".

## Current phase

**Phase 0 — MVP (no custom hardware)**. We use existing wearables (Apple Watch, Fitbit, Garmin, Galaxy Watch) via Apple HealthKit and Google Health Connect APIs. The goal is to validate the hypothesis: do people want to see and share how their heart reacted during events?

Custom hardware (Tumtum smart band) comes in Phase 1, only after Phase 0 validates demand.

## Tech stack

### Frontend
- **Framework**: Next.js 14+ (App Router) with TypeScript
- **Styling**: Tailwind CSS
- **Data visualization**: D3.js for HR curve animations
- **State management**: Zustand
- **Target**: Progressive Web App (mobile-first, installable)

### Backend
- **Framework**: FastAPI (Python 3.11+)
- **ORM**: SQLAlchemy (async) with Alembic migrations
- **Auth**: JWT tokens + OAuth 2.0 (Google and Apple sign-in)
- **Task queue**: Celery with Redis broker (for card generation)

### Database
- **Primary**: PostgreSQL 16 with TimescaleDB extension
- **Cache**: Redis
- **Object storage**: Cloudflare R2 (S3-compatible)

### Infrastructure
- **Frontend hosting**: Vercel
- **Backend hosting**: Railway
- **CDN**: Cloudflare
- **Monitoring**: Sentry (errors) + PostHog (analytics)
- **CI/CD**: GitHub Actions

## Project structure

```
tumtum-app/
├── frontend/                 # Next.js app
│   ├── app/                  # App Router pages
│   │   ├── (auth)/           # Auth pages (login, signup)
│   │   ├── (app)/            # Authenticated app pages
│   │   │   ├── events/       # Event browsing and selection
│   │   │   ├── experience/   # HR visualization + peak moments
│   │   │   ├── cards/        # Share card generation and gallery
│   │   │   └── profile/      # User profile and collections
│   │   ├── (public)/         # Public pages
│   │   │   ├── page.tsx      # Landing page
│   │   │   └── [username]/   # Public profile
│   │   ├── api/              # API routes (BFF pattern)
│   │   ├── layout.tsx
│   │   └── globals.css
│   ├── components/
│   │   ├── ui/               # Base UI components (Button, Card, Input...)
│   │   ├── hr/               # Heart rate specific (HRCurve, PeakMarker...)
│   │   ├── cards/            # Share card templates (SoloCard, ComparisonCard)
│   │   ├── marketing/        # Public landing only — never used inside the app
│   │   └── layout/           # Layout components (Nav, Footer, Sidebar)
│   ├── lib/
│   │   ├── api.ts            # Backend API client
│   │   ├── health/           # HealthKit / Health Connect integrations
│   │   ├── stores/           # Zustand stores
│   │   └── utils/            # Helpers, formatters, constants
│   ├── public/
│   │   └── fonts/
│   ├── tailwind.config.ts
│   ├── next.config.ts
│   └── package.json
│
├── backend/                  # FastAPI app
│   ├── app/
│   │   ├── main.py           # FastAPI app entry point
│   │   ├── config.py         # Settings and env vars
│   │   ├── models/           # SQLAlchemy models
│   │   ├── schemas/          # Pydantic schemas (request/response)
│   │   ├── api/
│   │   │   ├── auth.py       # Auth endpoints
│   │   │   ├── events.py     # Event CRUD and search
│   │   │   ├── health.py     # Health data ingestion
│   │   │   ├── experience.py # HR visualization data
│   │   │   ├── cards.py      # Card generation
│   │   │   └── users.py      # User profile
│   │   ├── services/
│   │   │   ├── peak_detection.py    # HR peak detection algorithm
│   │   │   ├── event_correlator.py  # Match peaks to event timeline
│   │   │   ├── card_generator.py    # Generate share card images
│   │   │   ├── health_sync.py       # Sync from HealthKit/Google Fit
│   │   │   └── setlist_service.py   # Setlist.fm API integration
│   │   ├── core/
│   │   │   ├── auth.py       # JWT logic
│   │   │   ├── database.py   # DB connection and session
│   │   │   └── redis.py      # Redis connection
│   │   └── tasks/            # Celery async tasks
│   ├── alembic/              # Database migrations
│   ├── tests/
│   ├── requirements.txt
│   └── Dockerfile
│
├── android/                  # THE app — cc.tumtum.app, Kotlin + Compose + Room: the designed
│   └── app/                  #   experience, absorbing the proven pipeline (docs/one-app-plan.md)
├── android-capture/          # cc.tumtum.capture — the proven capture app, kept as the
│   └── app/                  #   reference until every piece is ported, then retired
│
├── docs/                     # Decision log and durable research documents
│
├── shared/                   # Shared assets
│   └── brand/                # Logo, fonts, brand guidelines
│
├── docker-compose.yml        # Local dev environment
├── .github/
│   └── workflows/            # CI/CD pipelines
├── CLAUDE.md                 # This file
└── README.md
```

## Database schema (core tables)

```sql
-- Users
users: id (uuid PK), email, name, avatar_url, auth_provider, auth_provider_id, created_at, updated_at

-- Wearable connections
wearable_connections: id (uuid PK), user_id (FK), provider (apple_health|google_fit|garmin|fitbit), access_token, refresh_token, last_sync_at, status (active|expired|revoked)

-- Events
events: id (uuid PK), name, subtitle, venue, city, country, date, start_time, end_time, event_type (concert|sports|festival), external_id, cover_image_url, created_at

-- Event timeline entries (songs in setlist, goals in match, etc)
event_timeline: id (uuid PK), event_id (FK), timestamp, label, entry_type (song_start|goal|halftime|encore|highlight), metadata (jsonb)

-- HR sessions (one per user per event)
hr_sessions: id (uuid PK), user_id (FK), event_id (FK), start_time, end_time, avg_bpm, max_bpm, min_bpm, data_quality_score (0-100), source_device, created_at

-- HR data points (TimescaleDB hypertable — partitioned by time)
hr_data: time (timestamptz), session_id (FK), bpm (smallint), rr_interval_ms (smallint), motion_level (smallint), source

-- Detected peaks
peaks: id (uuid PK), session_id (FK), timestamp, bpm, duration_seconds, magnitude (float), timeline_entry_id (FK nullable), rank (smallint)

-- Generated share cards
cards: id (uuid PK), user_id (FK), session_id (FK), peak_id (FK), card_type (solo|comparison), image_url, video_url, metadata (jsonb), created_at

-- Share tracking
shares: id (uuid PK), card_id (FK), platform (instagram|tiktok|x|whatsapp|link|native), shared_at

-- The event feed (22/09): posts are published health data, only by explicit
-- act, only to people with a measured night at the event
event_posts: id, event_id (FK), user_id (FK), session_id (FK), bpm, moment_at, label, quote, skin, created_at, deleted_at
event_post_reactions: id, post_id (FK), user_id (FK), created_at   -- SENTI TB, the only reaction
post_reports: id, post_id (FK), reporter_id (FK), reason (abuse|fake|other), created_at, resolution, resolved_at
user_blocks: id, blocker_id (FK), blocked_id (FK), created_at     -- hides both ways

-- The tour (22/09). Since 23/09 its dates share ONE feed, with the night as a
-- filter; a post reaches the other dates only with its series_posts row.
-- Football stays one feed per match for now (club/league kinds unused).
event_series: id, name, kind (tour|club|league), created_at
event_series_members: event_id (PK, FK), series_id (FK)
series_posts: post_id (PK, FK), series_id (FK), created_at      -- the author's consent to the wider audience

-- Sessions (22/09): access token 1 h; refresh token 90 days from last use, rotated
refresh_tokens: id, user_id (FK), family_id, parent_id, token_hash, expires_at, revoked_at, revoke_reason, created_at

-- Sign-up codes (24/09, #64): an account exists only once its e-mail is proved.
-- What the account will be waits here until the 6-digit code comes back; the
-- code is stored only as a keyed hash; unconfirmed rows are deleted after a day.
signup_codes: id, email, email_key, name, hashed_password, code_hash, attempts, expires_at, used_at, created_at

-- Public waitlist (landing page). Email and nothing else: the page promises
-- "a gente só usa seu e-mail pra te avisar dos próximos eventos", and a column
-- we do not have is a promise we cannot accidentally break.
waitlist_entries: id (uuid PK), email (unique), source, created_at
```

## Brand identity

Source of truth: **TUMTUM Manual de Marca, MVP v0.4 (2026-08-31)**. It marks
each rule LOCKED (approved, do not change), WORKING (current recommendation,
still being refined) or NEVER (rejected). Where anything here is thinner than
the manual, the manual wins. **`docs/design-brief.md` is this section expanded
into a self-contained handoff** for design tools and outside collaborators.

**v0.4 replaced the palette.** Acid Lime `#C6FF00` is gone — it was the primary
accent through v0.1 and appears in code and artwork built before 31/08 (the
card generator, `tailwind.config.ts`, the rendered cards). Anything still lime
is out of date, not a variant.

### Colour — two neutrals and a proprietary pop duo

| Token | Hex | Role |
|---|---|---|
| Black | `#000000` | Primary canvas and text |
| White | `#FFFFFF` | Neutral / inverse |
| **TumTum Pink** | **`#FF6F91`** | **Primary accent** |
| Toxic Yellow | `#EFFF00` | Secondary accent |

- Black and white are the structural neutrals; the accents carry emphasis.
- **Pink may occupy large surfaces** — full backgrounds and colour fields — which
  the previous acid accent could not. Yellow works best on labels, CTAs,
  highlights and data markers.
- Roughly **70% TumTum Pink / 30% Toxic Yellow** in accent use. A working
  balance, not a layout quota.
- Digital default: black canvas, white for supporting information, Pink for the
  main emphasis, Toxic Yellow as a second explosion.
- **Never set text in white on Pink or Toxic Yellow** — 2.65:1 and 1.11:1. Black
  on either is excellent (7.93:1 and 18.97:1). Pink on yellow is not a pair.
- **Pink on black is 7.93:1**, against Acid Lime's 17.7:1. It passes AA
  everywhere and AAA at large sizes, but it is roughly half as loud: emphasis
  now needs **scale**, not a brighter tint.
- Mutation skins may introduce other colours; this palette stays the anchor.

### Type

- **Chosmos** — the TUMTUM wordmark only. Never in UI, body or marketing.
- **Instrument Sans** (SIL OFL 1.1) — everything else. Bold 700 for hero numbers,
  Semibold 600 / Bold 700 for headlines, Medium 500 / Semibold 600 for UI labels,
  Regular 400 for body, tabular figures for aligned data.
- Do not add a third family.

### Logo

The wordmark reads **TUMTUM**, uppercase, in Chosmos. The **official vector is
the only acceptable production source** — a screenshot, a raster export or an
AI-generated approximation is not a master. Never stretch, compress, slant,
redraw the letters, or alter the silhouette.

**Approved finishes: black, white, or an approved graphic version** (the
mutation skins — zebra, inflável, cromado and so on). **Flat coloured wordmarks
are forbidden** — no Pink, no Toxic Yellow, no other colour. The accents colour
the interface around the logo, never the logo itself. On Pink or Yellow
surfaces the logo defaults to black.

**Chosmos must never be given to an AI tool.** The Typozon EULA (v3.4) carries
an explicit AI/machine-learning restriction, so a lookalike rendered from
another font is a legal problem as well as a brand one. In any mockup, place
the official asset or a marked placeholder.

### Name

**TumTum** in running text. **TUMTUM** as the wordmark. Never *Tum Tum*,
*Tumtum* or *tumtum*, except where a technical handle forces another form.

### Voice

- The person is the subject: *"Seu coração foi a 187"*, never *"Detectamos um
  pico de 187"*.
- Short. Nobody reads a paragraph in the middle of a crowd.
- Brazilian Portuguese as spoken, no caricature.
- Zero medical advice: no diagnosis, no reassurance, no "normal/abnormal".
- Prefer: batida, momento, noite, galera, junto, sentiu, arrepio, vibe.
- Use with care: coração, BPM — neutral units, never wrapped in clinical reading.
- Avoid: **frequência cardíaca as marketing language**, resposta fisiológica,
  zona, recuperação, performance, diagnóstico. They pull the brand to healthtech.

### Territory: Mutante Pop

The silhouette is fixed; the surface is not. One recognisable structure that
survives thousands of appearances — ten skins in the MVP kit (Base, Zebra, Neon,
Cromado, Inflável, Glitter, Iridescente, Pelúcia, Plástico, Borracha). Mutation
is surface treatment, never redesign.

### NEVER

- ECG, heartbeat or waveform as decoration — identity, logo, pattern, divider,
  background or generic motion.
- Making TumTum look like healthtech, fitness or wellness.
- Letting an image generator redraw the logo.
- Dropping a club crest, artist logo or tour identity inside the wordmark.

**A real heart-rate line is allowed** when it is the user's own data and the
chart is product information — specifically the "Minha noite" card and
community/comparison contexts where the line adds evidence. Simple BPM over
time, never an ECG trace; TumTum Pink for the main emphasis on dark surfaces and
Toxic Yellow for highlight points and labels; no heart zones, risk colours,
"normal ranges" or recovery scores. The chart answers *"when did this happen?"*,
never *"what does this mean clinically?"*. **A gap in a capture is drawn as a
gap** — the line breaks rather than crossing minutes nobody measured.

### Share cards — five narratives for one moment

All five are born from the same moment object (event, timestamp, user value,
context, media, timeline, community and artist data where they exist).

| # | Name | Needs |
|---|---|---|
| 01 | Só o momento | Peak, time, moment label, event context. **Always available — the universal default** |
| 02 | Ver o momento | Licensed event photo/video matched to the moment |
| 03 | Minha noite | The user's time series with the moment marked |
| 04 | A galera | A statistically and privately valid collective sample |
| 05 | Na mesma vibe | Explicit artist/athlete participation and validated sync |

Show only the formats a given moment can actually produce, as visual previews
rather than labels. Ordering is by rarity and emotional value, not a fixed
taxonomy.

### Tone

Bold because it was born somewhere loud. Funny about itself, never about the
user. Takes the person's feeling seriously and never takes the brand seriously.
Intimate with personal data. When forced to choose between *trustworthy* and
*fun*, choose fun — except on health-data permission, privacy and consent
screens, where the brand goes quiet and careful.

## Coding standards

### General
- Language: TypeScript (strict mode) for frontend, Python 3.11+ with type hints for backend
- All code, comments, git commits, and documentation in **English**
- UI text and user-facing strings in **Portuguese (Brazil)** as default, with i18n structure ready for English
- Use conventional commits: `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`
- Never commit secrets, API keys, or .env files

### Frontend
- Use Server Components by default, Client Components only when needed (interactivity, hooks)
- Use `use client` directive explicitly
- Tailwind classes only — no inline styles, no CSS modules
- Components: PascalCase files, one component per file
- Zustand stores: one store per domain (useAuthStore, useEventStore, useHRStore)
- API calls: centralized in `lib/api.ts` using fetch with typed responses
- Error boundaries on every page

### Backend
- Async everywhere — all endpoints and DB queries must be async
- Pydantic v2 for all request/response schemas
- Dependency injection via FastAPI Depends()
- All endpoints return typed Pydantic models, never raw dicts
- Use HTTPException with meaningful status codes and messages
- Business logic in `services/`, not in route handlers
- Database queries in model methods or dedicated repository functions
- Tests: pytest with async support, minimum 80% coverage on services

### Database
- Alembic for all schema changes — never modify DB manually
- UUIDs for all primary keys (except hr_data which uses composite key)
- All timestamps in UTC (timestamptz)
- Indexes on: user_id (all tables), event_id, session_id, time (hr_data hypertable)
- TimescaleDB chunk interval: 1 day for hr_data

## Peak detection algorithm

```
Input: HR data array [{ time, bpm }], event timeline [{ time, label }]

1. Smooth: 5-second moving average on BPM values
2. Baseline: 1200-second centered rolling MEDIAN
3. Spread: interquartile range of the same window, scaled to a standard
   deviation (IQR / 1.349)
4. Z-score: (smoothed_bpm - baseline) / spread for each point, with three guards:
   - spread <= 1.0 → z = deviation / 10 when the deviation is positive, else 0
     (a very steady stretch would otherwise divide by near-zero and
     manufacture huge z-scores)
   - deviation > 30 bpm → z is at least deviation / 15 (an absolute rise that
     large is always significant)
   - deviation < 10 bpm → z cannot open a region; deviation < 5 bpm → z = 0
     (a robust spread on a quiet hour is a couple of bpm, so a 4 bpm wobble
     would otherwise score z > 2; a moment is a rise a person would feel)
5. Regions with hysteresis: a region opens where z > 2.0 and stays open
   while z > 1.0 (one noisy dip must not split a song into slivers)
6. Filter: regions < 5 seconds are discarded (noise)
7. Extract: peak_bpm = max(region), peak_time = timestamp of max;
   start_time and end_time bound the whole region
8. Merge: regions separated by ≤ 30 seconds become one (union of bounds)
9. Rank: by magnitude (z-score × duration_seconds), keep the top 20
10. Match: each peak → the LATEST timeline entry between (start_time − 60 s)
    and (peak_time + 15 s) — the thing that caused it; if none, the nearest
    entry within ±60 s of the peak

Output: [{ timestamp, bpm, duration, magnitude, start_time, end_time, matched_label }]
```

**The median is the point, and the twenty minutes follow from it.** Recorded
2026-09-17. The previous version used a 300-second rolling *mean*, widened
from 60 s because a peak that sits inside its own reference window raises the
mean it is measured against. That fixed a 13-second spike and left the same
failure one timescale up: a mean is contaminated by anything approaching half
its window, so a goal celebration (2–3 min) and a favourite song sung from
start to finish (4 min) were **invisible**, and the detector reported only the
short spikes inside them — with durations of 8–22 s, which is exactly what the
Realness night reported. A median does not move until the elevation fills half
the window, so the window can be wide enough for a song and a spike is not
lost to it. Simulated in `scripts/simulate_moment_detection.py`; guaranteed in
`backend/tests/test_peak_detection.py` and `test_event_correlator.py`.

Step 10 changed with it: a song's peak is wherever the heart was highest,
often three minutes in, so "nearest entry to the peak" named the *next* song.
The cause of a moment precedes it; the rule now looks back from the region's
start.

These numbers are the interface, not trivia: a validation protocol has to be
designed against them. The values live in `detect_peaks()` in
`backend/app/services/peak_detection.py` and `correlate_peaks_to_timeline()` in
`event_correlator.py`; change them there and here together.

## Key external APIs

| API | Purpose | Auth | Rate limit |
|-----|---------|------|------------|
| Apple HealthKit | Read HR data from iPhone/Apple Watch | OAuth (on-device) | N/A |
| Google Health Connect | Read HR data from Android/Wear OS | OAuth 2.0 REST | Standard Google quotas |
| Setlist.fm | Concert setlists with song order | API key (free) | 2 req/sec |
| API-Football | Match events (goals, cards) | API key (freemium) | 100 req/day (free) |
| Spotify Web API | Song metadata, album art | OAuth 2.0 | Standard Spotify quotas |

## Sprint roadmap

| Sprint | Weeks | Focus |
|--------|-------|-------|
| 0 | 1–2 | Foundation: project setup, auth, DB schema, CI/CD, design system |
| 1 | 3–4 | Health data integration: HealthKit + Google Fit, ingestion pipeline |
| 2 | 5–6 | Event system: Setlist.fm + sports APIs, peak detection, correlation |
| 3 | 7–8 | HR visualization: animated curve, experience view, onboarding |
| 4 | 9–10 | Content engine: share card generation, social sharing, CDN |
| 5 | 11–12 | Profile + collections + polish: gallery, PWA, performance |
| 6 | 13–14 | Beta launch: deploy, analytics, early adopter testing |

## Important context

- This is a startup MVP. Speed > perfection. Ship fast, iterate based on feedback.
- The founder (Felipe) is non-technical but actively learning. Explain decisions clearly.
- Primary market: Brazil (São Paulo). Events = concerts + football matches.
- Share cards are the viral engine. They must be visually stunning and instantly shareable.
- Privacy is critical: health data is sensitive. Minimal collection, clear consent, user control.
- The smart band hardware is NOT part of Phase 0. Don't build BLE or real-time streaming yet.

## Product rules (standing, from the founder)

- **Events are TumTum's. The fan never creates one.** (21/09) The events
  where TumTum works are registered by us and appear on the fan's screen to
  be *activated* or not. No name, venue, kind, date or hour is ever asked of
  a fan. Creating or typing an event is an operator act: the shortcuts live
  at the foot of AO VIVO and are turned on by the *Cadastrar eventos pelo
  celular* switch behind the OPERADOR door in Configurações — until the web
  admin (decision log, open item 40) replaces them.
- **A time is never typed.** (21/09) Every date or time field in the product
  is the platform's picker — the rolling selector — never a free-text
  `HH:mm` or `dd/MM/yyyy`. This holds for operator screens too.
- **The card arrives ready, or it says nothing.** (22/09) A moment reaches
  the fan already named — *"tocou Yellow às 22h12"*, *"pênalti aos 39 do
  primeiro tempo"* — and **nothing is ever asked of the fan to get there**.
  No guess list, no "tava rolando uma dessas?", no tap to remember. A time
  the app derived names nothing; only a measured one does. Note the
  distinction this rests on: *no user action* is not *no TumTum action* —
  the fan is the user, and staff are an operating cost the rule permits.
- **A night is recorded only with an account.** (25/09) *"O usuário só
  consegue fazer a leitura se ele tiver logado."* Every capture start — the
  fan's *Começar agora*, the operator's *Começa agora* — needs a live
  session; without one the app says *"Entra na sua conta pra gravar a
  noite"* with the way in. Offline is fine: a live session is one the phone
  can still renew, and the night goes up when the signal does.
- **A night belongs to the account that recorded it.** (25/09) The phone
  shows the signed-in account's nights. Another account's are **hidden,
  never deleted**, and come back when that account signs in; creating an
  account wipes nothing, and deleting one takes only its own nights.
- **Signed out, the phone is nobody's.** (25/09) After *Sair*, no name,
  photo, @, profile or night of the last account is shown anywhere: the
  corner of every tab reads ENTRAR. *"Tudo dá a entender de que eu ainda
  estou na minha conta"* — and nothing may.
- **The fan's device is a watch.** (25/09) *"Praticamente todo mundo vai
  usar só relógio."* Setup leads with the watch (Health Connect); a chest
  strap is the quiet second road, for the pilot's operator-set phones.
- **A reading without skin contact is not a beat.** (25/09) The sensor says
  in every packet whether it feels skin; a strap on a table sent numbers.
  Such a reading never becomes a number on screen, a point on the curve or
  a beat on the server — the night keeps a gap there.
