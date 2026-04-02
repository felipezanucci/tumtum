const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000'

class ApiError extends Error {
  constructor(
    public status: number,
    public detail: string,
  ) {
    super(detail)
    this.name = 'ApiError'
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const token =
    typeof window !== 'undefined' ? localStorage.getItem('access_token') : null

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
    const body = await response.json().catch(() => ({ detail: 'Erro desconhecido' }))
    throw new ApiError(response.status, body.detail || 'Erro desconhecido')
  }

  if (response.status === 204) return undefined as T
  return response.json()
}

// --- Auth ---

export interface TokenResponse {
  access_token: string
  token_type: string
}

export interface UserResponse {
  id: string
  email: string
  name: string
  avatar_url: string | null
  auth_provider: string
  created_at: string
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

  me: () => request<UserResponse>('/api/auth/me'),
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

export interface SyncStatus {
  connection_id: string
  status: string
  records_synced: number
  last_sync_at: string | null
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

  triggerSync: (connectionId: string, startTime: string, endTime: string) =>
    request<SyncStatus>('/api/health/sync', {
      method: 'POST',
      body: JSON.stringify({
        connection_id: connectionId,
        start_time: startTime,
        end_time: endTime,
      }),
    }),
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
