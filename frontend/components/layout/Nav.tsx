'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { usePathname } from 'next/navigation'

import { Wordmark } from '@/components/brand'

/**
 * "Ao vivo" — the browser capture screen — is deliberately absent.
 *
 * Capture is the Android app's job now, and the browser cannot be a fallback
 * for it even in principle: a Polar H10 accepts two simultaneous BLE
 * connections, and during an event both are already spoken for by the app and
 * by Polar's own app running in parallel as the reference recording. A third
 * would not connect. Leaving "Ao vivo" in the menu therefore offered a route
 * that could only fail — and offered it under the most confident label in the
 * bar, to someone standing in a dark crowd looking for the capture screen.
 *
 * The route itself still exists and still works in a plain browser; it is
 * simply no longer advertised. `ExperienceActivity` also intercepts it inside
 * the app's WebView and drops back to the native capture screen, which stays
 * as the second net.
 */
const navLinks = [
  { href: '/events', label: 'Eventos' },
  { href: '/import', label: 'Importar' },
  { href: '/cards', label: 'Cards' },
  { href: '/profile', label: 'Perfil' },
]

/**
 * The five links used to sit in a horizontally scrolling strip. On a 360px
 * phone — the one this is tested on — that strip overflowed by 47px with
 * "Perfil" entirely off screen, and its left edge touched the wordmark with
 * no gap at all. A sideways-scrolling row of links is close to invisible as
 * an affordance, so a whole section of the app was simply unreachable.
 *
 * On a phone they now live behind a button in the opposite corner from the
 * wordmark: nothing is crowded, and nothing is hidden. Above `sm` there is
 * room for all five, so they stay in the bar where they are one tap away.
 */
export default function Nav() {
  const pathname = usePathname()
  const [open, setOpen] = useState(false)

  // Following a link should leave the menu behind.
  useEffect(() => {
    setOpen(false)
  }, [pathname])

  useEffect(() => {
    if (!open) return
    function onKey(event: KeyboardEvent) {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [open])

  const isActive = (href: string) => pathname?.startsWith(href)

  return (
    <nav className="sticky top-0 z-40 border-b border-tumtum-border bg-tumtum-black/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        {/*
          Home, from inside the app, is the app — not the front door. This
          wordmark pointed at "/", the public landing page, so tapping it while
          signed in and halfway through a session dropped you onto a page
          asking you to create an account. Nav only ever renders inside the
          authenticated area, so its home is the events list.
        */}
        <Link href="/events" className="flex shrink-0 items-center gap-2">
          <Wordmark className="h-5 w-auto text-tumtum-white" />
        </Link>

        {/* Phones: one button, far from the wordmark. */}
        <button
          type="button"
          onClick={() => setOpen((prev) => !prev)}
          aria-expanded={open}
          aria-controls="nav-menu"
          aria-label={open ? 'Fechar menu' : 'Abrir menu'}
          className="-mr-2 flex h-11 w-11 items-center justify-center rounded-lg text-tumtum-white sm:hidden"
        >
          <svg viewBox="0 0 24 24" className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            {open ? (
              <>
                <line x1="5" y1="5" x2="19" y2="19" />
                <line x1="19" y1="5" x2="5" y2="19" />
              </>
            ) : (
              <>
                <line x1="3" y1="7" x2="21" y2="7" />
                <line x1="3" y1="12" x2="21" y2="12" />
                <line x1="3" y1="17" x2="21" y2="17" />
              </>
            )}
          </svg>
        </button>

        {/* Anything wider has room for all five. */}
        <div className="hidden items-center gap-6 sm:flex">
          {navLinks.map(({ href, label }) => (
            <Link
              key={href}
              href={href}
              className={`whitespace-nowrap text-sm transition-colors ${
                isActive(href)
                  ? 'font-medium text-tumtum-white'
                  : 'text-tumtum-muted hover:text-tumtum-white'
              }`}
            >
              {label}
            </Link>
          ))}
        </div>
      </div>

      {open && (
        <>
          {/* Tapping anywhere else closes it, which is what a dropdown owes you. */}
          <div
            className="fixed inset-0 top-16 z-30 sm:hidden"
            onClick={() => setOpen(false)}
            aria-hidden
          />
          <div
            id="nav-menu"
            className="relative z-40 border-t border-tumtum-border bg-tumtum-black sm:hidden"
          >
            {navLinks.map(({ href, label }) => (
              <Link
                key={href}
                href={href}
                className={`flex items-center border-l-2 px-4 py-4 text-base transition-colors ${
                  isActive(href)
                    ? 'border-tumtum-lime bg-tumtum-surface font-medium text-tumtum-white'
                    : 'border-transparent text-tumtum-muted'
                }`}
              >
                {label}
              </Link>
            ))}
          </div>
        </>
      )}
    </nav>
  )
}
