'use client'

import { create } from 'zustand'
import { health, type HRSession, type HRSessionDetail, type WearableConnection, type SyncStatus } from '@/lib/api'

interface HRState {
  // Wearable connections
  connections: WearableConnection[]
  connectionsLoading: boolean
  loadConnections: () => Promise<void>
  connectWearable: (provider: string, accessToken: string, refreshToken?: string) => Promise<void>
  disconnectWearable: (connectionId: string) => Promise<void>

  // HR sessions
  sessions: HRSession[]
  sessionsLoading: boolean
  currentSession: HRSessionDetail | null
  loadSessions: () => Promise<void>
  loadSession: (sessionId: string) => Promise<void>
  uploadSession: (data: Parameters<typeof health.createSession>[0]) => Promise<HRSession>

  // Sync
  syncStatus: SyncStatus | null
  triggerSync: (connectionId: string, startTime: string, endTime: string) => Promise<void>
}

export const useHRStore = create<HRState>((set) => ({
  // Wearable connections
  connections: [],
  connectionsLoading: false,

  loadConnections: async () => {
    set({ connectionsLoading: true })
    try {
      const connections = await health.listWearables()
      set({ connections })
    } finally {
      set({ connectionsLoading: false })
    }
  },

  connectWearable: async (provider, accessToken, refreshToken) => {
    const connection = await health.connectWearable(provider, accessToken, refreshToken)
    set((state) => ({ connections: [connection, ...state.connections] }))
  },

  disconnectWearable: async (connectionId) => {
    await health.disconnectWearable(connectionId)
    set((state) => ({
      connections: state.connections.filter((c) => c.id !== connectionId),
    }))
  },

  // HR sessions
  sessions: [],
  sessionsLoading: false,
  currentSession: null,

  loadSessions: async () => {
    set({ sessionsLoading: true })
    try {
      const sessions = await health.listSessions()
      set({ sessions })
    } finally {
      set({ sessionsLoading: false })
    }
  },

  loadSession: async (sessionId) => {
    set({ sessionsLoading: true })
    try {
      const session = await health.getSession(sessionId)
      set({ currentSession: session })
    } finally {
      set({ sessionsLoading: false })
    }
  },

  uploadSession: async (data) => {
    const session = await health.createSession(data)
    set((state) => ({ sessions: [session, ...state.sessions] }))
    return session
  },

  // Sync
  syncStatus: null,

  triggerSync: async (connectionId, startTime, endTime) => {
    const status = await health.triggerSync(connectionId, startTime, endTime)
    set({ syncStatus: status })
  },
}))
