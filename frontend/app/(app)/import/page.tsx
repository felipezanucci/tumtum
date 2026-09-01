'use client'

import { useEffect, useMemo, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useEventStore } from '@/lib/stores/useEventStore'
import { useHRStore } from '@/lib/stores/useHRStore'
import {
  parseHRFile,
  filterSamplesByWindow,
  ImportParseError,
  type ParseResult,
} from '@/lib/health/import-parsers'
import {
  analyseQuality,
  formatDuration,
  VERDICT_LABELS,
  VERDICT_DESCRIPTIONS,
  TARGET_INTERVAL_SECONDS,
  type QualityVerdict,
} from '@/lib/health/quality'
import { Button, Card, Input, Loading, Badge } from '@/components/ui'
import { Nav } from '@/components/layout'

/**
 * A single POST carries the whole session, so cap the payload. A 3-hour event at
 * one reading per second is ~10.800 points, comfortably under this ceiling.
 */
const MAX_UPLOAD_POINTS = 25_000

const formatLabels: Record<ParseResult['format'], string> = {
  apple_health_xml: 'Exportação do Apple Saúde (XML)',
  health_connect_json: 'Exportação do Health Connect (JSON)',
  samsung_health_csv: 'Exportação do Samsung Health (CSV)',
  generic_csv: 'CSV genérico',
  generic_json: 'JSON genérico',
}

const verdictStyles: Record<QualityVerdict, string> = {
  good: 'border-emerald-500/40 bg-emerald-500/10 text-emerald-300',
  marginal: 'border-amber-500/40 bg-amber-500/10 text-amber-300',
  insufficient: 'border-red-500/50 bg-red-500/10 text-red-400',
}

/** Convert epoch millis to the "YYYY-MM-DDTHH:mm" value a datetime-local input expects. */
function toLocalInputValue(millis: number): string {
  const date = new Date(millis)
  const offsetMillis = date.getTime() - date.getTimezoneOffset() * 60_000
  return new Date(offsetMillis).toISOString().slice(0, 16)
}

/** Parse a datetime-local value (local timezone) back to epoch millis. */
function fromLocalInputValue(value: string): number | null {
  const millis = Date.parse(value)
  return Number.isNaN(millis) ? null : millis
}

export default function ImportPage() {
  const router = useRouter()
  const { eventList, loadEvents, analyzeSession } = useEventStore()
  const { uploadSession } = useHRStore()

  const [fileName, setFileName] = useState<string | null>(null)
  const [parsing, setParsing] = useState(false)
  const [parseResult, setParseResult] = useState<ParseResult | null>(null)
  const [parseError, setParseError] = useState<string | null>(null)

  const [eventId, setEventId] = useState('')
  const [sourceDevice, setSourceDevice] = useState('JStyle V8')
  const [startInput, setStartInput] = useState('')
  const [endInput, setEndInput] = useState('')

  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState<string | null>(null)

  useEffect(() => {
    loadEvents()
  }, [loadEvents])

  const windowSamples = useMemo(() => {
    if (!parseResult) return []
    const start = fromLocalInputValue(startInput)
    const end = fromLocalInputValue(endInput)
    if (start === null || end === null || start >= end) return parseResult.samples
    return filterSamplesByWindow(parseResult.samples, start, end)
  }, [parseResult, startInput, endInput])

  const report = useMemo(() => {
    if (windowSamples.length < 2) return null
    try {
      return analyseQuality(windowSamples)
    } catch {
      return null
    }
  }, [windowSamples])

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (!file) return

    setParsing(true)
    setParseError(null)
    setParseResult(null)
    setUploadError(null)
    setFileName(file.name)

    try {
      const content = await file.text()
      const result = parseHRFile(file.name, content)
      setParseResult(result)

      const firstMillis = Date.parse(result.samples[0].time)
      const lastMillis = Date.parse(result.samples[result.samples.length - 1].time)
      setStartInput(toLocalInputValue(firstMillis))
      setEndInput(toLocalInputValue(lastMillis))
    } catch (error) {
      setParseError(
        error instanceof ImportParseError
          ? error.message
          : 'Não consegui ler esse arquivo. Tente exportar em CSV ou JSON.',
      )
    } finally {
      setParsing(false)
    }
  }

  /** Selecting an event narrows the window to that event's scheduled times. */
  function handleEventChange(value: string) {
    setEventId(value)
    const selected = eventList.find((item) => item.id === value)
    if (!selected?.start_time || !selected.end_time) return

    setStartInput(toLocalInputValue(Date.parse(selected.start_time)))
    setEndInput(toLocalInputValue(Date.parse(selected.end_time)))
  }

  async function handleUpload() {
    if (windowSamples.length < 10) {
      setUploadError('São necessárias ao menos 10 leituras no período selecionado.')
      return
    }
    if (windowSamples.length > MAX_UPLOAD_POINTS) {
      setUploadError(
        `O período selecionado tem ${windowSamples.length.toLocaleString('pt-BR')} leituras, ` +
          `acima do limite de ${MAX_UPLOAD_POINTS.toLocaleString('pt-BR')}. Reduza o intervalo.`,
      )
      return
    }

    setUploading(true)
    setUploadError(null)

    try {
      const session = await uploadSession({
        start_time: windowSamples[0].time,
        end_time: windowSamples[windowSamples.length - 1].time,
        source_device: sourceDevice.trim() || undefined,
        event_id: eventId || undefined,
        data_points: windowSamples.map((sample) => ({
          time: sample.time,
          bpm: sample.bpm,
          source: parseResult?.format,
        })),
      })

      // Peak detection has to run before the experience view has anything to show.
      await analyzeSession(session.id)
      router.push(`/experience?session=${session.id}`)
    } catch (error) {
      setUploadError(
        error instanceof Error
          ? error.message
          : 'Falha ao enviar os dados. Tente novamente.',
      )
      setUploading(false)
    }
  }

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-2xl px-4 py-8">
          <h1 className="text-3xl font-bold text-tumtum-white">
            Importar batimentos
          </h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            Envie o arquivo exportado do app do seu dispositivo para ver sua curva real
            com suas batidas. Aceita CSV, JSON e a exportação do Apple Saúde.
          </p>

          {/* Step 1 — file */}
          <Card className="mt-6">
            <Card.Header>
              <Card.Title>1. Escolha o arquivo</Card.Title>
            </Card.Header>

            <label className="flex cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-tumtum-border px-4 py-8 text-center transition-colors hover:border-tumtum-muted">
              <span className="text-3xl">📂</span>
              <span className="mt-3 text-sm font-medium text-tumtum-white">
                {fileName ?? 'Toque para selecionar'}
              </span>
              <span className="mt-1 text-xs text-tumtum-muted">
                .csv, .tsv, .json ou .xml
              </span>
              <input
                type="file"
                accept=".csv,.tsv,.json,.xml,.txt,text/csv,application/json,text/xml"
                onChange={handleFileChange}
                className="hidden"
              />
            </label>

            {parsing && (
              <div className="mt-4 flex justify-center">
                <Loading />
              </div>
            )}

            {parseError && (
              <p className="mt-4 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
                {parseError}
              </p>
            )}

            {parseResult && (
              <div className="mt-4 space-y-2">
                <div className="flex items-center gap-2">
                  <Badge variant="accent">{formatLabels[parseResult.format]}</Badge>
                  <span className="text-sm text-tumtum-muted">
                    {parseResult.samples.length.toLocaleString('pt-BR')} leituras
                  </span>
                </div>
                {parseResult.warnings.map((warning) => (
                  <p key={warning} className="text-xs text-tumtum-muted">
                    {warning}
                  </p>
                ))}
              </div>
            )}
          </Card>

          {/* Step 2 — window, event and device */}
          {parseResult && (
            <Card className="mt-4">
              <Card.Header>
                <Card.Title>2. Ajuste o período</Card.Title>
              </Card.Header>

              <div className="space-y-4">
                <div>
                  <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="event">
                    Evento (opcional)
                  </label>
                  <select
                    id="event"
                    value={eventId}
                    onChange={(e) => handleEventChange(e.target.value)}
                    className="w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none"
                  >
                    <option value="">Sem evento — só a curva</option>
                    {eventList.map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.name}
                      </option>
                    ))}
                  </select>
                  <p className="mt-1 text-xs text-tumtum-muted">
                    Vincular a um evento é o que permite casar seus picos com os momentos
                    (músicas, gols) da linha do tempo.
                  </p>
                </div>

                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div>
                    <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="start">
                      Início
                    </label>
                    <input
                      id="start"
                      type="datetime-local"
                      value={startInput}
                      onChange={(e) => setStartInput(e.target.value)}
                      className="w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="end">
                      Fim
                    </label>
                    <input
                      id="end"
                      type="datetime-local"
                      value={endInput}
                      onChange={(e) => setEndInput(e.target.value)}
                      className="w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none"
                    />
                  </div>
                </div>

                <Input
                  label="Dispositivo"
                  value={sourceDevice}
                  onChange={(e) => setSourceDevice(e.target.value)}
                  placeholder="Ex: JStyle V8"
                />
              </div>
            </Card>
          )}

          {/* Step 3 — quality report */}
          {report && (
            <Card className="mt-4">
              <Card.Header>
                <Card.Title>3. Qualidade dos dados</Card.Title>
              </Card.Header>

              <div className={`rounded-lg border p-4 ${verdictStyles[report.verdict]}`}>
                <p className="text-lg font-semibold">{VERDICT_LABELS[report.verdict]}</p>
                <p className="mt-1 text-sm opacity-90">
                  {VERDICT_DESCRIPTIONS[report.verdict]}
                </p>
              </div>

              <dl className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
                <Metric label="Leituras" value={report.sampleCount.toLocaleString('pt-BR')} />
                <Metric label="Duração" value={formatDuration(report.durationSeconds)} />
                <Metric
                  label="Cadência"
                  value={`${report.medianIntervalSeconds.toFixed(1)}s`}
                  hint={`alvo ${TARGET_INTERVAL_SECONDS}s`}
                />
                <Metric label="Cobertura" value={`${(report.coverage * 100).toFixed(0)}%`} />
                <Metric label="Interrupções" value={String(report.gapCount)} />
                <Metric
                  label="BPM"
                  value={`${report.minBpm}–${report.maxBpm}`}
                  hint={`média ${report.avgBpm}`}
                />
              </dl>

              <ul className="mt-4 space-y-2">
                {report.notes.map((note) => (
                  <li key={note} className="flex gap-2 text-sm text-tumtum-muted">
                    <span aria-hidden="true">•</span>
                    <span>{note}</span>
                  </li>
                ))}
              </ul>
            </Card>
          )}

          {/* Step 4 — upload */}
          {parseResult && (
            <div className="mt-6">
              {uploadError && (
                <p className="mb-3 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
                  {uploadError}
                </p>
              )}
              <Button
                onClick={handleUpload}
                loading={uploading}
                disabled={windowSamples.length < 10}
                size="lg"
                className="w-full"
              >
                {uploading ? 'Processando...' : 'Ver minha experiência'}
              </Button>
              <p className="mt-2 text-center text-xs text-tumtum-muted">
                {windowSamples.length.toLocaleString('pt-BR')} leituras serão enviadas.
              </p>
            </div>
          )}
        </div>
      </main>
    </>
  )
}

function Metric({ label, value, hint }: { label: string; value: string; hint?: string }) {
  return (
    <div className="rounded-lg border border-tumtum-border bg-tumtum-black p-3">
      <dt className="text-xs text-tumtum-muted">{label}</dt>
      <dd className="mt-1 text-lg font-semibold text-tumtum-white">{value}</dd>
      {hint && <p className="text-xs text-tumtum-muted">{hint}</p>}
    </div>
  )
}
