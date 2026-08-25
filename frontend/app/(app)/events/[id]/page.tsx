'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { useEventStore } from '@/lib/stores/useEventStore'
import { demo } from '@/lib/api'
import { TimelineBar } from '@/components/hr'
import { Badge, Button, Loading } from '@/components/ui'
import { Nav } from '@/components/layout'

export default function EventDetailPage() {
  const params = useParams()
  const router = useRouter()
  const eventId = params.id as string

  const { currentEvent, eventsLoading, loadEvent } = useEventStore()
  const [simulating, setSimulating] = useState(false)
  const [simulateError, setSimulateError] = useState<string | null>(null)

  useEffect(() => {
    if (eventId) loadEvent(eventId)
  }, [eventId, loadEvent])

  async function handleSimulate() {
    setSimulating(true)
    setSimulateError(null)
    try {
      const result = await demo.simulate(eventId)
      router.push(`/experience?session=${result.session.id}`)
    } catch (err: any) {
      setSimulateError(err.detail || 'Erro ao simular experiência')
    } finally {
      setSimulating(false)
    }
  }

  if (eventsLoading) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-black">
          <Loading size="lg" />
        </main>
      </>
    )
  }

  if (!currentEvent) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-black">
          <p className="text-tumtum-muted">Evento não encontrado.</p>
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
      <main className="min-h-screen bg-tumtum-black">
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
          <h1 className="text-3xl font-bold text-tumtum-white">{event.name}</h1>
          {event.subtitle && (
            <p className="mt-1 text-lg text-tumtum-muted">{event.subtitle}</p>
          )}

          <div className="mt-4 flex flex-wrap gap-4 text-sm text-tumtum-muted">
            {event.venue && <span>📍 {event.venue}</span>}
            {event.city && <span>{event.city}{event.country ? `, ${event.country}` : ''}</span>}
            <span>📅 {formattedDate}</span>
          </div>

          {/* Simulate CTA */}
          <div className="mt-8 rounded-xl border border-tumtum-border bg-tumtum-surface p-6">
            <h2 className="text-lg font-semibold text-tumtum-white">
              Simular Experiência
            </h2>
            <p className="mt-1 text-sm text-tumtum-muted">
              Veja como seria sua noite nesse evento, com dados de exemplo.
            </p>
            <Button
              className="mt-4"
              onClick={handleSimulate}
              disabled={simulating}
            >
              {simulating ? 'Simulando...' : 'Simular Experiência'}
            </Button>
            {simulateError && (
              <p className="mt-2 text-sm text-red-400">{simulateError}</p>
            )}
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
