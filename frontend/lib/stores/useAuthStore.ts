'use client'

import { create } from 'zustand'
import { auth, type UserResponse } from '@/lib/api'

interface AuthState {
  user: UserResponse | null
  token: string | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (email: string, name: string, password: string) => Promise<void>
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
      const { access_token } = await auth.login(email, password)
      localStorage.setItem('access_token', access_token)
      set({ token: access_token })
      const user = await auth.me()
      set({ user })
    } finally {
      set({ loading: false })
    }
  },

  register: async (email, name, password) => {
    set({ loading: true })
    try {
      const { access_token } = await auth.register(email, name, password)
      localStorage.setItem('access_token', access_token)
      set({ token: access_token })
      const user = await auth.me()
      set({ user })
    } finally {
      set({ loading: false })
    }
  },

  logout: () => {
    localStorage.removeItem('access_token')
    set({ user: null, token: null })
  },

  loadUser: async () => {
    set({ loading: true })
    try {
      const user = await auth.me()
      set({ user })
    } catch {
      localStorage.removeItem('access_token')
      set({ user: null, token: null })
    } finally {
      set({ loading: false })
    }
  },
}))
