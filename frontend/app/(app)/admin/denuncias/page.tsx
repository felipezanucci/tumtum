'use client'

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'

import { ApiError, moderation, type ReportedPost } from '@/lib/api'
import { useCurrentUser } from '@/lib/hooks/useCurrentUser'
import { Loading, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'

/**
 * Operação › Denúncias — where a report from the feed is read by a person.
 *
 * Item 55 (22/09): the feed shipped without report or block, and the Play
 * listing cannot go public until both exist and "land where a human reads
 * them". This is that place. Two decisions and only two: keep the post, or
 * take it down. Three reports hide a post from the feed until one is made,
 * which this page says, so a hidden post is never mistaken for a live one.
 */

const reasonLabel: Record<string, string> = {
  abuse: 'ofensivo',
  fake: 'parece falso',
  other: 'outra coisa',
}

function when(iso: string): string {
  return new Date(iso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'America/Sao_Paulo',
  })
}

export default function ReportsPage() {
  const user = useCurrentUser()
  const [list, setList] = useState<ReportedPost[] | null>(null)
  const [needsSignIn, setNeedsSignIn] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState<string | null>(null)
  const [done, setDone] = useState<string | null>(null)

  const load = useCallback(() => {
    setError(null)
    return moderation
      .open()
      .then(setList)
      .catch((err: unknown) => {
        // An empty queue and a queue that could not be read are different
        // claims; only the first may say "nada pra revisar".
        if (err instanceof ApiError && err.status === 401) {
          setNeedsSignIn(true)
          return
        }
        setList(null)
        setError(err instanceof Error ? err.message : 'Não deu pra carregar as denúncias.')
      })
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  async function decide(post: ReportedPost, action: 'keep' | 'remove') {
    setBusy(post.post_id)
    setError(null)
    try {
      await moderation.resolve(post.post_id, action)
      setDone(
        action === 'remove'
          ? `O post de ${post.author.name} saiu do feed.`
          : `O post de ${post.author.name} fica — e volta pro feed se tinha sido escondido.`,
      )
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não deu pra salvar a decisão.')
    } finally {
      setBusy(null)
    }
  }

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-2xl px-4 py-8">
          <Link href="/admin/eventos" className="text-sm text-tumtum-muted hover:text-tumtum-white">
            ← Eventos
          </Link>
          <h1 className="mt-3 text-3xl font-hero text-tumtum-white">Denúncias</h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            O que alguém que estava lá achou que não devia estar no feed. Com três denúncias o post
            some do feed até você decidir.
          </p>

          {needsSignIn && <SignInRequired what="as denúncias" />}
          {user && user.is_admin !== true && (
            <p className="mt-6 text-sm text-tumtum-muted">Sua conta não opera a plataforma.</p>
          )}

          {done && <p className="mt-6 text-sm text-tumtum-white">{done}</p>}
          {error && (
            <p className="mt-6 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
              {error}
            </p>
          )}

          {list === null && !error && !needsSignIn && (
            <div className="flex justify-center py-16">
              <Loading />
            </div>
          )}

          {list !== null && list.length === 0 && (
            <p className="mt-8 text-sm text-tumtum-muted">Nada pra revisar agora.</p>
          )}

          {list !== null && list.length > 0 && (
            <ul className="mt-6 space-y-4">
              {list.map((post) => (
                <li
                  key={post.post_id}
                  className="rounded-lg border border-tumtum-border bg-tumtum-surface p-4"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="min-w-0">
                      <p className="text-sm text-tumtum-muted">{post.event_name}</p>
                      <p className="mt-1 font-headline text-tumtum-white">
                        {post.author.name} · <span className="tabular-nums">{post.bpm}</span> bpm
                        {post.label ? ` · ${post.label}` : ''}
                      </p>
                      {post.quote && (
                        <p className="mt-1 text-sm italic text-tumtum-white">“{post.quote}”</p>
                      )}
                    </div>
                    {post.hidden && (
                      <span className="shrink-0 rounded bg-tumtum-yellow px-2 py-0.5 text-xs font-label text-tumtum-black">
                        escondido
                      </span>
                    )}
                  </div>
                  <p className="mt-3 text-xs text-tumtum-muted">
                    {post.reports} {post.reports === 1 ? 'denúncia' : 'denúncias'} (
                    {Object.entries(post.reasons)
                      .map(([r, n]) => `${reasonLabel[r] ?? r}: ${n}`)
                      .join(', ')}
                    ) · desde {when(post.first_reported_at)}
                  </p>
                  <div className="mt-4 flex gap-2">
                    <button
                      type="button"
                      disabled={busy !== null}
                      onClick={() => decide(post, 'remove')}
                      className="rounded-lg bg-tumtum-pink px-4 py-2 text-sm font-label text-tumtum-black disabled:opacity-60"
                    >
                      {busy === post.post_id ? 'Salvando…' : 'Tirar do ar'}
                    </button>
                    <button
                      type="button"
                      disabled={busy !== null}
                      onClick={() => decide(post, 'keep')}
                      className="rounded-lg border border-tumtum-border px-4 py-2 text-sm font-label text-tumtum-white disabled:opacity-60"
                    >
                      Manter
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </main>
    </>
  )
}
