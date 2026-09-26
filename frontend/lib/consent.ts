/**
 * The rules around consent and age, kept pure so they are tested without a
 * browser (26/09, LGPD remediation). The words live in `consent-copy.ts`;
 * the calls live in `api.ts`.
 */

import { CONSENT_PURPOSES, isConsentPurpose, type ConsentPurpose } from './consent-copy'

/** The server's own sentence for an under-18 birth date, shown as-is. */
export const UNDER_AGE_MESSAGE = 'Você precisa ter 18 anos ou mais para usar a TumTum.'

export const MINIMUM_AGE = 18

export type ConsentChoices = Record<ConsentPurpose, boolean>

/** What `GET /api/consents` answers, reduced to what these rules read. */
export interface ConsentEntryLike {
  purpose: string
  granted: boolean
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
  return !choicesFrom(consents).terms
}

/**
 * A path to come back to after the consent screen. Only this site's own
 * paths: `//evil.example` and absolute URLs are dropped.
 */
export function safeNext(next: string | null | undefined): string | null {
  if (!next || !next.startsWith('/') || next.startsWith('//') || next.startsWith('/\\')) return null
  if (next.startsWith('/consentimento')) return null
  return next
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

/** What the sign-up form needs before its button wakes up. */
export function signupReady(fields: { birthDate: string; termsAccepted: boolean }): boolean {
  return fields.termsAccepted && parts(fields.birthDate) !== null
}
