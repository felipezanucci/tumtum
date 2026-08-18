'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'

import { Logo } from '@/components/brand'

const navLinks = [
  { href: '/events', label: 'Eventos' },
  { href: '/cards', label: 'Cards' },
  { href: '/profile', label: 'Perfil' },
]

export default function Nav() {
  const pathname = usePathname()

  return (
    <nav className="sticky top-0 z-40 border-b border-tumtum-border bg-tumtum-dark/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <Link href="/" className="flex items-center" aria-label="TumTum — início">
          {/* Nav inverts the primary lockup so the logo never outshouts the content. */}
          <Logo
            variant="horizontal"
            fill="#F4F2F7"
            risco="#FF2E3C"
            knockout="#0A0A0F"
            className="h-6 w-auto"
          />
        </Link>

        <div className="flex items-center gap-6">
          {navLinks.map(({ href, label }) => (
            <Link
              key={href}
              href={href}
              className={`text-sm transition-colors ${
                pathname?.startsWith(href)
                  ? 'font-medium text-tumtum-text-primary'
                  : 'text-tumtum-text-muted hover:text-tumtum-text-primary'
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
