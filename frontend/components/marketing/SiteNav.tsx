'use client'

import Link from 'next/link'
import { useState } from 'react'

import { Wordmark } from '@/components/brand/Wordmark'
import type { SiteCopy } from '@/lib/site-copy'

/**
 * The site's nav, in its two shapes.
 *
 * Above `md` it is the desktop handoff's row: wordmark, section links, sign
 * in, the pill, and the PT/EN switcher inline. Below it the mobile handoff
 * replaces all of that with a text MENU button and a panel that opens under
 * the bar — the design says text labels rather than an invented icon, which
 * is also why there is no hamburger here.
 *
 * This is the only client component on the page: a panel that opens is state,
 * and nothing else on the site needs any. Every link closes the panel,
 * including the language ones, so the menu can never stay open over a page
 * the person already navigated to.
 */
export function SiteNav({ copy }: { copy: SiteCopy }) {
  const [open, setOpen] = useState(false)
  const close = () => setOpen(false)

  const sections = [
    { href: '#como', label: copy.nav.how },
    { href: '#cards', label: copy.nav.cards },
    { href: '#feed', label: copy.nav.feed },
    { href: '#galeria', label: copy.nav.gallery },
  ]

  return (
    <header className="sticky top-0 z-20 border-b border-[#E6E6E6] bg-tumtum-white">
      <nav className="mx-auto flex max-w-[1440px] items-center justify-between gap-3 px-6 py-[18px] md:gap-6 md:px-16 md:py-[22px]">
        <Link href={copy.lang === 'pt' ? '/' : '/en'} aria-label="TumTum" onClick={close}>
          <Wordmark className="h-4 w-24 text-tumtum-black md:h-5 md:w-[132px]" />
        </Link>

        <div className="hidden items-center gap-7 text-sm font-headline lg:flex">
          {sections.map((s) => (
            <a key={s.href} href={s.href} className="transition-colors hover:text-tumtum-pink motion-reduce:transition-none">
              {s.label}
            </a>
          ))}
          <Link href="/login" className="text-[#8A8A8A] transition-colors hover:text-tumtum-black motion-reduce:transition-none">
            {copy.nav.signIn}
          </Link>
        </div>

        <div className="flex items-center gap-3 md:gap-5">
          <a
            href="#lista"
            onClick={close}
            className="whitespace-nowrap rounded-full bg-tumtum-pink px-4 py-2 text-[13px] font-headline text-tumtum-black transition-colors hover:bg-tumtum-yellow motion-reduce:transition-none md:px-5 md:py-2.5 md:text-sm"
          >
            {copy.nav.cta}
          </a>

          {/* Desktop keeps the switcher inline; mobile moves it into the panel. */}
          <span className="hidden items-center gap-1.5 text-xs lg:flex">
            <span className="font-hero text-tumtum-black underline underline-offset-[3px]">
              {copy.lang.toUpperCase()}
            </span>
            <span className="text-[#E6E6E6]">/</span>
            <Link
              href={copy.otherHref}
              className="font-label text-[#B4B4B4] transition-colors hover:text-tumtum-black motion-reduce:transition-none"
            >
              {copy.lang === 'pt' ? 'EN' : 'PT'}
            </Link>
          </span>

          <button
            type="button"
            onClick={() => setOpen((v) => !v)}
            aria-expanded={open}
            aria-controls="site-menu"
            className="text-xs font-hero tracking-[0.1em] text-tumtum-black lg:hidden"
          >
            {copy.nav.menu}
          </button>
        </div>
      </nav>

      {open && (
        <div id="site-menu" className="border-t border-[#1E1E1E] bg-tumtum-black px-6 py-4 lg:hidden">
          <ul className="flex flex-col">
            {sections.map((s) => (
              <li key={s.href}>
                <a href={s.href} onClick={close} className="block py-2 text-[13px] font-headline text-tumtum-white">
                  {s.label}
                </a>
              </li>
            ))}
            <li>
              <Link href="/login" onClick={close} className="block py-2 text-[13px] font-headline text-[#8A8A8A]">
                {copy.nav.signIn}
              </Link>
            </li>
          </ul>
          <div className="mt-3 flex gap-2">
            <span className="rounded bg-tumtum-pink px-2 py-1 text-[10px] font-hero text-tumtum-black">
              {copy.lang.toUpperCase()}
            </span>
            <Link
              href={copy.otherHref}
              onClick={close}
              className="rounded border border-[#2E2E2E] px-2 py-1 text-[10px] font-hero text-[#8A8A8A]"
            >
              {copy.lang === 'pt' ? 'EN' : 'PT'}
            </Link>
          </div>
        </div>
      )}
    </header>
  )
}
