'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'

import { ApiError, health, type HRSession } from '@/lib/api'
import { Badge, Card, Loading, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'
import { formatDuration } from '@/lib/health/quality'

/**
 * Every capture, listed.
 *
 * The profile has always counted sessions — "9 Sessões" — with no way to see
 * which nine. A person who captured from the Android app had no way to
 * confirm their night actually arrived: the count went up by one, silently,
 * and a number with no list is one more piece of invisible state. This page
 * is the receipt.
 */
export default function SessionsPage() {
  const [sessions, setSessions] = useState<HRSession[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [needsSignIn, setNeedsSignIn] = useState(false)

  useEffect(() => {
    health
      .listSessions()
      .then((list) =>
        // Newest first: "did my capture arrive?" is always about the last one.
        setSessions(
          [...list].sort((a, b) => Date.parse(b.start_time) - Date.parse(a.start_time)),
        ),
      )
      .catch((err: unknown) => {
        if (err instanceof ApiError && err.status === 401) {
          setNeedsSignIn(true)
          return
        }
        setError(err instanceof Error ? err.message : 'Não foi possível carregar.')
      })
  }, [])

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-2xl px-4 py-8">
          <h1 className="text-3xl font-hero text-tumtum-white">Suas sessões</h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            Cada captura que chegou até aqui, a mais recente primeiro.
          </p>

          {needsSignIn && <SignInRequired what="suas sessões" />}

          {error && (
            <p className="mt-6 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
              {error}
            </p>
          )}

          {!sessions && !error && !needsSignIn && (
            <div className="mt-10 flex justify-center">
              <Loading />
            </div>
          )}

          {sessions && sessions.length === 0 && (
            <p className="mt-6 text-tumtum-muted">
              Nenhuma sessão ainda. Abra o app TumTum no Android, conecte seu sensor e capture a primeira.
            </p>
          )}

          <div className="mt-6 space-y-3">
            {sessions?.map((session) => {
              const started = new Date(session.start_time)
              const seconds =
                (Date.parse(session.end_time) - Date.parse(session.start_time)) / 1000
              return (
                <Link
                  key={session.id}
                  href={`/experience?session=${session.id}`}
                  className="block"
                >
                  <Card className="transition-colors hover:border-tumtum-lime/50">
                    <div className="flex items-center justify-between gap-3">
                      <div>
                        <p className="font-medium text-tumtum-white">
                          {started.toLocaleDateString('pt-BR', {
                            day: '2-digit',
                            month: 'short',
                          })}{' '}
                          ·{' '}
                          {started.toLocaleTimeString('pt-BR', {
                            hour: '2-digit',
                            minute: '2-digit',
                          })}
                        </p>
                        <p className="mt-1 text-sm text-tumtum-muted">
                          {formatDuration(seconds)}
                          {session.source_device ? ` · ${session.source_device}` : ''}
                        </p>
                      </div>
                      <div className="text-right">
                        {session.max_bpm && (
                          <p className="text-lg font-semibold text-tumtum-lime">
                            {session.max_bpm} <span className="text-xs">máx</span>
                          </p>
                        )}
                        {session.avg_bpm && (
                          <p className="text-xs text-tumtum-muted">{session.avg_bpm} média</p>
                        )}
                      </div>
                    </div>
                  </Card>
                </Link>
              )
            })}
          </div>
        </div>
      </main>
    </>
  )
}
