'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'

import { ApiError, health, type HRSession } from '@/lib/api'
import { Button, Card, Loading, SignInRequired } from '@/components/ui'
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
 *
 * And, since 26/09, the way out for one night: "Apagar esta noite" takes the
 * readings, the moments, the cards and any feed post of that night, and
 * leaves the account and every other night alone.
 */
export default function SessionsPage() {
  const [sessions, setSessions] = useState<HRSession[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [needsSignIn, setNeedsSignIn] = useState(false)
  // The night whose delete is waiting for the second tap, and the one going.
  const [confirmingId, setConfirmingId] = useState<string | null>(null)
  const [deletingId, setDeletingId] = useState<string | null>(null)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  async function handleDelete(sessionId: string) {
    setDeletingId(sessionId)
    setDeleteError(null)
    setNotice(null)
    try {
      await health.deleteSession(sessionId)
      setSessions((prev) => prev?.filter((s) => s.id !== sessionId) ?? prev)
      setNotice('Noite apagada. As leituras, os momentos e os cards dela saíram dos servidores.')
      setConfirmingId(null)
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : 'Não deu pra apagar essa noite.')
    } finally {
      setDeletingId(null)
    }
  }

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

          {notice && (
            <p role="status" className="mt-6 rounded-lg border border-tumtum-border bg-tumtum-surface p-3 text-sm text-tumtum-white">
              {notice}
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
                <Card key={session.id} className="transition-colors hover:border-tumtum-pink/50">
                  <Link href={`/experience?session=${session.id}`} className="block">
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
                          <p className="text-lg font-semibold text-tumtum-pink">
                            {session.max_bpm} <span className="text-xs">máx</span>
                          </p>
                        )}
                        {session.avg_bpm && (
                          <p className="text-xs text-tumtum-muted">{session.avg_bpm} média</p>
                        )}
                      </div>
                    </div>
                  </Link>
                  <div className="mt-4 border-t border-tumtum-border pt-3">
                    {confirmingId === session.id ? (
                      <div>
                        <p className="text-sm text-tumtum-white">
                          Apagar essa noite de vez? Vão junto as leituras, os momentos, os cards e
                          qualquer post dela no feed.
                        </p>
                        {deleteError && (
                          <p role="alert" className="mt-2 text-sm text-red-400">{deleteError}</p>
                        )}
                        <div className="mt-3 flex gap-2">
                          <Button
                            size="sm"
                            variant="danger"
                            loading={deletingId === session.id}
                            onClick={() => void handleDelete(session.id)}
                          >
                            Apagar de vez
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            disabled={deletingId === session.id}
                            onClick={() => {
                              setConfirmingId(null)
                              setDeleteError(null)
                            }}
                          >
                            Cancelar
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <button
                        type="button"
                        onClick={() => {
                          setConfirmingId(session.id)
                          setDeleteError(null)
                          setNotice(null)
                        }}
                        className="text-sm text-tumtum-muted underline-offset-2 hover:text-tumtum-white hover:underline"
                      >
                        Apagar esta noite
                      </button>
                    )}
                  </div>
                </Card>
              )
            })}
          </div>
        </div>
      </main>
    </>
  )
}
