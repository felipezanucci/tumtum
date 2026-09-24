'use client'

import { create } from 'zustand'
import {
  auth,
  clearTokens,
  storeTokens,
  type SignupStarted,
  type UserResponse,
} from '@/lib/api'

interface AuthState {
  user: UserResponse | null
  token: string | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  /** Sends the code (#64); creates nothing. */
  startSignup: (email: string, name: string, password: string) => Promise<SignupStarted>
  /** The code came back: the account is made and signed in. */
  confirmSignup: (email: string, code: string) => Promise<void>
  logout: () => void
  loadUser: () => Promise<void>
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: typeof window !== 'undefined' ? localStorage.getItem('access_token') : null,
  loading: false,

  login: async (email, password) => {
    set({ loading: true })
    try {
      const tokens = await auth.login(email, password)
      storeTokens(tokens)
      set({ token: tokens.access_token })
      const user = await auth.me()
      set({ user })
    } finally {
      set({ loading: false })
    }
  },

  startSignup: async (email, name, password) => {
    set({ loading: true })
    try {
      return await auth.signupStart(email, name, password)
    } finally {
      set({ loading: false })
    }
  },

  confirmSignup: async (email, code) => {
    set({ loading: true })
    try {
      const tokens = await auth.signupConfirm(email, code)
      storeTokens(tokens)
      set({ token: tokens.access_token })
      const user = await auth.me()
      set({ user })
    } finally {
      set({ loading: false })
    }
  },

  logout: () => {
    void auth.logout()
    set({ user: null, token: null })
  },

  loadUser: async () => {
    set({ loading: true })
    try {
      const user = await auth.me()
      set({ user })
    } catch {
      clearTokens()
      set({ user: null, token: null })
    } finally {
      set({ loading: false })
    }
  },
}))
