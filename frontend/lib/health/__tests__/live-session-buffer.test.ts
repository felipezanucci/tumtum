import { beforeEach, describe, expect, it } from 'vitest'
import { clearSnapshot, loadSnapshot, saveSnapshot } from '../live-session-buffer'
import type { HRSample } from '../import-parsers'

const KEY = 'tumtum:live-session'
let store: Record<string, string> = {}

beforeEach(() => {
  store = {}
  ;(globalThis as unknown as { window: unknown }).window = {
    localStorage: {
      getItem: (k: string) => store[k] ?? null,
      setItem: (k: string, v: string) => {
        store[k] = v
      },
      removeItem: (k: string) => {
        delete store[k]
      },
    },
  }
})

const T0 = Date.parse('2026-08-26T10:00:00.000Z')

/** A strap notifies at roughly 1 Hz, but never on exact second boundaries. */
function jitteredCapture(n: number, seed = 7): HRSample[] {
  let s = seed
  const rnd = () => (s = (s * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff
  const out: HRSample[] = []
  let t = T0
  for (let i = 0; i < n; i++) {
    out.push({ time: new Date(t).toISOString(), bpm: 70 + Math.round(rnd() * 75) })
    t += 1085 + (rnd() - 0.5) * 300
  }
  return out
}

describe('live session snapshot', () => {
  it('returns null when there is nothing stored', () => {
    expect(loadSnapshot()).toBeNull()
  })

  it('round-trips a capture without losing or shifting a single reading', () => {
    const samples = jitteredCapture(306)
    saveSnapshot({ startedAt: samples[0].time, deviceName: 'Polar H10', eventId: null, samples })

    const back = loadSnapshot()
    expect(back).not.toBeNull()
    expect(back!.samples).toEqual(samples)
    expect(back!.deviceName).toBe('Polar H10')
  })

  it('keeps every timestamp distinct', () => {
    // Regression: offsets were stored as whole seconds, so two readings under a
    // second apart collapsed onto one timestamp. hr_data is keyed by
    // (time, session_id), so the duplicate made the backend reject the entire
    // session — a whole concert lost to a rounding boundary.
    const samples = jitteredCapture(306)
    saveSnapshot({ startedAt: samples[0].time, deviceName: null, eventId: null, samples })

    const times = loadSnapshot()!.samples.map((s) => s.time)
    expect(new Set(times).size).toBe(times.length)
  })

  it('still reads a v1 payload, dropping only the collisions', () => {
    // A capture taken before the fix must stay saveable: losing two readings
    // beats losing all 306.
    const samples = jitteredCapture(306)
    const offsets = samples.map((s) => Math.round((Date.parse(s.time) - T0) / 1000))
    const collisions = offsets.length - new Set(offsets).size
    expect(collisions).toBeGreaterThan(0) // the payload really is degenerate

    store[KEY] = JSON.stringify({
      v: 1,
      startedAt: samples[0].time,
      deviceName: 'Polar H10',
      eventId: null,
      t0: T0,
      offsets,
      bpms: samples.map((s) => s.bpm),
    })

    const back = loadSnapshot()!
    expect(back.samples).toHaveLength(samples.length - collisions)
    const times = back.samples.map((s) => s.time)
    expect(new Set(times).size).toBe(times.length)
  })

  it('rejects a payload from a schema newer than it understands', () => {
    store[KEY] = JSON.stringify({ v: 99, t0: T0, offsetsMs: [0], bpms: [70] })
    expect(loadSnapshot()).toBeNull()
  })

  it('survives corrupted storage rather than throwing', () => {
    store[KEY] = 'not json'
    expect(loadSnapshot()).toBeNull()
  })

  it('stores nothing for an empty capture, and clears on demand', () => {
    saveSnapshot({ startedAt: new Date(T0).toISOString(), deviceName: null, eventId: null, samples: [] })
    expect(loadSnapshot()).toBeNull()

    const samples = jitteredCapture(5)
    saveSnapshot({ startedAt: samples[0].time, deviceName: null, eventId: null, samples })
    expect(loadSnapshot()).not.toBeNull()
    clearSnapshot()
    expect(loadSnapshot()).toBeNull()
  })
})
