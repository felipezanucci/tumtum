/**
 * The window an imported file may cover (26/09, LGPD audit B1).
 *
 * The import used to send the whole file when no event was chosen — a day or
 * a week of someone's heart, uploaded for a page that promises to read "only
 * the event you chose". Now an event is required, and the window is the
 * event's own hours with 30 minutes on each side: the same margin the
 * Android app reads from Health Connect. Nothing outside it leaves the
 * browser.
 *
 * An event's `start_time` and `end_time` are wall-clock times in São Paulo
 * (the operator types them that way; any offset the column carries is
 * meaningless, see `offset_aware` in the backend). Brazil has had no daylight
 * saving since 2019, so the offset is fixed.
 */

export const IMPORT_MARGIN_MINUTES = 30

const SAO_PAULO_OFFSET = '-03:00'

export interface EventHours {
  date: string
  start_time: string | null
  end_time: string | null
}

export interface ImportWindow {
  startMs: number
  endMs: number
}

function clockOf(value: string | null): string | null {
  if (!value) return null
  const match = /^(\d{2}):(\d{2})(?::(\d{2}))?/.exec(value.trim())
  if (!match) return null
  return `${match[1]}:${match[2]}:${match[3] ?? '00'}`
}

/**
 * The instants an import may cover for this event, or null when the event
 * has no start or no end — then there is no window to promise, and nothing
 * is sent.
 */
export function eventImportWindow(event: EventHours): ImportWindow | null {
  const day = /^\d{4}-\d{2}-\d{2}/.exec(event.date)?.[0]
  const start = clockOf(event.start_time)
  const end = clockOf(event.end_time)
  if (!day || !start || !end) return null

  const startMs = Date.parse(`${day}T${start}${SAO_PAULO_OFFSET}`)
  let endMs = Date.parse(`${day}T${end}${SAO_PAULO_OFFSET}`)
  if (Number.isNaN(startMs) || Number.isNaN(endMs)) return null
  // A show from 21h to 1h ends on the next day.
  if (endMs <= startMs) endMs += 24 * 60 * 60 * 1000

  const margin = IMPORT_MARGIN_MINUTES * 60 * 1000
  return { startMs: startMs - margin, endMs: endMs + margin }
}

/** "21:30 → 01:30", in São Paulo time, for the line that says what is sent. */
export function formatImportWindow(window: ImportWindow): string {
  const fmt = (ms: number) =>
    new Date(ms).toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
      timeZone: 'America/Sao_Paulo',
    })
  return `${fmt(window.startMs)} → ${fmt(window.endMs)}`
}
