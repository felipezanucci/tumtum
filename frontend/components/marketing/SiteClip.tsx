'use client'

import { useEffect, useRef } from 'react'

/**
 * A decorative event clip that actually plays on a phone.
 *
 * `autoplay muted playsinline` alone is not enough here, and the reason is
 * where these clips sit: every one of them is far below the fold. Mobile
 * browsers refuse or defer autoplay for a video that is off screen, the
 * attempt happens once at load, and nothing retries it when the person
 * finally scrolls down — so the card sits on its poster frame forever.
 *
 * So playback is driven by visibility instead: play on entering the viewport,
 * pause on leaving. That fixes the real bug and costs nothing — a clip nobody
 * can see was only ever burning battery and cellular data.
 *
 * When autoplay is genuinely blocked — iOS Low Power Mode and Safari's
 * "Never Auto-Play" both do this, and no page can override either — the
 * rejection is swallowed and the poster frame stays. That is the honest
 * outcome: a still frame of the real moment, never a broken box.
 */
export function SiteClip({ src, className = '' }: { src: string; className?: string }) {
  const ref = useRef<HTMLVideoElement>(null)

  useEffect(() => {
    const video = ref.current
    if (!video) return

    // Safari needs the property set, not just the attribute, before play().
    video.muted = true

    const play = () => {
      const attempt = video.play()
      // Older browsers return undefined rather than a promise.
      if (attempt && typeof attempt.catch === 'function') attempt.catch(() => {})
    }

    if (typeof IntersectionObserver === 'undefined') {
      play()
      return
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) play()
        else if (!video.paused) video.pause()
      },
      // Margin on both axes: vertical for the scroll, horizontal because the
      // cards are a swipe carousel — the next card should already be running
      // when it slides in, not start from a still.
      { rootMargin: '200px', threshold: 0.01 },
    )
    observer.observe(video)
    return () => observer.disconnect()
  }, [])

  return (
    <video
      ref={ref}
      className={`h-full w-full object-cover ${className}`}
      poster={`/site/${src}-poster.jpg`}
      // preload="metadata" keeps the page light: the clips only fetch their
      // frames once the observer decides they are about to be seen.
      preload="metadata"
      autoPlay
      muted
      loop
      playsInline
      aria-hidden="true"
    >
      {/* MP4 first: H.264 is the universal one and the smaller file here, and
          it is what iOS Safari needs. WebM is the fallback for builds that
          ship without the proprietary codec — the browser downloads only the
          first source it can actually play, so carrying both costs nothing at
          runtime. */}
      <source src={`/site/${src}.mp4`} type="video/mp4" />
      <source src={`/site/${src}.webm`} type="video/webm" />
    </video>
  )
}
