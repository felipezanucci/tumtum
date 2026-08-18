'use client'

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

        {/* Event */}
        <div className="text-center">
          <p className="text-lg font-semibold text-tumtum-text-primary">{eventName}</p>
          <p className="text-sm text-tumtum-text-muted">{eventDate}</p>
        </div>

        {/* Sync percentage */}
        <div className="text-center">
          <p className="text-6xl font-bold text-tumtum-accent">{syncPercentage}%</p>
          <p className="mt-1 text-sm text-tumtum-text-muted">em sincronia</p>
        </div>

        {/* Comparison */}
        <div className="flex w-full items-center justify-around">
          <div className="text-center">
            <p className="text-4xl font-bold text-tumtum-red">{userPeakBpm}</p>
            <p className="mt-1 text-xs text-tumtum-text-muted">Seu pico</p>
          </div>
          <span className="text-lg font-medium text-tumtum-text-muted">vs</span>
          <div className="text-center">
            <p className="text-4xl font-bold text-tumtum-accent">{artistPeakBpm}</p>
            <p className="mt-1 text-xs text-tumtum-text-muted">{artistName}</p>
          </div>
        </div>

        {/* User */}
        <p className="text-sm text-tumtum-text-muted">@{userName}</p>
      </div>

      <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-tumtum-accent/5 to-transparent" />
    </div>
  )
}
