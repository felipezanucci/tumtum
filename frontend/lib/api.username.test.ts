import { describe, it, expect, vi, afterEach } from 'vitest'

import { auth, users } from './api'

type Call = [string, RequestInit]

function respondWith(status: number, body: unknown) {
  const fetchMock = vi.fn(async (..._args: unknown[]) => ({
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  }))
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

afterEach(() => vi.unstubAllGlobals())

const signup = {
  email: 'fa@tumtum.cc',
  name: 'Felipe',
  password: 'segredo',
  birth_date: '1990-01-01',
  terms_accepted: true,
  read_heart_rate: true,
}

describe('the @ on the wire (28/09)', () => {
  it('asks the check with the name escaped, and answers what the server said', async () => {
    const fetchMock = respondWith(200, { username: 'fe/lipe', available: false, reason: 'x' })
    const answer = await auth.checkUsername('fe/lipe?')
    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toBe('/api/auth/username/fe%2Flipe%3F')
    expect(init.method).toBeUndefined()
    expect(answer).toEqual({ username: 'fe/lipe', available: false, reason: 'x' })
  })

  it('does not ask the refresh cookie first, in a tab with no session', async () => {
    // With a window, any other call in a token-less tab asks /refresh first.
    const store = () => ({ getItem: () => null, setItem: () => undefined, removeItem: () => undefined })
    vi.stubGlobal('window', { localStorage: store(), sessionStorage: store() })
    vi.resetModules()
    const fresh = await import('./api')
    const fetchMock = respondWith(200, { username: 'felipe', available: true, reason: null })
    await fresh.auth.checkUsername('felipe')
    expect(fetchMock).toHaveBeenCalledTimes(1)
    expect((fetchMock.mock.calls[0] as unknown as Call)[0]).toBe('/api/auth/username/felipe')
  })

  it('a check that fails is an error, never an answer', async () => {
    respondWith(500, { detail: 'boom' })
    await expect(auth.checkUsername('felipe')).rejects.toMatchObject({ status: 500 })
  })

  it('sign-up carries the @ when given', async () => {
    const fetchMock = respondWith(200, { email: signup.email, expires_in_seconds: 900, resend_after_seconds: 60 })
    await auth.signupStart({ ...signup, username: 'felipe' })
    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toMatch(/\/api\/auth\/register\/start$/)
    expect(JSON.parse(init.body as string).username).toBe('felipe')
  })

  it('sign-up leaves the @ out when none is given', async () => {
    const fetchMock = respondWith(200, { email: signup.email, expires_in_seconds: 900, resend_after_seconds: 60 })
    await auth.signupStart(signup)
    const [, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(JSON.parse(init.body as string)).not.toHaveProperty('username')
  })

  it('a taken @ at sign-up is the server sentence', async () => {
    respondWith(409, { detail: 'Esse @ já tem dono. Tenta outro.' })
    await expect(auth.signupStart({ ...signup, username: 'felipe' })).rejects.toMatchObject({
      status: 409,
      message: 'Esse @ já tem dono. Tenta outro.',
    })
  })

  it('the profile update sends the @', async () => {
    const fetchMock = respondWith(200, { username: 'felipe' })
    await users.updateProfile({ username: 'felipe' })
    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toMatch(/\/api\/users\/me$/)
    expect(init.method).toBe('PATCH')
    expect(JSON.parse(init.body as string)).toEqual({ username: 'felipe' })
  })

  it('a fixed @ refused by the profile is the server sentence', async () => {
    respondWith(409, { detail: 'O @ é fixo: escolhido uma vez, não muda.' })
    await expect(users.updateProfile({ username: 'outro' })).rejects.toMatchObject({
      status: 409,
      message: 'O @ é fixo: escolhido uma vez, não muda.',
    })
  })
})
