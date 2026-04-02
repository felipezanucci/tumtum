'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useEventStore } from '@/lib/stores/useEventStore'
import { Card, Badge, Loading, Input } from '@/components/ui'
import { Nav } from '@/components/layout'

const eventTypeLabels: Record<string, string> = {
  concert: 'Show',
  sports: 'Esporte',
  festival: 'Festival',
}

const eventTypeBadgeVariant: Record<string, 'default' | 'accent' | 'success'> = {
  concert: 'accent',
  sports: 'success',
  festival: 'default',
}

export default function EventsPage() {
  const { eventList, eventsLoading, loadEvents } = useEventStore()
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState<string>('')

  useEffect(() => {
    loadEvents({ q: search || undefined, event_type: typeFilter || undefined })
  }, [search, typeFilter, loadEvents])

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-dark">
        <div className="mx-auto max-w-4xl px-4 py-8">
          <h1 className="mb-6 text-3xl font-bold text-tumtum-text-primary">Eventos</h1>

          {/* Filters */}
          <div className="mb-6 flex flex-col gap-3 sm:flex-row">
            <div className="flex-1">
              <Input
                placeholder="Buscar eventos..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <div className="flex gap-2">
              {['', 'concert', 'sports', 'festival'].map((type) => (
                <button
                  key={type}
                  onClick={() => setTypeFilter(type)}
                  className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                    typeFilter === type
                      ? 'bg-tumtum-red text-white'
                      : 'bg-tumtum-surface text-tumtum-text-muted hover:text-tumtum-text-primary'
                  }`}
                >
                  {type === '' ? 'Todos' : eventTypeLabels[type]}
                </button>
              ))}
            </div>
          </div>

          {/* Event List */}
          {eventsLoading ? (
            <Loading size="lg" className="py-20" />
          ) : eventList.length === 0 ? (
            <div className="py-20 text-center">
              <p className="text-lg text-tumtum-text-muted">Nenhum evento encontrado</p>
              <p className="mt-1 text-sm text-tumtum-text-muted">
                Tente ajustar os filtros ou buscar por outro termo.
              </p>
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {eventList.map((event) => (
                <Link key={event.id} href={`/events/${event.id}`}>
                  <Card hoverable className="h-full">
                    {event.cover_image_url && (
                      <div className="mb-3 h-32 overflow-hidden rounded-lg bg-tumtum-dark">
                        <img
                          src={event.cover_image_url}
                          alt={event.name}
                          className="h-full w-full object-cover"
                        />
                      </div>
                    )}
                    <Badge variant={eventTypeBadgeVariant[event.event_type] || 'default'}>
                      {eventTypeLabels[event.event_type] || event.event_type}
                    </Badge>
                    <h3 className="mt-2 text-lg font-semibold text-tumtum-text-primary">
                      {event.name}
                    </h3>
                    {event.subtitle && (
                      <p className="text-sm text-tumtum-text-muted">{event.subtitle}</p>
                    )}
                    <div className="mt-2 flex flex-wrap gap-2 text-xs text-tumtum-text-muted">
                      {event.venue && <span>{event.venue}</span>}
                      {event.city && <span>• {event.city}</span>}
                      <span>
                        •{' '}
                        {new Date(event.date).toLocaleDateString('pt-BR', {
                          day: '2-digit',
                          month: 'short',
                          year: 'numeric',
                        })}
                      </span>
                    </div>
                  </Card>
                </Link>
              ))}
            </div>
          )}
        </div>
      </main>
    </>
  )
}
