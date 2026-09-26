import { CONSENT_TEXT_VERSION, type ConsentPurpose } from './consent-copy'
import type { RequestKind, RequestStatus } from './privacy-requests'

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000'

/**
 * Who is asking, on every request (26/09 contract). The server writes it next
 * to each consent, so a "yes" can be traced to the screen that asked for it.
 */
export const CLIENT_HEADER = { 'X-Tumtum-Client': 'web/site' } as const

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
 * The server refused because a consent is missing (403, `consent_required`).
 * The page that catches this opens the consent screen on `purpose` — never
 * retries in silence (26/09 contract).
 */
export class ConsentRequiredError extends ApiError {
  constructor(
    detail: string,
    public purpose: ConsentPurpose | string,
  ) {
    super(403, detail)
    this.name = 'ConsentRequiredError'
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
        headers: { 'Content-Type': 'application/json', ...CLIENT_HEADER },
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

/**
 * Send one request and return the response only when it succeeded. Every
 * failure becomes an ApiError with a sentence a person can read.
 */
async function send(
  path: string,
  options: RequestInit = {},
  retried = false,
): Promise<Response> {
  const token = stored(ACCESS_KEY)
  const refreshHeld = stored(REFRESH_KEY)

  let response: Response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...CLIENT_HEADER,
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
    return send(path, options, true)
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
    if (response.status === 403 && body.code === 'consent_required') {
      throw new ConsentRequiredError(
        typeof body.detail === 'string' ? body.detail : 'Falta o seu consentimento para isso.',
        typeof body.purpose === 'string' ? body.purpose : '',
      )
    }
    // A 422 from Pydantic carries a list of field problems, not a sentence;
    // passed through, it reached a screen as "[object Object]" or crashed it.
    const detail =
      typeof body.detail === 'string'
        ? body.detail
        : body.detail
          ? 'O servidor não aceitou esses dados. Confere o que você digitou.'
          : 'Erro desconhecido'
    throw new ApiError(response.status, detail)
  }

  return response
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await send(path, options)
  if (response.status === 204) return undefined as T
  // 202 ("the code is on its way") may carry no body at all.
  if (response.status === 202) return (await response.json().catch(() => undefined)) as T
  return response.json()
}

/**
 * The file name a response names in its Content-Disposition, or the fallback
 * when it names none. Reads the RFC 5987 `filename*=UTF-8''…` form first (it
 * is the one that can carry an accent), then plain `filename=`, quoted or not.
 * The header is readable cross-origin only because the API exposes it.
 */
export function filenameFromDisposition(disposition: string | null, fallback: string): string {
  if (!disposition) return fallback
  const extended = /filename\*\s*=\s*(?:[\w-]+)?'[^']*'([^;]+)/i.exec(disposition)?.[1]
  if (extended) {
    try {
      const decoded = decodeURIComponent(extended.trim().replace(/^"|"$/g, ''))
      if (decoded) return decoded
    } catch {
      // A malformed escape falls through to the plain form.
    }
  }
  const plain = /filename\s*=\s*(?:"([^"]*)"|([^;]+))/i.exec(disposition)
  const name = (plain?.[1] ?? plain?.[2] ?? '').trim()
  return name || fallback
}

/**
 * Fetch a file the API serves behind the session. A plain link or an `<img>`
 * cannot carry the token, so the bytes come through here.
 */
async function fetchFile(path: string, fallbackName: string): Promise<{ blob: Blob; name: string }> {
  const response = await send(path)
  const blob = await response.blob()
  const name = filenameFromDisposition(response.headers.get('Content-Disposition'), fallbackName)
  return { blob, name }
}

/** Hand a blob to the browser as a download under the given name. */
function saveBlob(blob: Blob, name: string): void {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = name
  document.body.appendChild(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 10_000)
}

/**
 * Fetch a file behind the session and save it as a download. Returns the
 * name it was saved under, so the screen can say which file landed.
 */
async function download(path: string, fallbackName: string): Promise<string> {
  const { blob, name } = await fetchFile(path, fallbackName)
  saveBlob(blob, name)
  return name
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
  /** `YYYY-MM-DD`, or null for an account made before 26/09 (it is asked once). */
  birth_date: string | null
  /**
   * Whether this account operates the platform — registers events, attaches
   * a match or a setlist. Decided by the server's `admin_emails`; the site
   * only uses it to show the operator's doors, every endpoint checks itself.
   */
  is_admin: boolean
}

/** Where the sign-up code went, and the server's own windows (#64). */
export interface SignupStarted {
  email: string
  expires_in_seconds: number
  resend_after_seconds: number
}

/**
 * What sign-up sends (26/09): the birth date is checked against 18 by the
 * server, and the Terms and the heart-rate reading are two separate yeses.
 */
export interface SignupStartData {
  email: string
  name: string
  password: string
  /** `YYYY-MM-DD` */
  birth_date: string
  terms_accepted: boolean
  read_heart_rate: boolean
}

export const auth = {
  /**
   * Step one of an account (#64, 24/09): the server mails a 6-digit code to
   * the address and creates nothing. The account exists only after
   * `signupConfirm`, so an address nobody reads never becomes one.
   */
  signupStart: (data: SignupStartData) =>
    request<SignupStarted>('/api/auth/register/start', {
      method: 'POST',
      body: JSON.stringify({
        email: data.email,
        name: data.name,
        password: data.password,
        birth_date: data.birth_date,
        terms_accepted: data.terms_accepted,
        consent_text_version: CONSENT_TEXT_VERSION,
        read_heart_rate: data.read_heart_rate,
      }),
    }),

  /** Step two: the code came back, and the account is made and signed in. */
  signupConfirm: (email: string, code: string) =>
    request<TokenResponse>('/api/auth/register/confirm', {
      method: 'POST',
      body: JSON.stringify({ email, code }),
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
      headers: { 'Content-Type': 'application/json', ...CLIENT_HEADER },
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

/** R-R intervals and motion are neither sent nor returned since 26/09. */
export interface HRDataPoint {
  time: string
  bpm: number
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

  /**
   * "Apagar esta noite": the readings, the moments, the cards and any feed
   * post of this night go with it. The account stays.
   */
  deleteSession: (sessionId: string) =>
    request<void>(`/api/health/sessions/${sessionId}`, { method: 'DELETE' }),
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
  /**
   * When the card became public — set by sharing it, cleared by
   * "Despublicar". Null means its page and image answer 404 to everyone.
   */
  published_at: string | null
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

export type CardImageFormat = 'story' | 'og'

function cardPreviewPath(cardId: string, format: CardImageFormat): string {
  return `/api/cards/${encodeURIComponent(cardId)}/preview?format=${format}`
}

function cardFileName(cardId: string): string {
  return `tumtum-${cardId.slice(0, 8)}.png`
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

  /**
   * The owner's own card image, published or not (26/09). `/image` is public
   * and answers only once the card is shared, and an `<img>` cannot carry the
   * token, so the PNG comes through the session and is handed back as an
   * object URL. Whoever asks for one owns it: release it with
   * `revokePreviewUrl` when the image leaves the screen.
   */
  previewBlobUrl: async (cardId: string, format: CardImageFormat = 'story'): Promise<string> => {
    const { blob } = await fetchFile(cardPreviewPath(cardId, format), cardFileName(cardId))
    return URL.createObjectURL(blob)
  },

  revokePreviewUrl: (url: string) => URL.revokeObjectURL(url),

  /**
   * Save the owner's card as a PNG — no need to publish it first: a file on
   * the person's own phone is not a public page. Returns the saved name.
   */
  downloadPreview: (cardId: string, format: CardImageFormat = 'story') =>
    download(cardPreviewPath(cardId, format), cardFileName(cardId)),

  /** Read a shared card without signing in. Returns only what the image shows. */
  getPublic: (cardId: string) =>
    request<PublicCardData>(`/api/cards/${cardId}/public`),

  delete: (cardId: string) =>
    request<void>(`/api/cards/${cardId}`, { method: 'DELETE' }),

  /** Sharing is the act that publishes: the public page exists from here on. */
  trackShare: (cardId: string, platform: string) =>
    request<ShareData>(`/api/cards/${cardId}/share`, {
      method: 'POST',
      body: JSON.stringify({ platform }),
    }),

  /** Take the public page and image down. The card stays in the collection. */
  unpublish: (cardId: string) =>
    request<{ published_at: null }>(`/api/cards/${cardId}/unpublish`, { method: 'POST' }),
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
  /** `YYYY-MM-DD`, or null until it is asked once. */
  birth_date: string | null
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

  /** `birth_date` is accepted once, only while it is still empty. */
  updateProfile: (data: { name?: string; avatar_url?: string; birth_date?: string }) =>
    request<UserProfile>('/api/users/me', {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),

  getPublicProfile: (userId: string) =>
    request<PublicProfile>(`/api/users/${userId}`),

  /** Everything the account holds except the readings themselves (LGPD art. 18 II). */
  data: () => request<MyData>('/api/users/me/data'),

  /** The same, with every reading, as a JSON file. Resolves to the file name. */
  exportJson: () => download('/api/users/me/export', 'tumtum-export.json'),

  /** Every reading as `session_id,time,bpm`. Resolves to the file name. */
  exportCsv: () => download('/api/users/me/export.csv', 'tumtum-export.csv'),

  /** Step one: a 6-digit code goes to the NEW address. 401 wrong password, 409 taken. */
  changeEmail: (email: string, password: string) =>
    request<SignupStarted | undefined>('/api/users/me/email', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  /** Step two: the code came back, and the account's e-mail is the new one. */
  confirmEmail: (code: string) =>
    request<UserProfile>('/api/users/me/email/confirm', {
      method: 'POST',
      body: JSON.stringify({ code }),
    }),

  /** Deletes the account and everything in it. 401 when the password is wrong. */
  deleteAccount: (password: string) =>
    request<void>('/api/users/me/delete', {
      method: 'POST',
      body: JSON.stringify({ password }),
    }),

  /** Requests to the encarregado: answered within 15 days (`due_at`). */
  requests: {
    create: (kind: RequestKind, message: string) =>
      request<DataSubjectRequest>('/api/users/me/requests', {
        method: 'POST',
        body: JSON.stringify({ kind, message }),
      }),

    list: () => request<DataSubjectRequest[]>('/api/users/me/requests'),
  },
}

// --- Consents (26/09): one yes per purpose, recorded with its text version ---

export type ConsentMeans = 'tap' | 'checkbox' | 'button' | 'form'

export interface ConsentEntry {
  purpose: ConsentPurpose
  granted: boolean
  granted_at: string | null
  revoked_at: string | null
  text_version: string | null
}

export interface ConsentsResponse {
  text_version: string
  /** Always all seven, `granted: false` when never given or revoked. */
  consents: ConsentEntry[]
}

export const consents = {
  get: () => request<ConsentsResponse>('/api/consents'),

  /** Only the purposes present change. Answers with the whole state again. */
  put: (
    purposes: Partial<Record<ConsentPurpose, boolean>>,
    means: ConsentMeans,
    textVersion: string = CONSENT_TEXT_VERSION,
  ) =>
    request<ConsentsResponse>('/api/consents', {
      method: 'PUT',
      body: JSON.stringify({ text_version: textVersion, means, purposes }),
    }),
}

// --- Data-subject rights (26/09) ---

export interface DataSubjectRequest {
  id: string
  kind: RequestKind
  message: string
  status: RequestStatus
  opened_at: string
  due_at: string
  answered_at: string | null
  answer: string | null
}

/** `GET /api/users/me/data`. Loosely typed below the top level on purpose. */
export interface MyData {
  user: Record<string, unknown>
  birth_date: string | null
  consents: ConsentEntry[]
  sessions: Array<Record<string, unknown>>
  peaks: Array<Record<string, unknown>>
  cards: Array<Record<string, unknown>>
  posts: Array<Record<string, unknown>>
  reactions: Array<Record<string, unknown>>
  shares: Array<Record<string, unknown>>
  blocks: Array<Record<string, unknown>>
  reports_filed: Array<Record<string, unknown>>
  requests: DataSubjectRequest[]
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
