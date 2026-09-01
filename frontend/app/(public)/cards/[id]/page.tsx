import type { Metadata } from 'next'
import Link from 'next/link'

import { Wordmark } from '@/components/brand'
import { cards, type PublicCardData } from '@/lib/api'

/**
 * A shared card, open to anyone with the link.
 *
 * Every share until now pointed here and landed on nothing: the route did not
 * exist, so a card posted to WhatsApp took the person to a login wall. This is
 * the page the link has always promised.
 *
 * Public by design and narrow by design — it shows exactly what the image
 * shows, because sharing a card publishes one moment, not a person.
 */

async function loadCard(id: string): Promise<PublicCardData | null> {
  try {
    return await cards.getPublic(id)
  } catch {
    return null
  }
}

export async function generateMetadata({
  params,
}: {
  params: { id: string }
}): Promise<Metadata> {
  const card = await loadCard(params.id)
  if (!card) {
    return { title: 'Card não encontrado — TumTum' }
  }

  const moment = card.moment_label ? ` durante "${card.moment_label}"` : ''
  const title = `${card.peak_bpm} bpm${moment}`
  const description = `${card.user_name} em ${card.event_name}. A batida do momento.`
  // Preview slots are landscape everywhere; the 9:16 card is what people post.
  const image = cards.getPreviewImageUrl(card.id)

  return {
    title: `${title} — TumTum`,
    description,
    openGraph: {
      type: 'article',
      title,
      description,
      // The card image itself is the preview: the thing worth looking at is
      // the number, not a generic brand banner.
      images: [{ url: image, width: 1200, height: 630, alt: title }],
    },
    twitter: { card: 'summary_large_image', title, description, images: [image] },
  }
}

export default async function SharedCardPage({ params }: { params: { id: string } }) {
  const card = await loadCard(params.id)

  return (
    <main className="min-h-screen bg-tumtum-black">
      <div className="mx-auto flex min-h-screen max-w-md flex-col items-center px-4 py-10">
        <Link href="/" aria-label="TumTum">
          <Wordmark className="h-5 w-auto text-tumtum-white" />
        </Link>

        {card ? (
          <>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={cards.getImageUrl(card.id)}
              alt={`${card.peak_bpm} bpm de ${card.user_name} em ${card.event_name}`}
              className="mt-8 w-full rounded-2xl border border-tumtum-border"
            />
            <p className="mt-8 text-center text-lg text-tumtum-white">
              Seu coração também tem histórias assim.
            </p>
            <Link href="/signup" className="mt-4 w-full">
              <span className="block rounded-lg bg-tumtum-pink px-8 py-3 text-center text-lg font-label text-tumtum-black transition-colors hover:bg-tumtum-yellow">
                Descobrir a minha
              </span>
            </Link>
          </>
        ) : (
          <div className="mt-16 text-center">
            <p className="text-lg text-tumtum-white">Esse momento não está mais aqui.</p>
            <p className="mt-2 text-sm text-tumtum-muted">
              O link pode ter expirado ou o card foi apagado.
            </p>
            <Link href="/" className="mt-8 inline-block">
              <span className="rounded-lg bg-tumtum-pink px-8 py-3 text-lg font-label text-tumtum-black">
                Conhecer a TumTum
              </span>
            </Link>
          </div>
        )}
      </div>
    </main>
  )
}
