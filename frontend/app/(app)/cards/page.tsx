'use client'

import { useEffect, useState } from 'react'
import { cards, type CardData } from '@/lib/api'
import { Button, Card, Loading, Badge } from '@/components/ui'
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
  const [shareMenuId, setShareMenuId] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  useEffect(() => {
    loadCards()
  }, [])

  async function loadCards() {
    setLoading(true)
    try {
      const data = await cards.list()
      setCardList(data)
    } finally {
      setLoading(false)
    }
  }

  async function handleShare(card: CardData, platform: string) {
    const imageUrl = card.image_url ? cards.getImageUrl(card.id) : ''
    const shareData = {
      title: `Minha experiência no ${(card.metadata as any)?.event_name || 'evento'}`,
      text: `Meu coração chegou a ${(card.metadata as any)?.peak_bpm || '?'} bpm! 🫀`,
      url: `${window.location.origin}/cards/${card.id}`,
      imageUrl,
    }

    if (platform === 'link') {
      const success = await copyToClipboard(shareData.url)
      if (success) {
        setCopied(true)
        setTimeout(() => setCopied(false), 2000)
      }
    } else if (canNativeShare()) {
      await nativeShare(shareData)
    } else {
      const url = getShareUrl(platform as any, shareData)
      window.open(url, '_blank', 'noopener,noreferrer')
    }

    // Track share
    await cards.trackShare(card.id, platform).catch(() => {})
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
      <main className="min-h-screen bg-tumtum-dark">
        <div className="mx-auto max-w-4xl px-4 py-8">
          <div className="mb-6 flex items-center justify-between">
            <h1 className="text-3xl font-bold text-tumtum-text-primary">Seus Cards</h1>
          </div>

          {loading ? (
            <Loading size="lg" className="py-20" />
          ) : cardList.length === 0 ? (
            <div className="py-20 text-center">
              <div className="mb-4 text-5xl">🃏</div>
              <p className="text-lg text-tumtum-text-muted">Você ainda não criou nenhum card</p>
              <p className="mt-1 text-sm text-tumtum-text-muted">
                Vá para a aba Experiência e gere um card de um evento.
              </p>
            </div>
          ) : (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {cardList.map((card) => {
                const meta = card.metadata as any
                return (
                  <Card key={card.id} className="relative overflow-hidden">
                    {/* Card preview */}
                    {card.image_url && (
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
                    </div>

                    {meta?.event_name && (
                      <p className="mt-2 font-medium text-tumtum-text-primary">{meta.event_name}</p>
                    )}
                    {meta?.peak_bpm && (
                      <p className="text-sm text-tumtum-text-muted">Pico: {meta.peak_bpm} bpm</p>
                    )}

                    <p className="mt-1 text-xs text-tumtum-text-muted">
                      {new Date(card.created_at).toLocaleDateString('pt-BR')}
                    </p>

                    {/* Actions */}
                    <div className="mt-4 flex gap-2">
                      <Button
                        size="sm"
                        onClick={() => setShareMenuId(shareMenuId === card.id ? null : card.id)}
                        disabled={card.status !== 'ready'}
                      >
                        Compartilhar
                      </Button>
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => handleDownload(card)}
                        disabled={!card.image_url}
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

                    {/* Share menu */}
                    {shareMenuId === card.id && (
                      <div className="mt-3 rounded-lg border border-tumtum-border bg-tumtum-dark p-3">
                        <p className="mb-2 text-xs font-medium uppercase tracking-wider text-tumtum-text-muted">
                          Compartilhar em
                        </p>
                        <div className="flex flex-wrap gap-2">
                          {(['instagram', 'x', 'whatsapp', 'tiktok', 'link'] as const).map((platform) => (
                            <button
                              key={platform}
                              onClick={() => handleShare(card, platform)}
                              className="rounded-lg bg-tumtum-surface px-3 py-1.5 text-xs text-tumtum-text-primary transition-colors hover:bg-tumtum-border"
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
