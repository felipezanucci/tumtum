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

const HOURS = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'))

/**
 * Minutes in fives.
 *
 * Every minute would be sixty options to scroll past, and shows are not
 * scheduled at 22:07. If an event ever needs one, this is the line to change.
 */
const MINUTES = Array.from({ length: 12 }, (_, i) => String(i * 5).padStart(2, '0'))

/**
 * An hour and a minute, as two lists.
 *
 * This was `<input type="time">`, which hands the job to whatever the phone
 * feels like showing — on the Samsung this app is tested on, a clock face with
 * a hand to drag, which is a poor way to say "22:00" and worse in a hurry.
 * Two selects look and behave the same everywhere and can be read at a glance.
 */
export function TimeField({
  id,
  label,
  value,
  onChange,
}: {
  id: string
  label: string
  value: string
  onChange: (value: string) => void
}) {
  const [hour = '', minute = ''] = value ? value.split(':') : []

  // An event saved elsewhere can hold a minute the list does not offer. Left
  // alone, the select would fall back to "--" and quietly claim the event has
  // no time — the same lie this project keeps finding in empty states. Show
  // the real value instead, even when it is off the step.
  const minutes = minute && !MINUTES.includes(minute) ? [minute, ...MINUTES] : MINUTES

  const select =
    'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-2 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none'

  function set(nextHour: string, nextMinute: string) {
    // Clearing both means "no time", which is a real answer — the column is
    // nullable and an event without hours is allowed.
    if (!nextHour && !nextMinute) {
      onChange('')
      return
    }
    // Half a time is not a time. Filling the missing half with zero is what
    // somebody picking "22" and nothing else means, and it beats silently
    // discarding what they picked.
    onChange(`${nextHour || '00'}:${nextMinute || '00'}`)
  }

  return (
    <div>
      <label className="mb-1 block text-sm text-tumtum-muted" htmlFor={`${id}-hour`}>
        {label}
      </label>
      <div className="flex items-center gap-2">
        <select
          id={`${id}-hour`}
          aria-label={`${label} — hora`}
          className={select}
          value={hour}
          onChange={(e) => set(e.target.value, minute)}
        >
          <option value="">--</option>
          {HOURS.map((h) => (
            <option key={h} value={h}>
              {h}
            </option>
          ))}
        </select>
        <span className="text-tumtum-muted">:</span>
        <select
          id={`${id}-minute`}
          aria-label={`${label} — minuto`}
          className={select}
          value={minute}
          onChange={(e) => set(hour, e.target.value)}
        >
          <option value="">--</option>
          {minutes.map((m) => (
            <option key={m} value={m}>
              {m}
            </option>
          ))}
        </select>
      </div>
    </div>
  )
}

/** The months, named. "set" is read at a glance; "09" has to be counted. */
const MONTHS = [
  'jan', 'fev', 'mar', 'abr', 'mai', 'jun',
  'jul', 'ago', 'set', 'out', 'nov', 'dez',
]

/** How many days that month really has, leap years included. */
function daysIn(year: number, month: number): number {
  if (!year || !month) return 31
  return new Date(year, month, 0).getDate()
}

/**
 * A date as three lists, in the order this country writes it: dia, mês, ano.
 *
 * This was `<input type="date">`, and on Felipe's machine it rendered the
 * American order — month first — with the day typed into it. Two standing
 * rules meet here: **a time is never typed** (21/09), and every date or time
 * field is a picker rather than a free-text box, operator screens included.
 * `type="date"` obeys neither reliably: its order follows the browser's
 * locale, not the product's, and it accepts typing.
 *
 * The value stays ISO (`YYYY-MM-DD`) because that is what the API reads; only
 * the reading order changes.
 */
export function DateField({
  id,
  label,
  value,
  onChange,
}: {
  id: string
  label: string
  value: string
  onChange: (value: string) => void
}) {
  const [year = '', month = '', day = ''] = value ? value.split('-') : []
  const now = new Date().getFullYear()

  // Near-term events, plus whatever this one already holds — an event from
  // another year must never be silently dropped to "--".
  const years: number[] = []
  for (let y = now - 1; y <= now + 2; y++) years.push(y)
  const held = Number(year)
  if (held && !years.includes(held)) years.push(held)
  years.sort((a, b) => a - b)

  const lastDay = daysIn(Number(year) || now, Number(month))
  const days = Array.from({ length: lastDay }, (_, i) => String(i + 1).padStart(2, '0'))

  // Each select keeps a floor wide enough for its longest value plus the
  // native arrow. Without one, a narrow column (the football search's date,
  // on a desktop browser) shared the width three ways and cut "2026" to
  // "202" (#50, 23/09).
  const select =
    'min-w-0 rounded-lg border border-tumtum-border bg-tumtum-surface px-2 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none'

  function set(nextDay: string, nextMonth: string, nextYear: string) {
    if (!nextDay && !nextMonth && !nextYear) {
      onChange('')
      return
    }
    const y = nextYear || String(now)
    const m = nextMonth || '01'
    // 31 de janeiro, then February: clamp rather than drop. Picking a month
    // must not silently wipe the day somebody already chose.
    const max = daysIn(Number(y), Number(m))
    const d = Math.min(Number(nextDay) || 1, max)
    onChange(`${y}-${m}-${String(d).padStart(2, '0')}`)
  }

  return (
    <div>
      <label className="mb-1 block text-sm text-tumtum-muted" htmlFor={`${id}-day`}>
        {label}
      </label>
      <div className="flex items-center gap-2">
        <select
          id={`${id}-day`}
          aria-label={`${label} — dia`}
          className={`${select} flex-1 min-w-[4.75rem]`}
          value={day}
          onChange={(e) => set(e.target.value, month, year)}
        >
          <option value="">--</option>
          {days.map((d) => (
            <option key={d} value={d}>
              {d}
            </option>
          ))}
        </select>
        <select
          id={`${id}-month`}
          aria-label={`${label} — mês`}
          className={`${select} flex-[1.2] min-w-[5.5rem]`}
          value={month}
          onChange={(e) => set(day, e.target.value, year)}
        >
          <option value="">--</option>
          {MONTHS.map((name, i) => (
            <option key={name} value={String(i + 1).padStart(2, '0')}>
              {name}
            </option>
          ))}
        </select>
        <select
          id={`${id}-year`}
          aria-label={`${label} — ano`}
          className={`${select} flex-[1.4] min-w-[6.5rem]`}
          value={year}
          onChange={(e) => set(day, month, e.target.value)}
        >
          <option value="">--</option>
          {years.map((y) => (
            <option key={y} value={String(y)}>
              {y}
            </option>
          ))}
        </select>
      </div>
    </div>
  )
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
    'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none'
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

        <div className="mt-4">
          <DateField
            id="date"
            label="Data"
            value={form.date}
            onChange={(value) => set('date', value)}
          />
        </div>

        <div className="mt-4 grid grid-cols-2 gap-3">
          <TimeField
            id="start"
            label="Começa"
            value={form.start_time}
            onChange={(value) => set('start_time', value)}
          />
          <TimeField
            id="end"
            label="Termina"
            value={form.end_time}
            onChange={(value) => set('end_time', value)}
          />
        </div>

        {/*
          A night that crosses midnight needs no second date: an end earlier
          than the start can only mean the next day for any event under 24
          hours, so the form states that reading instead of asking for it.
          Storage stays one date and two times — the alternative is a schema
          change, and the deployed app cannot apply one (startup runs
          create_all, which never alters existing tables).
        */}
        {crossesMidnight(form) && (
          <p className="mt-2 text-xs text-tumtum-pink">
            Termina na madrugada do dia seguinte ({nextDayLabel(form.date)}).
          </p>
        )}

        <label className={`${label} mt-4`} htmlFor="venue">
          Local
        </label>
        <input
          id="venue"
          className={field}
          value={form.venue}
          onChange={(e) => set('venue', e.target.value)}
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

/** An end earlier than the start means the small hours of the next day. */
function crossesMidnight(form: EventFormValues): boolean {
  return Boolean(form.start_time && form.end_time && form.end_time < form.start_time)
}

/** "30/08", the day after the event's date. Parsed as UTC noon to dodge DST edges. */
function nextDayLabel(date: string): string {
  const parsed = new Date(`${date}T12:00:00Z`)
  if (Number.isNaN(parsed.getTime())) return 'dia seguinte'
  parsed.setUTCDate(parsed.getUTCDate() + 1)
  const day = String(parsed.getUTCDate()).padStart(2, '0')
  const month = String(parsed.getUTCMonth() + 1).padStart(2, '0')
  return `${day}/${month}`
}

/** The API wants seconds; the picker gives HH:MM. */
export function toApiTime(value: string): string | undefined {
  return value ? `${value}:00` : undefined
}
