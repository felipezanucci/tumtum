import { afterEach, beforeEach, describe, it, expect, vi } from 'vitest'
import { millisUntilTokenExpiry } from './api'

/** Build a JWT-shaped string with the given payload. Signature is irrelevant here. */
function token(payload: object): string {
  const body = Buffer.from(JSON.stringify(payload))
    .toString('base64')
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')
  return `header.${body}.signature`
}

const NOW = Date.parse('2026-08-29T20:00:00Z')

describe('millisUntilTokenExpiry', () => {
  it('reports the time left on a token that is still valid', () => {
    const t = token({ exp: NOW / 1000 + 3600, sub: 'x' })
    expect(millisUntilTokenExpiry(t, NOW)).toBe(3_600_000)
  })

  it('goes negative once the token has expired', () => {
    const t = token({ exp: NOW / 1000 - 60, sub: 'x' })
    expect(millisUntilTokenExpiry(t, NOW)).toBe(-60_000)
  })

  it('reads a payload whose base64url length needs padding', () => {
    // Vary the subject until the encoded payload is not a multiple of four,
    // which is exactly what plain atob refuses.
    let t = ''
    for (let i = 0; i < 12; i += 1) {
      const candidate = token({ exp: NOW / 1000 + 60, sub: 'a'.repeat(i) })
      if (candidate.split('.')[1].length % 4 !== 0) {
        t = candidate
        break
      }
    }
    expect(t).not.toBe('')
    expect(millisUntilTokenExpiry(t, NOW)).toBe(60_000)
  })

  it('returns null when the token carries no expiry', () => {
    expect(millisUntilTokenExpiry(token({ sub: 'x' }), NOW)).toBeNull()
  })

  it('returns null for something that is not a token', () => {
    expect(millisUntilTokenExpiry('not-a-token', NOW)).toBeNull()
    expect(millisUntilTokenExpiry('', NOW)).toBeNull()
    expect(millisUntilTokenExpiry('a.!!!not-base64!!!.c', NOW)).toBeNull()
  })
})

// --- The session without localStorage (legal opinion v1.1, §18, 26/09) ---

type Stored = Record<string, string>

function fakeStorage(store: Stored) {
  return {
    getItem: (k: string) => store[k] ?? null,
    setItem: (k: string, v: string) => {
      store[k] = v
    },
    removeItem: (k: string) => {
      delete store[k]
    },
  }
}

interface Call {
  url: string
  init: RequestInit
}

describe('the web session', () => {
  let local: Stored
  let tab: Stored
  let calls: Call[]
  let answers: Array<() => Response>

  function answer(status: number, body: unknown = {}) {
    answers.push(() => new Response(status === 204 ? null : JSON.stringify(body), { status }))
  }

  async function freshApi() {
    vi.resetModules()
    return import('./api')
  }

  function bodyOf(call: Call): Record<string, unknown> {
    return JSON.parse(String(call.init.body ?? '{}'))
  }

  beforeEach(() => {
    local = {}
    tab = {}
    calls = []
    answers = []
    vi.stubGlobal('window', { localStorage: fakeStorage(local), sessionStorage: fakeStorage(tab) })
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string, init: RequestInit = {}) => {
        calls.push({ url, init })
        const next = answers.shift()
        if (!next) throw new Error(`unexpected fetch to ${url}`)
        return next()
      }),
    )
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('keeps the access token in the tab and never writes a refresh token', async () => {
    const api = await freshApi()
    api.storeTokens({ access_token: 'access-1', refresh_token: 'refresh-must-not-stay' })
    expect(api.currentAccessToken()).toBe('access-1')
    expect(Object.values(tab)).toEqual(['access-1'])
    expect(local).toEqual({})
    expect(JSON.stringify(tab)).not.toContain('refresh-must-not-stay')
  })

  it('restores a new tab from the cookie with an empty body', async () => {
    const api = await freshApi()
    answer(200, { access_token: 'from-cookie', token_type: 'bearer', refresh_token: null })
    await expect(api.restoreSession()).resolves.toBe(true)
    expect(api.currentAccessToken()).toBe('from-cookie')
    expect(calls).toHaveLength(1)
    // Through the site's own origin (the rewrite), so the cookie is first-party.
    expect(calls[0].url).toBe('/api/auth/refresh')
    expect(calls[0].init.credentials).toBe('include')
    expect((calls[0].init.headers as Record<string, string>)['X-Tumtum-Client']).toBe('web/site')
    expect(bodyOf(calls[0])).toEqual({})
    expect(local).toEqual({})
  })

  it('treats a 401 from the cookie as signed out, and does not ask again', async () => {
    const api = await freshApi()
    answer(401, { detail: 'Sua sessão terminou. Entre de novo.' })
    await expect(api.restoreSession()).resolves.toBe(false)
    await expect(api.restoreSession()).resolves.toBe(false)
    expect(calls).toHaveLength(1)
    expect(api.currentAccessToken()).toBeNull()
  })

  it('moves a legacy localStorage session into the cookie once, then forgets it', async () => {
    local.access_token = 'old-access'
    local.refresh_token = 'old-refresh-token-0123456789'
    const api = await freshApi()
    answer(200, { access_token: 'migrated', token_type: 'bearer', refresh_token: null })
    await expect(api.restoreSession()).resolves.toBe(true)
    expect(calls).toHaveLength(1)
    expect(bodyOf(calls[0])).toEqual({ refresh_token: 'old-refresh-token-0123456789' })
    expect(calls[0].init.credentials).toBe('include')
    expect(local).toEqual({})
    expect(api.currentAccessToken()).toBe('migrated')
  })

  it('keeps a legacy token only when the server never answered', async () => {
    local.refresh_token = 'old-refresh-token-0123456789'
    const api = await freshApi()
    answers.push(() => {
      throw new TypeError('offline')
    })
    answers.push(() => {
      throw new TypeError('offline')
    })
    await expect(api.restoreSession()).resolves.toBe(false)
    expect(local.refresh_token).toBe('old-refresh-token-0123456789')
  })

  it('sends the cookie on auth calls and signs out through it', async () => {
    const api = await freshApi()
    answer(200, { access_token: 'signed-in', token_type: 'bearer', refresh_token: null })
    const tokens = await api.auth.login('ana@x.cc', 'segredo123')
    expect(calls[0].init.credentials).toBe('include')
    api.storeTokens(tokens)

    answer(204)
    await api.auth.logout()
    expect(api.currentAccessToken()).toBeNull()
    expect(tab).toEqual({})
    expect(calls[0].url).toBe('/api/auth/login')
    const out = calls[1]
    expect(out.url).toBe('/api/auth/logout')
    expect(out.init.credentials).toBe('include')
    expect(bodyOf(out)).toEqual({})
  })

  it('renews an expired access token through the cookie and retries', async () => {
    const api = await freshApi()
    api.storeTokens({ access_token: 'stale' })
    answer(401, { detail: 'Token inválido ou expirado' })
    answer(200, { access_token: 'fresh', token_type: 'bearer', refresh_token: null })
    answer(200, { id: 'u1' })
    await api.auth.me()
    expect(calls.map((c) => c.url.replace(/^.*\/api/, '/api'))).toEqual([
      '/api/auth/me',
      '/api/auth/refresh',
      '/api/auth/me',
    ])
    expect(bodyOf(calls[1])).toEqual({})
    const retried = calls[2].init.headers as Record<string, string>
    expect(retried.Authorization).toBe('Bearer fresh')
    expect(local).toEqual({})
  })

  describe('when the session ends (28/09)', () => {
    let assign: ReturnType<typeof vi.fn>

    function onPage(pathname: string) {
      assign = vi.fn()
      vi.stubGlobal('window', {
        localStorage: fakeStorage(local),
        sessionStorage: fakeStorage(tab),
        location: { pathname, assign },
      })
    }

    it('signs out and goes to /login?motivo=sessao when the cookie refuses the renewal', async () => {
      onPage('/profile')
      const api = await freshApi()
      api.storeTokens({ access_token: 'stale' })
      answer(401, { detail: 'Token inválido ou expirado' })
      answer(401, { detail: 'Sua sessão terminou. Entre de novo.' })

      const failure = await api.users.getProfile().catch((err: unknown) => err)

      expect(failure).toBeInstanceOf(api.ApiError)
      expect((failure as Error).message).toBe('Sua sessão terminou. Entra de novo.')
      expect((failure as Error).message).not.toContain('Token')
      expect(api.currentAccessToken()).toBeNull()
      expect(tab).toEqual({})
      expect(assign).toHaveBeenCalledTimes(1)
      expect(assign).toHaveBeenCalledWith('/login?motivo=sessao')
    })

    it('does the same when even the renewed token is refused', async () => {
      onPage('/sessions')
      const api = await freshApi()
      api.storeTokens({ access_token: 'stale' })
      answer(401, { detail: 'Token inválido ou expirado' })
      answer(200, { access_token: 'fresh', token_type: 'bearer', refresh_token: null })
      answer(401, { detail: 'Token inválido ou expirado' })

      await expect(api.auth.me()).rejects.toThrow('Sua sessão terminou. Entra de novo.')
      expect(api.currentAccessToken()).toBeNull()
      expect(assign).toHaveBeenCalledWith('/login?motivo=sessao')
    })

    it('keeps the session when the 401 after a renewal is a wrong password', async () => {
      onPage('/profile')
      const api = await freshApi()
      api.storeTokens({ access_token: 'stale' })
      answer(401, { detail: 'Senha incorreta' })
      answer(200, { access_token: 'fresh', token_type: 'bearer', refresh_token: null })
      answer(401, { detail: 'Senha incorreta' })

      await expect(api.users.deleteAccount('errada')).rejects.toThrow('Senha incorreta')
      expect(api.currentAccessToken()).toBe('fresh')
      expect(assign).not.toHaveBeenCalled()
    })

    it('never shows the raw token text when the renewal could not be asked', async () => {
      onPage('/profile')
      const api = await freshApi()
      api.storeTokens({ access_token: 'stale' })
      answer(401, { detail: 'Token inválido ou expirado' })
      answers.push(() => {
        throw new TypeError('offline')
      })

      const failure = await api.auth.me().catch((err: unknown) => err)
      expect((failure as Error).message).not.toContain('Token inválido')
      expect(assign).not.toHaveBeenCalled()
    })

    it('does not send someone already on the login page back to it', async () => {
      onPage('/login')
      const api = await freshApi()
      api.storeTokens({ access_token: 'stale' })
      answer(401, { detail: 'Token inválido ou expirado' })
      answer(401, {})

      await expect(api.auth.me()).rejects.toThrow('Sua sessão terminou. Entra de novo.')
      expect(assign).not.toHaveBeenCalled()
    })
  })

  it('sends everything else straight to the API with the bearer token', async () => {
    const api = await freshApi()
    api.storeTokens({ access_token: 'bearer-1' })
    answer(200, [])
    await api.events.list()
    expect(calls[0].url).toMatch(/^https?:\/\/[^/]+\/api\/events/)
    expect(calls[0].init.credentials).toBeUndefined()
    const headers = calls[0].init.headers as Record<string, string>
    expect(headers.Authorization).toBe('Bearer bearer-1')
  })
})
