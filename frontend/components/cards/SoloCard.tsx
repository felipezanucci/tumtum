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
      className={`relative overflow-hidden rounded-2xl bg-gradient-to-b from-tumtum-black via-tumtum-surface to-tumtum-black ${className}`}
      style={{ aspectRatio: '9/16', maxWidth: 360 }}
    >
      <div className="flex h-full flex-col items-center justify-between p-8">
        {/* Logo */}
        <h2
          className="text-xl font-hero uppercase tracking-widest text-tumtum-white"
        >
          TumTum
        </h2>

        {/* Event info */}
        <div className="text-center">
          <p className="text-lg font-semibold text-tumtum-white">{eventName}</p>
          <p className="text-sm text-tumtum-muted">{eventDate}</p>
        </div>

        {/* Peak BPM */}
        <div className="text-center">
          <p className="text-7xl font-bold text-tumtum-lime">{peakBpm}</p>
          <p className="text-xl font-semibold text-tumtum-yellow">BPM</p>
          {matchedLabel && (
            <p className="mt-2 text-sm text-tumtum-white">
              durante &quot;{matchedLabel}&quot;
            </p>
          )}
        </div>

        {/* Stats */}
        <div className="w-full">
          <div className="mb-4 h-px bg-tumtum-border" />
          <div className="flex justify-around">
            <div className="text-center">
              <p className="text-lg font-bold text-tumtum-white">{avgBpm}</p>
              <p className="text-xs text-tumtum-muted">Média</p>
            </div>
            <div className="text-center">
              <p className="text-lg font-bold text-tumtum-white">{maxBpm}</p>
              <p className="text-xs text-tumtum-muted">Máximo</p>
            </div>
          </div>
        </div>

        {/* User */}
        <p className="text-sm text-tumtum-muted">@{userName}</p>
      </div>

      {/* Decorative glow */}
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-tumtum-lime/5 to-transparent" />
    </div>
  )
}
