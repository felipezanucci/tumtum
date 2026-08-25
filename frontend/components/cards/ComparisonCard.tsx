'use client'

import { Wordmark } from '@/components/brand'

interface ComparisonCardProps {
  eventName: string
  eventDate: string
  userName: string
  artistName: string
  userPeakBpm: number
  artistPeakBpm: number
  syncPercentage: number
  className?: string
}

export default function ComparisonCard({
  eventName,
  eventDate,
  userName,
  artistName,
  userPeakBpm,
  artistPeakBpm,
  syncPercentage,
  className = '',
}: ComparisonCardProps) {
  return (
    <div
      className={`relative overflow-hidden rounded-2xl bg-gradient-to-b from-tumtum-black via-tumtum-surface to-tumtum-black ${className}`}
      style={{ aspectRatio: '9/16', maxWidth: 360 }}
    >
      <div className="flex h-full flex-col items-center justify-between p-8">
        {/* Logo */}
        <Wordmark className="h-5 w-auto text-tumtum-white" />

        {/* Event */}
        <div className="text-center">
          <p className="text-lg font-semibold text-tumtum-white">{eventName}</p>
          <p className="text-sm text-tumtum-muted">{eventDate}</p>
        </div>

        {/* Sync percentage */}
        <div className="text-center">
          <p className="text-6xl font-bold text-tumtum-white">{syncPercentage}%</p>
          <p className="mt-1 text-sm text-tumtum-muted">em sincronia</p>
        </div>

        {/* Comparison */}
        <div className="flex w-full items-center justify-around">
          <div className="text-center">
            <p className="text-4xl font-bold text-tumtum-lime">{userPeakBpm}</p>
            <p className="mt-1 text-xs text-tumtum-muted">Seu pico</p>
          </div>
          <span className="text-lg font-medium text-tumtum-muted">vs</span>
          <div className="text-center">
            <p className="text-4xl font-bold text-tumtum-white">{artistPeakBpm}</p>
            <p className="mt-1 text-xs text-tumtum-muted">{artistName}</p>
          </div>
        </div>

        {/* User */}
        <p className="text-sm text-tumtum-muted">@{userName}</p>
      </div>

      <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-tumtum-white/5 to-transparent" />
    </div>
  )
}
