'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

const navLinks = [
  { href: '/events', label: 'Eventos' },
  { href: '/live', label: 'Ao vivo' },
  { href: '/import', label: 'Importar' },
  { href: '/cards', label: 'Cards' },
  { href: '/profile', label: 'Perfil' },
]

export default function Nav() {
  const pathname = usePathname()

  return (
    <nav className="sticky top-0 z-40 border-b border-tumtum-border bg-tumtum-black/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <Link href="/" className="flex shrink-0 items-center gap-2">
          {/* PROVISIONAL. The wordmark is Chosmos and ships as an official
              vector; nothing may redraw it, so this stands in until the asset
              arrives. Colour is correct either way: white or black only —
              the Acid Lime and Toxic Yellow wordmarks are forbidden. */}
          <span className="text-xl font-hero uppercase tracking-widest text-tumtum-white">
            TUMTUM
          </span>
        </Link>

        <div className="flex items-center gap-3 overflow-x-auto sm:gap-6">
          {navLinks.map(({ href, label }) => (
            <Link
              key={href}
              href={href}
              className={`shrink-0 whitespace-nowrap text-sm transition-colors ${
                pathname?.startsWith(href)
                  ? 'font-medium text-tumtum-white'
                  : 'text-tumtum-muted hover:text-tumtum-white'
              }`}
            >
              {label}
            </Link>
          ))}
        </div>
      </div>
    </nav>
  )
}
