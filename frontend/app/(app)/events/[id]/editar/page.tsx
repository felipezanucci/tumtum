'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'

import { Nav } from '@/components/layout'
import { Loading } from '@/components/ui'
import { EventForm, type EventFormValues, toApiTime } from '@/components/events/EventForm'
import { events, type TumtumEvent } from '@/lib/api'

/**
 * Correct an event someone already created.
 *
 * There was no way to do this at all — not a missing screen, a missing
 * endpoint. An event typed with the wrong date could only be abandoned and
 * typed again, leaving the wrong one sitting in the list on the night it
 * mattered. And a wrong date is not cosmetic: it is what ties a capture to
 * the moments inside an event.
 */
export default function EditEventPage() {
  const router = useRouter()
  const params = useParams<{ id: string }>()
  const [event, setEvent] = useState<TumtumEvent | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    events
      .get(params.id)
      .then(setEvent)
      .catch((err: unknown) =>
        setError(err instanceof Error ? err.message : 'Não foi possível carregar o evento.'),
      )
  }, [params.id])

  if (error) {
    return (
      <>
        <Nav />
        <main className="min-h-screen bg-tumtum-black">
          <div className="mx-auto max-w-lg px-4 py-8">
            <p className="rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
              {error}
            </p>
          </div>
        </main>
      </>
    )
  }

  if (!event) {
    return (
      <>
        <Nav />
        <main className="min-h-screen bg-tumtum-black">
          <div className="flex justify-center py-16">
            <Loading />
          </div>
        </main>
      </>
    )
  }

  const initial: EventFormValues = {
    name: event.name,
    event_type: event.event_type,
    date: event.date.slice(0, 10),
    venue: event.venue ?? '',
    city: event.city ?? '',
    // The API returns HH:MM:SS; a time input wants HH:MM.
    start_time: event.start_time?.slice(0, 5) ?? '',
    end_time: event.end_time?.slice(0, 5) ?? '',
  }

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-lg px-4 py-8">
          <h1 className="text-3xl font-hero text-tumtum-white">Editar evento</h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            A data e o horário são o que ligam sua batida aos momentos da noite.
          </p>

          <EventForm
            initial={initial}
            submitLabel="Salvar alterações"
            savingLabel="Salvando..."
            onSubmit={async (form) => {
              await events.update(event.id, {
                name: form.name.trim(),
                event_type: form.event_type,
                date: form.date,
                venue: form.venue.trim() || undefined,
                city: form.city.trim() || undefined,
                start_time: toApiTime(form.start_time),
                end_time: toApiTime(form.end_time),
              })
              router.push(`/events/${event.id}`)
            }}
          />
        </div>
      </main>
    </>
  )
}
