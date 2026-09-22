'use client'

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'
import { useParams } from 'next/navigation'

import {
  ApiError,
  events,
  type EventDetail,
  type FixtureBrief,
  type SetlistBrief,
  type TimelineEntry,
} from '@/lib/api'
import { useCurrentUser } from '@/lib/hooks/useCurrentUser'
import { TimeField } from '@/components/events/EventForm'
import { Badge, Button, Loading, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'
import { formatDateOnly } from '@/lib/utils/dates'

/**
 * Operação › one event — its timeline, and where the timeline comes from.
 *
 * A moment gets its name from this list (`docs/naming-moments-plan.md`). Two
 * kinds of entry live here and the page keeps them apart, because the
 * server does:
 *
 * - **Exact** — an operator's tap during the capture (GOL, APITO INICIAL,
 *   2º TEMPO), a match minute anchored on those taps, a name a person typed.
 *   These *assert*: the correlator names a moment by them.
 * - **Estimated** — a setlist's order at four minutes a song, a match built
 *   from the schedule with no anchor. These only *offer*: the app shows them
 *   as "tava tocando uma dessas?" and never puts one on a card unasked.
 *
 * Everything here is operator-only on the server; this page says so instead
 * of showing an empty list.
 */

// Timestamps are stored in UTC; the operator reads and types São Paulo time.
// Brazil has had no daylight saving since 2019, so the offset is fixed.
const TZ = 'America/Sao_Paulo'
const OFFSET = '-03:00'

const entryIcon: Record<string, string> = {
  song_start: '🎵',
  goal: '⚽',
  halftime: '⏸',
  encore: '🔥',
  highlight: '⭐',
  kickoff: '⏱',
  second_half: '⏱',
}

const entryTypes = [
  { value: 'highlight', label: 'Momento' },
  { value: 'goal', label: 'Gol' },
  { value: 'song_start', label: 'Música' },
  { value: 'kickoff', label: 'Apito inicial' },
  { value: 'second_half', label: 'Começo do 2º tempo' },
  { value: 'halftime', label: 'Intervalo' },
  { value: 'encore', label: 'Bis' },
]

function clock(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', timeZone: TZ })
}

function isEstimated(entry: TimelineEntry): boolean {
  const m = entry.metadata ?? {}
  return m.estimated === true || m.anchored === false
}

function message(err: unknown, fallback: string): string {
  return err instanceof Error ? err.message : fallback
}

export default function AdminEventPage() {
  const params = useParams<{ id: string }>()
  const user = useCurrentUser()
  const [event, setEvent] = useState<EventDetail | null>(null)
  const [needsSignIn, setNeedsSignIn] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(() => {
    return events
      .get(params.id)
      .then(setEvent)
      .catch((err: unknown) => {
        if (err instanceof ApiError && err.status === 401) {
          setNeedsSignIn(true)
          return
        }
        setError(message(err, 'Não foi possível carregar o evento.'))
      })
  }, [params.id])

  useEffect(() => {
    load()
  }, [load])

  const operator = user?.is_admin === true

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-2xl px-4 py-8">
          <Link href="/admin/eventos" className="text-xs uppercase tracking-wider text-tumtum-muted">
            ← Operação · Eventos
          </Link>

          {needsSignIn && <SignInRequired what="este evento" />}
          {error && (
            <p className="mt-6 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
              {error}
            </p>
          )}
          {!event && !needsSignIn && !error && (
            <div className="mt-10 flex justify-center">
              <Loading />
            </div>
          )}

          {event && (
            <>
              <h1 className="mt-2 text-3xl font-hero text-tumtum-white">{event.name}</h1>
              <p className="mt-1 text-sm text-tumtum-muted tabular-nums">
                {formatDateOnly(event.date, { weekday: 'long', day: '2-digit', month: 'long' })}
                {event.start_time ? ` · ${event.start_time.slice(0, 5)}` : ' · sem horário de começo'}
                {event.end_time ? ` – ${event.end_time.slice(0, 5)}` : ''}
                {event.venue ? ` · ${event.venue}` : ''}
              </p>
              <div className="mt-3 flex items-center gap-3">
                <Badge variant={event.event_type === 'sports' ? 'success' : 'accent'}>
                  {event.event_type === 'sports' ? 'Jogo' : event.event_type === 'festival' ? 'Festival' : 'Show'}
                </Badge>
                {operator && (
                  <Link href={`/events/${event.id}/editar`} className="text-sm text-tumtum-pink">
                    Editar dados
                  </Link>
                )}
              </div>

              {user && !operator && (
                <div className="mt-6 rounded-lg border border-tumtum-border bg-tumtum-surface p-4">
                  <p className="font-headline text-tumtum-white">Sua conta não opera a plataforma.</p>
                  <p className="mt-2 text-sm text-tumtum-muted">
                    A linha do tempo abaixo é a do evento; mudar qualquer coisa nela é
                    liberado conta por conta, no servidor.
                  </p>
                </div>
              )}

              <Timeline event={event} operator={operator} onChange={load} />

              {operator && event.event_type === 'sports' && (
                <FootballSource event={event} onChange={load} />
              )}
              {operator && event.event_type !== 'sports' && (
                <SetlistSource event={event} onChange={load} />
              )}
              {operator && <ManualEntry event={event} onChange={load} />}
            </>
          )}
        </div>
      </main>
    </>
  )
}

/** The entries, exact and estimated told apart, each one removable. */
function Timeline({
  event,
  operator,
  onChange,
}: {
  event: EventDetail
  operator: boolean
  onChange: () => Promise<void>
}) {
  const [removing, setRemoving] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function remove(entry: TimelineEntry) {
    if (!window.confirm(`Tirar "${entry.label}" (${clock(entry.timestamp)}) da linha do tempo?`)) return
    setRemoving(entry.id)
    setError(null)
    try {
      await events.deleteTimelineEntry(event.id, entry.id)
      await onChange()
    } catch (err) {
      setError(message(err, 'Não foi possível remover.'))
    } finally {
      setRemoving(null)
    }
  }

  const kickoff = event.timeline.find((e) => e.entry_type === 'kickoff' && !isEstimated(e))
  const second = event.timeline.find((e) => e.entry_type === 'second_half' && !isEstimated(e))

  return (
    <section className="mt-8">
      <h2 className="text-sm font-medium uppercase tracking-wider text-tumtum-muted">
        Linha do tempo
      </h2>

      {event.event_type === 'sports' && (
        // The two anchors decide whether a match minute is a wall-clock time
        // or a guess. Their absence is said, not left blank.
        <div className="mt-3 grid grid-cols-2 gap-3 text-sm">
          <Anchor label="Apito inicial" entry={kickoff} />
          <Anchor label="2º tempo" entry={second} />
        </div>
      )}

      {event.timeline.length === 0 ? (
        <p className="mt-3 text-sm text-tumtum-muted">
          Nada ainda. As marcas do celular, um jogo ou um setlist entram aqui.
        </p>
      ) : (
        <ul className="mt-3 divide-y divide-tumtum-border rounded-lg border border-tumtum-border">
          {event.timeline.map((entry) => {
            const estimated = isEstimated(entry)
            return (
              <li key={entry.id} className="flex items-center gap-3 px-3 py-2">
                <span className="w-6 text-center">{entryIcon[entry.entry_type] ?? '•'}</span>
                <span className="w-12 shrink-0 text-sm text-tumtum-muted tabular-nums">
                  {clock(entry.timestamp)}
                </span>
                <span className={`min-w-0 flex-1 truncate text-sm ${estimated ? 'text-tumtum-muted' : 'text-tumtum-white'}`}>
                  {entry.label}
                </span>
                {estimated && (
                  <span className="shrink-0 rounded bg-tumtum-surface px-1.5 py-0.5 text-[10px] uppercase tracking-wider text-tumtum-yellow">
                    estimado
                  </span>
                )}
                {operator && (
                  <button
                    type="button"
                    onClick={() => remove(entry)}
                    disabled={removing === entry.id}
                    className="shrink-0 text-xs text-tumtum-muted hover:text-red-400 disabled:opacity-50"
                    aria-label={`Remover ${entry.label}`}
                  >
                    {removing === entry.id ? '…' : 'tirar'}
                  </button>
                )}
              </li>
            )
          })}
        </ul>
      )}
      {event.timeline.some(isEstimated) && (
        <p className="mt-2 text-xs text-tumtum-muted">
          <span className="text-tumtum-yellow">estimado</span> = horário derivado, não medido.
          No app vira um palpite pra pessoa escolher, nunca um nome no card.
        </p>
      )}
      {error && <p className="mt-2 text-sm text-red-400">{error}</p>}
    </section>
  )
}

function Anchor({ label, entry }: { label: string; entry: TimelineEntry | undefined }) {
  return (
    <div className="rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2">
      <p className="text-xs uppercase tracking-wider text-tumtum-muted">{label}</p>
      {entry ? (
        <p className="mt-1 font-headline text-tumtum-white tabular-nums">{clock(entry.timestamp)}</p>
      ) : (
        <p className="mt-1 text-xs text-tumtum-yellow">
          Não marcado. Sem ele, os minutos deste tempo vêm do horário agendado.
        </p>
      )}
    </div>
  )
}

/** Find the match on API-Football and build the timeline from it. */
function FootballSource({ event, onChange }: { event: EventDetail; onChange: () => Promise<void> }) {
  const [team, setTeam] = useState('')
  const [on, setOn] = useState(event.date.slice(0, 10))
  const [results, setResults] = useState<FixtureBrief[] | null>(null)
  const [busy, setBusy] = useState<'search' | number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const attached = event.external_id?.startsWith('api-football:')
    ? Number(event.external_id.split(':')[1])
    : null

  async function search() {
    setBusy('search')
    setError(null)
    try {
      setResults(await events.searchFixtures({ team: team.trim() || undefined, on: on || undefined }))
    } catch (err) {
      setError(message(err, 'A busca falhou.'))
    } finally {
      setBusy(null)
    }
  }

  async function attach(fixtureId: number) {
    setBusy(fixtureId)
    setError(null)
    try {
      await events.attachFixture(event.id, fixtureId)
      setResults(null)
      await onChange()
    } catch (err) {
      setError(message(err, 'Não foi possível montar a linha do tempo.'))
    } finally {
      setBusy(null)
    }
  }

  const field =
    'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none'

  return (
    <section className="mt-8">
      <h2 className="text-sm font-medium uppercase tracking-wider text-tumtum-muted">
        Jogo · API-Football
      </h2>
      <p className="mt-1 text-sm text-tumtum-muted">
        Os gols e cartões do jogo entram na linha do tempo. Com os dois toques do
        operador (apito inicial, 2º tempo) o minuto vira hora exata; sem eles, vira
        um palpite marcado como estimado. Pode rodar de novo depois do jogo: só as
        linhas deste jogo são trocadas.
      </p>
      {attached && (
        <p className="mt-2 text-sm text-tumtum-white">
          Jogo ligado: <span className="tabular-nums">#{attached}</span>{' '}
          <button
            type="button"
            onClick={() => attach(attached)}
            disabled={busy !== null}
            className="ml-2 text-tumtum-pink disabled:opacity-50"
          >
            {busy === attached ? 'Remontando…' : 'Remontar com as âncoras de agora'}
          </button>
        </p>
      )}
      <div className="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-[1fr_auto_auto]">
        <input
          className={field}
          placeholder="Time (Corinthians, Palmeiras…)"
          value={team}
          onChange={(e) => setTeam(e.target.value)}
        />
        <input className={field} type="date" value={on} onChange={(e) => setOn(e.target.value)} />
        <Button type="button" onClick={search} disabled={busy !== null}>
          {busy === 'search' ? 'Buscando…' : 'Buscar'}
        </Button>
      </div>
      {results && results.length === 0 && (
        <p className="mt-3 text-sm text-tumtum-muted">Nenhum jogo com esses dados.</p>
      )}
      {results && results.length > 0 && (
        <ul className="mt-3 space-y-2">
          {results.map((f) => (
            <li
              key={f.fixture_id}
              className="flex items-center justify-between gap-3 rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2"
            >
              <div className="min-w-0">
                <p className="truncate text-sm text-tumtum-white">
                  {f.home} × {f.away}
                </p>
                <p className="text-xs text-tumtum-muted">
                  {f.kickoff ? `${clock(f.kickoff)} · ` : ''}
                  {f.league ?? ''}
                  {f.status ? ` · ${f.status}` : ''}
                </p>
              </div>
              <Button size="sm" type="button" onClick={() => attach(f.fixture_id)} disabled={busy !== null}>
                {busy === f.fixture_id ? 'Montando…' : 'Usar este'}
              </Button>
            </li>
          ))}
        </ul>
      )}
      {error && <p className="mt-2 text-sm text-red-400">{error}</p>}
    </section>
  )
}

/** Find the setlist on Setlist.fm; every song comes in as an estimate. */
function SetlistSource({ event, onChange }: { event: EventDetail; onChange: () => Promise<void> }) {
  const [artist, setArtist] = useState(event.name)
  const [on, setOn] = useState(event.date.slice(0, 10))
  const [results, setResults] = useState<SetlistBrief[] | null>(null)
  const [busy, setBusy] = useState<'search' | string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const attached = event.external_id?.startsWith('setlist.fm:')
    ? event.external_id.split(':')[1]
    : null

  async function search() {
    setBusy('search')
    setError(null)
    try {
      setResults(await events.searchSetlists({ artist: artist.trim(), on: on || undefined }))
    } catch (err) {
      setError(message(err, 'A busca falhou.'))
    } finally {
      setBusy(null)
    }
  }

  async function attach(setlistId: string) {
    setBusy(setlistId)
    setError(null)
    try {
      await events.attachSetlist(event.id, setlistId)
      setResults(null)
      await onChange()
    } catch (err) {
      setError(message(err, 'Não foi possível montar a linha do tempo.'))
    } finally {
      setBusy(null)
    }
  }

  const field =
    'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none'

  return (
    <section className="mt-8">
      <h2 className="text-sm font-medium uppercase tracking-wider text-tumtum-muted">
        Setlist · Setlist.fm
      </h2>
      <p className="mt-1 text-sm text-tumtum-muted">
        O Setlist.fm tem a ordem das músicas e nunca o horário. Cada música entra
        estimada — quatro minutos a partir do começo do evento — e no app vira
        &ldquo;tava tocando uma dessas?&rdquo;, nunca um nome no card.
      </p>
      {/*
        An offer the page must not make silently. Researched 22/09 (open item
        44): the Setlist.fm API terms forbid keeping their data in our own
        database — which is what this button does — and the free key is
        non-commercial only, by purpose rather than by revenue. Offering the
        button with no warning is the same class of defect as an empty state
        that claims "nothing there": the operator would have no way to know.
      */}
      <p className="mt-2 rounded-lg border border-tumtum-yellow/40 bg-tumtum-yellow/5 p-3 text-sm text-tumtum-yellow">
        <strong>Só para teste.</strong> Os termos do Setlist.fm proíbem guardar os
        dados deles no nosso banco — que é o que este botão faz — e a chave grátis
        é só para uso não comercial. Vale para a chave paga também. Para valer em
        evento real, a ordem das músicas tem que vir de outro lugar (alguém
        digitando, por exemplo). Item 44 do log de decisões.
      </p>
      {!event.start_time && (
        <p className="mt-2 text-sm text-tumtum-yellow">
          O evento precisa de um horário de começo antes: é dele que a estimativa parte.
        </p>
      )}
      {attached && (
        <p className="mt-2 text-sm text-tumtum-white">
          Setlist ligado: <span className="font-mono">{attached}</span>
        </p>
      )}
      <div className="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-[1fr_auto_auto]">
        <input
          className={field}
          placeholder="Artista"
          value={artist}
          onChange={(e) => setArtist(e.target.value)}
        />
        <input className={field} type="date" value={on} onChange={(e) => setOn(e.target.value)} />
        <Button type="button" onClick={search} disabled={busy !== null || artist.trim().length < 2}>
          {busy === 'search' ? 'Buscando…' : 'Buscar'}
        </Button>
      </div>
      {results && results.length === 0 && (
        <p className="mt-3 text-sm text-tumtum-muted">Nenhum setlist com esses dados.</p>
      )}
      {results && results.length > 0 && (
        <ul className="mt-3 space-y-2">
          {results.map((s) => (
            <li
              key={s.setlist_id}
              className="flex items-center justify-between gap-3 rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2"
            >
              <div className="min-w-0">
                <p className="truncate text-sm text-tumtum-white">
                  {s.artist} · {s.event_date}
                </p>
                <p className="text-xs text-tumtum-muted">
                  {[s.venue, s.city].filter(Boolean).join(', ')}
                  {` · ${s.song_count} músicas`}
                </p>
              </div>
              <Button
                size="sm"
                type="button"
                onClick={() => attach(s.setlist_id)}
                disabled={busy !== null || !event.start_time || s.song_count === 0}
              >
                {busy === s.setlist_id ? 'Montando…' : 'Usar este'}
              </Button>
            </li>
          ))}
        </ul>
      )}
      {error && <p className="mt-2 text-sm text-red-400">{error}</p>}
    </section>
  )
}

/** One entry by hand — a time from the picker, never typed. */
function ManualEntry({ event, onChange }: { event: EventDetail; onChange: () => Promise<void> }) {
  const [label, setLabel] = useState('')
  const [type, setType] = useState('highlight')
  const [date, setDate] = useState(event.date.slice(0, 10))
  const [time, setTime] = useState(event.start_time?.slice(0, 5) ?? '')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function add(e: React.FormEvent) {
    e.preventDefault()
    if (!label.trim() || !time) {
      setError('Dê um nome e um horário à marca.')
      return
    }
    setBusy(true)
    setError(null)
    try {
      await events.addTimelineEntry(event.id, {
        timestamp: `${date}T${time}:00${OFFSET}`,
        label: label.trim(),
        entry_type: type,
        metadata: { source: 'operator-web' },
      })
      setLabel('')
      await onChange()
    } catch (err) {
      setError(message(err, 'Não foi possível adicionar.'))
    } finally {
      setBusy(false)
    }
  }

  const field =
    'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none'

  return (
    <form onSubmit={add} className="mt-8">
      <h2 className="text-sm font-medium uppercase tracking-wider text-tumtum-muted">
        Marca na mão
      </h2>
      <p className="mt-1 text-sm text-tumtum-muted">
        Um horário exato que você sabe — o apito de um jogo que ninguém marcou no
        celular, a música que abriu o show. Entra como exato e dá nome ao momento.
      </p>
      <div className="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
        <input
          className={field}
          placeholder="O que foi (Gol do Yuri, Apito inicial…)"
          value={label}
          onChange={(e) => setLabel(e.target.value)}
        />
        <select className={field} value={type} onChange={(e) => setType(e.target.value)}>
          {entryTypes.map((t) => (
            <option key={t.value} value={t.value}>
              {t.label}
            </option>
          ))}
        </select>
        <input className={field} type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        <TimeField id="manual" label="Hora (São Paulo)" value={time} onChange={setTime} />
      </div>
      {error && <p className="mt-2 text-sm text-red-400">{error}</p>}
      <Button type="submit" className="mt-3" disabled={busy}>
        {busy ? 'Adicionando…' : 'Adicionar marca'}
      </Button>
    </form>
  )
}
