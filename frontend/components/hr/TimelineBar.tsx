'use client'

import type { TimelineEntry } from '@/lib/api'
import { Music, Target, Pause, Flame, Star } from 'lucide-react'
import type { ReactNode } from 'react'

interface TimelineBarProps {
  entries: TimelineEntry[]
  className?: string
}

const entryTypeIcons: Record<string, ReactNode> = {
  song_start: <Music className="w-4 h-4 text-tumtum-text-primary" />,
  goal: <Target className="w-4 h-4 text-tumtum-text-primary" />,
  halftime: <Pause className="w-4 h-4 text-tumtum-text-primary" />,
  encore: <Flame className="w-4 h-4 text-tumtum-text-primary" />,
  highlight: <Star className="w-4 h-4 text-tumtum-text-primary" />,
}

export default function TimelineBar({ entries, className = '' }: TimelineBarProps) {
  if (entries.length === 0) return null

  return (
    <div className={`space-y-1 ${className}`}>
      <h3 className="mb-3 text-sm font-medium uppercase tracking-wider text-tumtum-text-muted">
        Timeline
      </h3>
      <div className="relative">
        {/* Vertical line */}
        <div className="absolute left-4 top-0 bottom-0 w-px bg-tumtum-border" />

        <div className="space-y-3">
          {entries.map((entry) => {
            const time = new Date(entry.timestamp).toLocaleTimeString('pt-BR', {
              hour: '2-digit',
              minute: '2-digit',
            })
            const icon = entryTypeIcons[entry.entry_type] || <span className="text-tumtum-text-muted">&#8226;</span>

            return (
              <div key={entry.id} className="relative flex items-start gap-3 pl-1">
                <div className="z-10 flex h-8 w-8 shrink-0 items-center justify-center rounded-full border border-tumtum-border bg-tumtum-dark text-sm">
                  {icon}
                </div>
                <div className="flex-1 pt-1">
                  <p className="text-sm font-medium text-tumtum-text-primary">
                    {entry.label}
                  </p>
                  <p className="text-xs text-tumtum-text-muted">{time}</p>
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
