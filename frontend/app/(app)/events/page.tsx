'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useEventStore } from '@/lib/stores/useEventStore'
import { ApiError, demo } from '@/lib/api'
import { Card, Badge, Button, Loading, Input, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'
import { formatDateOnly } from '@/lib/utils/dates'

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
  const { eventList, eventsLoading, eventsError, loadEvents } = useEventStore()
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState<string>('')
  const [seeding, setSeeding] = useState(false)

  useEffect(() => {
    loadEvents({ q: search || undefined, event_type: typeFilter || undefined })
  }, [search, typeFilter, loadEvents])

  async function handleSeed() {
    setSeeding(true)
    try {
      await demo.seed()
      loadEvents()
    } finally {
      setSeeding(false)
    }
  }

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-4xl px-4 py-8">
          <div className="mb-6 flex items-center justify-between gap-4">
            <h1 className="text-3xl font-hero text-tumtum-white">Eventos</h1>
            <Link href="/events/novo">
              <span className="shrink-0 rounded-lg bg-tumtum-lime px-4 py-2 text-sm font-label text-tumtum-black transition-colors hover:bg-tumtum-yellow">
                Novo evento
              </span>
            </Link>
          </div>

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
                      ? 'bg-tumtum-lime text-tumtum-black'
                      : 'bg-tumtum-surface text-tumtum-muted hover:text-tumtum-white'
                  }`}
                >
                  {type === '' ? 'Todos' : eventTypeLabels[type]}
                </button>
              ))}
            </div>
          </div>

          {/* Event List */}
          {eventsError instanceof ApiError && eventsError.status === 401 ? (
            <SignInRequired what="seus eventos" />
          ) : eventsError ? (
            <p className="mt-6 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
              {eventsError.message}
            </p>
          ) : eventsLoading ? (
            <Loading size="lg" className="py-20" />
          ) : eventList.length === 0 ? (
            <div className="py-20 text-center">
              <p className="text-lg text-tumtum-muted">Nenhum evento encontrado</p>
              <p className="mt-1 text-sm text-tumtum-muted">
                {search || typeFilter
                  ? 'Tente ajustar os filtros ou buscar por outro termo.'
                  : 'Popule o banco com eventos de demonstração para começar.'}
              </p>
              {!search && !typeFilter && (
                <Button
                  className="mt-4"
                  onClick={handleSeed}
                  disabled={seeding}
                >
                  {seeding ? 'Criando eventos...' : 'Carregar Eventos de Demo'}
                </Button>
              )}
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {eventList.map((event) => (
                <Link key={event.id} href={`/events/${event.id}`}>
                  <Card hoverable className="h-full">
                    {event.cover_image_url && (
                      <div className="mb-3 h-32 overflow-hidden rounded-lg bg-tumtum-black">
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
                    <h3 className="mt-2 text-lg font-semibold text-tumtum-white">
                      {event.name}
                    </h3>
                    {event.subtitle && (
                      <p className="text-sm text-tumtum-muted">{event.subtitle}</p>
                    )}
                    <div className="mt-2 flex flex-wrap gap-2 text-xs text-tumtum-muted">
                      {event.venue && <span>{event.venue}</span>}
                      {event.city && <span>• {event.city}</span>}
                      <span>
                        •{' '}
                        {formatDateOnly(event.date, {
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
