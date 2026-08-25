'use client'

import { Suspense, useEffect, useState } from 'react'
import { useSearchParams, useRouter } from 'next/navigation'
import { useEventStore } from '@/lib/stores/useEventStore'
import { cards } from '@/lib/api'
import { HRCurve, PeakMarker, TimelineBar } from '@/components/hr'
import { Button, Loading } from '@/components/ui'
import { Nav } from '@/components/layout'


/**
 * The detection compares every moment to the five minutes around it — see the
 * peak detection spec in CLAUDE.md. A capture shorter than that window has
 * nothing for a rise to stand out against, so finding nothing is the correct
 * answer rather than a failure.
 *
 * The screen used to say "analyse the session first" in both cases, which was
 * never true by the time anyone read it: the analysis runs the moment a
 * capture is saved.
 */
const BASELINE_WINDOW_SECONDS = 300

function noPeaksMessage(session: { start_time: string; end_time: string }): string {
  const seconds = (Date.parse(session.end_time) - Date.parse(session.start_time)) / 1000
  if (Number.isFinite(seconds) && seconds < BASELINE_WINDOW_SECONDS) {
    const minutes = Math.max(1, Math.round(seconds / 60))
    return (
      `Esta captura tem ${minutes} ${minutes === 1 ? 'minuto' : 'minutos'}. ` +
      'Para um momento se destacar, ele precisa dos cinco minutos ao redor ' +
      'para comparar — numa noite inteira sobra.'
    )
  }
  return 'Nenhum momento se destacou aqui. Sua batida seguiu no mesmo ritmo.'
}

export default function ExperiencePage() {
  return (
    <Suspense fallback={<><Nav /><main className="flex min-h-screen items-center justify-center bg-tumtum-black"><Loading size="lg" /></main></>}>
      <ExperienceContent />
    </Suspense>
  )
}

function ExperienceContent() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const sessionId = searchParams.get('session')

  const { experienceData, experienceLoading, loadExperience } = useEventStore()
  const [generatingCard, setGeneratingCard] = useState(false)
  const [cardError, setCardError] = useState<string | null>(null)

  useEffect(() => {
    if (sessionId) {
      loadExperience(sessionId)
    }
  }, [sessionId, loadExperience])

  if (!sessionId) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-black">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-tumtum-white">
              Nenhuma sessão selecionada
            </h1>
            <p className="mt-2 text-tumtum-muted">
              Selecione um evento para ver sua experiência.
            </p>
          </div>
        </main>
      </>
    )
  }

  if (experienceLoading) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-black">
          <Loading size="lg" />
        </main>
      </>
    )
  }

  if (!experienceData) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-black">
          <p className="text-tumtum-muted">Dados não encontrados.</p>
        </main>
      </>
    )
  }

  const { session, peaks, timeline } = experienceData

  async function handleGenerateCard() {
    if (!sessionId) return
    setGeneratingCard(true)
    setCardError(null)
    try {
      const topPeak = peaks.length > 0 ? peaks[0] : null
      await cards.create({
        session_id: sessionId,
        peak_id: topPeak?.id,
        card_type: 'solo',
        format: 'story',
      })
      router.push('/cards')
    } catch (err: any) {
      setCardError(err.detail || 'Erro ao gerar card')
    } finally {
      setGeneratingCard(false)
    }
  }

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-5xl px-4 py-8">
          {/* Header */}
          <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h1 className="text-3xl font-bold text-tumtum-white">
                Sua Experiência
              </h1>
              <div className="mt-2 flex flex-wrap gap-4 text-sm text-tumtum-muted">
                {session.avg_bpm && <span>Média: {session.avg_bpm} bpm</span>}
                {session.max_bpm && (
                  <span className="text-tumtum-lime">Máx: {session.max_bpm} bpm</span>
                )}
                {session.min_bpm && <span>Mín: {session.min_bpm} bpm</span>}
                {session.data_quality_score !== null && (
                  <span>Qualidade: {session.data_quality_score}%</span>
                )}
              </div>
            </div>
            <div className="flex flex-col items-end gap-1">
              <Button onClick={handleGenerateCard} disabled={generatingCard}>
                {generatingCard ? 'Gerando...' : 'Gerar Card para Compartilhar'}
              </Button>
              {cardError && (
                <p className="text-xs text-red-400">{cardError}</p>
              )}
            </div>
          </div>

          {/* HR Curve */}
          <div className="mb-8 rounded-xl border border-tumtum-border bg-tumtum-surface p-4">
            <HRCurve
              data={experienceData.hr_data || []}
              peaks={peaks}
              timeline={timeline}
              height={350}
              animated
            />
            <p className="mt-2 text-center text-xs text-tumtum-muted">
              Sua batida ao longo da noite
            </p>
          </div>

          <div className="grid gap-8 lg:grid-cols-3">
            {/* Peaks */}
            <div className="lg:col-span-2">
              <h2 className="mb-4 text-lg font-semibold text-tumtum-white">
                Seus Picos de Emoção
              </h2>
              {peaks.length === 0 ? (
                <p className="text-tumtum-muted">{noPeaksMessage(session)}</p>
              ) : (
                <div className="space-y-3">
                  {peaks.map((peak) => (
                    <PeakMarker key={peak.id} peak={peak} rank={peak.rank ?? undefined} />
                  ))}
                </div>
              )}
            </div>

            {/* Timeline */}
            <div>
              <TimelineBar entries={timeline} />
            </div>
          </div>
        </div>
      </main>
    </>
  )
}
