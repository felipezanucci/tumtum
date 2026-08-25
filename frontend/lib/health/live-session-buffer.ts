/**
 * Crash-recovery buffer for a live capture session.
 *
 * A two-hour event is a long time to trust a browser tab. If it is reloaded,
 * backgrounded into oblivion, or crashes, the readings collected so far should
 * survive. Samples are stored compactly (a base timestamp plus per-sample
 * second offsets) so a four-hour session at 1 Hz stays well inside the
 * localStorage budget.
 */

import type { HRSample } from './import-parsers'

const STORAGE_KEY = 'tumtum:live-session'
const SCHEMA_VERSION = 1

export interface LiveSessionSnapshot {
  startedAt: string
  deviceName: string | null
  eventId: string | null
  samples: HRSample[]
}

interface StoredSnapshot {
  v: number
  startedAt: string
  deviceName: string | null
  eventId: string | null
  t0: number
  /** Whole seconds elapsed since t0, one per sample. */
  offsets: number[]
  bpms: number[]
}

/** Persist the session. Safe to call often; callers should still throttle. */
export function saveSnapshot(snapshot: LiveSessionSnapshot): void {
  if (typeof window === 'undefined' || snapshot.samples.length === 0) return

  const t0 = Date.parse(snapshot.samples[0].time)
  const stored: StoredSnapshot = {
    v: SCHEMA_VERSION,
    startedAt: snapshot.startedAt,
    deviceName: snapshot.deviceName,
    eventId: snapshot.eventId,
    t0,
    offsets: snapshot.samples.map((s) => Math.round((Date.parse(s.time) - t0) / 1000)),
    bpms: snapshot.samples.map((s) => s.bpm),
  }

  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(stored))
  } catch {
    // Quota exceeded or storage disabled: the in-memory session still stands,
    // so losing the backup must not interrupt the capture.
  }
}

/** Recover a previous session, or null when there is nothing usable. */
export function loadSnapshot(): LiveSessionSnapshot | null {
  if (typeof window === 'undefined') return null

  let raw: string | null
  try {
    raw = window.localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
  if (!raw) return null

  try {
    const stored = JSON.parse(raw) as StoredSnapshot
    if (stored.v !== SCHEMA_VERSION || !Array.isArray(stored.bpms)) return null
    if (stored.bpms.length === 0) return null

    const samples: HRSample[] = stored.bpms.map((bpm, i) => ({
      time: new Date(stored.t0 + (stored.offsets[i] ?? i) * 1000).toISOString(),
      bpm,
    }))
    return {
      startedAt: stored.startedAt,
      deviceName: stored.deviceName,
      eventId: stored.eventId,
      samples,
    }
  } catch {
    return null
  }
}

export function clearSnapshot(): void {
  if (typeof window === 'undefined') return
  try {
    window.localStorage.removeItem(STORAGE_KEY)
  } catch {
    // Nothing to do: a stale snapshot is harmless, the user can discard it.
  }
}
