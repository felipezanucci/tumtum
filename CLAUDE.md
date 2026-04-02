# CLAUDE.md — Tumtum Project

## What is Tumtum

Tumtum is a live entertainment technology platform that captures how people feel during their most exciting moments — concerts, sports matches, festivals — by monitoring their heart rate and correlating it with the event timeline. Users can collect, relive, and share those emotional highlights on social media.

The core product loop is: wear a device (or connect existing wearable) → attend event → see your HR curve synced to the event moments → generate a shareable card → post on social media.

Future feature: artists and athletes can also share their heart rate, enabling fans to compare their heartbeat with their favorite performer ("I was 78% in sync with Chris Martin during A Sky Full of Stars").

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
shares: id (uuid PK), card_id (FK), platform (instagram|tiktok|x|whatsapp|link), shared_at
```

## Brand identity

- **Primary color**: #C0392B (Tumtum red)
- **Secondary red**: #E74C3C
- **Accent (artist)**: #00D2FF (cyan — used for artist HR line in comparison cards)
- **Dark background**: #08080C
- **Surface**: #111118
- **Border**: #1A1A24
- **Text muted**: #6B6B80
- **Text primary**: #F0F0F5
- **Logo font**: Georgia, serif — bold, uppercase, letter-spacing 2-3px
- **Body font**: system-ui, -apple-system, sans-serif
- **Theme**: Dark mode only (for MVP)
- **Tone**: Premium, emotional, nocturnal. The app should feel like being at a concert at night.

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
2. Baseline: 60-second centered rolling mean
3. Std dev: 60-second centered rolling standard deviation
4. Z-score: (smoothed_bpm - baseline) / std for each point
5. Threshold: mark points where z-score > 2.0 as "elevated"
6. Group: consecutive elevated points → "peak region"
7. Filter: peak regions < 5 seconds are discarded (noise)
8. Extract: peak_bpm = max(region), peak_time = timestamp of max
9. Merge: peaks within 30 seconds of each other → keep highest
10. Rank: by magnitude (z-score × duration_seconds)
11. Match: top N peaks → nearest event_timeline entry (±60s window)

Output: [{ timestamp, bpm, duration, magnitude, matched_label }]
```

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
