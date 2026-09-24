/**
 * The six digits a person types back from the sign-up e-mail (#64, 24/09).
 * Pure, so the rules are tested without a browser; the app's twin is
 * `SignupCode` in the Android client.
 */

export const CODE_LENGTH = 6

/**
 * What the field keeps of what was typed or pasted: ASCII digits, at most six.
 * A paste of the whole subject line ("390875 é seu código…") keeps the code.
 */
export function codeDigits(input: string): string {
  return input.replace(/[^0-9]/g, '').slice(0, CODE_LENGTH)
}

export function isCompleteCode(code: string): boolean {
  return /^[0-9]{6}$/.test(code)
}

/** Whole seconds before another code may be asked for, rounded up; 0 means now. */
export function secondsUntil(resendAtMs: number, nowMs: number): number {
  return nowMs >= resendAtMs ? 0 : Math.ceil((resendAtMs - nowMs) / 1000)
}
