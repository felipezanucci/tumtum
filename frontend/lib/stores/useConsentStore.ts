'use client'

import { create } from 'zustand'

import { consents as consentsApi, type ConsentEntry, type ConsentMeans } from '@/lib/api'
import type { ConsentPurpose } from '@/lib/consent-copy'

/**
 * The signed-in person's consents (26/09), loaded once per page visit and
 * shared by the gate, the consent screen and the profile.
 *
 * `failed` is kept apart from "nothing granted": a request that could not be
 * made says nothing about what the person chose, and the gate must not send
 * someone to the consent screen because the server was unreachable.
 */
interface ConsentState {
  userId: string | null
  entries: ConsentEntry[] | null
  status: 'idle' | 'loading' | 'loaded' | 'failed'
  error: string | null
  load: (userId: string) => Promise<void>
  save: (
    purposes: Partial<Record<ConsentPurpose, boolean>>,
    means: ConsentMeans,
  ) => Promise<ConsentEntry[]>
  reset: () => void
}

export const useConsentStore = create<ConsentState>((set, get) => ({
  userId: null,
  entries: null,
  status: 'idle',
  error: null,

  load: async (userId) => {
    if (get().status === 'loading' && get().userId === userId) return
    set({ userId, status: 'loading', error: null })
    try {
      const response = await consentsApi.get()
      if (get().userId !== userId) return
      set({ entries: response.consents, status: 'loaded' })
    } catch (err) {
      if (get().userId !== userId) return
      set({
        entries: null,
        status: 'failed',
        error: err instanceof Error ? err.message : 'Não deu pra ler suas escolhas.',
      })
    }
  },

  save: async (purposes, means) => {
    const response = await consentsApi.put(purposes, means)
    set({ entries: response.consents, status: 'loaded', error: null })
    return response.consents
  },

  reset: () => set({ userId: null, entries: null, status: 'idle', error: null }),
}))
