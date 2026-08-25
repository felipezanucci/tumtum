'use client'

import { useRouter } from 'next/navigation'
import { useState } from 'react'

import { Nav } from '@/components/layout'
import { Button, Card } from '@/components/ui'
import { events } from '@/lib/api'

/**
 * Add an event by hand.
 *
 * The API could always create one; nothing in the app could reach it, so a
 * capture at a real event had no event to attach to and its card read
 * "Evento". At a pilot somebody has to be able to add the night they are
 * standing in, minutes before it starts.
 */

const TYPES = [
  { value: 'concert', label: 'Show' },
  { value: 'festival', label: 'Festival' },
  { value: 'sports', label: 'Jogo' },
]

export default function NewEventPage() {
  const router = useRouter()
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({
    name: '',
    event_type: 'concert',
    date: new Date().toISOString().slice(0, 10),
    venue: '',
    city: 'São Paulo',
    start_time: '',
    end_time: '',
  })

  function set(field: keyof typeof form, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!form.name.trim()) {
      setError('Dê um nome ao evento.')
      return
    }
    setSaving(true)
    setError(null)
    try {
      await events.create({
        name: form.name.trim(),
        event_type: form.event_type,
        date: form.date,
        venue: form.venue.trim() || undefined,
        city: form.city.trim() || undefined,
        country: 'Brasil',
        // The API wants seconds; a time input gives HH:MM.
        start_time: form.start_time ? `${form.start_time}:00` : undefined,
        end_time: form.end_time ? `${form.end_time}:00` : undefined,
      })
      router.push('/events')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não foi possível salvar.')
      setSaving(false)
    }
  }

  const field =
    'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-lime focus:outline-none'
  const label = 'mb-1 block text-sm text-tumtum-muted'

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

          <form onSubmit={handleSubmit}>
            <Card className="mt-6">
              <label className={label} htmlFor="name">
                Nome
              </label>
              <input
                id="name"
                className={field}
                value={form.name}
                onChange={(e) => set('name', e.target.value)}
                placeholder="Realness Festival 2026"
                autoFocus
              />

              <label className={`${label} mt-4`} htmlFor="type">
                Tipo
              </label>
              <select
                id="type"
                className={field}
                value={form.event_type}
                onChange={(e) => set('event_type', e.target.value)}
              >
                {TYPES.map((t) => (
                  <option key={t.value} value={t.value}>
                    {t.label}
                  </option>
                ))}
              </select>

              <label className={`${label} mt-4`} htmlFor="date">
                Data
              </label>
              <input
                id="date"
                type="date"
                className={field}
                value={form.date}
                onChange={(e) => set('date', e.target.value)}
              />

              <div className="mt-4 grid grid-cols-2 gap-3">
                <div>
                  <label className={label} htmlFor="start">
                    Começa
                  </label>
                  <input
                    id="start"
                    type="time"
                    className={field}
                    value={form.start_time}
                    onChange={(e) => set('start_time', e.target.value)}
                  />
                </div>
                <div>
                  <label className={label} htmlFor="end">
                    Termina
                  </label>
                  <input
                    id="end"
                    type="time"
                    className={field}
                    value={form.end_time}
                    onChange={(e) => set('end_time', e.target.value)}
                  />
                </div>
              </div>

              <label className={`${label} mt-4`} htmlFor="venue">
                Local
              </label>
              <input
                id="venue"
                className={field}
                value={form.venue}
                onChange={(e) => set('venue', e.target.value)}
                placeholder="Vibra São Paulo"
              />

              <label className={`${label} mt-4`} htmlFor="city">
                Cidade
              </label>
              <input
                id="city"
                className={field}
                value={form.city}
                onChange={(e) => set('city', e.target.value)}
              />
            </Card>

            {error && (
              <p className="mt-4 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
                {error}
              </p>
            )}

            <Button type="submit" size="lg" loading={saving} className="mt-6 w-full">
              {saving ? 'Salvando...' : 'Salvar evento'}
            </Button>
          </form>
        </div>
      </main>
    </>
  )
}
