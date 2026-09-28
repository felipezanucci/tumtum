'use client'

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'

import { ApiError, admin, type AdminDataSubjectRequest } from '@/lib/api'
import { adminDueLine, adminRequestKindLabel, requestStatusLabel } from '@/lib/privacy-requests'
import type { RequestStatus } from '@/lib/privacy-requests'
import { useCurrentUser } from '@/lib/hooks/useCurrentUser'
import { Loading, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'

/**
 * Operação › Pedidos ao encarregado (28/09) — where a request a person made
 * under LGPD art. 18 is read and answered. The contract gives each one 15
 * days, so the deadline is the loudest thing on a row: "Vence em N dias",
 * "Vence hoje", "Atrasado há N dias".
 *
 * Three acts: answer (the text goes back with the request, and the person
 * sees it in their profile), close, and reopen one that was answered or
 * closed by mistake. An empty queue and a queue that could not be read are
 * different claims; only the first may say "Nenhum pedido aberto".
 */

function day(iso: string): string {
  return new Date(iso).toLocaleDateString('pt-BR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'America/Sao_Paulo',
  })
}

const DONE: Record<RequestStatus, string> = {
  answered: 'Resposta salva. A pessoa vê no perfil dela.',
  closed: 'Pedido fechado.',
  open: 'Pedido reaberto. O prazo volta a contar.',
}

export default function RequestsPage() {
  const user = useCurrentUser()
  const [list, setList] = useState<AdminDataSubjectRequest[] | null>(null)
  const [needsSignIn, setNeedsSignIn] = useState(false)
  const [notOperator, setNotOperator] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<{ id: string; message: string } | null>(null)
  const [busy, setBusy] = useState<string | null>(null)
  const [done, setDone] = useState<{ id: string; message: string } | null>(null)
  const [drafts, setDrafts] = useState<Record<string, string>>({})

  const load = useCallback(() => {
    setError(null)
    return admin.requests
      .list()
      .then((rows) => {
        setList(rows)
        // A saved answer starts as the draft, so editing it is editing it.
        setDrafts((prev) => {
          const next: Record<string, string> = {}
          for (const row of rows) next[row.id] = prev[row.id] ?? row.answer ?? ''
          return next
        })
      })
      .catch((err: unknown) => {
        if (err instanceof ApiError && err.status === 401) {
          setNeedsSignIn(true)
          return
        }
        if (err instanceof ApiError && err.status === 403) {
          setNotOperator(true)
          return
        }
        setList(null)
        setError(err instanceof Error ? err.message : '')
      })
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  async function act(row: AdminDataSubjectRequest, status: RequestStatus) {
    const draft = (drafts[row.id] ?? '').trim()
    // Closing with an answer typed keeps the answer; nothing typed is lost.
    const answer = status === 'answered' || (status === 'closed' && draft && draft !== (row.answer ?? ''))
      ? draft
      : undefined
    setBusy(row.id)
    setActionError(null)
    setDone(null)
    try {
      const updated = await admin.requests.answer(row.id, { status, answer })
      setList((prev) => (prev ? prev.map((r) => (r.id === updated.id ? updated : r)) : prev))
      setDone({ id: row.id, message: DONE[status] })
    } catch (err) {
      setActionError({
        id: row.id,
        message: err instanceof Error ? err.message : 'Não deu pra salvar. Tenta de novo.',
      })
    } finally {
      setBusy(null)
    }
  }

  const openCount = list?.filter((r) => r.status === 'open').length ?? 0

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-2xl px-4 py-8">
          <Link href="/admin/eventos" className="text-sm text-tumtum-muted hover:text-tumtum-white">
            ← Eventos
          </Link>
          <h1 className="mt-3 text-3xl font-hero text-tumtum-white">Pedidos ao encarregado</h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            O que as pessoas pediram sobre os próprios dados. Cada pedido tem 15 dias pra ser
            respondido.
          </p>

          {needsSignIn && <SignInRequired what="os pedidos" />}
          {(notOperator || (user && user.is_admin !== true)) && (
            <p className="mt-6 text-sm text-tumtum-muted">Sua conta não opera a plataforma.</p>
          )}

          {error !== null && (
            <div
              role="alert"
              className="mt-6 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400"
            >
              <p>Não deu pra carregar os pedidos. {error}</p>
              <button
                type="button"
                onClick={() => void load()}
                className="mt-2 text-tumtum-white underline underline-offset-2"
              >
                Tentar de novo
              </button>
            </div>
          )}

          {list === null && error === null && !needsSignIn && !notOperator && (
            <div className="flex justify-center py-16">
              <Loading />
            </div>
          )}

          {list !== null && openCount === 0 && (
            <p className="mt-8 text-sm text-tumtum-muted">Nenhum pedido aberto.</p>
          )}

          {list !== null && list.length > 0 && (
            <ul className="mt-6 space-y-4">
              {list.map((row) => {
                const isOpen = row.status === 'open'
                const late = isOpen && adminDueLine(row.due_at).startsWith('Atrasado')
                const draft = drafts[row.id] ?? ''
                return (
                  <li
                    key={row.id}
                    className="rounded-lg border border-tumtum-border bg-tumtum-surface p-4"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="min-w-0">
                        <p className="font-headline text-tumtum-white">
                          {adminRequestKindLabel(row.kind)}
                        </p>
                        <p className="mt-1 truncate text-sm text-tumtum-muted">
                          {row.user_name ?? 'Conta apagada'}
                          {row.user_email ? ` · ${row.user_email}` : ''}
                        </p>
                      </div>
                      {isOpen ? (
                        <span
                          className={`shrink-0 rounded px-2 py-0.5 text-xs font-label text-tumtum-black ${
                            late ? 'bg-tumtum-pink' : 'bg-tumtum-yellow'
                          }`}
                        >
                          {adminDueLine(row.due_at)}
                        </span>
                      ) : (
                        <span className="shrink-0 rounded border border-tumtum-border px-2 py-0.5 text-xs font-label text-tumtum-muted">
                          {requestStatusLabel(row.status)}
                        </span>
                      )}
                    </div>

                    {row.message ? (
                      <p className="mt-3 whitespace-pre-line text-sm text-tumtum-white">{row.message}</p>
                    ) : (
                      <p className="mt-3 text-sm italic text-tumtum-muted">Sem mensagem.</p>
                    )}
                    <p className="mt-2 text-xs text-tumtum-muted">
                      Aberto em {day(row.opened_at)}
                      {row.answered_at ? ` · respondido em ${day(row.answered_at)}` : ''}
                    </p>

                    <label htmlFor={`answer-${row.id}`} className="mt-4 block text-sm font-medium text-tumtum-white">
                      Resposta
                    </label>
                    <textarea
                      id={`answer-${row.id}`}
                      value={draft}
                      onChange={(e) => setDrafts((prev) => ({ ...prev, [row.id]: e.target.value }))}
                      rows={3}
                      maxLength={8000}
                      disabled={busy !== null}
                      className="mt-1.5 w-full rounded-lg border border-tumtum-border bg-tumtum-black px-3 py-2 text-sm text-tumtum-white placeholder:text-tumtum-muted focus:outline-none focus:ring-2 focus:ring-tumtum-pink disabled:opacity-60"
                      placeholder="O que a pessoa vai ler no perfil dela."
                    />

                    <div className="mt-3 flex flex-wrap gap-2">
                      <button
                        type="button"
                        disabled={busy !== null || draft.trim() === ''}
                        onClick={() => void act(row, 'answered')}
                        className="rounded-lg bg-tumtum-pink px-4 py-2 text-sm font-label text-tumtum-black disabled:opacity-60"
                      >
                        {busy === row.id ? 'Salvando…' : 'Responder'}
                      </button>
                      {row.status !== 'closed' && (
                        <button
                          type="button"
                          disabled={busy !== null}
                          onClick={() => void act(row, 'closed')}
                          className="rounded-lg border border-tumtum-border px-4 py-2 text-sm font-label text-tumtum-white disabled:opacity-60"
                        >
                          Fechar
                        </button>
                      )}
                      {!isOpen && (
                        <button
                          type="button"
                          disabled={busy !== null}
                          onClick={() => void act(row, 'open')}
                          className="rounded-lg border border-tumtum-border px-4 py-2 text-sm font-label text-tumtum-white disabled:opacity-60"
                        >
                          Reabrir
                        </button>
                      )}
                    </div>
                    {draft.trim() === '' && (
                      <p className="mt-2 text-xs text-tumtum-muted">Escreva a resposta pra poder responder.</p>
                    )}
                    {done?.id === row.id && (
                      <p role="status" className="mt-2 text-sm text-tumtum-white">
                        {done.message}
                      </p>
                    )}
                    {actionError?.id === row.id && (
                      <p role="alert" className="mt-2 text-sm text-red-400">
                        {actionError.message}
                      </p>
                    )}
                  </li>
                )
              })}
            </ul>
          )}
        </div>
      </main>
    </>
  )
}
