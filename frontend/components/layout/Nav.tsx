'use client'

import Link from 'next/link'

export default function Nav() {
  return (
    <nav className="sticky top-0 z-40 border-b border-tumtum-border bg-tumtum-dark/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <Link href="/" className="flex items-center gap-2">
          <span
            className="text-xl font-bold uppercase tracking-widest text-tumtum-red"
            style={{ fontFamily: 'Georgia, serif' }}
          >
            Tumtum
          </span>
        </Link>

        <div className="flex items-center gap-6">
          <Link
            href="/events"
            className="text-sm text-tumtum-text-muted transition-colors hover:text-tumtum-text-primary"
          >
            Eventos
          </Link>
          <Link
            href="/profile"
            className="text-sm text-tumtum-text-muted transition-colors hover:text-tumtum-text-primary"
          >
            Perfil
          </Link>
        </div>
      </div>
    </nav>
  )
}
