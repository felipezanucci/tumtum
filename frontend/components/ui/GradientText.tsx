'use client'

type GradientVariant = 'warm' | 'cool' | 'neon' | 'fire' | 'sunset'

const variantClasses: Record<GradientVariant, string> = {
  warm: 'text-gradient-warm',
  cool: 'text-gradient-cool',
  neon: 'text-gradient-neon',
  fire: 'text-gradient-fire',
  sunset: 'text-gradient-sunset',
}

interface GradientTextProps {
  children: React.ReactNode
  variant?: GradientVariant
  as?: 'h1' | 'h2' | 'h3' | 'h4' | 'p' | 'span'
  className?: string
}

export default function GradientText({
  children,
  variant = 'warm',
  as: Component = 'span',
  className = '',
}: GradientTextProps) {
  return (
    <Component className={`${variantClasses[variant]} ${className}`}>
      {children}
    </Component>
  )
}
