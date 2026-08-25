'use client'

import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { ApiError, auth, millisUntilTokenExpiry } from '@/lib/api'
import { useAuthStore } from '@/lib/stores/useAuthStore'
import { useEventStore } from '@/lib/stores/useEventStore'
import { useHRStore } from '@/lib/stores/useHRStore'
import {
  HeartRateMonitor,
  isWebBluetoothAvailable,
  getBrowserWarning,
  type BleState,
  type HRReading,
} from '@/lib/health/ble-heart-rate'
import {
  analyseQuality,
  formatDuration,
  VERDICT_LABELS,
  VERDICT_DESCRIPTIONS,
  type QualityVerdict,
} from '@/lib/health/quality'
import type { HRSample } from '@/lib/health/import-parsers'
import {
  clearSnapshot,
  loadSnapshot,
  saveSnapshot,
  type LiveSessionSnapshot,
} from '@/lib/health/live-session-buffer'
import { Button, Card, Badge, Loading } from '@/components/ui'
import { Nav } from '@/components/layout'

/** How often the in-memory session is mirrored to localStorage. */
const SNAPSHOT_INTERVAL_MS = 10_000

/**
 * Event mode exists because a browser cannot capture Bluetooth with the screen
 * off — Android freezes the page — so at a six-hour event the screen is lit the
 * whole night and battery becomes the thing that ends the capture.
 *
 * It does not touch the data. Every reading is still stored; only the display
 * stops redrawing for a screen nobody is watching. On a black canvas over an
 * OLED panel, unlit pixels cost nothing, which is why the mode goes almost
 * entirely dark rather than merely simpler.
 */
const QUIET_DISPLAY_MS = 5_000
const QUIET_CLOCK_MS = 30_000
/**
 * Leaving event mode is a press and hold, not a tap. The phone spends the night
 * in a pocket, and a tap-anywhere exit would drop back to the full screen —
 * where the next accidental touch could land on "Encerrar e ver minha
 * experiência" and end the capture for good.
 */
const QUIET_EXIT_HOLD_MS = 1_200
const EXIT_RING_RADIUS = 54
const EXIT_RING_LENGTH = 2 * Math.PI * EXIT_RING_RADIUS

/**
 * Warn when a token would expire inside a long capture. Comfortably longer
 * than an event, because the cost of re-entering an account before starting is
 * nothing and the cost of discovering it at the end is the whole night.
 */
const TOKEN_RENEWAL_WARNING_MS = 8 * 60 * 60 * 1000

const stateLabels: Record<BleState, string> = {
  unsupported: 'Não suportado',
  idle: 'Desconectado',
  connecting: 'Conectando...',
  connected: 'Conectado',
  reconnecting: 'Reconectando...',
  disconnected: 'Desconectado',
  error: 'Erro',
}

const stateVariants: Record<BleState, 'default' | 'success' | 'warning' | 'danger'> = {
  unsupported: 'danger',
  idle: 'default',
  connecting: 'warning',
  connected: 'success',
  reconnecting: 'warning',
  disconnected: 'default',
  error: 'danger',
}

const verdictStyles: Record<QualityVerdict, string> = {
  good: 'border-emerald-500/40 bg-emerald-500/10 text-emerald-300',
  marginal: 'border-amber-500/40 bg-amber-500/10 text-amber-300',
  insufficient: 'border-red-500/50 bg-red-500/10 text-red-400',
}

export default function LivePage() {
  const router = useRouter()
  const { eventList, loadEvents, analyzeSession } = useEventStore()
  const { uploadSession } = useHRStore()

  const monitorRef = useRef<HeartRateMonitor | null>(null)
  const wakeLockRef = useRef<WakeLockSentinel | null>(null)
  /** Whether a capture is running and therefore still wants the screen held. */
  const wantsWakeLockRef = useRef(false)
  const samplesRef = useRef<HRSample[]>([])
  const lastSnapshotRef = useRef(0)
  /** Mirrors the selection so the capture callback never reads a stale value. */
  const eventIdRef = useRef('')

  const [supported, setSupported] = useState<boolean | null>(null)
  const [state, setState] = useState<BleState>('idle')
  const [stateDetail, setStateDetail] = useState<string | null>(null)
  const [deviceName, setDeviceName] = useState<string | null>(null)
  const [lastReading, setLastReading] = useState<HRReading | null>(null)
  const [sampleCount, setSampleCount] = useState(0)
  const [startedAt, setStartedAt] = useState<string | null>(null)
  const [now, setNow] = useState(Date.now())
  const [eventId, setEventId] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [recovered, setRecovered] = useState<LiveSessionSnapshot | null>(null)
  const [eventsUnavailable, setEventsUnavailable] = useState(false)
  const [browserWarning, setBrowserWarning] = useState<string | null>(null)
  const [chooserStuck, setChooserStuck] = useState(false)
  const [quiet, setQuiet] = useState(false)
  const [holdingExit, setHoldingExit] = useState(false)
  const [confirmingFinish, setConfirmingFinish] = useState(false)
  const quietRef = useRef(false)
  const lastDisplayRef = useRef(0)
  const holdTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    setSupported(isWebBluetoothAvailable())
    setBrowserWarning(getBrowserWarning())
    // Linking a session to an event is optional, and capture works with no
    // backend at all — so a failed event fetch must not disturb this screen.
    loadEvents().catch(() => setEventsUnavailable(true))
    const snapshot = loadSnapshot()
    if (snapshot) setRecovered(snapshot)
  }, [loadEvents])

  // Drives the elapsed-time readout without re-rendering on every BLE packet.
  useEffect(() => {
    if (!startedAt) return
    const id = setInterval(() => setNow(Date.now()), quiet ? QUIET_CLOCK_MS : 1000)
    return () => clearInterval(id)
  }, [startedAt, quiet])

  useEffect(() => {
    eventIdRef.current = eventId
  }, [eventId])

  // The capture callback is captured once, so it reads the mode through a ref.
  useEffect(() => {
    quietRef.current = quiet
  }, [quiet])

  const handleReading = useCallback(
    (reading: HRReading) => {
      // The sample is always kept. What follows only decides how often the
      // screen is asked to redraw.
      samplesRef.current.push({ time: reading.time, bpm: reading.bpm })

      const nowMs = Date.now()
      if (!quietRef.current || nowMs - lastDisplayRef.current >= QUIET_DISPLAY_MS) {
        lastDisplayRef.current = nowMs
        setLastReading(reading)
        setSampleCount(samplesRef.current.length)
      }

      if (nowMs - lastSnapshotRef.current >= SNAPSHOT_INTERVAL_MS) {
        lastSnapshotRef.current = nowMs
        saveSnapshot({
          startedAt: samplesRef.current[0].time,
          deviceName: monitorRef.current?.getDeviceName() ?? null,
          eventId: eventIdRef.current || null,
          samples: samplesRef.current,
        })
      }
    },
    [],
  )

  /**
   * Android throttles background tabs aggressively — the JStyle bench runs made
   * that concrete. A screen wake lock keeps a capture alive while the phone sits
   * on a table during an event.
   */
  async function acquireWakeLock() {
    wantsWakeLockRef.current = true
    try {
      if ('wakeLock' in navigator) {
        wakeLockRef.current = await navigator.wakeLock.request('screen')
      }
    } catch {
      // Not fatal: capture continues, the screen may just sleep.
    }
  }

  function releaseWakeLock() {
    wantsWakeLockRef.current = false
    wakeLockRef.current?.release().catch(() => undefined)
    wakeLockRef.current = null
  }

  async function handleConnect() {
    setError(null)
    setChooserStuck(false)
    // requestDevice() never settles when a browser exposes the API without
    // implementing the chooser, so nothing would ever tell the user why.
    const stuckTimer = setTimeout(() => setChooserStuck(true), 8000)
    const monitor = new HeartRateMonitor({
      onReading: handleReading,
      onStateChange: (next, detail) => {
        setState(next)
        setStateDetail(detail ?? null)
        setDeviceName(monitorRef.current?.getDeviceName() ?? null)
        // The monitor captures this callback once, so `startedAt` read from the
        // closure is frozen at its value when the sensor was first connected —
        // which made every reconnection restart the elapsed clock at zero while
        // the reading count kept climbing. A functional update reads live state.
        if (next === 'connected') {
          setStartedAt((prev) => prev ?? new Date().toISOString())
        }
      },
    })
    monitorRef.current = monitor
    try {
      await monitor.connect()
    } finally {
      clearTimeout(stuckTimer)
    }
    setChooserStuck(false)
    setDeviceName(monitor.getDeviceName())
    await acquireWakeLock()
  }

  // Recompute every tenth reading rather than every one: the analysis sorts the
  // interval list, and a four-hour session holds ~14k samples.
  const qualityTick = Math.floor(sampleCount / 10)
  const report = useMemo(() => {
    if (sampleCount < 2) return null
    try {
      return analyseQuality(samplesRef.current)
    } catch {
      return null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [qualityTick])

  async function handleFinish() {
    const samples = samplesRef.current
    if (samples.length < 10) {
      setError('São necessárias ao menos 10 leituras para salvar a sessão.')
      return
    }

    setSaving(true)
    setError(null)
    try {
      await monitorRef.current?.disconnect()
      releaseWakeLock()

      const session = await uploadSession({
        start_time: samples[0].time,
        end_time: samples[samples.length - 1].time,
        source_device: deviceName ?? 'Sensor BLE',
        event_id: eventId || undefined,
        data_points: samples.map((s) => ({ time: s.time, bpm: s.bpm, source: 'ble_hrs' })),
      })

      await analyzeSession(session.id)
      clearSnapshot()
      router.push(`/experience?session=${session.id}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao salvar a sessão.')
      setSaving(false)
      setConfirmingFinish(false)
    }
  }

  function startExitHold(event: React.PointerEvent<HTMLButtonElement>) {
    // Hold on to this pointer even if the finger drifts off the button. A press
    // held for over a second always moves a little, and letting the pointer go
    // used to cancel the hold with nothing on screen to say why.
    try {
      event.currentTarget.setPointerCapture(event.pointerId)
    } catch {
      // Capture is an optimisation here, not a requirement.
    }
    setHoldingExit(true)
    holdTimerRef.current = setTimeout(() => {
      setHoldingExit(false)
      setQuiet(false)
      // Confirm in the hand: the screen is about to change brightness in a
      // dark room, and a buzz says it worked before the eyes adjust.
      navigator.vibrate?.(40)
    }, QUIET_EXIT_HOLD_MS)
  }

  function cancelExitHold() {
    setHoldingExit(false)
    if (holdTimerRef.current) clearTimeout(holdTimerRef.current)
    holdTimerRef.current = null
  }

  function handleDiscardRecovered() {
    clearSnapshot()
    setRecovered(null)
  }

  async function handleResumeRecovered() {
    if (!recovered) return
    samplesRef.current = recovered.samples
    setSampleCount(recovered.samples.length)
    setStartedAt(recovered.samples[0].time)
    setEventId(recovered.eventId ?? '')
    setDeviceName(recovered.deviceName)
    setRecovered(null)
  }

  /**
   * A screen wake lock does not survive the page being hidden: the Screen Wake
   * Lock spec releases it whenever the document's visibility changes to hidden,
   * and nothing takes it back. So one glance at a message during an event used
   * to end the capture — the lock was gone, the screen slept a minute later,
   * and Android froze the tab with the Bluetooth connection inside it. The
   * capture only asked for the lock once, when the sensor connected.
   */
  useEffect(() => {
    function handleVisibility() {
      if (document.visibilityState !== 'visible') return

      // Show the truth the instant someone looks. Android freezes the page's
      // timers in the background, and in event mode the clock only ticks every
      // 30 seconds, so returning to the app used to show a reading count and an
      // elapsed time frozen up to half a minute in the past. The capture was
      // running the whole time — but from the outside a live capture and a dead
      // one looked exactly the same, which is not a thing to wonder about in
      // the middle of an event.
      setNow(Date.now())
      setSampleCount(samplesRef.current.length)
      lastDisplayRef.current = Date.now()

      if (!wantsWakeLockRef.current) return
      if (wakeLockRef.current && !wakeLockRef.current.released) return
      acquireWakeLock().catch(() => undefined)
    }
    document.addEventListener('visibilitychange', handleVisibility)
    return () => document.removeEventListener('visibilitychange', handleVisibility)
    // acquireWakeLock only touches refs, so it never goes stale.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    return () => {
      monitorRef.current?.disconnect().catch(() => undefined)
      releaseWakeLock()
      if (holdTimerRef.current) clearTimeout(holdTimerRef.current)
    }
  }, [])

  // Saving needs an account, but nothing on this page used to check for one.
  // A capture could run for a whole concert and only fail at the very end,
  // which is the worst possible moment to learn about it.
  //
  // Holding a token is not the same as holding a valid one. They last 24
  // hours, so one saved a few days before an event still looks like an account
  // here, and the capture would only discover otherwise when it was saved.
  const authToken = useAuthStore((s) => s.token)
  const [signedIn, setSignedIn] = useState<boolean | null>(null)
  const [tokenExpiringSoon, setTokenExpiringSoon] = useState(false)
  useEffect(() => {
    const stored = authToken ?? window.localStorage.getItem('access_token')
    if (!stored) {
      setSignedIn(false)
      return
    }
    const left = millisUntilTokenExpiry(stored)
    setTokenExpiringSoon(left !== null && left > 0 && left < TOKEN_RENEWAL_WARNING_MS)
    let cancelled = false
    auth
      .me()
      .then(() => {
        if (!cancelled) setSignedIn(true)
      })
      .catch((err: unknown) => {
        if (cancelled) return
        // Only a refusal means signed out. Capture needs no backend at all, so
        // a server that cannot be reached right now must not block the night.
        setSignedIn(!(err instanceof ApiError && err.status === 401))
      })
    return () => {
      cancelled = true
    }
  }, [authToken])

  const capturing = state === 'connected' || state === 'reconnecting'
  const elapsed = startedAt ? (now - Date.parse(startedAt)) / 1000 : 0

  // Almost nothing is drawn here on purpose: on an OLED panel an unlit pixel
  // draws no power, and the readout that remains is dim so the phone can sit in
  // a pocket or on a table without lighting up a room.
  if (quiet && capturing) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-tumtum-black px-6">
        <div className="text-6xl font-hero tabular-nums text-tumtum-muted">
          {lastReading ? lastReading.bpm : '--'}
        </div>
        <div className="mt-2 text-xs text-tumtum-muted/60">bpm</div>
        <div className="mt-10 text-xs text-tumtum-muted/60">
          {startedAt ? formatDuration(elapsed) : '--'} · {sampleCount.toLocaleString('pt-BR')} leituras
        </div>
        {state === 'reconnecting' && (
          <div className="mt-6 text-xs text-amber-500/70">reconectando…</div>
        )}
        {/*
          Big enough to find without looking, and it shows the hold filling.
          The first version was a line of 10px text at 40% opacity that gave no
          sign it had been pressed, so a hold that was silently cancelled and a
          hold that was never registered looked exactly the same.

          `touchAction: none` keeps the browser from reading the press as the
          start of a scroll and cancelling the pointer partway through.
        */}
        <button
          type="button"
          onPointerDown={startExitHold}
          onPointerUp={cancelExitHold}
          onPointerCancel={cancelExitHold}
          style={{ touchAction: 'none' }}
          className="relative mt-12 flex h-40 w-40 items-center justify-center rounded-full"
          aria-label="Segure para sair do modo evento"
        >
          <svg className="absolute inset-0 h-full w-full -rotate-90" viewBox="0 0 120 120">
            <circle
              cx="60"
              cy="60"
              r={EXIT_RING_RADIUS}
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              className="text-tumtum-muted/25"
            />
            <circle
              cx="60"
              cy="60"
              r={EXIT_RING_RADIUS}
              fill="none"
              stroke="currentColor"
              strokeWidth="3"
              strokeLinecap="round"
              strokeDasharray={EXIT_RING_LENGTH}
              strokeDashoffset={holdingExit ? 0 : EXIT_RING_LENGTH}
              style={{
                transition: `stroke-dashoffset ${holdingExit ? QUIET_EXIT_HOLD_MS : 200}ms linear`,
              }}
              className="text-tumtum-lime"
            />
          </svg>
          <span className="px-6 text-center text-sm text-tumtum-muted">
            {holdingExit ? 'segurando…' : 'segure para sair'}
          </span>
        </button>
      </main>
    )
  }

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-2xl px-4 py-8">
          <h1 className="text-3xl font-bold text-tumtum-white">Captura ao vivo</h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            Conecte uma cinta ou relógio que transmita sua batida por Bluetooth
            e acompanhe seu batimento em tempo real durante o evento.
          </p>

          {signedIn === false && (
            <Card className="mt-6 border-amber-500/50">
              <Card.Header>
                <Card.Title>Entre na sua conta antes de capturar</Card.Title>
              </Card.Header>
              <Card.Content>
                <p>
                  A captura funciona sem conta, mas o salvamento não. Sem entrar,
                  você só descobriria isso no fim do evento.
                </p>
                <p className="mt-3">
                  Seus dados ficam guardados neste navegador enquanto isso — se já
                  capturou algo, entre na conta e volte aqui para recuperar.
                </p>
              </Card.Content>
              <Link href="/login">
                <Button className="mt-4 w-full">Entrar na minha conta</Button>
              </Link>
            </Card>
          )}

          {signedIn !== false && tokenExpiringSoon && !capturing && (
            <Card className="mt-6 border-amber-500/50">
              <Card.Header>
                <Card.Title>Entre de novo antes de começar</Card.Title>
              </Card.Header>
              <Card.Content>
                <p>
                  Seu acesso vence em menos de oito horas, ou seja, pode vencer no
                  meio de um evento longo. A captura continuaria, mas o salvamento
                  falharia no fim.
                </p>
                <p className="mt-3">
                  Sair e entrar de novo agora leva dez segundos e resolve.
                </p>
              </Card.Content>
              <Link href="/login">
                <Button variant="secondary" className="mt-4 w-full">
                  Entrar de novo
                </Button>
              </Link>
            </Card>
          )}

          {supported === null && (
            <div className="mt-8 flex justify-center">
              <Loading />
            </div>
          )}

          {supported === false && (
            <Card className="mt-6">
              <Card.Header>
                <Card.Title>Seu navegador não suporta captura ao vivo</Card.Title>
              </Card.Header>
              <Card.Content>
                <p>
                  A captura por Bluetooth funciona no Chrome do Android. No iPhone o
                  Safari ainda não oferece esse recurso.
                </p>
                <p className="mt-3">
                  Você pode registrar sua experiência importando os dados do seu
                  relógio depois do evento.
                </p>
              </Card.Content>
              <Link href="/import">
                <Button className="mt-4 w-full">Importar dados de um arquivo</Button>
              </Link>
            </Card>
          )}

          {supported && recovered && (
            <Card className="mt-6 border-amber-500/40">
              <Card.Header>
                <Card.Title>Sessão interrompida encontrada</Card.Title>
              </Card.Header>
              <Card.Content>
                {recovered.samples.length.toLocaleString('pt-BR')} leituras de{' '}
                {recovered.deviceName ?? 'um sensor'}, iniciadas em{' '}
                {new Date(recovered.startedAt).toLocaleString('pt-BR')}.
              </Card.Content>
              <div className="mt-4 flex gap-3">
                <Button onClick={handleResumeRecovered} className="flex-1">
                  Recuperar
                </Button>
                <Button onClick={handleDiscardRecovered} variant="secondary" className="flex-1">
                  Descartar
                </Button>
              </div>
            </Card>
          )}

          {supported && (
            <>
              <Card className="mt-6">
                <div className="flex items-center justify-between">
                  <Badge variant={stateVariants[state]}>
                    {stateLabels[state]}
                    {stateDetail ? ` — ${stateDetail}` : ''}
                  </Badge>
                  {deviceName && (
                    <span className="text-sm text-tumtum-muted">{deviceName}</span>
                  )}
                </div>

                <div className="mt-6 text-center">
                  <div className="text-7xl font-bold tabular-nums text-tumtum-yellow">
                    {lastReading ? lastReading.bpm : '--'}
                  </div>
                  <div className="mt-1 text-sm text-tumtum-muted">bpm</div>
                </div>

                <dl className="mt-6 grid grid-cols-3 gap-3">
                  <Metric label="Tempo" value={startedAt ? formatDuration(elapsed) : '--'} />
                  <Metric label="Leituras" value={sampleCount.toLocaleString('pt-BR')} />
                  <Metric
                    label="Contato"
                    value={
                      lastReading?.contact === null || lastReading === null
                        ? '--'
                        : lastReading.contact
                          ? 'OK'
                          : 'Solto'
                    }
                  />
                </dl>

                {lastReading && lastReading.rrIntervalsMs.length > 0 && (
                  <p className="mt-3 text-center text-xs text-tumtum-muted">
                    Intervalos R-R disponíveis ({lastReading.rrIntervalsMs.join(', ')} ms)
                  </p>
                )}

                {browserWarning && (
                  <p className="mt-4 rounded-lg border border-amber-500/40 bg-amber-500/10 p-3 text-sm text-amber-300">
                    {browserWarning}
                  </p>
                )}

                {chooserStuck && (
                  <div className="mt-4 rounded-lg border border-amber-500/40 bg-amber-500/10 p-3 text-sm text-amber-300">
                    <p className="font-medium">O seletor de dispositivos não abriu.</p>
                    <ul className="mt-2 list-disc space-y-1 pl-4 text-xs">
                      <li>Use o <strong>Google Chrome</strong> — outros navegadores Android costumam travar aqui.</li>
                      <li>Ligue o <strong>Bluetooth</strong> do celular.</li>
                      <li>Ligue a <strong>Localização</strong> — o Android exige para buscar dispositivos.</li>
                      <li>Permita <strong>Dispositivos por perto</strong> para o navegador.</li>
                    </ul>
                  </div>
                )}

                {capturing && (
                  <Button
                    variant="secondary"
                    onClick={() => setQuiet(true)}
                    className="mt-6 w-full"
                  >
                    Modo evento — economizar bateria
                  </Button>
                )}

                {!capturing && (
                  <Button onClick={handleConnect} size="lg" className="mt-6 w-full">
                    {sampleCount > 0 ? 'Reconectar sensor' : 'Conectar sensor'}
                  </Button>
                )}
              </Card>

              {report && (
                <Card className="mt-4">
                  <Card.Header>
                    <Card.Title>Qualidade do sinal</Card.Title>
                  </Card.Header>
                  <div className={`rounded-lg border p-4 ${verdictStyles[report.verdict]}`}>
                    <p className="font-semibold">{VERDICT_LABELS[report.verdict]}</p>
                    <p className="mt-1 text-sm opacity-90">
                      {VERDICT_DESCRIPTIONS[report.verdict]}
                    </p>
                  </div>
                  <dl className="mt-4 grid grid-cols-3 gap-3">
                    <Metric label="Cadência" value={`${report.medianIntervalSeconds.toFixed(1)}s`} />
                    <Metric label="Cobertura" value={`${(report.coverage * 100).toFixed(0)}%`} />
                    <Metric label="BPM" value={`${report.minBpm}–${report.maxBpm}`} />
                  </dl>
                </Card>
              )}

              {sampleCount > 0 && (
                <Card className="mt-4">
                  <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="event">
                    Evento (opcional)
                  </label>
                  {eventsUnavailable && (
                    <p className="mb-2 text-xs text-amber-400">
                      Não foi possível carregar os eventos. A captura continua
                      funcionando; para salvar a sessão é preciso estar conectado.
                    </p>
                  )}
                  <select
                    id="event"
                    value={eventId}
                    onChange={(e) => setEventId(e.target.value)}
                    className="w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-lime focus:outline-none"
                  >
                    <option value="">Sem evento — só a curva</option>
                    {eventList.map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.name}
                      </option>
                    ))}
                  </select>
                </Card>
              )}

              {error && (
                <p className="mt-4 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
                  {error}
                </p>
              )}

              {/*
                One tap used to end a capture that could be hours old. A phone
                spends an event in a pocket, and this is the only control on
                the screen whose cost is the whole night, so it asks first.
              */}
              {sampleCount > 0 && !confirmingFinish && (
                <Button
                  onClick={() => setConfirmingFinish(true)}
                  loading={saving}
                  size="lg"
                  className="mt-6 w-full"
                >
                  {saving ? 'Salvando...' : 'Encerrar e ver minha experiência'}
                </Button>
              )}

              {sampleCount > 0 && confirmingFinish && (
                <Card className="mt-6 border-tumtum-lime/50">
                  <Card.Header>
                    <Card.Title>Encerrar a captura?</Card.Title>
                  </Card.Header>
                  <Card.Content>
                    <p>
                      São {formatDuration(elapsed)} e{' '}
                      {sampleCount.toLocaleString('pt-BR')} leituras. Depois de
                      encerrar, o sensor é desconectado e a captura não continua.
                    </p>
                  </Card.Content>
                  <div className="mt-4 flex gap-3">
                    <Button
                      variant="secondary"
                      onClick={() => setConfirmingFinish(false)}
                      className="flex-1"
                    >
                      Continuar capturando
                    </Button>
                    <Button onClick={handleFinish} loading={saving} className="flex-1">
                      {saving ? 'Salvando...' : 'Encerrar'}
                    </Button>
                  </div>
                </Card>
              )}
            </>
          )}
        </div>
      </main>
    </>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border border-tumtum-border bg-tumtum-black p-3 text-center">
      <dt className="text-xs text-tumtum-muted">{label}</dt>
      <dd className="mt-1 text-lg font-semibold text-tumtum-white">{value}</dd>
    </div>
  )
}
