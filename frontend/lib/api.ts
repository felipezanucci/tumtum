const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000'

export class ApiError extends Error {
  constructor(
    public status: number,
    public detail: string,
  ) {
    super(detail)
    this.name = 'ApiError'
  }
}

/**
 * Whether a 401 body carries FastAPI's default text rather than a reason of
 * its own. Anything the API bothered to write is better than a guess.
 */
function isGenericAuthFailure(detail: unknown): boolean {
  if (typeof detail !== 'string') return true
  const text = detail.trim().toLowerCase()
  return text === '' || text === 'not authenticated' || text === 'erro desconhecido'
}

// --- The session (#34, 22/09) ---
//
// The access token lasts an hour; the refresh token renews it and lasts 90
// days from its last use. Every renewal rotates the refresh token, and the
// server treats a spent one presented again as a stolen copy — so two tabs
// renewing at once would sign each other out. Renewal therefore runs under a
// browser-wide lock, and a tab that waited finds the other tab's fresh token
// instead of spending the old one again.

const ACCESS_KEY = 'access_token'
const REFRESH_KEY = 'refresh_token'

function stored(key: string): string | null {
  return typeof window !== 'undefined' ? localStorage.getItem(key) : null
}

/** Keep what the server just issued. Every sign-in path goes through here. */
export function storeTokens(tokens: { access_token: string; refresh_token?: string | null }) {
  localStorage.setItem(ACCESS_KEY, tokens.access_token)
  if (tokens.refresh_token) localStorage.setItem(REFRESH_KEY, tokens.refresh_token)
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_KEY)
  localStorage.removeItem(REFRESH_KEY)
}

async function withRenewLock<T>(work: () => Promise<T>): Promise<T> {
  const locks = typeof navigator !== 'undefined' ? navigator.locks : undefined
  return locks ? locks.request('tumtum-renew', work) : work()
}

/**
 * Trade the refresh token for a new pair. True when there is now a fresh
 * access token — renewed here or, while this tab waited, by another one.
 */
async function renew(spent: string | null): Promise<boolean> {
  return withRenewLock(async () => {
    const refresh = stored(REFRESH_KEY)
    if (!refresh) return false
    if (spent !== null && refresh !== spent) return true // another tab renewed
    try {
      const response = await fetch(`${API_BASE}/api/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refresh_token: refresh }),
      })
      if (!response.ok) {
        // Refused for good (expired, revoked, reused): forget it, so the page
        // says "sessão expirou" — which is now true.
        if (response.status === 401) clearTokens()
        return false
      }
      storeTokens(await response.json())
      return true
    } catch {
      return false
    }
  })
}

async function request<T>(
  path: string,
  options: RequestInit = {},
  retried = false,
): Promise<T> {
  const token = stored(ACCESS_KEY)
  const refreshHeld = stored(REFRESH_KEY)

  let response: Response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    })
  } catch {
    // fetch only rejects when the request never reached a server: the API is
    // down, unreachable from this device, or blocked by CORS. Naming the URL
    // turns "erro desconhecido" into something diagnosable — a misconfigured
    // NEXT_PUBLIC_API_URL still pointing at localhost is visible immediately.
    throw new ApiError(0, `Não foi possível falar com o servidor em ${API_BASE}`)
  }

  // An hour-old access token is routine now, not an ending: renew once and
  // ask again. Only a request that carried a token is retried — a 401 on
  // /login is a wrong password, not an expired session.
  if (response.status === 401 && token && !retried && (await renew(refreshHeld))) {
    return request<T>(path, options, true)
  }

  if (!response.ok) {
    const body = await response.json().catch(() => ({ detail: 'Erro desconhecido' }))
    // FastAPI answers a missing or expired token with the English string
    // "Not authenticated", which reached the user verbatim and explained
    // nothing about what to do next. Only that one gets replaced: a 401 also
    // means a wrong email or password, and this used to overwrite the server's
    // own "Email ou senha incorretos" with a claim about an expired session —
    // telling someone who mistyped a letter to sign in again, which they were
    // already trying to do.
    if (response.status === 401 && isGenericAuthFailure(body.detail)) {
      throw new ApiError(401, 'Sua sessão expirou. Entre na sua conta para continuar.')
    }
    throw new ApiError(response.status, body.detail || 'Erro desconhecido')
  }

  if (response.status === 204) return undefined as T
  return response.json()
}

// --- Auth ---

export interface TokenResponse {
  access_token: string
  token_type: string
  refresh_token?: string | null
}

export interface UserResponse {
  id: string
  email: string
  name: string
  avatar_url: string | null
  auth_provider: string
  created_at: string
  /**
   * Whether this account operates the platform — registers events, attaches
   * a match or a setlist. Decided by the server's `admin_emails`; the site
   * only uses it to show the operator's doors, every endpoint checks itself.
   */
  is_admin: boolean
}

export const auth = {
  register: (email: string, name: string, password: string) =>
    request<TokenResponse>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, name, password }),
    }),

  login: (email: string, password: string) =>
    request<TokenResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  /** "Sair" means out: the server revokes this browser's refresh chain. */
  logout: async () => {
    const refresh = stored(REFRESH_KEY)
    clearTokens()
    if (!refresh) return
    await fetch(`${API_BASE}/api/auth/logout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refresh_token: refresh }),
    }).catch(() => undefined)
  },

  me: () => request<UserResponse>('/api/auth/me'),
}

/**
 * Read how long a stored token has left, without verifying it.
 *
 * Tokens last 24 hours, which is shorter than the gap between deciding to
 * capture an event and the event ending. One that is valid when a six-hour
 * capture starts can expire before it is saved — and the save is the moment
 * when there is finally something to lose. Only the server can say whether a
 * token is genuine; this just reads the expiry it carries, which is enough to
 * warn someone before they start.
 *
 * Returns milliseconds remaining, or null when the token carries no readable
 * expiry — in which case there is nothing to warn about.
 */
export const passwordReset = {
  /** Always resolves the same way — the API refuses to say who has an account. */
  request: (email: string) =>
    request<{ message: string }>('/api/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ email }),
    }),

  /** Returns a token: choosing a new password signs you in. */
  complete: (token: string, password: string) =>
    request<TokenResponse>('/api/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ token, password }),
    }),
}

export function millisUntilTokenExpiry(token: string, now = Date.now()): number | null {
  const payload = token.split('.')[1]
  if (!payload) return null
  try {
    // JWT uses base64url, which atob does not accept.
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    const claims = JSON.parse(atob(padded))
    if (typeof claims.exp !== 'number') return null
    return claims.exp * 1000 - now
  } catch {
    return null
  }
}

// --- Health ---

export interface WearableConnection {
  id: string
  provider: string
  status: string
  last_sync_at: string | null
  created_at: string
}

export interface HRDataPoint {
  time: string
  bpm: number
  rr_interval_ms: number | null
  motion_level: number | null
  source: string | null
}

export interface HRSession {
  id: string
  user_id: string
  event_id: string | null
  start_time: string
  end_time: string
  avg_bpm: number | null
  max_bpm: number | null
  min_bpm: number | null
  data_quality_score: number | null
  source_device: string | null
  created_at: string
}

export interface HRSessionDetail extends HRSession {
  data_points: HRDataPoint[]
}

export const health = {
  connectWearable: (provider: string, accessToken: string, refreshToken?: string) =>
    request<WearableConnection>('/api/health/wearables', {
      method: 'POST',
      body: JSON.stringify({
        provider,
        access_token: accessToken,
        refresh_token: refreshToken,
      }),
    }),

  listWearables: () => request<WearableConnection[]>('/api/health/wearables'),

  disconnectWearable: (connectionId: string) =>
    request<void>(`/api/health/wearables/${connectionId}`, { method: 'DELETE' }),

  createSession: (data: {
    start_time: string
    end_time: string
    source_device?: string
    event_id?: string
    data_points: Array<{
      time: string
      bpm: number
      rr_interval_ms?: number
      motion_level?: number
      source?: string
    }>
  }) =>
    request<HRSession>('/api/health/sessions', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  listSessions: () => request<HRSession[]>('/api/health/sessions'),

  getSession: (sessionId: string) =>
    request<HRSessionDetail>(`/api/health/sessions/${sessionId}`),

}

// --- Events ---

export interface TumtumEvent {
  id: string
  name: string
  subtitle: string | null
  venue: string | null
  city: string | null
  country: string | null
  date: string
  start_time: string | null
  end_time: string | null
  event_type: string
  external_id: string | null
  cover_image_url: string | null
  created_at: string
}

export interface TimelineEntry {
  id: string
  event_id: string
  timestamp: string
  label: string
  entry_type: string
  metadata: Record<string, unknown> | null
}

export interface EventDetail extends TumtumEvent {
  timeline: TimelineEntry[]
}

export interface Peak {
  id: string
  session_id: string
  timestamp: string
  bpm: number
  duration_seconds: number
  magnitude: number
  timeline_entry_id: string | null
  rank: number | null
  matched_label: string | null
}

/** A match as API-Football lists it. */
export interface FixtureBrief {
  fixture_id: number
  kickoff: string | null
  home: string | null
  away: string | null
  league: string | null
  status: string | null
}

/**
 * The server's live watch of a match (#52): it polls the fixture every 15 s
 * while the match is on and writes the two whistles as it sees them.
 */
export interface MatchWatch {
  state: 'off' | 'idle' | 'waiting' | 'watching' | 'done'
  status: string | null
  scheduled: string | null
  last_polled_at: string | null
  kickoff_at: string | null
  second_half_at: string | null
  polls: number
  notes: string[]
  spent_today: number
  budget: number
  last_error: string | null
}

/**
 * One line of the operator's script for a show.
 *
 * A concert has no API that says which song was playing at 22h12, so a person
 * taps COMEÇOU and that tap is the measurement. `started_at` is null until
 * then, and null is what the screen counts to know which song is next.
 */
export interface SetlistSong {
  id: string
  position: number
  title: string
  started_at: string | null
}

export interface HRDataPointBrief {
  time: string
  bpm: number
}

export interface ExperienceData {
  session: {
    id: string
    event_id: string | null
    start_time: string
    end_time: string
    avg_bpm: number | null
    max_bpm: number | null
    min_bpm: number | null
    data_quality_score: number | null
    source_device: string | null
  }
  peaks: Peak[]
  timeline: TimelineEntry[]
  hr_data: HRDataPointBrief[]
}

export const events = {
  create: (data: {
    name: string
    event_type: string
    date: string
    subtitle?: string
    venue?: string
    city?: string
    country?: string
    start_time?: string
    end_time?: string
    cover_image_url?: string
  }) =>
    request<TumtumEvent>('/api/events', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  /** Correct an event. Only the fields sent are changed. */
  update: (
    id: string,
    data: Partial<{
      name: string
      event_type: string
      date: string
      subtitle: string
      venue: string
      city: string
      country: string
      start_time: string
      end_time: string
      cover_image_url: string
    }>,
  ) =>
    request<TumtumEvent>(`/api/events/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  list: (params?: {
    q?: string
    event_type?: string
    city?: string
    date_from?: string
    date_to?: string
  }) => {
    const searchParams = new URLSearchParams()
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value) searchParams.set(key, value)
      })
    }
    const qs = searchParams.toString()
    return request<TumtumEvent[]>(`/api/events${qs ? `?${qs}` : ''}`)
  },

  get: (eventId: string) => request<EventDetail>(`/api/events/${eventId}`),

  addTimelineEntry: (eventId: string, data: {
    timestamp: string
    label: string
    entry_type: string
    metadata?: Record<string, unknown>
  }) =>
    request<TimelineEntry>(`/api/events/${eventId}/timeline`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getTimeline: (eventId: string) =>
    request<TimelineEntry[]>(`/api/events/${eventId}/timeline`),

  // --- Operator only. The server answers 403 for anyone else. ---

  deleteTimelineEntry: (eventId: string, entryId: string) =>
    request<void>(`/api/events/${eventId}/timeline/${entryId}`, { method: 'DELETE' }),

  /** Matches on API-Football, by team and/or date (YYYY-MM-DD). */
  searchFixtures: (params: { team?: string; on?: string }) => {
    const qs = new URLSearchParams()
    if (params.team) qs.set('team', params.team)
    if (params.on) qs.set('on', params.on)
    return request<FixtureBrief[]>(`/api/events/sources/football?${qs.toString()}`)
  },

  /** Build the timeline from a match; the rows this source wrote before are replaced. */
  attachFixture: (eventId: string, fixtureId: number) =>
    request<TimelineEntry[]>(`/api/events/${eventId}/timeline/football`, {
      method: 'POST',
      body: JSON.stringify({ fixture_id: fixtureId }),
    }),

  /** What the live watch is doing for this match. */
  getWatch: (eventId: string) => request<MatchWatch>(`/api/events/${eventId}/watch`),

  /** The operator's script for a show, and how far through it the show is. */
  getSetlist: (eventId: string) =>
    request<SetlistSong[]>(`/api/events/${eventId}/setlist`),

  /** Paste the order, before the show. Replaces the list; measured times survive. */
  replaceSetlist: (eventId: string, songs: string[]) =>
    request<SetlistSong[]>(`/api/events/${eventId}/setlist`, {
      method: 'PUT',
      body: JSON.stringify({ songs }),
    }),

  /**
   * COMEÇOU — one tap, and the song has a measured time on every fan's night.
   * With no position it advances to the first song nobody has started.
   */
  startSong: (eventId: string, position?: number) =>
    request<SetlistSong>(`/api/events/${eventId}/setlist/start`, {
      method: 'POST',
      body: JSON.stringify({ position: position ?? null }),
    }),
}

// --- Experience ---

export const experience = {
  analyze: (sessionId: string) =>
    request<Peak[]>(`/api/experience/${sessionId}/analyze`, { method: 'POST' }),

  get: (sessionId: string) =>
    request<ExperienceData>(`/api/experience/${sessionId}`),
}

// --- Cards ---

export interface CardData {
  id: string
  user_id: string
  session_id: string
  peak_id: string | null
  card_type: string
  image_url: string | null
  video_url: string | null
  status: string
  metadata: Record<string, unknown> | null
  created_at: string
}

export interface ShareData {
  id: string
  card_id: string
  platform: string
  shared_at: string
}

export interface PublicCardData {
  id: string
  event_name: string
  event_date: string
  peak_bpm: number
  moment_label: string | null
  moment_time: string | null
  user_name: string
}

export const cards = {
  create: (data: {
    session_id: string
    peak_id?: string
    card_type?: string
    format?: string
  }) =>
    request<CardData>('/api/cards', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  list: () => request<CardData[]>('/api/cards'),

  get: (cardId: string) => request<CardData>(`/api/cards/${cardId}`),

  getImageUrl: (cardId: string) => `${API_BASE}/api/cards/${cardId}/image`,

  /** Landscape variant, sized for the link-preview slot rather than a Story. */
  getPreviewImageUrl: (cardId: string) =>
    `${API_BASE}/api/cards/${cardId}/image?format=og`,

  /** Read a shared card without signing in. Returns only what the image shows. */
  getPublic: (cardId: string) =>
    request<PublicCardData>(`/api/cards/${cardId}/public`),

  delete: (cardId: string) =>
    request<void>(`/api/cards/${cardId}`, { method: 'DELETE' }),

  trackShare: (cardId: string, platform: string) =>
    request<ShareData>(`/api/cards/${cardId}/share`, {
      method: 'POST',
      body: JSON.stringify({ platform }),
    }),
}

// --- Users ---

export interface UserProfile {
  id: string
  email: string
  name: string
  avatar_url: string | null
  auth_provider: string
  created_at: string
  total_sessions: number
  total_events: number
  total_cards: number
  highest_bpm: number | null
}

export interface PublicProfile {
  name: string
  avatar_url: string | null
  created_at: string
  total_sessions: number
  total_events: number
  total_cards: number
}

// --- Demo ---

export const demo = {
  seed: () =>
    request<{ message: string; events?: Array<{ id: string; name: string }> }>('/api/demo/seed', {
      method: 'POST',
    }),

  simulate: (eventId: string) =>
    request<ExperienceData>(`/api/demo/simulate/${eventId}`, {
      method: 'POST',
    }),
}

// --- Users ---

// --- Waitlist ---

export interface WaitlistJoinResult {
  email: string
  already_joined: boolean
}

export const waitlist = {
  /** Public: no account exists yet at the point someone asks to be told. */
  join: (entry: {
    email: string
    first_name?: string
    last_name?: string
    source?: string
  }) =>
    request<WaitlistJoinResult>('/api/waitlist', {
      method: 'POST',
      body: JSON.stringify(entry),
    }),

  count: () => request<{ total: number }>('/api/waitlist/count'),

  /** Restricted to the accounts named in the API's waitlist_admin_emails. */
  list: () => request<WaitlistEntry[]>('/api/waitlist'),
}

export interface WaitlistEntry {
  email: string
  first_name: string | null
  last_name: string | null
  source: string | null
  created_at: string
}

export const users = {
  getProfile: () => request<UserProfile>('/api/users/me'),

  updateProfile: (data: { name?: string; avatar_url?: string }) =>
    request<UserProfile>('/api/users/me', {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  getPublicProfile: (userId: string) =>
    request<PublicProfile>(`/api/users/${userId}`),
}

// --- Moderation: the operator's queue of reported posts (#36, 22/09) ---

export interface ReportedPost {
  post_id: string
  event_id: string
  event_name: string
  author: { name: string; initials: string }
  bpm: number
  moment_at: string
  label: string | null
  quote: string | null
  reports: number
  reasons: Record<string, number>
  first_reported_at: string
  hidden: boolean
}

export const moderation = {
  /** Every post with a report nobody has decided on. Operator only. */
  open: () => request<ReportedPost[]>('/api/admin/reports'),

  /** Keep it (it comes back if reports had hidden it) or take it down. */
  resolve: (postId: string, action: 'keep' | 'remove') =>
    request<void>(`/api/admin/reports/${postId}`, {
      method: 'POST',
      body: JSON.stringify({ action }),
    }),
}

// --- Series: the tour, club or championship above one event (#33, 22/09) ---

export type SeriesKind = 'tour' | 'club' | 'league'

export interface SeriesBrief {
  id: string
  name: string
  kind: SeriesKind
  dates: number
}

export const series = {
  list: () => request<SeriesBrief[]>('/api/series'),

  ofEvent: (eventId: string) => request<SeriesBrief | null>(`/api/events/${eventId}/series`),

  /** Operator only. */
  create: (name: string, kind: SeriesKind) =>
    request<SeriesBrief>('/api/series', {
      method: 'POST',
      body: JSON.stringify({ name, kind }),
    }),

  /** Operator only. Null takes the event out of any series. */
  assign: (eventId: string, seriesId: string | null) =>
    request<SeriesBrief | null>(`/api/events/${eventId}/series`, {
      method: 'PUT',
      body: JSON.stringify({ series_id: seriesId }),
    }),
}
