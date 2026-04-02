'use client'

import { Suspense, useEffect } from 'react'
import { useSearchParams } from 'next/navigation'
import { useEventStore } from '@/lib/stores/useEventStore'
import { HRCurve, PeakMarker, TimelineBar } from '@/components/hr'
import { Loading } from '@/components/ui'
import { Nav } from '@/components/layout'

export default function ExperiencePage() {
  return (
    <Suspense fallback={<><Nav /><main className="flex min-h-screen items-center justify-center bg-tumtum-dark"><Loading size="lg" /></main></>}>
      <ExperienceContent />
    </Suspense>
  )
}

function ExperienceContent() {
  const searchParams = useSearchParams()
  const sessionId = searchParams.get('session')

  const { experienceData, experienceLoading, loadExperience } = useEventStore()

  useEffect(() => {
    if (sessionId) {
      loadExperience(sessionId)
    }
  }, [sessionId, loadExperience])

  if (!sessionId) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-dark">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-tumtum-text-primary">
              Nenhuma sessão selecionada
            </h1>
            <p className="mt-2 text-tumtum-text-muted">
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
        <main className="flex min-h-screen items-center justify-center bg-tumtum-dark">
          <Loading size="lg" />
        </main>
      </>
    )
  }

  if (!experienceData) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-dark">
          <p className="text-tumtum-text-muted">Dados não encontrados.</p>
        </main>
      </>
    )
  }

  const { session, peaks, timeline } = experienceData

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-dark">
        <div className="mx-auto max-w-5xl px-4 py-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-tumtum-text-primary">
              Sua Experiência
            </h1>
            <div className="mt-2 flex flex-wrap gap-4 text-sm text-tumtum-text-muted">
              {session.avg_bpm && <span>Média: {session.avg_bpm} bpm</span>}
              {session.max_bpm && (
                <span className="text-tumtum-red">Máx: {session.max_bpm} bpm</span>
              )}
              {session.min_bpm && <span>Mín: {session.min_bpm} bpm</span>}
              {session.data_quality_score !== null && (
                <span>Qualidade: {session.data_quality_score}%</span>
              )}
            </div>
          </div>

          {/* HR Curve — placeholder data for the chart */}
          <div className="mb-8 rounded-xl border border-tumtum-border bg-tumtum-surface p-4">
            <HRCurve
              data={[]}
              peaks={peaks}
              timeline={timeline}
              height={350}
              animated
            />
            <p className="mt-2 text-center text-xs text-tumtum-text-muted">
              Curva de frequência cardíaca durante o evento
            </p>
          </div>

          <div className="grid gap-8 lg:grid-cols-3">
            {/* Peaks */}
            <div className="lg:col-span-2">
              <h2 className="mb-4 text-lg font-semibold text-tumtum-text-primary">
                Seus Picos de Emoção
              </h2>
              {peaks.length === 0 ? (
                <p className="text-tumtum-text-muted">Nenhum pico detectado ainda. Analise a sessão primeiro.</p>
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
