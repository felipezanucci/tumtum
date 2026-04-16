'use client'

type BackgroundVariant =
  | 'warm'
  | 'cool'
  | 'neon'
  | 'fire'
  | 'night'
  | 'ocean'
  | 'sunset'
  | 'aurora'
  | 'animated'

const variantClasses: Record<BackgroundVariant, string> = {
  warm: 'bg-wrapped-warm',
  cool: 'bg-wrapped-cool',
  neon: 'bg-wrapped-neon',
  fire: 'bg-wrapped-fire',
  night: 'bg-wrapped-night',
  ocean: 'bg-wrapped-ocean',
  sunset: 'bg-wrapped-sunset',
  aurora: 'bg-wrapped-aurora',
  animated: 'bg-wrapped-animated',
}

interface GradientBackgroundProps {
  children: React.ReactNode
  variant?: BackgroundVariant
  noise?: boolean
  className?: string
}

export default function GradientBackground({
  children,
  variant = 'warm',
  noise = false,
  className = '',
}: GradientBackgroundProps) {
  return (
    <div
      className={`
        relative ${variantClasses[variant]}
        ${noise ? 'noise-overlay' : ''}
        ${className}
      `}
    >
      <div className="relative z-10">{children}</div>
    </div>
  )
}
