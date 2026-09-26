'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { ApiError, cards, type CardData } from '@/lib/api'
import { Button, Card, Loading, Badge, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'
import { nativeShare, canNativeShare, getShareUrl, copyToClipboard, downloadImage } from '@/lib/utils/share'

const platformLabels: Record<string, string> = {
  instagram: 'Instagram',
  tiktok: 'TikTok',
  x: 'X (Twitter)',
  whatsapp: 'WhatsApp',
  link: 'Copiar link',
}

export default function CardsPage() {
  const [cardList, setCardList] = useState<CardData[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<Error | null>(null)
  const [shareMenuId, setShareMenuId] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)
  /** Building the image file to hand over takes a moment on a slow connection. */
  const [sharingId, setSharingId] = useState<string | null>(null)
  const [unpublishingId, setUnpublishingId] = useState<string | null>(null)
  /** One line per card for what just happened to it — or what failed. */
  const [cardNotice, setCardNotice] = useState<{ id: string; text: string; error?: boolean } | null>(null)

  function markPublished(cardId: string, publishedAt: string | null) {
    setCardList((prev) => prev.map((c) => (c.id === cardId ? { ...c, published_at: publishedAt } : c)))
  }

  /**
   * "Despublicar" (26/09): the public page and the image answer 404 from
   * now on. Posts already made in other apps stay there — the line says so.
   */
  async function handleUnpublish(card: CardData) {
    setUnpublishingId(card.id)
    setCardNotice(null)
    try {
      await cards.unpublish(card.id)
      markPublished(card.id, null)
      setCardNotice({
        id: card.id,
        text: 'Despublicado. O link não abre mais. O que já foi postado em outras redes fica lá.',
      })
    } catch (error) {
      setCardNotice({
        id: card.id,
        text: error instanceof Error ? error.message : 'Não deu pra despublicar.',
        error: true,
      })
    } finally {
      setUnpublishingId(null)
    }
  }

  useEffect(() => {
    loadCards()
  }, [])

  async function loadCards() {
    setLoading(true)
    setLoadError(null)
    try {
      const data = await cards.list()
      setCardList(data)
    } catch (error) {
      // Without this the list stays empty and the page reads as "you have no
      // cards" — a claim about the person's gallery, made when the request
      // was refused. The two have to look different.
      setLoadError(error instanceof Error ? error : new Error('Falhou'))
    } finally {
      setLoading(false)
    }
  }

  /**
   * A card is portrait and fills a phone, so a list of platforms rendered under
   * it landed below the fold: the button appeared to do nothing at all.
   *
   * On a phone there was nothing worth showing anyway. Every entry in that list
   * opened the same system sheet, which already lists Instagram, WhatsApp and
   * TikTok — the real ones, not links that approximate them. So the button now
   * opens that sheet directly, and the list stays for browsers that have no
   * sheet of their own, where the per-platform links genuinely differ.
   */
  async function handleSharePressed(card: CardData) {
    if (!canNativeShare()) {
      setShareMenuId(shareMenuId === card.id ? null : card.id)
      return
    }
    setSharingId(card.id)
    try {
      await handleShare(card, 'native')
    } finally {
      setSharingId(null)
    }
  }

  async function handleShare(card: CardData, platform: string) {
    const imageUrl = card.image_url ? cards.getImageUrl(card.id) : ''
    // The person is the subject, and the anatomical heart emoji went: the
    // manual keeps BPM as a neutral unit and steers the voice away from
    // anything that reads clinical.
    const meta = card.metadata as { event_name?: string; peak_bpm?: number; matched_label?: string }
    const where = meta?.matched_label
      ? `durante "${meta.matched_label}"`
      : meta?.event_name
        ? `em ${meta.event_name}`
        : 'no evento'
    const shareData = {
      title: `${meta?.peak_bpm ?? '?'} bpm — TumTum`,
      text: `Meu coração foi a ${meta?.peak_bpm ?? '?'} ${where}.`,
      url: `${window.location.origin}/cards/${card.id}`,
      imageUrl,
    }

    // Sharing is what publishes (26/09): the server opens the public page
    // and the image only after this call. For a link it starts together with
    // the share — the clipboard and a new window need the tap's own moment,
    // which an awaited request would spend — and if it fails, the card says
    // its link does not open yet. The system sheet carries the image itself,
    // which does not exist publicly until this lands, so that one waits.
    setCardNotice(null)
    const publishing = cards
      .trackShare(card.id, platform)
      .then(() => {
        if (!card.published_at) markPublished(card.id, new Date().toISOString())
      })
      .catch((error: unknown) => {
        setCardNotice({
          id: card.id,
          text: `O link ainda não abre: ${error instanceof Error ? error.message : 'não deu pra publicar o card.'}`,
          error: true,
        })
      })

    if (platform === 'link') {
      const success = await copyToClipboard(shareData.url)
      if (success) {
        setCopied(true)
        setTimeout(() => setCopied(false), 2000)
      }
    } else if (platform === 'native' || canNativeShare()) {
      await publishing
      await nativeShare(shareData)
    } else {
      const url = getShareUrl(platform as any, shareData)
      window.open(url, '_blank', 'noopener,noreferrer')
    }

    await publishing
    setShareMenuId(null)
  }

  async function handleDownload(card: CardData) {
    if (card.image_url) {
      downloadImage(cards.getImageUrl(card.id), `tumtum-${card.id.slice(0, 8)}.png`)
    }
  }

  async function handleDelete(cardId: string) {
    await cards.delete(cardId)
    setCardList((prev) => prev.filter((c) => c.id !== cardId))
  }

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-4xl px-4 py-8">
          <div className="mb-6 flex items-center justify-between">
            <h1 className="text-3xl font-bold text-tumtum-white">Seus Cards</h1>
          </div>

          {loadError instanceof ApiError && loadError.status === 401 ? (
            <SignInRequired what="seus cards" />
          ) : loadError ? (
            <p className="mt-6 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
              {loadError.message}
            </p>
          ) : loading ? (
            <Loading size="lg" className="py-20" />
          ) : cardList.length === 0 ? (
            <div className="py-20 text-center">
              <div className="mb-4 text-5xl">🃏</div>
              <p className="text-lg text-tumtum-muted">Você ainda não criou nenhum card</p>
              <p className="mt-1 text-sm text-tumtum-muted">
                Vá para a aba Experiência e gere um card de um evento.
              </p>
            </div>
          ) : (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {cardList.map((card) => {
                const meta = card.metadata as any
                return (
                  <Card key={card.id} className="relative overflow-hidden">
                    {/* Card preview. The image is served only once the card is
                        public (26/09), so a private card shows its words. */}
                    {card.image_url && !card.published_at && (
                      <div className="mb-4 flex aspect-[9/16] max-h-64 w-full flex-col items-center justify-center rounded-lg border border-tumtum-border bg-tumtum-black p-4 text-center">
                        <p className="text-5xl font-hero text-tumtum-pink">{meta?.peak_bpm ?? '—'}</p>
                        <p className="mt-1 text-xs uppercase tracking-wider text-tumtum-muted">bpm</p>
                        {meta?.matched_label && (
                          <p className="mt-3 text-sm text-tumtum-white">{meta.matched_label}</p>
                        )}
                        <p className="mt-4 text-xs text-tumtum-muted">
                          A imagem aparece aqui quando o card for compartilhado.
                        </p>
                      </div>
                    )}
                    {card.image_url && card.published_at && (
                      <div className="mb-4 overflow-hidden rounded-lg">
                        <img
                          src={cards.getImageUrl(card.id)}
                          alt="Share card"
                          className="w-full"
                        />
                      </div>
                    )}

                    <div className="flex items-center gap-2">
                      <Badge variant={card.card_type === 'solo' ? 'default' : 'accent'}>
                        {card.card_type === 'solo' ? 'Solo' : 'Comparação'}
                      </Badge>
                      <Badge variant={card.status === 'ready' ? 'success' : 'warning'}>
                        {card.status === 'ready' ? 'Pronto' : card.status}
                      </Badge>
                      <Badge variant={card.published_at ? 'accent' : 'default'}>
                        {card.published_at ? 'Público' : 'Só você vê'}
                      </Badge>
                    </div>

                    {meta?.event_name && (
                      <p className="mt-2 font-medium text-tumtum-white">{meta.event_name}</p>
                    )}
                    {meta?.peak_bpm && (
                      <p className="text-sm text-tumtum-muted">Pico: {meta.peak_bpm} bpm</p>
                    )}

                    <p className="mt-1 text-xs text-tumtum-muted">
                      {new Date(card.created_at).toLocaleDateString('pt-BR')}
                    </p>

                    {/* Actions */}
                    <div className="mt-4 flex gap-2">
                      <Button
                        size="sm"
                        onClick={() => handleSharePressed(card)}
                        loading={sharingId === card.id}
                        disabled={card.status !== 'ready'}
                      >
                        Compartilhar
                      </Button>
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => handleDownload(card)}
                        disabled={!card.image_url || !card.published_at}
                      >
                        Baixar
                      </Button>
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => handleDelete(card.id)}
                      >
                        Excluir
                      </Button>
                    </div>

                    {card.published_at ? (
                      <div className="mt-3 flex flex-wrap items-center gap-3 text-sm">
                        <Link href={`/cards/${card.id}`} target="_blank" className="text-tumtum-pink underline underline-offset-2">
                          Ver página pública
                        </Link>
                        <Button
                          size="sm"
                          variant="secondary"
                          loading={unpublishingId === card.id}
                          onClick={() => void handleUnpublish(card)}
                        >
                          Despublicar
                        </Button>
                      </div>
                    ) : (
                      <p className="mt-3 text-xs text-tumtum-muted">
                        Compartilhar abre uma página pública com o número, o momento e o evento.
                        Dá pra despublicar depois.
                      </p>
                    )}

                    {cardNotice?.id === card.id && (
                      <p
                        role={cardNotice.error ? 'alert' : 'status'}
                        className={`mt-2 text-sm ${cardNotice.error ? 'text-red-400' : 'text-tumtum-white'}`}
                      >
                        {cardNotice.text}
                      </p>
                    )}

                    {/* Share menu */}
                    {shareMenuId === card.id && (
                      <div
                        ref={(node) => node?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })}
                        className="mt-3 rounded-lg border border-tumtum-pink/50 bg-tumtum-black p-3"
                      >
                        <p className="mb-2 text-xs font-medium uppercase tracking-wider text-tumtum-muted">
                          Compartilhar em
                        </p>
                        <div className="flex flex-wrap gap-2">
                          {(['instagram', 'x', 'whatsapp', 'tiktok', 'link'] as const).map((platform) => (
                            <button
                              key={platform}
                              onClick={() => handleShare(card, platform)}
                              className="rounded-lg bg-tumtum-surface px-3 py-1.5 text-xs text-tumtum-white transition-colors hover:bg-tumtum-border"
                            >
                              {platform === 'link' && copied ? 'Copiado!' : platformLabels[platform]}
                            </button>
                          ))}
                        </div>
                      </div>
                    )}
                  </Card>
                )
              })}
            </div>
          )}
        </div>
      </main>
    </>
  )
}
