import { afterEach, describe, expect, it, vi } from 'vitest'

import { clearTokens, feed, storeTokens, users, type SharingOverview } from './api'

type Call = [string, RequestInit]

const OVERVIEW: SharingOverview = {
  operators: [{ name: 'Railway', role: 'operador', what: 'Banco de dados', why: 'Hospedar o servidor', where: '[a confirmar]' }],
  never: ['clubes', 'artistas', 'produtoras', 'festivais', 'anunciantes'],
  published_cards: [],
  feed_posts: [],
  shares: [],
  anpd: { url: 'https://www.gov.br/anpd/pt-br/canais_atendimento/cidadao-titular-de-dados', note: 'Canal do titular.' },
}

afterEach(() => {
  clearTokens()
  vi.unstubAllGlobals()
})

describe('users.sharing()', () => {
  it('asks GET /api/users/me/sharing with the bearer token and returns the body', async () => {
    const fetchMock = vi.fn(async (..._args: unknown[]) => ({
      ok: true,
      status: 200,
      json: async () => OVERVIEW,
    }))
    vi.stubGlobal('fetch', fetchMock)
    storeTokens({ access_token: 'token-abc' })

    const result = await users.sharing()

    expect(fetchMock).toHaveBeenCalledTimes(1)
    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toMatch(/\/api\/users\/me\/sharing$/)
    expect(init.method ?? 'GET').toBe('GET')
    const headers = init.headers as Record<string, string>
    expect(headers.Authorization).toBe('Bearer token-abc')
    expect(headers['X-Tumtum-Client']).toBe('web/site')
    expect(result).toEqual(OVERVIEW)
  })
})

describe('feed.deletePost()', () => {
  it('sends DELETE /api/events/{event}/feed/{post} with the bearer token and takes the 204', async () => {
    const fetchMock = vi.fn(async (..._args: unknown[]) => ({
      ok: true,
      status: 204,
      json: async () => {
        throw new Error('a 204 has no body')
      },
    }))
    vi.stubGlobal('fetch', fetchMock)
    storeTokens({ access_token: 'token-abc' })

    await expect(feed.deletePost('event-1', 'post-2')).resolves.toBeUndefined()

    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toMatch(/\/api\/events\/event-1\/feed\/post-2$/)
    expect(init.method).toBe('DELETE')
    expect((init.headers as Record<string, string>).Authorization).toBe('Bearer token-abc')
  })
})
