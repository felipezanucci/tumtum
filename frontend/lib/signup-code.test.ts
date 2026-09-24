import { describe, it, expect, vi, afterEach } from 'vitest'
import { codeDigits, isCompleteCode, secondsUntil } from './signup-code'
import { auth } from './api'

describe('the sign-up code field (#64)', () => {
  it('keeps six digits and nothing else', () => {
    expect(codeDigits('123 456')).toBe('123456')
    expect(codeDigits('123-456')).toBe('123456')
    expect(codeDigits('1234567')).toBe('123456')
    expect(codeDigits('012345')).toBe('012345')
    expect(codeDigits('abc')).toBe('')
  })

  it('keeps the code from a pasted subject line', () => {
    expect(codeDigits('390875 é seu código da TumTum')).toBe('390875')
  })

  it('takes only six ASCII digits as a code', () => {
    expect(isCompleteCode('012345')).toBe(true)
    expect(isCompleteCode('12345')).toBe(false)
    expect(isCompleteCode('１２３４５６')).toBe(false)
  })

  it('counts the wait to ask again up, never below zero', () => {
    expect(secondsUntil(60_000, 0)).toBe(60)
    expect(secondsUntil(60_000, 59_500)).toBe(1)
    expect(secondsUntil(60_000, 60_000)).toBe(0)
    expect(secondsUntil(60_000, 90_000)).toBe(0)
  })
})

describe('a refusal that is a list, not a sentence', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('becomes a sentence instead of reaching the screen as an object', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => ({
        ok: false,
        status: 422,
        json: async () => ({ detail: [{ loc: ['body', 'email'], msg: 'value is not a valid email address' }] }),
      })),
    )
    await expect(auth.signupStart('a@b', 'Ana', 'segredo123')).rejects.toThrow(
      'O servidor não aceitou esses dados',
    )
  })
})
