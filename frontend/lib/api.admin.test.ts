import { afterEach, describe, expect, it, vi } from 'vitest'

import { admin, clearTokens, storeTokens, type AdminDataSubjectRequest } from './api'

type Call = [string, RequestInit]

const REQUEST: AdminDataSubjectRequest = {
  id: 'req-1',
  kind: 'deletion',
  message: 'Apaguem minha noite de sábado.',
  status: 'open',
  opened_at: '2026-09-20T12:00:00Z',
  due_at: '2026-10-05T12:00:00Z',
  answered_at: null,
  answer: null,
  user_id: 'user-1',
  user_email: 'ana@exemplo.com',
  user_name: 'Ana',
}

function respond(body: unknown, status = 200) {
  const fetchMock = vi.fn(async (..._args: unknown[]) => ({
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  }))
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

afterEach(() => {
  clearTokens()
  vi.unstubAllGlobals()
})

describe('admin.requests.list()', () => {
  it('asks GET /api/admin/requests with the bearer token and returns the queue', async () => {
    const fetchMock = respond([REQUEST])
    storeTokens({ access_token: 'token-op' })

    await expect(admin.requests.list()).resolves.toEqual([REQUEST])

    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toMatch(/\/api\/admin\/requests$/)
    expect(init.method ?? 'GET').toBe('GET')
    expect((init.headers as Record<string, string>).Authorization).toBe('Bearer token-op')
  })
})

describe('admin.requests.answer()', () => {
  it('sends PATCH /api/admin/requests/{id} with the status and the answer', async () => {
    const answered = { ...REQUEST, status: 'answered', answer: 'Feito.', answered_at: '2026-09-28T12:00:00Z' }
    const fetchMock = respond(answered)
    storeTokens({ access_token: 'token-op' })

    await expect(admin.requests.answer('req-1', { status: 'answered', answer: 'Feito.' })).resolves.toEqual(answered)

    const [url, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(url).toMatch(/\/api\/admin\/requests\/req-1$/)
    expect(init.method).toBe('PATCH')
    expect(JSON.parse(String(init.body))).toEqual({ status: 'answered', answer: 'Feito.' })
  })

  it('leaves the answer out when closing or reopening without one', async () => {
    const fetchMock = respond({ ...REQUEST, status: 'closed' })
    storeTokens({ access_token: 'token-op' })

    await admin.requests.answer('req-1', { status: 'closed' })

    const [, init] = fetchMock.mock.calls[0] as unknown as Call
    expect(JSON.parse(String(init.body))).toEqual({ status: 'closed' })
  })
})
