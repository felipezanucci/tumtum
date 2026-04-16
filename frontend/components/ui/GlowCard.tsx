'use client'

type GlowColor = 'red' | 'magenta' | 'cyan' | 'purple'

const glowClasses: Record<GlowColor, string> = {
  red: 'hover:shadow-glow-md',
  magenta: 'hover:shadow-glow-magenta-md',
  cyan: 'hover:shadow-glow-cyan-md',
  purple: 'hover:shadow-glow-purple-md',
}

const borderHoverClasses: Record<GlowColor, string> = {
  red: 'hover:border-tumtum-red/40',
  magenta: 'hover:border-wrapped-magenta/40',
  cyan: 'hover:border-tumtum-accent/40',
  purple: 'hover:border-wrapped-purple/40',
}

interface GlowCardProps {
  children: React.ReactNode
  glow?: GlowColor
  className?: string
  onClick?: () => void
}

export default function GlowCard({
  children,
  glow = 'magenta',
  className = '',
  onClick,
}: GlowCardProps) {
  return (
    <div
      onClick={onClick}
      className={`
        relative overflow-hidden rounded-3xl
        bg-tumtum-surface border border-tumtum-border
        transition-all duration-300 ease-spring
        ${glowClasses[glow]}
        ${borderHoverClasses[glow]}
        ${onClick ? 'cursor-pointer' : ''}
        ${className}
      `}
    >
      {children}
    </div>
  )
}
