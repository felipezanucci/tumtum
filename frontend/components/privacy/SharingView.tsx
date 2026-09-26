'use client'

import { useCallback, useEffect, useState } from 'react'

import { cards, feed, users, type SharingOverview } from '@/lib/api'
import {
  audienceLabel,
  neverLine,
  platformLabel,
  reactionsLabel,
  safeHttpsUrl,
  sharingDate,
} from '@/lib/sharing'
import { Button } from '@/components/ui'

/**
 * "Com quem seus dados estão" (LGPD art. 18 VII, 26/09): the operators that
 * hold data on TumTum's behalf, who never gets it, and everything the person
 * made public — with the undo next to each public card.
 *
 * An empty state is a claim: "Nenhum card público" is said only after the
 * server answered. A refused request says it could not ask.
 */
export default function SharingView() {
  const [data, setData] = useState<SharingOverview | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [refreshError, setRefreshError] = useState<string | null>(null)
  const [unpublishing, setUnpublishing] = useState<string | null>(null)
  const [cardNotice, setCardNotice] = useState<string | null>(null)
  const [cardError, setCardError] = useState<{ id: string; message: string } | null>(null)
  const [removingPost, setRemovingPost] = useState<string | null>(null)
  const [postNotice, setPostNotice] = useState<string | null>(null)
  const [postError, setPostError] = useState<{ id: string; message: string } | null>(null)

  const load = useCallback(async () => {
    setLoadError(null)
    try {
      setData(await users.sharing())
    } catch (err) {
      setLoadError(err instanceof Error ? err.message : 'Erro desconhecido')
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  async function handleUnpublish(cardId: string) {
    setUnpublishing(cardId)
    setCardNotice(null)
    setCardError(null)
    setRefreshError(null)
    try {
      await cards.unpublish(cardId)
    } catch (err) {
      setCardError({
        id: cardId,
        message: err instanceof Error ? err.message : 'Não deu pra despublicar. Tenta de novo.',
      })
      setUnpublishing(null)
      return
    }
    // The server said yes: the card leaves the list now, whatever the refresh does.
    setData((prev) =>
      prev ? { ...prev, published_cards: prev.published_cards.filter((c) => c.id !== cardId) } : prev,
    )
    setCardNotice('Card despublicado. O link público parou de funcionar; o card continua na sua coleção.')
    setUnpublishing(null)
    try {
      setData(await users.sharing())
    } catch {
      setRefreshError('Não consegui atualizar a lista agora. Recarregue a página pra ver tudo de novo.')
    }
  }

  async function handleRemovePost(eventId: string, postId: string) {
    setRemovingPost(postId)
    setPostNotice(null)
    setPostError(null)
    setRefreshError(null)
    try {
      await feed.deletePost(eventId, postId)
    } catch (err) {
      setPostError({
        id: postId,
        message: err instanceof Error ? err.message : 'Não deu pra tirar do feed. Tenta de novo.',
      })
      setRemovingPost(null)
      return
    }
    // The server said yes: the post leaves the list now, whatever the refresh does.
    setData((prev) =>
      prev ? { ...prev, feed_posts: prev.feed_posts.filter((p) => p.id !== postId) } : prev,
    )
    setPostNotice('Post tirado do feed. Ninguém mais vê, nem nas outras datas da turnê.')
    setRemovingPost(null)
    try {
      setData(await users.sharing())
    } catch {
      setRefreshError('Não consegui atualizar a lista agora. Recarregue a página pra ver tudo de novo.')
    }
  }

  if (loadError) {
    return (
      <div role="alert" className="rounded-lg border border-red-500/40 bg-red-500/10 p-4 text-sm text-red-400">
        <p>Não consegui carregar com quem seus dados estão. {loadError}</p>
        <button
          type="button"
          onClick={() => void load()}
          className="mt-2 text-tumtum-white underline underline-offset-2"
        >
          Tentar de novo
        </button>
      </div>
    )
  }

  if (data === null) return <SharingSkeleton />

  const never = neverLine(data.never)
  const anpdUrl = safeHttpsUrl(data.anpd.url)

  return (
    <div className="space-y-8">
      {/* (a) Operators */}
      <div>
        <p className="text-sm leading-relaxed text-tumtum-muted">
          Empresas contratadas que guardam ou processam dados em nome da TumTum, e só pra isso.
        </p>
        {data.operators.length === 0 ? (
          <p className="mt-3 text-sm text-tumtum-muted">Nenhum operador listado.</p>
        ) : (
          <ul className="mt-3 divide-y divide-tumtum-border rounded-lg border border-tumtum-border">
            {data.operators.map((op) => (
              <li key={op.name} className="p-3">
                <div className="flex flex-wrap items-baseline justify-between gap-x-3 gap-y-1">
                  <p className="text-sm font-semibold text-tumtum-white">{op.name}</p>
                  <p className="text-xs text-tumtum-muted">{op.role}</p>
                </div>
                <dl className="mt-2 grid grid-cols-[auto_1fr] gap-x-3 gap-y-1 text-sm">
                  <dt className="text-tumtum-faint">Recebe</dt>
                  <dd className="text-tumtum-muted">{op.what}</dd>
                  <dt className="text-tumtum-faint">Por quê</dt>
                  <dd className="text-tumtum-muted">{op.why}</dd>
                  <dt className="text-tumtum-faint">Onde</dt>
                  <dd className="text-tumtum-muted">{op.where}</dd>
                </dl>
              </li>
            ))}
          </ul>
        )}
        {/* (b) Who never gets it */}
        {never && <p className="mt-3 text-sm font-medium text-tumtum-white">{never}</p>}
      </div>

      {/* (c) Public cards */}
      <div>
        <p className="text-sm font-medium text-tumtum-white">Cards públicos</p>
        <p className="mt-1 text-sm text-tumtum-muted">
          Qualquer pessoa com o link vê o card. Despublicar tira a página do ar na hora.
        </p>
        {cardNotice && <p role="status" className="mt-2 text-sm text-tumtum-white">{cardNotice}</p>}
        {refreshError && <p className="mt-2 text-sm text-red-400">{refreshError}</p>}
        {data.published_cards.length === 0 ? (
          <p className="mt-2 text-sm text-tumtum-muted">Nenhum card público.</p>
        ) : (
          <ul className="mt-2 space-y-2">
            {data.published_cards.map((card) => {
              const href = safeHttpsUrl(card.public_url)
              return (
                <li key={card.id} className="rounded-lg border border-tumtum-border p-3">
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-tumtum-white">{card.event_name}</p>
                      <p className="text-xs text-tumtum-muted">Público desde {sharingDate(card.published_at)}</p>
                      {href && (
                        <a
                          href={href}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="mt-1 block truncate text-xs text-tumtum-pink underline underline-offset-2"
                        >
                          {href}
                        </a>
                      )}
                    </div>
                    <Button
                      size="sm"
                      variant="secondary"
                      loading={unpublishing === card.id}
                      disabled={unpublishing !== null}
                      onClick={() => void handleUnpublish(card.id)}
                    >
                      Despublicar
                    </Button>
                  </div>
                  {cardError?.id === card.id && (
                    <p role="alert" className="mt-2 text-sm text-red-400">{cardError.message}</p>
                  )}
                </li>
              )
            })}
          </ul>
        )}
      </div>

      {/* (d) Feed posts */}
      <div>
        <p className="text-sm font-medium text-tumtum-white">Posts no feed</p>
        <p className="mt-1 text-sm text-tumtum-muted">
          Só vê quem gravou a noite no mesmo evento. Tirar do feed tira o post do ar pra todo mundo na hora.
        </p>
        {postNotice && <p role="status" className="mt-2 text-sm text-tumtum-white">{postNotice}</p>}
        {data.feed_posts.length === 0 ? (
          <p className="mt-2 text-sm text-tumtum-muted">Nenhum post no feed.</p>
        ) : (
          <ul className="mt-2 space-y-2">
            {data.feed_posts.map((post) => (
              <li key={post.id} className="rounded-lg border border-tumtum-border p-3">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-tumtum-white">{post.event_name}</p>
                    <p className="mt-0.5 text-xs text-tumtum-muted">
                      {sharingDate(post.created_at)} · {audienceLabel(post.audience)} ·{' '}
                      {reactionsLabel(post.reactions)}
                    </p>
                  </div>
                  <Button
                    size="sm"
                    variant="secondary"
                    loading={removingPost === post.id}
                    disabled={removingPost !== null}
                    onClick={() => void handleRemovePost(post.event_id, post.id)}
                  >
                    Tirar do feed
                  </Button>
                </div>
                {postError?.id === post.id && (
                  <p role="alert" className="mt-2 text-sm text-red-400">{postError.message}</p>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* (e) Recorded shares */}
      <div>
        <p className="text-sm font-medium text-tumtum-white">Compartilhamentos registrados</p>
        <p className="mt-1 text-sm text-tumtum-muted">
          Quando você compartilha um card, a TumTum guarda só pra qual rede foi e quando.
        </p>
        {data.shares.length === 0 ? (
          <p className="mt-2 text-sm text-tumtum-muted">Nenhum compartilhamento registrado.</p>
        ) : (
          <ul className="mt-2 divide-y divide-tumtum-border rounded-lg border border-tumtum-border">
            {data.shares.map((share, i) => (
              <li
                key={`${share.card_id}-${share.shared_at}-${i}`}
                className="flex items-center justify-between gap-3 px-3 py-2 text-sm"
              >
                <span className="text-tumtum-white">{platformLabel(share.platform)}</span>
                <span className="text-xs text-tumtum-muted">{sharingDate(share.shared_at)}</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* (f) ANPD */}
      <div>
        <p className="text-sm font-medium text-tumtum-white">Não resolveu com a gente?</p>
        {data.anpd.note && <p className="mt-1 text-sm leading-relaxed text-tumtum-muted">{data.anpd.note}</p>}
        {anpdUrl && (
          <a
            href={anpdUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="mt-1 inline-block text-sm text-tumtum-pink underline underline-offset-2"
          >
            Falar com a ANPD (Autoridade Nacional de Proteção de Dados)
          </a>
        )}
      </div>
    </div>
  )
}

/** The section's shape while it loads, so nothing below it jumps. */
function SharingSkeleton() {
  return (
    <div aria-busy="true" aria-label="Carregando com quem seus dados estão" className="animate-pulse space-y-4 motion-reduce:animate-none">
      <div className="h-4 w-3/4 rounded bg-tumtum-surface" />
      <div className="h-24 rounded-lg bg-tumtum-surface" />
      <div className="h-4 w-1/2 rounded bg-tumtum-surface" />
      <div className="h-14 rounded-lg bg-tumtum-surface" />
      <div className="h-14 rounded-lg bg-tumtum-surface" />
    </div>
  )
}
