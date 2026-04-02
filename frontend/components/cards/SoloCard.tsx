'use client'

import type { Peak } from '@/lib/api'

interface SoloCardProps {
  eventName: string
  eventDate: string
  peakBpm: number
  avgBpm: number
  maxBpm: number
  matchedLabel?: string | null
  userName: string
  className?: string
}

export default function SoloCard({
  eventName,
  eventDate,
  peakBpm,
  avgBpm,
  maxBpm,
  matchedLabel,
  userName,
  className = '',
}: SoloCardProps) {
  return (
    <div
      className={`relative overflow-hidden rounded-2xl bg-gradient-to-b from-tumtum-dark via-tumtum-surface to-tumtum-dark ${className}`}
      style={{ aspectRatio: '9/16', maxWidth: 360 }}
    >
      <div className="flex h-full flex-col items-center justify-between p-8">
        {/* Logo */}
        <h2
          className="text-xl font-bold uppercase tracking-widest text-tumtum-red"
          style={{ fontFamily: 'Georgia, serif' }}
        >
          Tumtum
        </h2>

        {/* Event info */}
        <div className="text-center">
          <p className="text-lg font-semibold text-tumtum-text-primary">{eventName}</p>
          <p className="text-sm text-tumtum-text-muted">{eventDate}</p>
        </div>

        {/* Peak BPM */}
        <div className="text-center">
          <p className="text-7xl font-bold text-tumtum-red">{peakBpm}</p>
          <p className="text-xl font-semibold text-tumtum-red-secondary">BPM</p>
          {matchedLabel && (
            <p className="mt-2 text-sm text-tumtum-text-primary">
              durante &quot;{matchedLabel}&quot;
            </p>
          )}
        </div>

        {/* Stats */}
        <div className="w-full">
          <div className="mb-4 h-px bg-tumtum-border" />
          <div className="flex justify-around">
            <div className="text-center">
              <p className="text-lg font-bold text-tumtum-text-primary">{avgBpm}</p>
              <p className="text-xs text-tumtum-text-muted">Média</p>
            </div>
            <div className="text-center">
              <p className="text-lg font-bold text-tumtum-text-primary">{maxBpm}</p>
              <p className="text-xs text-tumtum-text-muted">Máximo</p>
            </div>
          </div>
        </div>

        {/* User */}
        <p className="text-sm text-tumtum-text-muted">@{userName}</p>
      </div>

      {/* Decorative glow */}
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-tumtum-red/5 to-transparent" />
    </div>
  )
}
