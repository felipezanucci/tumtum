'use client'

import { useEffect } from 'react'

import { useAuthStore } from '@/lib/stores/useAuthStore'
import type { UserResponse } from '@/lib/api'

/**
 * The signed-in person, loaded once per page visit.
 *
 * The auth store only knows the user after a login in this tab; on a reload
 * it holds the token and nothing else, so anything that reads `user` to
 * decide what to show — the operator's doors, for one — would decide "no"
 * for everyone until the next login. This asks the server as soon as there
 * is a token to ask with — and, in a tab that holds none yet, first asks the
 * refresh cookie for one. `null` means "not known yet" as much as "not
 * signed in"; callers that must tell the two apart look at `token` and
 * `sessionChecked` too.
 */
export function useCurrentUser(): UserResponse | null {
  const user = useAuthStore((s) => s.user)
  const token = useAuthStore((s) => s.token)
  const loadUser = useAuthStore((s) => s.loadUser)
  const sessionChecked = useAuthStore((s) => s.sessionChecked)
  const restore = useAuthStore((s) => s.restore)

  // A new tab holds no access token: the cookie is asked once (26/09).
  useEffect(() => {
    if (!token && !sessionChecked) void restore()
  }, [token, sessionChecked, restore])

  useEffect(() => {
    if (token && !user) loadUser()
  }, [token, user, loadUser])

  return user
}
