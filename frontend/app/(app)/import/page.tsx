'use client'

import { useEffect, useMemo, useState } from 'react'
import { useRouter } from 'next/navigation'
import { ConsentRequiredError } from '@/lib/api'
import { consentHref } from '@/lib/consent'
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
import {
  eventImportWindow,
  formatImportWindow,
  IMPORT_MARGIN_MINUTES,
} from '@/lib/health/import-window'
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

/**
 * Importing a file since 26/09 (LGPD audit B1): the event comes first and is
 * required, and only the readings inside the event's hours — with 30 minutes
 * on each side — ever leave the browser. The window is derived from the
 * event, never typed, so there is no "whole file" to send by accident.
 */
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

  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState<string | null>(null)

  useEffect(() => {
    loadEvents()
  }, [loadEvents])

  const selectedEvent = eventList.find((item) => item.id === eventId) ?? null
  const importWindow = useMemo(
    () => (selectedEvent ? eventImportWindow(selectedEvent) : null),
    [selectedEvent],
  )

  // Nothing outside the event's window, ever: no event, no window, nothing.
  const windowSamples = useMemo(() => {
    if (!parseResult || !importWindow) return []
    return filterSamplesByWindow(parseResult.samples, importWindow.startMs, importWindow.endMs)
  }, [parseResult, importWindow])

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

  async function handleUpload() {
    if (!eventId || !importWindow) {
      setUploadError('Escolha o evento antes de enviar.')
      return
    }
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
        event_id: eventId,
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
      // Keeping the night on the server needs its own yes: ask for it
      // where it is given, never retry in silence.
      if (error instanceof ConsentRequiredError) {
        router.push(consentHref(error.purpose, '/import'))
        return
      }
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

          {/* Step 1 — the event, required: it decides what may leave the browser */}
          <Card className="mt-6">
            <Card.Header>
              <Card.Title>1. Escolha o evento</Card.Title>
            </Card.Header>

            <label className="mb-1 block text-sm text-tumtum-muted" htmlFor="event">
              Evento
            </label>
            <select
              id="event"
              value={eventId}
              onChange={(e) => {
                setEventId(e.target.value)
                setUploadError(null)
              }}
              className="w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2 text-tumtum-white focus:border-tumtum-pink focus:outline-none"
            >
              <option value="">Escolha o evento da noite</option>
              {eventList.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name}
                </option>
              ))}
            </select>

            {selectedEvent && importWindow && (
              <p className="mt-3 text-sm text-tumtum-white">
                Só as leituras entre {formatImportWindow(importWindow)} saem do seu navegador: o
                evento, com {IMPORT_MARGIN_MINUTES} minutos de cada lado. O resto do arquivo fica
                aqui.
              </p>
            )}
            {selectedEvent && !importWindow && (
              <p className="mt-3 rounded-lg border border-amber-500/40 bg-amber-500/10 p-3 text-sm text-amber-300">
                Esse evento ainda não tem horário de começo e de fim, então não dá pra saber que
                pedaço do arquivo é dele. Nada é enviado sem essa janela.
              </p>
            )}
          </Card>

          {/* Step 2 — file */}
          {selectedEvent && importWindow && (
            <Card className="mt-4">
              <Card.Header>
                <Card.Title>2. Escolha o arquivo</Card.Title>
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
                  <p className="text-sm text-tumtum-white">
                    {windowSamples.length === 0
                      ? `Nenhuma leitura desse arquivo cai entre ${formatImportWindow(importWindow)}. Confere se é o arquivo dessa noite.`
                      : `${windowSamples.length.toLocaleString('pt-BR')} leituras caem na janela do evento.`}
                  </p>
                </div>
              )}
            </Card>
          )}

          {/* Device */}
          {parseResult && importWindow && (
            <Card className="mt-4">
              <Input
                label="Dispositivo"
                value={sourceDevice}
                onChange={(e) => setSourceDevice(e.target.value)}
                placeholder="Ex: JStyle V8"
              />
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
          {parseResult && importWindow && (
            <div className="mt-6">
              {uploadError && (
                <p className="mb-3 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
                  {uploadError}
                </p>
              )}
              <Button
                onClick={handleUpload}
                loading={uploading}
                disabled={!eventId || windowSamples.length < 10}
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
