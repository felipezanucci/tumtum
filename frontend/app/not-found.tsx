import Link from 'next/link'

import { Wordmark } from '@/components/brand'

/**
 * The 404 nobody had written.
 *
 * Without this, an unknown path fell through to Next's default page — which
 * is unbranded, in English, and offers nowhere to go. A single-segment path
 * is caught earlier by the public profile route, which now says the same
 * thing in the same words: "nada por aqui", rather than asserting that a
 * particular user does not exist.
 */
export default function NotFound() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
      <div className="max-w-sm text-center">
        <Wordmark className="mx-auto h-6 w-auto text-tumtum-white" />
        <p className="mt-8 text-lg text-tumtum-white">Nada por aqui</p>
        <p className="mt-1 text-sm text-tumtum-muted">
          Este endereço não existe na TumTum. Pode ser um link antigo ou um erro
          de digitação.
        </p>
        <Link
          href="/"
          className="mt-6 inline-block text-sm text-tumtum-pink hover:underline"
        >
          Ir para a página inicial
        </Link>
      </div>
    </main>
  )
}
