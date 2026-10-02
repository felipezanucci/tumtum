/**
 * The rules around consent and age, kept pure so they are tested without a
 * browser (26/09, LGPD remediation). The words live in `consent-copy.ts`;
 * the calls live in `api.ts`.
 */

import {
  CONSENT_PT,
  CONSENT_PURPOSES,
  CONSENT_TEXT_VERSION,
  isConsentPurpose,
  type ConsentCopy,
  type ConsentPurpose,
} from './consent-copy'

/** The server's own sentence for an under-18 birth date, shown as-is. */
export const UNDER_AGE_MESSAGE = 'Você precisa ter 18 anos ou mais para usar a TumTum.'

export const MINIMUM_AGE = 18

export type ConsentChoices = Record<ConsentPurpose, boolean>

/** What `GET /api/consents` answers, reduced to what these rules read. */
export interface ConsentEntryLike {
  purpose: string
  granted: boolean
  /** The text the yes was given under; the gate re-asks when it is not the current one (02/10). */
  text_version?: string | null
}

function pad(n: number): string {
  return String(n).padStart(2, '0')
}

/** A local calendar day as `YYYY-MM-DD`. */
export function toDateOnly(day: Date): string {
  return `${day.getFullYear()}-${pad(day.getMonth() + 1)}-${pad(day.getDate())}`
}

function parts(value: string): [number, number, number] | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value.trim())
  if (!match) return null
  const y = Number(match[1])
  const m = Number(match[2])
  const d = Number(match[3])
  // Reject 2026-02-31 and friends: the Date constructor would roll it over.
  const check = new Date(y, m - 1, d)
  if (check.getFullYear() !== y || check.getMonth() !== m - 1 || check.getDate() !== d) return null
  return [y, m, d]
}

/**
 * Whether someone born on `birthDate` (`YYYY-MM-DD`) is 18 on `today`.
 * Null when the value is not a real date. Someone born on 29 February
 * turns 18 on 1 March in a year that has no 29th.
 */
export function isAdult(birthDate: string, today: Date = new Date()): boolean | null {
  const born = parts(birthDate)
  if (!born) return null
  const [y, m, d] = born
  const ty = today.getFullYear()
  const tm = today.getMonth() + 1
  const td = today.getDate()
  let age = ty - y
  if (tm < m || (tm === m && td < d)) age -= 1
  return age >= MINIMUM_AGE
}

/**
 * The latest birth date that is 18 today — the `max` of the date picker.
 * A hint only: the server decides.
 */
export function latestAdultBirthDate(today: Date = new Date()): string {
  const y = today.getFullYear() - MINIMUM_AGE
  const m = today.getMonth()
  // 29 February 18 years back may not exist; the 28th is then the last day.
  const lastDay = new Date(y, m + 1, 0).getDate()
  return toDateOnly(new Date(y, m, Math.min(today.getDate(), lastDay)))
}

/** Every purpose, off unless the server says it is granted. */
export function choicesFrom(entries: readonly ConsentEntryLike[] | null | undefined): ConsentChoices {
  const choices = Object.fromEntries(CONSENT_PURPOSES.map((p) => [p, false])) as ConsentChoices
  for (const entry of entries ?? []) {
    if (isConsentPurpose(entry.purpose)) choices[entry.purpose] = entry.granted === true
  }
  return choices
}

/** Only what changed: the PUT sends these and nothing else. */
export function changedChoices(
  before: ConsentChoices,
  after: ConsentChoices,
): Partial<ConsentChoices> {
  const changed: Partial<ConsentChoices> = {}
  for (const purpose of CONSENT_PURPOSES) {
    if (before[purpose] !== after[purpose]) changed[purpose] = after[purpose]
  }
  return changed
}

/**
 * Whether a signed-in person must see the consent screen before anything
 * else: no birth date on the account, or the Terms not accepted. When the
 * consents could not be read, only the birth date decides — a request that
 * failed is not a "no".
 */
export function needsConsentGate(
  user: { birth_date?: string | null } | null | undefined,
  consents: readonly ConsentEntryLike[] | null | undefined,
): boolean {
  if (!user) return false
  if (!user.birth_date) return true
  if (!consents) return false
  if (!choicesFrom(consents).terms) return true
  // The words changed since this person said yes (02/10): they are asked
  // again, once, and the new yes is recorded under the new version.
  const terms = consents.find((entry) => entry.purpose === 'terms')
  return terms?.text_version != null && terms.text_version !== CONSENT_TEXT_VERSION
}

/** The fixed origin a `next` must resolve to — never `location`, which a page can be framed or proxied under. */
const SITE_ORIGIN = 'https://tumtum.cc'

// eslint-disable-next-line no-control-regex -- control characters are exactly what this refuses
const UNSAFE_NEXT = /[\u0000-\u001f\u007f\\]/

/**
 * A path to come back to after the consent screen. Only this site's own
 * paths: `//evil.example` and absolute URLs are dropped.
 */
export function safeNext(next: string | null | undefined): string | null {
  if (!next || !next.startsWith('/')) return null
  // Browsers strip tabs and newlines and read `\` as `/`, so `/\t/evil.com`
  // and `/\evil.com` become `//evil.com`: refuse them before parsing, and
  // their percent-encoded forms too.
  let decoded: string
  try {
    decoded = decodeURIComponent(next)
  } catch {
    return null
  }
  for (const candidate of [next, decoded]) {
    if (UNSAFE_NEXT.test(candidate) || candidate.startsWith('//')) return null
  }
  let url: URL
  try {
    url = new URL(next, SITE_ORIGIN)
  } catch {
    return null
  }
  if (url.origin !== SITE_ORIGIN) return null
  if (url.pathname.startsWith('/consentimento')) return null
  return `${url.pathname}${url.search}${url.hash}`
}

/** Where a `consent_required` refusal sends the person. */
export function consentHref(purpose?: string | null, next?: string | null): string {
  const params = new URLSearchParams()
  if (purpose && isConsentPurpose(purpose)) params.set('focus', purpose)
  const back = safeNext(next)
  if (back) params.set('next', back)
  const qs = params.toString()
  return qs ? `/consentimento?${qs}` : '/consentimento'
}

/**
 * What the sign-up form needs before its button wakes up: a real birth date
 * and both core yeses — the Terms and reading the heart rate (28/09). They
 * stay two boxes, because the health-data yes must be its own act.
 */
export function signupReady(fields: {
  birthDate: string
  termsAccepted: boolean
  readHeartRate: boolean
}): boolean {
  return fields.termsAccepted && fields.readHeartRate && parts(fields.birthDate) !== null
}

/**
 * The line under a switch right after it was saved: what happened, not a
 * generic "salvo" (28/09). On, "Ligado."; off, the purpose's own sentence
 * about what stops, or a plain "Desligado." where nothing more is true.
 */
export function consentNotice(
  purpose: ConsentPurpose,
  granted: boolean,
  copy: ConsentCopy = CONSENT_PT,
): string {
  if (granted) return copy.grantedNotice
  return copy.purposes[purpose].off
}

/**
 * The lines under each switch: the purposes just saved say what happened, and
 * a purpose that is off keeps saying what that means (`off`) for as long as
 * it is off — on a fresh load too, not only after the tap (02/10: every key
 * has such a sentence, and every one starts with "Desligado.").
 */
export function consentNotes(
  saved: ConsentChoices,
  justSaved: Partial<ConsentChoices>,
  copy: ConsentCopy = CONSENT_PT,
): Partial<Record<ConsentPurpose, string>> {
  const notes: Partial<Record<ConsentPurpose, string>> = {}
  for (const purpose of CONSENT_PURPOSES) {
    if (!saved[purpose]) notes[purpose] = copy.purposes[purpose].off
    // Only what the server now holds: a change it did not keep says nothing.
    const changed = justSaved[purpose]
    if (changed !== undefined && changed === saved[purpose]) {
      notes[purpose] = consentNotice(purpose, changed, copy)
    }
  }
  return notes
}
