'use client'

import Link from 'next/link'

/** Moved with the page from `/events/novo` (22/09): the way back is the admin. */
export default function NewAdminEventError() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
      <div className="text-center">
        <p className="text-lg text-tumtum-white">Algo deu errado nessa tela.</p>
        <Link href="/admin/eventos" className="mt-6 inline-block">
          <span className="rounded-lg bg-tumtum-pink px-8 py-3 text-lg font-label text-tumtum-black">
            Voltar aos eventos
          </span>
        </Link>
      </div>
    </main>
  )
}
