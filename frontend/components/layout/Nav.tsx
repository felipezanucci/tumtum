'use client'

import Link from 'next/link'
import { Wordmark } from '@/components/brand'
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
          <Wordmark className="h-5 w-auto text-tumtum-white" />
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
