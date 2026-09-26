import { describe, it, expect } from 'vitest'

import { eventImportWindow, formatImportWindow } from './import-window'

const at = (iso: string) => Date.parse(iso)

describe('eventImportWindow', () => {
  it('is the event with 30 minutes on each side, in São Paulo time', () => {
    const window = eventImportWindow({ date: '2026-09-25', start_time: '21:00:00', end_time: '23:30:00' })
    expect(window).toEqual({
      startMs: at('2026-09-25T20:30:00-03:00'),
      endMs: at('2026-09-26T00:00:00-03:00'),
    })
  })

  it('ignores the meaningless offset a time column carries', () => {
    const window = eventImportWindow({
      date: '2026-09-25',
      start_time: '21:00:00+00:00',
      end_time: '23:00:00+00:00',
    })
    expect(window?.startMs).toBe(at('2026-09-25T20:30:00-03:00'))
  })

  it('ends on the next day when the show crosses midnight', () => {
    const window = eventImportWindow({ date: '2026-09-25', start_time: '22:00', end_time: '01:00' })
    expect(window?.endMs).toBe(at('2026-09-26T01:30:00-03:00'))
  })

  it('has no window without a start or an end — and then nothing is sent', () => {
    expect(eventImportWindow({ date: '2026-09-25', start_time: null, end_time: '23:00' })).toBeNull()
    expect(eventImportWindow({ date: '2026-09-25', start_time: '21:00', end_time: null })).toBeNull()
    expect(eventImportWindow({ date: 'amanhã', start_time: '21:00', end_time: '23:00' })).toBeNull()
  })

  it('reads as the two São Paulo clock times', () => {
    const window = eventImportWindow({ date: '2026-09-25', start_time: '21:00', end_time: '23:00' })
    expect(window && formatImportWindow(window)).toBe('20:30 → 23:30')
  })
})
