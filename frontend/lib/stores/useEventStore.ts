'use client'

import { create } from 'zustand'
import {
  events,
  experience,
  type TumtumEvent,
  type EventDetail,
  type Peak,
  type ExperienceData,
} from '@/lib/api'

interface EventState {
  // Events
  eventList: TumtumEvent[]
  eventsLoading: boolean
  currentEvent: EventDetail | null
  eventsError: Error | null
  loadEvents: (params?: {
    q?: string
    event_type?: string
    city?: string
    date_from?: string
    date_to?: string
  }) => Promise<void>
  loadEvent: (eventId: string) => Promise<void>

  // Experience
  experienceData: ExperienceData | null
  experienceLoading: boolean
  analyzeSession: (sessionId: string) => Promise<Peak[]>
  loadExperience: (sessionId: string) => Promise<void>
}

export const useEventStore = create<EventState>((set) => ({
  // Events
  eventList: [],
  eventsError: null,
  eventsLoading: false,
  currentEvent: null,

  loadEvents: async (params) => {
    set({ eventsLoading: true, eventsError: null })
    try {
      const eventList = await events.list(params)
      set({ eventList })
    } catch (error) {
      // Swallowing this used to leave eventList empty, and the page says
      // "Nenhum evento encontrado" when the list is empty — so a refused
      // request rendered as "you have no events". The failure has to survive
      // for the page to tell the two apart.
      set({ eventsError: error instanceof Error ? error : new Error('Falhou') })
    } finally {
      set({ eventsLoading: false })
    }
  },

  loadEvent: async (eventId) => {
    set({ eventsLoading: true })
    try {
      const currentEvent = await events.get(eventId)
      set({ currentEvent })
    } finally {
      set({ eventsLoading: false })
    }
  },

  // Experience
  experienceData: null,
  experienceLoading: false,

  analyzeSession: async (sessionId) => {
    set({ experienceLoading: true })
    try {
      const peaks = await experience.analyze(sessionId)
      return peaks
    } finally {
      set({ experienceLoading: false })
    }
  },

  loadExperience: async (sessionId) => {
    set({ experienceLoading: true })
    try {
      const experienceData = await experience.get(sessionId)
      set({ experienceData })
    } finally {
      set({ experienceLoading: false })
    }
  },
}))
