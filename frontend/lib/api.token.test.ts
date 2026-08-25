import { describe, it, expect } from 'vitest'
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
