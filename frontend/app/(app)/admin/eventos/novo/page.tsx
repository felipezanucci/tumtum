'use client'

import { useRouter } from 'next/navigation'
import Link from 'next/link'

import { Nav } from '@/components/layout'
import { EventForm, emptyEvent, toApiTime } from '@/components/events/EventForm'
import { events } from '@/lib/api'

/**
 * Operação › Eventos › Novo — register an event TumTum covers.
 *
 * Moved here from `/events/novo` on 2026-09-22. Two things were wrong with
 * where it used to live. It sat inside the fan's part of the site, which the
 * product rule forbids — **events are TumTum's, the fan never creates one**
 * (21/09) — and it spoke in the fan's voice, about "a noite que você vai
 * capturar". And on saving it pushed to `/events`, the fan's list, so the
 * operator was dropped out of the admin mid-job: Felipe read it as being
 * signed out and had to type `/admin/eventos` back in by hand.
 *
 * Saving now opens the event's own operator page, which is where the work
 * continues — timeline, setlist, the match.
 */
export default function NewAdminEventPage() {
  const router = useRouter()

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-lg px-4 py-8">
          <Link
            href="/admin/eventos"
            className="text-sm text-tumtum-muted hover:text-tumtum-white"
          >
            ← Eventos
          </Link>
          <h1 className="mt-3 text-3xl font-hero text-tumtum-white">Novo evento</h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            O evento aparece para quem estiver lá, para ser ativado. Depois de salvar
            você cai na página dele, onde entram o setlist, o jogo e a linha do tempo.
          </p>

          <EventForm
            initial={emptyEvent}
            submitLabel="Salvar evento"
            savingLabel="Salvando..."
            onSubmit={async (form) => {
              const created = await events.create({
                name: form.name.trim(),
                event_type: form.event_type,
                date: form.date,
                venue: form.venue.trim() || undefined,
                city: form.city.trim() || undefined,
                country: 'Brasil',
                start_time: toApiTime(form.start_time),
                end_time: toApiTime(form.end_time),
              })
              router.push(`/admin/eventos/${created.id}`)
            }}
          />
        </div>
      </main>
    </>
  )
}
