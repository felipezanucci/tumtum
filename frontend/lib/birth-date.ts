/**
 * The sign-up birth date, picked from three lists (28/09).
 *
 * The native `<input type="date">` drew mm/dd/yyyy on Felipe's machine — a
 * Brazilian reading 03/04 as the 3rd of April was told the 4th of March. Three
 * selects in pt-BR order (dia, mês, ano) cannot be read the wrong way, and
 * nothing is typed. The form still holds one ISO `YYYY-MM-DD` string, so the
 * rules in `consent.ts` and the API payload do not change.
 */

import { latestAdultBirthDate, toDateOnly } from './consent'

export const MONTHS_PT = [
  'janeiro',
  'fevereiro',
  'março',
  'abril',
  'maio',
  'junho',
  'julho',
  'agosto',
  'setembro',
  'outubro',
  'novembro',
  'dezembro',
] as const

/** How far back the year list goes. */
export const OLDEST_AGE = 120

/** 1 to 31, always: a day the month does not have leaves the date empty. */
export const BIRTH_DAYS: readonly number[] = Array.from({ length: 31 }, (_, i) => i + 1)

/**
 * The years offered, newest first: from the year of the latest 18-year-old
 * birth date down to 120 years back. Someone born late in the newest year is
 * still under 18 — `isAdult` says so, and the server decides.
 */
export function birthYears(today: Date = new Date()): number[] {
  const newest = Number(latestAdultBirthDate(today).slice(0, 4))
  const oldest = today.getFullYear() - OLDEST_AGE
  const years: number[] = []
  for (let y = newest; y >= oldest; y -= 1) years.push(y)
  return years
}

/**
 * `YYYY-MM-DD` from the three picked values (`month` is 1–12), or `''` while
 * any is missing or the date does not exist — 31 de fevereiro stays empty.
 */
export function assembleBirthDate(day: string, month: string, year: string): string {
  if (!/^\d{1,2}$/.test(day) || !/^\d{1,2}$/.test(month) || !/^\d{4}$/.test(year)) return ''
  const d = Number(day)
  const m = Number(month)
  const y = Number(year)
  if (m < 1 || m > 12 || d < 1) return ''
  const date = new Date(y, m - 1, d)
  // The Date constructor rolls 31/02 over into March; a rolled date is no date.
  if (date.getFullYear() !== y || date.getMonth() !== m - 1 || date.getDate() !== d) return ''
  return toDateOnly(date)
}

/** All three are picked and still make no date: the form says so. */
export function isImpossibleBirthDate(day: string, month: string, year: string): boolean {
  return day !== '' && month !== '' && year !== '' && assembleBirthDate(day, month, year) === ''
}
