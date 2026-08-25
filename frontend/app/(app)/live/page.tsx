'use client'

import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
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
  insufficient: 'border-tumtum-red/50 bg-tumtum-red/10 text-tumtum-red-secondary',
}

export default function LivePage() {
  const router = useRouter()
  const { eventList, loadEvents, analyzeSession } = useEventStore()
  const { uploadSession } = useHRStore()

  const monitorRef = useRef<HeartRateMonitor | null>(null)
  const wakeLockRef = useRef<WakeLockSentinel | null>(null)
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
    const id = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(id)
  }, [startedAt])

  useEffect(() => {
    eventIdRef.current = eventId
  }, [eventId])

  const handleReading = useCallback(
    (reading: HRReading) => {
      samplesRef.current.push({ time: reading.time, bpm: reading.bpm })
      setLastReading(reading)
      setSampleCount(samplesRef.current.length)

      const nowMs = Date.now()
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
    try {
      if ('wakeLock' in navigator) {
        wakeLockRef.current = await navigator.wakeLock.request('screen')
      }
    } catch {
      // Not fatal: capture continues, the screen may just sleep.
    }
  }

  function releaseWakeLock() {
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
    }
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

  useEffect(() => {
    return () => {
      monitorRef.current?.disconnect().catch(() => undefined)
      releaseWakeLock()
    }
  }, [])

  // Saving needs an account, but nothing on this page used to check for one.
  // A capture could run for a whole concert and only fail at the very end,
  // which is the worst possible moment to learn about it.
  const authToken = useAuthStore((s) => s.token)
  const [signedIn, setSignedIn] = useState<boolean | null>(null)
  useEffect(() => {
    setSignedIn(Boolean(authToken ?? window.localStorage.getItem('access_token')))
  }, [authToken])

  const capturing = state === 'connected' || state === 'reconnecting'
  const elapsed = startedAt ? (now - Date.parse(startedAt)) / 1000 : 0

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-dark">
        <div className="mx-auto max-w-2xl px-4 py-8">
          <h1 className="text-3xl font-bold text-tumtum-text-primary">Captura ao vivo</h1>
          <p className="mt-2 text-sm text-tumtum-text-muted">
            Conecte uma cinta ou relógio que transmita frequência cardíaca por Bluetooth
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
                    <span className="text-sm text-tumtum-text-muted">{deviceName}</span>
                  )}
                </div>

                <div className="mt-6 text-center">
                  <div className="text-7xl font-bold tabular-nums text-tumtum-red-secondary">
                    {lastReading ? lastReading.bpm : '--'}
                  </div>
                  <div className="mt-1 text-sm text-tumtum-text-muted">bpm</div>
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
                  <p className="mt-3 text-center text-xs text-tumtum-text-muted">
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
                  <label className="mb-1 block text-sm text-tumtum-text-muted" htmlFor="event">
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
                    className="w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-text-primary focus:border-tumtum-red focus:outline-none"
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
                <p className="mt-4 rounded-lg border border-tumtum-red/40 bg-tumtum-red/10 p-3 text-sm text-tumtum-red-secondary">
                  {error}
                </p>
              )}

              {sampleCount > 0 && (
                <Button
                  onClick={handleFinish}
                  loading={saving}
                  size="lg"
                  className="mt-6 w-full"
                >
                  {saving ? 'Salvando...' : 'Encerrar e ver minha experiência'}
                </Button>
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
    <div className="rounded-lg border border-tumtum-border bg-tumtum-dark p-3 text-center">
      <dt className="text-xs text-tumtum-text-muted">{label}</dt>
      <dd className="mt-1 text-lg font-semibold text-tumtum-text-primary">{value}</dd>
    </div>
  )
}
