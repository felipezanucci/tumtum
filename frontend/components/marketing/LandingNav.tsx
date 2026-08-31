'use client'

import { useEffect, useState } from 'react'

import { Wordmark } from '@/components/brand'

/**
 * The public bar: the wordmark, and one thing to do.
 *
 * Transparent over the hero video, solid once the page scrolls, so the
 * wordmark stays legible against whatever frame is playing behind it.
 */
export function LandingNav() {
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 80)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <nav
      className={`fixed inset-x-0 top-0 z-50 transition-colors duration-300 ${
        scrolled
          ? 'border-b border-tumtum-border bg-black/90 backdrop-blur-[10px]'
          : 'bg-gradient-to-b from-black/70 to-transparent'
      }`}
    >
      <div className="mx-auto flex max-w-[1120px] items-center justify-between px-6 py-[18px]">
        <Wordmark className="w-[132px] text-tumtum-white" />
        <a
          href="#lista"
          className="rounded-full bg-tumtum-pink px-5 py-2.5 text-sm font-headline text-tumtum-black transition-transform hover:scale-105 motion-reduce:transition-none motion-reduce:hover:scale-100"
        >
          Entrar na lista
        </a>
      </div>
    </nav>
  )
}
