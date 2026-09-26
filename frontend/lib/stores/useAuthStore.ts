'use client'

import { create } from 'zustand'
import {
  auth,
  clearTokens,
  currentAccessToken,
  onAccessTokenChange,
  restoreSession,
  storeTokens,
  type SignupStartData,
  type SignupStarted,
  type UserResponse,
} from '@/lib/api'

interface AuthState {
  user: UserResponse | null
  /**
   * The access token this tab holds — never the refresh token, which lives
   * in an httpOnly cookie the site cannot read (26/09).
   */
  token: string | null
  /**
   * Whether the cookie has been asked on this page load. Until it has, no
   * token means "not known yet", not "signed out".
   */
  sessionChecked: boolean
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  /** Sends the code (#64); creates nothing. */
  startSignup: (data: SignupStartData) => Promise<SignupStarted>
  /** The code came back: the account is made and signed in. */
  confirmSignup: (email: string, code: string) => Promise<void>
  logout: () => void
  loadUser: () => Promise<void>
  /** Ask the cookie for a session, once per page load. */
  restore: () => Promise<void>
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: currentAccessToken(),
  sessionChecked: currentAccessToken() !== null,
  loading: false,

  restore: async () => {
    const signedIn = await restoreSession()
    set({ token: signedIn ? currentAccessToken() : null, sessionChecked: true })
  },

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

  startSignup: async (data) => {
    set({ loading: true })
    try {
      return await auth.signupStart(data)
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
    set({ user: null, token: null, sessionChecked: true })
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

// Renewals, refusals and restores happen inside lib/api; the store follows.
onAccessTokenChange((token) => {
  useAuthStore.setState((state) => ({
    token,
    sessionChecked: true,
    user: token ? state.user : null,
  }))
})
