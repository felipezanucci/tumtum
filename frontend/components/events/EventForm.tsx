'use client'

import { useState } from 'react'

import { Button, Card } from '@/components/ui'

export interface EventFormValues {
  name: string
  event_type: string
  date: string
  venue: string
  city: string
  start_time: string
  end_time: string
}

const TYPES = [
  { value: 'concert', label: 'Show' },
  { value: 'festival', label: 'Festival' },
  { value: 'sports', label: 'Jogo' },
]

export const emptyEvent: EventFormValues = {
  name: '',
  event_type: 'concert',
  date: new Date().toISOString().slice(0, 10),
  venue: '',
  city: 'São Paulo',
  start_time: '',
  end_time: '',
}

/**
 * The fields of an event, shared by creating one and correcting one.
 *
 * Correcting matters as much as creating: the date is what ties a capture to
 * the moments inside an event, and a night typed wrong could only be
 * abandoned and typed again.
 */
export function EventForm({
  initial,
  submitLabel,
  savingLabel,
  onSubmit,
}: {
  initial: EventFormValues
  submitLabel: string
  savingLabel: string
  onSubmit: (values: EventFormValues) => Promise<void>
}) {
  const [form, setForm] = useState(initial)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function set(field: keyof EventFormValues, value: string) {
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
      await onSubmit(form)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não foi possível salvar.')
      setSaving(false)
    }
  }

  const field =
    'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-lime focus:outline-none'
  const label = 'mb-1 block text-sm text-tumtum-muted'

  return (
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
        {saving ? savingLabel : submitLabel}
      </Button>
    </form>
  )
}

/** The API wants seconds; a time input gives HH:MM. */
export function toApiTime(value: string): string | undefined {
  return value ? `${value}:00` : undefined
}
