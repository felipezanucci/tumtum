'use client'

import { useEffect, useRef, useState } from 'react'

/**
 * Fades a section in the first time it is scrolled to.
 *
 * Starts visible and only hides itself once the observer is known to work.
 * The other order — hidden in the markup, revealed by script — leaves the
 * whole page blank for anyone whose JavaScript failed, and a marketing page
 * that shows nothing is worse than one that simply does not animate.
 */
export function Reveal({ children }: { children: React.ReactNode }) {
  const ref = useRef<HTMLDivElement>(null)
  const [armed, setArmed] = useState(false)
  const [shown, setShown] = useState(false)

  useEffect(() => {
    const node = ref.current
    if (!node) return

    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (reduced || typeof IntersectionObserver === 'undefined') {
      setShown(true)
      return
    }

    setArmed(true)
    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (!entry.isIntersecting) continue
          setShown(true)
          observer.unobserve(entry.target)
        }
      },
      { threshold: 0.15 },
    )
    observer.observe(node)
    return () => observer.disconnect()
  }, [])

  const hidden = armed && !shown

  return (
    <div
      ref={ref}
      className={`transition-[opacity,transform] duration-[600ms] ease-out ${
        hidden ? 'translate-y-[22px] opacity-0' : 'translate-y-0 opacity-100'
      }`}
    >
      {children}
    </div>
  )
}
