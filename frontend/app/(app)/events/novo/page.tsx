'use client'

import { useRouter } from 'next/navigation'

import { Nav } from '@/components/layout'
import { EventForm, emptyEvent, toApiTime } from '@/components/events/EventForm'
import { events } from '@/lib/api'

/**
 * Add an event by hand.
 *
 * The API could always create one; nothing in the app could reach it, so a
 * capture at a real event had no event to attach to and its card read
 * "Evento". At a pilot somebody has to be able to add the night they are
 * standing in, minutes before it starts.
 */
export default function NewEventPage() {
  const router = useRouter()

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-lg px-4 py-8">
          <h1 className="text-3xl font-hero text-tumtum-white">Novo evento</h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            Cadastre a noite antes de começar a capturar. É o que faz seu card dizer onde
            você estava.
          </p>

          <EventForm
            initial={emptyEvent}
            submitLabel="Salvar evento"
            savingLabel="Salvando..."
            onSubmit={async (form) => {
              await events.create({
                name: form.name.trim(),
                event_type: form.event_type,
                date: form.date,
                venue: form.venue.trim() || undefined,
                city: form.city.trim() || undefined,
                country: 'Brasil',
                start_time: toApiTime(form.start_time),
                end_time: toApiTime(form.end_time),
              })
              router.push('/events')
            }}
          />
        </div>
      </main>
    </>
  )
}
