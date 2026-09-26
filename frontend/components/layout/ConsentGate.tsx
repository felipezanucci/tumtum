'use client'

import { useEffect } from 'react'
import { usePathname, useRouter } from 'next/navigation'

import { useCurrentUser } from '@/lib/hooks/useCurrentUser'
import { useConsentStore } from '@/lib/stores/useConsentStore'
import { consentHref, needsConsentGate } from '@/lib/consent'

/**
 * Before anything else, the consent screen (26/09 contract).
 *
 * After sign-in, an account with no birth date — every account made before
 * 26/09 — or without the Terms accepted is sent to `/consentimento`, and
 * comes back to where it was going once it is done. Renders nothing.
 * Signed out, it does nothing: the pages themselves say "entre na sua conta".
 */
export default function ConsentGate() {
  const user = useCurrentUser()
  const pathname = usePathname()
  const router = useRouter()
  const { userId, entries, status, load, reset } = useConsentStore()

  useEffect(() => {
    if (!user) {
      if (userId) reset()
      return
    }
    if (userId !== user.id || status === 'idle') void load(user.id)
  }, [user, userId, status, load, reset])

  const consentsKnown = user && userId === user.id && status !== 'idle' && status !== 'loading'

  useEffect(() => {
    if (!user || !consentsKnown) return
    if (pathname?.startsWith('/consentimento')) return
    if (needsConsentGate(user, status === 'loaded' ? entries : null)) {
      router.replace(consentHref(null, pathname))
    }
  }, [user, consentsKnown, status, entries, pathname, router])

  return null
}
