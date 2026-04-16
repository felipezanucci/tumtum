'use client'

type StatSize = 'hero' | 'lg' | 'md'
type GradientVariant = 'warm' | 'cool' | 'neon' | 'fire' | 'sunset' | 'none'

const sizeClasses: Record<StatSize, string> = {
  hero: 'text-stat-hero',
  lg: 'text-stat-lg',
  md: 'text-stat-md',
}

const gradientClasses: Record<GradientVariant, string> = {
  warm: 'text-gradient-warm',
  cool: 'text-gradient-cool',
  neon: 'text-gradient-neon',
  fire: 'text-gradient-fire',
  sunset: 'text-gradient-sunset',
  none: '',
}

interface StatDisplayProps {
  value: string | number
  label: string
  unit?: string
  size?: StatSize
  gradient?: GradientVariant
  className?: string
}

export default function StatDisplay({
  value,
  label,
  unit,
  size = 'lg',
  gradient = 'warm',
  className = '',
}: StatDisplayProps) {
  return (
    <div className={`flex flex-col ${className}`}>
      <div className="flex items-baseline gap-2">
        <span
          className={`font-display ${sizeClasses[size]} ${gradientClasses[gradient]} ${
            gradient === 'none' ? 'text-tumtum-text-primary' : ''
          }`}
        >
          {value}
        </span>
        {unit && (
          <span className="text-label-lg uppercase text-tumtum-text-muted">
            {unit}
          </span>
        )}
      </div>
      <span className="text-label-sm uppercase text-tumtum-text-muted mt-2">
        {label}
      </span>
    </div>
  )
}
