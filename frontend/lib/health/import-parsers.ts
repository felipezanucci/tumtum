/**
 * Heart rate file import parsers.
 *
 * Phase 0 has no cloud path for Android HR data: Health Connect is an on-device
 * API with no REST surface, and the Google Fit REST API is being turned down.
 * Until a native bridge exists, the only way to get REAL heart rate data from an
 * Android device into TumTum is a file export from the vendor app.
 *
 * These parsers are deliberately format-tolerant because every wearable vendor
 * exports something slightly different. Supported inputs:
 *
 * - Apple Health XML export (export.xml)
 * - Health Connect JSON export
 * - Samsung Health CSV (com.samsung.shealth.tracker.heart_rate.csv)
 * - Generic CSV/TSV with a timestamp column and a BPM column
 * - Generic JSON containing objects with timestamp + BPM fields
 */

import { parseHealthKitExport } from './apple-health'

/** Physiological bounds — matches the backend HRDataPointInput constraints. */
export const MIN_VALID_BPM = 30
export const MAX_VALID_BPM = 250

export interface HRSample {
  /** ISO 8601 timestamp (UTC). */
  time: string
  bpm: number
}

export type ImportFormat =
  | 'apple_health_xml'
  | 'health_connect_json'
  | 'samsung_health_csv'
  | 'generic_csv'
  | 'generic_json'

export interface ParseResult {
  format: ImportFormat
  samples: HRSample[]
  /** Rows that were recognised but rejected (bad timestamp or out-of-range BPM). */
  discarded: number
  warnings: string[]
}

export class ImportParseError extends Error {}

/** Header names that identify a timestamp column, in priority order. */
const TIME_KEYS = [
  'time',
  'timestamp',
  'start_time',
  'starttime',
  'startdate',
  'start_date',
  'datetime',
  'date_time',
  'date',
  'data',
  'hora',
  'horario',
  'data_hora',
  'measurement_time',
  'measure_time',
  'record_time',
  'collect_time',
  'create_time',
  'test_time',
  'local_time',
  'utc_time',
  'end_time',
].map(canonicaliseKey)

/** Header names that identify a BPM column, in priority order. */
const BPM_KEYS = [
  'bpm',
  'heart_rate',
  'heart_rate_bpm',
  'heartrate',
  'heart rate',
  'beats_per_minute',
  'beatsperminute',
  'frequencia_cardiaca',
  'ritmo_cardiaco',
  'batimentos',
  'batimento',
  'pulse',
  'pulso',
  'hr',
  'fc',
  'value',
].map(canonicaliseKey)

/** Remove accents so "Frequência cardíaca" matches "frequencia_cardiaca". */
function stripDiacritics(value: string): string {
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '')
}

/**
 * Reduce a header cell to a comparable key: strip quotes, whitespace, BOM,
 * accents and vendor namespace prefixes, then collapse punctuation to
 * underscores. "com.samsung.health.heart_rate.heart_rate" becomes "heart_rate"
 * and "Frequência cardíaca" becomes "frequencia_cardiaca".
 */
function canonicaliseKey(key: string): string {
  const cleaned = stripDiacritics(
    key
      .replace(/^\ufeff/, '')
      .trim()
      .replace(/^["']|["']$/g, '')
      .trim()
      .toLowerCase(),
  )
  // Strip the namespace before collapsing dots, so a Samsung start_time column
  // is not mistaken for a heart_rate column by its prefix.
  const lastSegment = cleaned.split('.').pop() ?? cleaned
  const base = lastSegment.length > 0 ? lastSegment : cleaned
  return base.replace(/[^a-z0-9]+/g, '_').replace(/^_+|_+$/g, '')
}

/**
 * Pick the index of the first column whose normalised header matches a candidate.
 * Exact matches win over substring matches so "heart_rate" beats "heart_rate_max".
 */
function findColumn(headers: string[], candidates: string[]): number {
  const normalised = headers.map(canonicaliseKey)
  for (const candidate of candidates) {
    const exact = normalised.indexOf(candidate)
    if (exact !== -1) return exact
  }
  for (const candidate of candidates) {
    const partial = normalised.findIndex((h) => h.includes(candidate))
    if (partial !== -1) return partial
  }
  return -1
}

/**
 * Parse a timestamp that may be an ISO string, a "YYYY-MM-DD HH:mm:ss" string,
 * or a Unix epoch in seconds or milliseconds.
 *
 * Returns epoch milliseconds, or null when the value is not a usable timestamp.
 */
export function parseTimestamp(raw: string | number): number | null {
  if (typeof raw === 'number') return epochToMillis(raw)

  const value = raw.trim().replace(/^["']|["']$/g, '')
  if (!value) return null

  if (/^-?\d+(\.\d+)?$/.test(value)) {
    return epochToMillis(Number(value))
  }

  const direct = Date.parse(value)
  if (!Number.isNaN(direct)) return direct

  // "2026-08-24 21:30:00" — valid in most engines but not guaranteed by the spec.
  const spaceSeparated = Date.parse(value.replace(' ', 'T'))
  if (!Number.isNaN(spaceSeparated)) return spaceSeparated

  return null
}

/** Disambiguate epoch seconds from epoch milliseconds. */
function epochToMillis(value: number): number | null {
  if (!Number.isFinite(value) || value <= 0) return null
  // ~1e11 ms is 1973; anything smaller is far more likely to be seconds.
  return value > 1e11 ? value : value * 1000
}

/** Round and range-check a BPM value. Returns null when unusable. */
function normaliseBpm(raw: string | number): number | null {
  const value = typeof raw === 'number' ? raw : Number(String(raw).trim().replace(/^["']|["']$/g, ''))
  if (!Number.isFinite(value)) return null
  const rounded = Math.round(value)
  if (rounded < MIN_VALID_BPM || rounded > MAX_VALID_BPM) return null
  return rounded
}

/** Split a CSV/TSV line, honouring double-quoted cells. */
function splitRow(line: string, delimiter: string): string[] {
  const cells: string[] = []
  let current = ''
  let inQuotes = false

  for (let i = 0; i < line.length; i += 1) {
    const char = line[i]
    if (char === '"') {
      if (inQuotes && line[i + 1] === '"') {
        current += '"'
        i += 1
      } else {
        inQuotes = !inQuotes
      }
    } else if (char === delimiter && !inQuotes) {
      cells.push(current)
      current = ''
    } else {
      current += char
    }
  }
  cells.push(current)
  return cells
}

/** Guess the delimiter from the most frequent separator in the header row. */
function detectDelimiter(line: string): string {
  const counts = [
    { delimiter: ',', count: (line.match(/,/g) ?? []).length },
    { delimiter: ';', count: (line.match(/;/g) ?? []).length },
    { delimiter: '\t', count: (line.match(/\t/g) ?? []).length },
  ]
  counts.sort((a, b) => b.count - a.count)
  return counts[0].count > 0 ? counts[0].delimiter : ','
}

/** How far into a file a real sample header might still be hiding. */
const MAX_PREAMBLE_ROWS = 12

/** Column names that mean "the session began at", not "the session lasted". */
const ORIGIN_TIME_KEYS = [
  'start_time',
  'starttime',
  'hora_de_inicio',
  'horario_de_inicio',
  'hora_inicio',
]

/** "01:23:45", "23:45", "1:23:45.500" — an offset from the start, not a clock time. */
function parseElapsedSeconds(raw: string): number | null {
  const value = raw.trim().replace(/^["']|["']$/g, '')
  const match = /^(?:(\d+):)?(\d{1,2}):(\d{1,2}(?:[.,]\d+)?)$/.exec(value)
  if (!match) return null
  const hours = match[1] ? Number(match[1]) : 0
  const minutes = Number(match[2])
  const seconds = Number(match[3].replace(',', '.'))
  if (minutes > 59 || seconds >= 60) return null
  return hours * 3600 + minutes * 60 + seconds
}

/**
 * Find the moment an export's elapsed timestamps are counted from.
 *
 * Polar Flow, and every tool shaped like it, writes a metadata block above the
 * samples — a date on one row, a start time on another — and then counts the
 * samples from zero. Without that anchor the readings have a shape but no place
 * on the calendar, which is exactly what correlating them to an event needs.
 */
function findElapsedOrigin(
  preamble: string[][],
): { millis: number; ambiguousDate: boolean } | null {
  let date: { year: number; month: number; day: number; ambiguous: boolean } | null = null
  let timeOfDay: number | null = null

  // Read by column name where the preamble names its columns. A Polar file
  // carries both "Start time" (15:55:59) and "Duration" (00:02:31), and both
  // look exactly like a clock — taking whichever appears first only works
  // while the vendor keeps that column order.
  for (let i = 0; i + 1 < preamble.length; i += 1) {
    const at = findColumn(preamble[i], ORIGIN_TIME_KEYS)
    if (at === -1) continue
    const clock = /^(\d{1,2}):(\d{2})(?::(\d{2}))?$/.exec(
      (preamble[i + 1][at] ?? '').trim().replace(/^["']|["']$/g, ''),
    )
    if (clock && +clock[1] <= 23) {
      timeOfDay = +clock[1] * 3600 + +clock[2] * 60 + (clock[3] ? +clock[3] : 0)
      break
    }
  }

  for (const row of preamble) {
    for (const cell of row) {
      const value = cell.trim().replace(/^["']|["']$/g, '')
      if (!value) continue

      if (!date) {
        const iso = /^(\d{4})[-/](\d{1,2})[-/](\d{1,2})$/.exec(value)
        if (iso) {
          date = { year: +iso[1], month: +iso[2], day: +iso[3], ambiguous: false }
          continue
        }
        // "29-08-2026" or "08/29/2026": the day and the month can only be told
        // apart when one of them is too large to be a month.
        const parts = /^(\d{1,2})[-/](\d{1,2})[-/](\d{4})$/.exec(value)
        if (parts) {
          const first = +parts[1]
          const second = +parts[2]
          const dayFirst = first > 12 || second <= 12
          date = {
            year: +parts[3],
            month: dayFirst ? second : first,
            day: dayFirst ? first : second,
            ambiguous: first <= 12 && second <= 12,
          }
          continue
        }
      }

      if (timeOfDay === null) {
        const clock = /^(\d{1,2}):(\d{2})(?::(\d{2}))?$/.exec(value)
        if (clock && +clock[1] <= 23) {
          timeOfDay = +clock[1] * 3600 + +clock[2] * 60 + (clock[3] ? +clock[3] : 0)
        }
      }
    }
  }

  if (!date) return null
  // Local time: an export written by a phone in São Paulo means São Paulo.
  const start = new Date(date.year, date.month - 1, date.day, 0, 0, 0, 0)
  if (Number.isNaN(start.getTime())) return null
  return {
    millis: start.getTime() + (timeOfDay ?? 0) * 1000,
    ambiguousDate: date.ambiguous,
  }
}

interface HeaderAttempt {
  headerIndex: number
  headers: string[]
  timeIndex: number
  bpmIndex: number
  samples: HRSample[]
  discarded: number
  longestRun: number
  elapsed: boolean
  ambiguousDate: boolean
}

/** Read every row below `headerIndex` using the given columns. */
function readRows(
  lines: string[],
  delimiter: string,
  headerIndex: number,
  timeIndex: number,
  bpmIndex: number,
  origin: number | null,
): { samples: HRSample[]; discarded: number; longestRun: number } {
  const samples: HRSample[] = []
  let discarded = 0
  // Readings are written one after another; a summary block is one row and
  // then something else. The longest unbroken run tells the two apart.
  let longestRun = 0
  let run = 0

  for (let i = headerIndex + 1; i < lines.length; i += 1) {
    const cells = splitRow(lines[i], delimiter)
    if (cells.length <= Math.max(timeIndex, bpmIndex)) {
      discarded += 1
      run = 0
      continue
    }

    const bpm = normaliseBpm(cells[bpmIndex])
    const millis =
      origin === null
        ? parseTimestamp(cells[timeIndex])
        : (() => {
            const elapsed = parseElapsedSeconds(cells[timeIndex])
            return elapsed === null ? null : origin + elapsed * 1000
          })()

    if (millis === null || bpm === null) {
      discarded += 1
      run = 0
      continue
    }

    samples.push({ time: new Date(millis).toISOString(), bpm })
    run += 1
    longestRun = Math.max(longestRun, run)
  }

  return { samples, discarded, longestRun }
}

function parseCsv(content: string): ParseResult {
  const lines = content
    .split(/\r?\n/)
    .filter((line) => line.trim().length > 0)

  if (lines.length < 2) {
    throw new ImportParseError('O arquivo CSV não tem linhas de dados suficientes.')
  }

  const delimiter = detectDelimiter(lines[0])

  // A header row is not the right header row just because its labels look
  // right. A Polar Flow export opens with a summary block whose columns are
  // named "Start time" and "Average heart rate (bpm)" — convincing enough to
  // match on, and followed by a single row of totals rather than samples. So
  // every candidate is judged by what it actually yields, and the one that
  // produces the most readings wins.
  const attempts: HeaderAttempt[] = []
  const limit = Math.min(MAX_PREAMBLE_ROWS, lines.length - 1)

  for (let headerIndex = 0; headerIndex < limit; headerIndex += 1) {
    const headers = splitRow(lines[headerIndex], delimiter)
    const timeIndex = findColumn(headers, TIME_KEYS)
    const bpmIndex = findColumn(headers, BPM_KEYS)
    if (timeIndex === -1 || bpmIndex === -1) continue

    const absolute = readRows(lines, delimiter, headerIndex, timeIndex, bpmIndex, null)
    attempts.push({
      headerIndex,
      headers,
      timeIndex,
      bpmIndex,
      ...absolute,
      elapsed: false,
      ambiguousDate: false,
    })

    // Timestamps counted from zero need the start moment from the block above.
    if (absolute.samples.length === 0) {
      const preamble = lines
        .slice(0, headerIndex)
        .map((line) => splitRow(line, delimiter))
      const origin = findElapsedOrigin(preamble)
      if (origin) {
        const relative = readRows(
          lines,
          delimiter,
          headerIndex,
          timeIndex,
          bpmIndex,
          origin.millis,
        )
        attempts.push({
          headerIndex,
          headers,
          timeIndex,
          bpmIndex,
          ...relative,
          elapsed: true,
          ambiguousDate: origin.ambiguousDate,
        })
      }
    }
  }

  // Longest unbroken run first, then the deeper header: a preamble sits above
  // its data, so when two candidates read equally well the lower one is the
  // one describing the samples.
  const best = attempts.reduce<HeaderAttempt | null>((winner, attempt) => {
    if (winner === null) return attempt
    if (attempt.longestRun !== winner.longestRun) {
      return attempt.longestRun > winner.longestRun ? attempt : winner
    }
    return attempt.headerIndex > winner.headerIndex ? attempt : winner
  }, null)

  if (!best || best.samples.length === 0) {
    const looked = attempts.length
      ? attempts
          .map((a) => `linha ${a.headerIndex + 1}: ${a.headers.map(canonicaliseKey).join(', ')}`)
          .join(' | ')
      : splitRow(lines[0], delimiter).map(canonicaliseKey).join(', ')
    throw new ImportParseError(
      'Não encontrei colunas de horário e batimentos com dados no CSV. ' +
        `Colunas lidas: ${looked}`,
    )
  }

  const isSamsung = best.headerIndex > 0 && lines[0].toLowerCase().includes('samsung')
  const format: ImportFormat = isSamsung ? 'samsung_health_csv' : 'generic_csv'
  const warnings: string[] = [
    `Colunas usadas: horário = "${best.headers[best.timeIndex].trim()}", ` +
      `batimentos = "${best.headers[best.bpmIndex].trim()}".`,
  ]
  if (best.elapsed) {
    warnings.push(
      'Os horários do arquivo são contados a partir do início, então usei a data ' +
        'e a hora de início do cabeçalho para situá-los. Confira o horário abaixo.',
    )
  }
  if (best.ambiguousDate) {
    warnings.push(
      'A data do arquivo pode ser dia/mês ou mês/dia — não dá para saber pelo ' +
        'arquivo. Se o dia estiver errado, corrija o período antes de enviar.',
    )
  }

  return { format, samples: best.samples, discarded: best.discarded, warnings }
}

/**
 * Walk an arbitrary JSON tree and collect every object that carries both a
 * timestamp-ish field and a BPM-ish field.
 *
 * Vendor JSON exports nest their samples differently (Health Connect wraps them
 * under records[].samples[], others use a flat array), so rather than encode
 * every shape we search for the leaf objects that actually hold a reading.
 */
function collectJsonSamples(
  node: unknown,
  samples: HRSample[],
  counters: { discarded: number; nodes: number },
  inheritedTime: number | null = null,
): void {
  // Guard against pathological files locking up the browser tab.
  if (counters.nodes > 2_000_000) return

  if (Array.isArray(node)) {
    for (const child of node) {
      counters.nodes += 1
      collectJsonSamples(child, samples, counters, inheritedTime)
    }
    return
  }

  if (node === null || typeof node !== 'object') return

  const record = node as Record<string, unknown>
  const entries = Object.entries(record)

  let timeValue: number | null = null
  let bpmValue: number | null = null

  for (const key of TIME_KEYS) {
    const match = entries.find(([entryKey]) => canonicaliseKey(entryKey) === key)
    if (match && (typeof match[1] === 'string' || typeof match[1] === 'number')) {
      timeValue = parseTimestamp(match[1])
      if (timeValue !== null) break
    }
  }

  for (const key of BPM_KEYS) {
    const match = entries.find(([entryKey]) => canonicaliseKey(entryKey) === key)
    if (match && (typeof match[1] === 'string' || typeof match[1] === 'number')) {
      bpmValue = normaliseBpm(match[1])
      if (bpmValue !== null) break
    }
  }

  const effectiveTime = timeValue ?? inheritedTime

  if (bpmValue !== null && effectiveTime !== null) {
    samples.push({ time: new Date(effectiveTime).toISOString(), bpm: bpmValue })
  } else if (bpmValue !== null && effectiveTime === null) {
    counters.discarded += 1
  }

  for (const [, value] of entries) {
    if (value !== null && typeof value === 'object') {
      counters.nodes += 1
      // A parent record's startTime applies to nested samples that lack their own.
      collectJsonSamples(value, samples, counters, timeValue ?? inheritedTime)
    }
  }
}

function parseJson(content: string): ParseResult {
  let parsed: unknown
  try {
    parsed = JSON.parse(content)
  } catch {
    throw new ImportParseError('O arquivo não é um JSON válido.')
  }

  const samples: HRSample[] = []
  const counters = { discarded: 0, nodes: 0 }
  collectJsonSamples(parsed, samples, counters)

  const looksLikeHealthConnect = /beatsPerMinute|heartRateRecord/i.test(content.slice(0, 20_000))

  return {
    format: looksLikeHealthConnect ? 'health_connect_json' : 'generic_json',
    samples,
    discarded: counters.discarded,
    warnings: [],
  }
}

function parseAppleXml(content: string): ParseResult {
  const raw = parseHealthKitExport(content)
  const samples: HRSample[] = []
  let discarded = 0

  for (const sample of raw) {
    const millis = parseTimestamp(sample.startDate)
    const bpm = normaliseBpm(sample.value)
    if (millis === null || bpm === null) {
      discarded += 1
      continue
    }
    samples.push({ time: new Date(millis).toISOString(), bpm })
  }

  return { format: 'apple_health_xml', samples, discarded, warnings: [] }
}

/** Sort chronologically and drop duplicate timestamps, keeping the first reading. */
function sortAndDedupe(samples: HRSample[]): { samples: HRSample[]; duplicates: number } {
  const sorted = [...samples].sort((a, b) => a.time.localeCompare(b.time))
  const deduped: HRSample[] = []
  let duplicates = 0

  for (const sample of sorted) {
    if (deduped.length > 0 && deduped[deduped.length - 1].time === sample.time) {
      duplicates += 1
      continue
    }
    deduped.push(sample)
  }

  return { samples: deduped, duplicates }
}

/**
 * Detect the format of an exported heart rate file and parse it into samples.
 *
 * @throws {ImportParseError} when the format is unrecognised or has no readings.
 */
export function parseHRFile(fileName: string, content: string): ParseResult {
  const trimmed = content.trimStart()
  if (!trimmed) {
    throw new ImportParseError('O arquivo está vazio.')
  }

  let result: ParseResult
  if (trimmed.startsWith('<')) {
    result = parseAppleXml(content)
  } else if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
    result = parseJson(trimmed)
  } else {
    result = parseCsv(content)
  }

  const { samples, duplicates } = sortAndDedupe(result.samples)
  const warnings = [...result.warnings]
  if (duplicates > 0) {
    warnings.push(`${duplicates} leituras com horário duplicado foram unificadas.`)
  }
  if (result.discarded > 0) {
    warnings.push(
      `${result.discarded} linhas foram descartadas (horário inválido ou BPM fora de ${MIN_VALID_BPM}–${MAX_VALID_BPM}).`,
    )
  }

  if (samples.length === 0) {
    throw new ImportParseError(
      `Nenhuma batida encontrada em "${fileName}". ` +
        'Confira se o arquivo exportado tem batidas com horário.',
    )
  }

  return { ...result, samples, warnings }
}

/** Keep only the samples inside [startMillis, endMillis]. */
export function filterSamplesByWindow(
  samples: HRSample[],
  startMillis: number,
  endMillis: number,
): HRSample[] {
  return samples.filter((sample) => {
    const millis = Date.parse(sample.time)
    return millis >= startMillis && millis <= endMillis
  })
}
