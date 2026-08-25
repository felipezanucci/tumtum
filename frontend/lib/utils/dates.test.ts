import { describe, it, expect } from 'vitest'
import { formatDateOnly, parseDateOnly } from './dates'

describe('formatDateOnly', () => {
  it('keeps the day it was given, west of Greenwich', () => {
    // The bug: new Date('2026-08-29') is midnight UTC, which in São Paulo is
    // the evening of the 28th. The Realness Festival moved a day on screen.
    expect(formatDateOnly('2026-08-29')).toBe('29 de agosto de 2026')
  })

  it('keeps the day for the first of a month, where an offset would cross into the previous one', () => {
    expect(formatDateOnly('2026-01-01')).toBe('1 de janeiro de 2026')
  })

  it('accepts a full timestamp by reading only the date part', () => {
    expect(formatDateOnly('2026-08-29T00:00:00Z')).toBe('29 de agosto de 2026')
  })

  it('honours the format asked for', () => {
    expect(formatDateOnly('2026-08-29', { day: '2-digit', month: '2-digit', year: 'numeric' })).toBe(
      '29/08/2026',
    )
  })

  it('shows the raw value rather than "Invalid Date" for something unexpected', () => {
    expect(formatDateOnly('depois de amanhã')).toBe('depois de amanhã')
    expect(formatDateOnly('')).toBe('')
  })
})

describe('parseDateOnly', () => {
  it('builds the day in local time, not UTC', () => {
    const date = parseDateOnly('2026-08-29')!
    expect(date.getFullYear()).toBe(2026)
    expect(date.getMonth()).toBe(7)
    expect(date.getDate()).toBe(29)
    expect(date.getHours()).toBe(0)
  })

  it('returns null for a value that is not a date', () => {
    expect(parseDateOnly('29/08/2026')).toBeNull()
  })
})
