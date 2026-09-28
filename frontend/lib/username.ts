/**
 * The @ of an account (28/09): one owner, chosen once. The server holds the
 * rules (`backend/app/services/usernames.py`) and answers
 * `GET /api/auth/username/{name}`; this file mirrors the instant ones so the
 * field can say something true before the network does. When the server
 * answers, its sentence wins.
 *
 * Pure, so the rules are tested without a browser.
 */

export const USERNAME_MIN_LENGTH = 3
export const USERNAME_MAX_LENGTH = 20

// The server's own sentences, word for word.
export const USERNAME_TOO_SHORT = `O @ precisa de pelo menos ${USERNAME_MIN_LENGTH} letras ou números.`
export const USERNAME_TOO_LONG = `O @ pode ter até ${USERNAME_MAX_LENGTH} caracteres.`
export const USERNAME_BAD_CHARS = 'O @ só aceita letras sem acento, números e _.'
export const USERNAME_TAKEN = 'Esse @ já tem dono. Tenta outro.'

/** What the field says when the check itself could not be made. */
export const USERNAME_CHECK_FAILED = 'Não deu pra conferir agora.'
/**
 * Said once an @ is set: the promise the server keeps, in the words of its
 * own 409 when a set @ is sent again as another.
 */
export const USERNAME_FIXED = 'O @ é fixo: escolhido uma vez, não muda.'

/**
 * What the field keeps of what was typed or pasted: no spaces, no @ in front,
 * lower case, and only `a-z`, `0-9` and `_`. An accent is dropped as typed
 * rather than refused later ("joão" keeps "joo" on screen, so the person sees
 * at once what the @ will be). Cut at the maximum length, like the code field.
 */
export function cleanUsername(raw: string): string {
  return raw
    .trim()
    .replace(/^@+/, '')
    .toLowerCase()
    .replace(/[^a-z0-9_]/g, '')
    .slice(0, USERNAME_MAX_LENGTH)
}

/**
 * Why `clean` (already through `cleanUsername`) cannot be an @ without asking
 * anybody, or null when only the server can tell (reserved or taken).
 */
export function localUsernameProblem(clean: string): string | null {
  if (clean.length < USERNAME_MIN_LENGTH) return USERNAME_TOO_SHORT
  if (clean.length > USERNAME_MAX_LENGTH) return USERNAME_TOO_LONG
  if (!/^[a-z0-9_]+$/.test(clean)) return USERNAME_BAD_CHARS
  return null
}

/**
 * Whether a refusal from sign-up or the profile is about the @ — so it is
 * shown under the @ field and not as a general error. Every sentence the
 * server writes about an @ names it as a word of its own ("O @ …", "Esse @
 * …"); an address ("seu@email.com") never does.
 */
export function isUsernameSentence(message: string | null | undefined): boolean {
  return typeof message === 'string' && /(^|\s)@(\s|$)/.test(message)
}

/** What the server said about one name. */
export interface UsernameAnswer {
  /** The name it was asked about, cleaned. */
  username: string
  available: boolean
  reason: string | null
}

/**
 * The line under the field, derived from the state every time it is shown —
 * never written once at the moment of a keystroke.
 *
 * - `empty`: nothing typed yet; a hint, not a complaint.
 * - `invalid`: a local rule fails; its sentence.
 * - `checking`: the server has not answered for *this* name.
 * - `available`: the server said yes for this very name.
 * - `unavailable`: the server said no; its sentence.
 * - `failed`: the question could not be asked. Never "disponível".
 */
export type UsernameStatus =
  | { kind: 'empty' }
  | { kind: 'invalid'; message: string }
  | { kind: 'checking' }
  | { kind: 'available'; username: string }
  | { kind: 'unavailable'; message: string }
  | { kind: 'failed'; message: string }

export function usernameStatus(
  clean: string,
  answer: UsernameAnswer | null,
  failedFor: string | null,
): UsernameStatus {
  if (clean.length === 0) return { kind: 'empty' }
  const local = localUsernameProblem(clean)
  if (local) return { kind: 'invalid', message: local }
  // An answer counts only for the name it was about: a stale "disponível"
  // under a name that changed since would be a claim nobody checked.
  if (answer && answer.username === clean) {
    return answer.available
      ? { kind: 'available', username: clean }
      : { kind: 'unavailable', message: answer.reason || USERNAME_TAKEN }
  }
  if (failedFor === clean) return { kind: 'failed', message: USERNAME_CHECK_FAILED }
  return { kind: 'checking' }
}

/** The words for a status, as the field shows them. */
export function usernameStatusText(status: UsernameStatus): string {
  switch (status.kind) {
    case 'empty':
      return `De ${USERNAME_MIN_LENGTH} a ${USERNAME_MAX_LENGTH} letras, números ou _.`
    case 'invalid':
    case 'unavailable':
    case 'failed':
      return status.message
    case 'checking':
      return 'Conferindo…'
    case 'available':
      return `@${status.username} disponível`
  }
}
