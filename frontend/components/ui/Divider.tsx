type DividerVariant = 'default' | 'gradient'

interface DividerProps {
  variant?: DividerVariant
  className?: string
}

export default function Divider({ variant = 'default', className = '' }: DividerProps) {
  if (variant === 'gradient') {
    return <div className={`divider-gradient w-full ${className}`} />
  }

  return (
    <div className={`h-px w-full bg-tumtum-border ${className}`} />
  )
}
