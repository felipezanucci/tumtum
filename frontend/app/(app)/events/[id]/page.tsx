'use client'

import { useEffect } from 'react'
import { useParams } from 'next/navigation'
import { useEventStore } from '@/lib/stores/useEventStore'
import { TimelineBar } from '@/components/hr'
import { Badge, Loading } from '@/components/ui'
import { Nav } from '@/components/layout'

export default function EventDetailPage() {
  const params = useParams()
  const eventId = params.id as string

  const { currentEvent, eventsLoading, loadEvent } = useEventStore()

  useEffect(() => {
    if (eventId) loadEvent(eventId)
  }, [eventId, loadEvent])

  if (eventsLoading) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-dark">
          <Loading size="lg" />
        </main>
      </>
    )
  }

  if (!currentEvent) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-dark">
          <p className="text-tumtum-text-muted">Evento não encontrado.</p>
        </main>
      </>
    )
  }

  const event = currentEvent
  const formattedDate = new Date(event.date).toLocaleDateString('pt-BR', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  })

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-dark">
        <div className="mx-auto max-w-4xl px-4 py-8">
          {/* Cover */}
          {event.cover_image_url && (
            <div className="mb-6 h-64 overflow-hidden rounded-xl">
              <img
                src={event.cover_image_url}
                alt={event.name}
                className="h-full w-full object-cover"
              />
            </div>
          )}

          {/* Info */}
          <Badge variant="accent" className="mb-3">
            {event.event_type === 'concert'
              ? 'Show'
              : event.event_type === 'sports'
              ? 'Esporte'
              : 'Festival'}
          </Badge>
          <h1 className="text-3xl font-bold text-tumtum-text-primary">{event.name}</h1>
          {event.subtitle && (
            <p className="mt-1 text-lg text-tumtum-text-muted">{event.subtitle}</p>
          )}

          <div className="mt-4 flex flex-wrap gap-4 text-sm text-tumtum-text-muted">
            {event.venue && <span>📍 {event.venue}</span>}
            {event.city && <span>{event.city}{event.country ? `, ${event.country}` : ''}</span>}
            <span>📅 {formattedDate}</span>
          </div>

          {/* Timeline */}
          {event.timeline.length > 0 && (
            <div className="mt-10">
              <TimelineBar entries={event.timeline} />
            </div>
          )}
        </div>
      </main>
    </>
  )
}
