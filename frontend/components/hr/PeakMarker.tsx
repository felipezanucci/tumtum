'use client'

import type { Peak } from '@/lib/api'

interface PeakMarkerProps {
  peak: Peak
  rank?: number
  className?: string
}

export default function PeakMarker({ peak, rank, className = '' }: PeakMarkerProps) {
  const time = new Date(peak.timestamp).toLocaleTimeString('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
  })

  return (
    <div
      className={`flex items-center gap-3 rounded-lg border border-tumtum-border bg-tumtum-surface p-3 ${className}`}
    >
      {rank && (
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-tumtum-pink text-sm font-bold text-tumtum-black">
          {rank}
        </div>
      )}
      <div className="flex-1 min-w-0">
        <div className="flex items-baseline gap-2">
          <span className="text-lg font-bold text-tumtum-pink">{peak.bpm} bpm</span>
          <span className="text-sm text-tumtum-muted">{time}</span>
        </div>
        {peak.matched_label && (
          <p className="truncate text-sm text-tumtum-white">{peak.matched_label}</p>
        )}
        <p className="text-xs text-tumtum-muted">
          {peak.duration_seconds}s de duração
        </p>
      </div>
    </div>
  )
}
