'use client'

import { useEffect, useState, type ReactNode } from 'react'
import { cards, type CardImageFormat } from '@/lib/api'

interface OwnerCardImageProps {
  cardId: string
  format?: CardImageFormat
  alt: string
  className?: string
  /**
   * What stands in when the image could not be fetched. It must read as a
   * failure, not as "this card has no image" — an empty slot is a claim.
   */
  fallback?: ReactNode
}

const aspect: Record<CardImageFormat, string> = {
  story: 'aspect-[9/16]',
  og: 'aspect-[1200/630]',
}

/**
 * The owner's own card, published or not (26/09). The public `/image` answers
 * only once a card is shared, and an `<img>` cannot carry the session, so the
 * PNG is fetched through it and shown from an object URL — released when the
 * image leaves the screen or the card changes.
 */
export default function OwnerCardImage({
  cardId,
  format = 'story',
  alt,
  className = '',
  fallback,
}: OwnerCardImageProps) {
  const [url, setUrl] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let active = true
    let held: string | null = null
    setUrl(null)
    setFailed(false)
    cards
      .previewBlobUrl(cardId, format)
      .then((objectUrl) => {
        // Unmounted or switched card while the bytes were on their way: the
        // URL was never shown, so it is released at once.
        if (!active) {
          cards.revokePreviewUrl(objectUrl)
          return
        }
        held = objectUrl
        setUrl(objectUrl)
      })
      .catch(() => {
        if (active) setFailed(true)
      })
    return () => {
      active = false
      if (held) cards.revokePreviewUrl(held)
    }
  }, [cardId, format])

  if (failed) {
    return (
      <>
        {fallback ?? (
          <div
            role="alert"
            className={`flex ${aspect[format]} w-full items-center justify-center rounded-lg border border-tumtum-border bg-tumtum-surface p-3 text-center text-xs text-tumtum-muted ${className}`}
          >
            Não deu pra carregar a imagem agora.
          </div>
        )}
      </>
    )
  }

  if (!url) {
    return (
      <div
        role="status"
        aria-label="Carregando o card"
        className={`${aspect[format]} w-full animate-pulse rounded-lg bg-tumtum-surface ${className}`}
      />
    )
  }

  // A blob: URL made here, already in memory — next/image would add nothing.
  // eslint-disable-next-line @next/next/no-img-element
  return <img src={url} alt={alt} className={`w-full ${className}`} />
}
