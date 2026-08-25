/**
 * A calendar date is not an instant, and treating one as the other moves it.
 *
 * `new Date('2026-08-29')` is parsed as midnight **UTC**, so rendering it in
 * São Paulo shows 28 August — an event saved correctly for the 29th appeared
 * on the 28th, and re-typing the date could never fix it because the date was
 * never wrong.
 *
 * These helpers take the `YYYY-MM-DD` a date column returns and build the day
 * itself, in local time, where no offset can shift it.
 */

/** Parse a `YYYY-MM-DD` value as that day, locally. Returns null if malformed. */
export function parseDateOnly(value: string): Date | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(value)
  if (!match) return null
  const date = new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]))
  return Number.isNaN(date.getTime()) ? null : date
}

/**
 * Render a `YYYY-MM-DD` value in pt-BR. Falls back to the raw string rather
 * than showing "Invalid Date" for something unexpected.
 */
export function formatDateOnly(
  value: string,
  options: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'long', year: 'numeric' },
): string {
  const date = parseDateOnly(value)
  return date ? date.toLocaleDateString('pt-BR', options) : value
}
