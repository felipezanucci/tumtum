'use client'

import { useCallback, useEffect, useState } from 'react'

import { auth } from '@/lib/api'
import {
  localUsernameProblem,
  usernameStatus,
  type UsernameAnswer,
  type UsernameStatus,
} from '@/lib/username'

/** How long typing must pause before the server is asked. */
export const USERNAME_CHECK_DEBOUNCE_MS = 400

/**
 * Ask the server whether `clean` (already through `cleanUsername`) can be an
 * @, once typing pauses, and keep what it said — for that name only.
 *
 * The status is derived on every render from the name on screen and the last
 * answer (`usernameStatus`): an answer about "felipe" says nothing about
 * "felipez", so the line reads "Conferindo…" until the server speaks about
 * the name actually typed. A request that fails is "Não deu pra conferir
 * agora.", never "disponível".
 *
 * `reject` lets a later refusal (a 409 at sign-up, say) replace the answer:
 * the name was free a minute ago and is somebody's now.
 */
export function useUsernameCheck(clean: string): {
  status: UsernameStatus
  retry: () => void
  reject: (username: string, reason: string) => void
} {
  const [answer, setAnswer] = useState<UsernameAnswer | null>(null)
  const [failedFor, setFailedFor] = useState<string | null>(null)
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    // A failure or an answer about another name is not about this one.
    setFailedFor(null)
    setAnswer((held) => (held && held.username === clean ? held : null))
    if (localUsernameProblem(clean)) return

    let live = true
    const timer = setTimeout(async () => {
      try {
        const result = await auth.checkUsername(clean)
        if (!live) return
        setAnswer({
          // Keyed by what was asked: the field and the server clean alike.
          username: clean,
          available: result.available,
          reason: result.reason,
        })
      } catch {
        if (live) setFailedFor(clean)
      }
    }, USERNAME_CHECK_DEBOUNCE_MS)
    return () => {
      live = false
      clearTimeout(timer)
    }
  }, [clean, attempt])

  const retry = useCallback(() => setAttempt((n) => n + 1), [])
  const reject = useCallback((username: string, reason: string) => {
    setFailedFor(null)
    setAnswer({ username, available: false, reason })
  }, [])

  return { status: usernameStatus(clean, answer, failedFor), retry, reject }
}
