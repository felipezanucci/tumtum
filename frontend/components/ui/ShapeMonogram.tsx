'use client'

type ShapeVariant = 'blob' | 'blobAlt' | 'spike' | 'diamond' | 'hexagon' | 'arch' | 'squircle'
type GradientVariant = 'warm' | 'cool' | 'neon' | 'fire' | 'sunset' | 'aurora'

const shapeClasses: Record<ShapeVariant, string> = {
  blob: 'shape-blob',
  blobAlt: 'shape-blob-alt',
  spike: 'shape-spike',
  diamond: 'shape-diamond',
  hexagon: 'shape-hexagon',
  arch: 'shape-arch',
  squircle: 'shape-squircle',
}

const gradientBgClasses: Record<GradientVariant, string> = {
  warm: 'bg-wrapped-warm',
  cool: 'bg-wrapped-cool',
  neon: 'bg-wrapped-neon',
  fire: 'bg-wrapped-fire',
  sunset: 'bg-wrapped-sunset',
  aurora: 'bg-wrapped-aurora',
}

type SizePreset = 'sm' | 'md' | 'lg' | 'xl'

const sizeClasses: Record<SizePreset, string> = {
  sm: 'w-16 h-16',
  md: 'w-24 h-24',
  lg: 'w-36 h-36',
  xl: 'w-48 h-48',
}

interface ShapeMonogramProps {
  shape?: ShapeVariant
  gradient?: GradientVariant
  size?: SizePreset
  animate?: boolean
  opacity?: number
  className?: string
}

export default function ShapeMonogram({
  shape = 'blob',
  gradient = 'warm',
  size = 'md',
  animate = false,
  opacity = 100,
  className = '',
}: ShapeMonogramProps) {
  return (
    <div
      className={`
        ${shapeClasses[shape]}
        ${gradientBgClasses[gradient]}
        ${sizeClasses[size]}
        ${animate && shape === 'blob' ? 'animate-morph' : ''}
        ${animate && shape !== 'blob' ? 'animate-rotate-slow' : ''}
        ${className}
      `}
      style={{ opacity: opacity / 100 }}
    />
  )
}
