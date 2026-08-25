import { describe, it, expect, vi, afterEach } from 'vitest'
import { ApiError, auth } from './api'

function respondWith(status: number, body: unknown) {
  vi.stubGlobal(
    'fetch',
    vi.fn(async () => ({
      ok: status >= 200 && status < 300,
      status,
      json: async () => body,
    })),
  )
}

afterEach(() => vi.unstubAllGlobals())

describe('401 handling', () => {
  it('keeps the reason the server gave for a wrong email or password', async () => {
    // Mistyping one letter of an email is a 401, and used to be reported as an
    // expired session — telling someone to sign in again, which is what they
    // were already doing.
    respondWith(401, { detail: 'Email ou senha incorretos' })
    await expect(auth.login('felipe@exemplo.com', 'x')).rejects.toThrow(
      'Email ou senha incorretos',
    )
  })

  it('replaces FastAPI\'s English default, which explains nothing', async () => {
    respondWith(401, { detail: 'Not authenticated' })
    await expect(auth.me()).rejects.toThrow('Sua sessão expirou')
  })

  it('replaces it whatever the casing or padding', async () => {
    respondWith(401, { detail: '  NOT AUTHENTICATED  ' })
    await expect(auth.me()).rejects.toThrow('Sua sessão expirou')
  })

  it('falls back to the session message when the body carries no reason', async () => {
    respondWith(401, {})
    await expect(auth.me()).rejects.toThrow('Sua sessão expirou')
  })

  it('still reports 401 as the status, whichever message it carries', async () => {
    respondWith(401, { detail: 'Email ou senha incorretos' })
    await auth.login('a@b.c', 'x').catch((err: unknown) => {
      expect(err).toBeInstanceOf(ApiError)
      expect((err as ApiError).status).toBe(401)
    })
  })

  it('leaves other failures alone', async () => {
    respondWith(409, { detail: 'Email já cadastrado' })
    await expect(auth.register('a@b.c', 'A', 'x')).rejects.toThrow('Email já cadastrado')
  })
})
