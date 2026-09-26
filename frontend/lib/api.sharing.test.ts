import { afterEach, describe, expect, it, vi } from 'vitest'

import { clearTokens, storeTokens, users, type SharingOverview } from './api'

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
