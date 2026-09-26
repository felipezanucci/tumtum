import { describe, it, expect, vi, afterEach } from 'vitest'

import { ApiError, ConsentRequiredError, auth, consents, health, users } from './api'
import { CONSENT_TEXT_VERSION } from './consent-copy'

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

describe('the client header', () => {
  it('goes on every request', async () => {
    const fetchMock = respondWith(200, { text_version: CONSENT_TEXT_VERSION, consents: [] })
    await consents.get()
    const [, init] = fetchMock.mock.calls[0] as unknown as Call
    expect((init.headers as Record<string, string>)['X-Tumtum-Client']).toBe('web/site')
  })
})

describe('consent_required', () => {
  it('becomes a ConsentRequiredError carrying the purpose', async () => {
    respondWith(403, {
      detail: 'Pra guardar a noite, falta o seu sim.',
      code: 'consent_required',
      purpose: 'keep_night',
    })
    const err = await health.listSessions().catch((e: unknown) => e)
    expect(err).toBeInstanceOf(ConsentRequiredError)
    expect(err).toBeInstanceOf(ApiError)
    expect((err as ConsentRequiredError).purpose).toBe('keep_night')
    expect((err as ConsentRequiredError).status).toBe(403)
    expect((err as ConsentRequiredError).message).toBe('Pra guardar a noite, falta o seu sim.')
  })

  it('leaves any other 403 a plain ApiError', async () => {
    respondWith(403, { detail: 'Só operadores.' })
    const err = await health.listSessions().catch((e: unknown) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err).not.toBeInstanceOf(ConsentRequiredError)
  })
})

describe('what the consent and account calls send', () => {
  it('PUT carries the text version, the means and only the purposes given', async () => {
    const fetchMock = respondWith(200, { text_version: CONSENT_TEXT_VERSION, consents: [] })
    await consents.put({ keep_night: true }, 'tap')
    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toMatch(/\/api\/consents$/)
    expect(init.method).toBe('PUT')
    expect(JSON.parse(init.body as string)).toEqual({
      text_version: CONSENT_TEXT_VERSION,
      means: 'tap',
      purposes: { keep_night: true },
    })
  })

  it('deleting the account posts the password to the new route', async () => {
    const fetchMock = respondWith(204, null)
    await users.deleteAccount('segredo')
    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toMatch(/\/api\/users\/me\/delete$/)
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body as string)).toEqual({ password: 'segredo' })
  })

  it('an e-mail change answered with 202 and no body resolves', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => ({
        ok: true,
        status: 202,
        json: async () => {
          throw new SyntaxError('Unexpected end of JSON input')
        },
      })),
    )
    await expect(users.changeEmail('novo@exemplo.com', 'segredo')).resolves.toBeUndefined()
  })
})

describe('sign-up', () => {
  it('sends the birth date and the two separate yeses with the text version', async () => {
    const fetchMock = respondWith(200, { email: 'a@b.c', expires_in_seconds: 900, resend_after_seconds: 60 })
    await auth.signupStart({
      email: 'a@b.c',
      name: 'Ana',
      password: 'segredo123',
      birth_date: '1995-04-12',
      terms_accepted: true,
      read_heart_rate: false,
    })
    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toMatch(/\/api\/auth\/register\/start$/)
    expect(JSON.parse(init.body as string)).toEqual({
      email: 'a@b.c',
      name: 'Ana',
      password: 'segredo123',
      birth_date: '1995-04-12',
      terms_accepted: true,
      consent_text_version: CONSENT_TEXT_VERSION,
      read_heart_rate: false,
    })
  })
})
