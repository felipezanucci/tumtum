'use client'

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'
import { useParams } from 'next/navigation'

import {
  ApiError,
  events,
  type EventDetail,
  type FixtureBrief,
  type MatchWatch,
  type SetlistSong,
  type TimelineEntry,
  series,
  type SeriesBrief,
} from '@/lib/api'
import { useCurrentUser } from '@/lib/hooks/useCurrentUser'
import { DateField, TimeField } from '@/components/events/EventForm'
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
 * - **Estimated** — a match built from the schedule with no anchor. These
 *   name nothing: the correlator never sees them, and the moment stays
 *   unnamed rather than carrying a guess (22/09 — the app either knows or
 *   says nothing, and never asks the fan to remember).
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
                  <Link href={`/admin/eventos/${event.id}/editar`} className="text-sm text-tumtum-pink">
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

              {/* Football stays one feed per match for now (Felipe, 23/09): a
                  derby belongs to two clubs. Only shows join a tour. */}
              {operator && event.event_type !== 'sports' && <SeriesSection event={event} />}

              <Timeline event={event} operator={operator} onChange={load} />

              {operator && event.event_type === 'sports' && (
                <FootballSource event={event} onChange={load} />
              )}
              {operator && event.event_type !== 'sports' && (
                <ShowSetlist event={event} onChange={load} />
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
          Não nomeia momento nenhum: o momento fica sem nome até uma hora medida
          chegar (um toque do operador, ou os períodos da API do jogo).
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
    // A failed search must not leave the previous answer on screen: with a
    // stale empty list the page would print "Nenhum jogo com esses dados"
    // beside the error explaining that no search was made (22/09).
    setResults(null)
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
        Os gols e cartões do jogo entram na linha do tempo. No dia do jogo o
        servidor acompanha a partida ao vivo e marca sozinho o apito inicial e o
        do 2º tempo — é isso que transforma o minuto em hora exata. O toque do
        operador continua valendo como reserva. Pode rodar de novo depois do
        jogo: só as linhas deste jogo são trocadas.
      </p>
      {attached && <LiveWatch eventId={event.id} />}
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
      <div className="mt-3 grid grid-cols-1 items-end gap-3 sm:grid-cols-[1fr_auto_auto]">
        <div>
          <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="fixture-team">
            Time
          </label>
          <input
            id="fixture-team"
            className={field}
            value={team}
            onChange={(e) => setTeam(e.target.value)}
          />
        </div>
        <DateField id="fixture-on" label="Data do jogo" value={on} onChange={setOn} />
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

/**
 * What the server's live watch of this match is doing (#52), in a sentence.
 *
 * The watch runs by itself; this line exists so the operator is never left
 * wondering whether it is — the app stating its own state honestly. Asked
 * again every 30 s while the page is open, which costs the API nothing: the
 * answer comes from the server's memory, not from API-Football.
 */
function LiveWatch({ eventId }: { eventId: string }) {
  const [watch, setWatch] = useState<MatchWatch | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let alive = true
    const ask = () =>
      events
        .getWatch(eventId)
        .then((w) => {
          if (!alive) return
          setWatch(w)
          setError(null)
        })
        .catch((err: unknown) => {
          if (alive) setError(message(err, 'Não deu pra perguntar ao servidor.'))
        })
    void ask()
    const timer = setInterval(ask, 30_000)
    return () => {
      alive = false
      clearInterval(timer)
    }
  }, [eventId])

  if (error) {
    return <p className="mt-2 text-sm text-red-400">Acompanhamento ao vivo: {error}</p>
  }
  if (!watch) return null

  const sentence = {
    off: 'O acompanhamento ao vivo está desligado: falta a chave da API no servidor.',
    idle: 'O acompanhamento ao vivo começa sozinho no dia do jogo.',
    waiting: watch.scheduled
      ? `Vou começar a olhar 10 minutos antes das ${clock(watch.scheduled)}.`
      : 'Vou começar a olhar 10 minutos antes do jogo.',
    watching: `Acompanhando ao vivo${watch.status ? ` — agora: ${watch.status}` : ''}.`,
    done: 'Jogo acompanhado até o fim.',
  }[watch.state]

  return (
    <div className="mt-3 rounded-lg border border-tumtum-border bg-tumtum-surface p-3 text-sm">
      <p className="text-tumtum-white">{sentence}</p>
      {(watch.kickoff_at || watch.second_half_at) && (
        <p className="mt-1 text-tumtum-muted">
          {watch.kickoff_at && <>Apito inicial medido às {clock(watch.kickoff_at)}. </>}
          {watch.second_half_at && <>2º tempo medido às {clock(watch.second_half_at)}.</>}
        </p>
      )}
      {watch.notes.map((note) => (
        <p key={note} className="mt-1 text-tumtum-muted">
          {note}
        </p>
      ))}
      {watch.last_error && (
        <p className="mt-1 text-red-400">Última tentativa falhou: {watch.last_error}</p>
      )}
      {watch.state !== 'off' && watch.state !== 'idle' && (
        <p className="mt-1 text-xs text-tumtum-muted tabular-nums">
          {watch.spent_today} de {watch.budget} consultas hoje
        </p>
      )}
    </div>
  )
}

/**
 * The operator's script for a show — paste the order, then one button.
 *
 * A concert has no API. Setlist.fm publishes order and never times, audio
 * fingerprinting matches a studio recording and not a band playing live, and
 * four-minutes-a-song is outside the correlator's window by the third song.
 * So a person taps, and the tap is the measurement that names the moment on
 * every fan's card.
 *
 * The order goes in beforehand, with no pressure — a tour plays close to the
 * same set every night, so last night's is a good draft. During the show
 * there is nothing to read and nothing to decide: the next song is large, and
 * COMEÇOU is the only thing to press.
 */
function ShowSetlist({ event, onChange }: { event: EventDetail; onChange: () => Promise<void> }) {
  const [songs, setSongs] = useState<SetlistSong[] | null>(null)
  const [draft, setDraft] = useState('')
  const [editing, setEditing] = useState(false)
  const [busy, setBusy] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      setSongs(await events.getSetlist(event.id))
      setError(null)
    } catch (err) {
      // An empty list and a list we could not fetch are different things, and
      // the screen must never show the first while meaning the second.
      setSongs(null)
      setError(message(err, 'Não deu para carregar a ordem das músicas.'))
    }
  }, [event.id])

  useEffect(() => {
    void load()
  }, [load])

  const next = songs?.find((s) => s.started_at === null) ?? null
  const done = songs?.filter((s) => s.started_at !== null).length ?? 0

  async function save() {
    setBusy('save')
    setError(null)
    try {
      const saved = await events.replaceSetlist(event.id, draft.split('\n'))
      setSongs(saved)
      setEditing(false)
      await onChange()
    } catch (err) {
      setError(message(err, 'Não deu para salvar a ordem.'))
    } finally {
      setBusy(null)
    }
  }

  async function start(position?: number) {
    setBusy(position ? `p${position}` : 'next')
    setError(null)
    try {
      await events.startSong(event.id, position)
      await load()
      await onChange()
    } catch (err) {
      setError(message(err, 'Não deu para marcar o começo.'))
    } finally {
      setBusy(null)
    }
  }

  const field =
    'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none'

  return (
    <section className="mt-8">
      <h2 className="text-sm font-medium uppercase tracking-wider text-tumtum-muted">
        Show · ordem das músicas
      </h2>
      <p className="mt-1 text-sm text-tumtum-muted">
        Cole a ordem antes do show. Durante, um toque em COMEÇOU a cada música: é
        esse toque que dá a hora exata e nomeia o momento no card de quem estava lá.
      </p>

      {songs === null && !error && <p className="mt-3 text-sm text-tumtum-muted">Carregando…</p>}

      {songs !== null && (songs.length === 0 || editing) && (
        <div className="mt-3">
          <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="setlist-draft">
            Uma música por linha, na ordem do show
          </label>
          <textarea
            id="setlist-draft"
            className={`${field} min-h-[180px] font-mono text-sm`}
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
          />
          {songs.some((s) => s.started_at !== null) && (
            <p className="mt-2 text-sm text-tumtum-muted">
              As horas já marcadas continuam onde estão. Corrigir um nome ou a ordem
              não apaga nada — você não vai ter que marcar de novo.
            </p>
          )}
          <div className="mt-2 flex gap-2">
            <Button type="button" onClick={save} disabled={busy !== null}>
              {busy === 'save' ? 'Salvando…' : 'Salvar a ordem'}
            </Button>
            {editing && (
              <Button variant="secondary" type="button" onClick={() => setEditing(false)} disabled={busy !== null}>
                Cancelar
              </Button>
            )}
          </div>
        </div>
      )}

      {songs !== null && songs.length > 0 && !editing && (
        <>
          {/* The one control of the night, and it says what it will do. */}
          <div className="mt-4 rounded-xl border border-tumtum-border bg-tumtum-surface p-4">
            {next ? (
              <>
                <p className="text-xs uppercase tracking-wider text-tumtum-muted">Próxima</p>
                <p className="mt-1 truncate text-2xl font-semibold text-tumtum-white">
                  {next.position}. {next.title}
                </p>
                <button
                  type="button"
                  onClick={() => start()}
                  disabled={busy !== null}
                  className="mt-4 w-full rounded-xl bg-tumtum-pink px-4 py-5 text-lg font-bold uppercase tracking-wide text-black disabled:opacity-50"
                >
                  {busy === 'next' ? 'Marcando…' : 'Começou'}
                </button>
              </>
            ) : (
              <p className="text-sm text-tumtum-white">
                Todas as {songs.length} músicas já começaram. O show está inteiro na linha do tempo.
              </p>
            )}
            <p className="mt-3 text-xs text-tumtum-muted">
              {done} de {songs.length} marcadas
            </p>
          </div>

          <ul className="mt-3 space-y-1">
            {songs.map((s) => (
              <li
                key={s.id}
                className="flex items-center justify-between gap-3 rounded-lg border border-tumtum-border px-3 py-2"
              >
                <span className="min-w-0 truncate text-sm text-tumtum-white">
                  <span className="tabular-nums text-tumtum-muted">{s.position}.</span> {s.title}
                </span>
                {s.started_at ? (
                  <span className="shrink-0 tabular-nums text-sm text-tumtum-pink">
                    {clock(s.started_at)}
                  </span>
                ) : (
                  <button
                    type="button"
                    onClick={() => start(s.position)}
                    disabled={busy !== null}
                    className="shrink-0 text-xs uppercase tracking-wider text-tumtum-muted disabled:opacity-50"
                  >
                    {busy === `p${s.position}` ? '…' : 'marcar'}
                  </button>
                )}
              </li>
            ))}
          </ul>

          <button
            type="button"
            onClick={() => {
              setDraft(songs.map((s) => s.title).join('\n'))
              setEditing(true)
            }}
            className="mt-3 text-sm text-tumtum-pink"
          >
            Corrigir a ordem
          </button>
          <p className="mt-1 text-xs text-tumtum-muted">
            As músicas que já começaram guardam a hora, desde que continuem no mesmo lugar.
          </p>
        </>
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
        <div>
          <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="manual-label">
            O que foi
          </label>
          <input
            id="manual-label"
            className={field}
            value={label}
            onChange={(e) => setLabel(e.target.value)}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="manual-type">
            Tipo
          </label>
          <select id="manual-type" className={field} value={type} onChange={(e) => setType(e.target.value)}>
          {entryTypes.map((t) => (
            <option key={t.value} value={t.value}>
              {t.label}
            </option>
          ))}
          </select>
        </div>
        <DateField id="manual-date" label="Data" value={date} onChange={setDate} />
        <TimeField id="manual" label="Hora (São Paulo)" value={time} onChange={setTime} />
      </div>
      {error && <p className="mt-2 text-sm text-red-400">{error}</p>}
      <Button type="submit" className="mt-3" disabled={busy}>
        {busy ? 'Adicionando…' : 'Adicionar marca'}
      </Button>
    </form>
  )
}

/**
 * Which tour this show belongs to (#33, 22/09; one feed since #65, 23/09).
 *
 * Every date of a tour opens onto the same feed, starting on all dates, with
 * the fan's own night as a filter. A post from one date reaches the others
 * only if its author chose that when posting — this section only says which
 * dates belong together. Clubs and championships stay in the data but are
 * not offered: football is one feed per match for now.
 */
function SeriesSection({ event }: { event: EventDetail }) {
  const [all, setAll] = useState<SeriesBrief[] | null>(null)
  const [current, setCurrent] = useState<SeriesBrief | null | undefined>(undefined)
  const [name, setName] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState<string | null>(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      const [list, mine] = await Promise.all([series.list(), series.ofEvent(event.id)])
      setAll(list)
      setCurrent(mine)
    } catch (err) {
      // Not knowing the tour is not the same as having none.
      setError(message(err, 'Não deu pra carregar as turnês.'))
    }
  }, [event.id])

  useEffect(() => {
    void load()
  }, [load])

  async function assign(seriesId: string | null) {
    setBusy(true)
    setError(null)
    setSaved(null)
    try {
      const result = await series.assign(event.id, seriesId)
      setCurrent(result)
      setSaved(result ? `Agora faz parte de ${result.name}.` : 'Saiu da turnê.')
      await load()
    } catch (err) {
      setError(message(err, 'Não deu pra salvar.'))
    } finally {
      setBusy(false)
    }
  }

  async function createAndAssign() {
    if (name.trim().length < 2) {
      setError('Dê um nome, com pelo menos duas letras.')
      return
    }
    setBusy(true)
    setError(null)
    try {
      const created = await series.create(name.trim(), 'tour')
      setName('')
      await assign(created.id)
    } catch (err) {
      setError(message(err, 'Não deu pra criar.'))
      setBusy(false)
    }
  }

  const field =
    'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none'

  // Only tours are offered; one this event is already in stays listed
  // whatever its kind, so the select never shows a value it does not have.
  const choices = (all ?? []).filter((s) => s.kind === 'tour' || s.id === current?.id)

  return (
    <section className="mt-8">
      <h2 className="text-sm font-medium uppercase tracking-wider text-tumtum-muted">Turnê</h2>
      <p className="mt-1 text-sm text-tumtum-muted">
        As datas de uma turnê têm um feed só: quem foi em qualquer uma delas se
        encontra ali. Show avulso não precisa de nada aqui.
      </p>

      {current === undefined && !error && <p className="mt-3 text-sm text-tumtum-muted">Carregando…</p>}

      {current !== undefined && (
        <p className="mt-3 text-sm text-tumtum-white">
          {current
            ? current.dates > 1
              ? `Faz parte de ${current.name} · ${current.dates} datas`
              : `Faz parte de ${current.name} · só esta data por enquanto`
            : 'Este show não faz parte de nenhuma turnê.'}
        </p>
      )}

      {all !== null && (
        <div className="mt-3">
          <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="series-pick">
            Turnê deste show
          </label>
          <select
            id="series-pick"
            className={field}
            value={current?.id ?? ''}
            disabled={busy}
            onChange={(e) => assign(e.target.value || null)}
          >
            <option value="">Nenhuma</option>
            {choices.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>
      )}

      <div className="mt-3 grid grid-cols-1 items-end gap-3 sm:grid-cols-[1fr_auto]">
        <div>
          <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="series-new">
            Ou crie uma turnê nova
          </label>
          <input
            id="series-new"
            className={field}
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>
        <Button type="button" onClick={createAndAssign} disabled={busy}>
          {busy ? 'Salvando…' : 'Criar e ligar'}
        </Button>
      </div>

      {saved && <p className="mt-2 text-sm text-tumtum-white">{saved}</p>}
      {error && <p className="mt-2 text-sm text-red-400">{error}</p>}
    </section>
  )
}
