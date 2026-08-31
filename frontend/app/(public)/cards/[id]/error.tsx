'use client'

import Link from 'next/link'

export default function SharedCardError() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
      <div className="text-center">
        <p className="text-lg text-tumtum-white">Não foi possível abrir esse momento.</p>
        <Link href="/" className="mt-6 inline-block">
          <span className="rounded-lg bg-tumtum-pink px-8 py-3 text-lg font-label text-tumtum-black">
            Conhecer a TumTum
          </span>
        </Link>
      </div>
    </main>
  )
}
