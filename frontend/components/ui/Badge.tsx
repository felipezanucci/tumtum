type BadgeVariant = 'default' | 'success' | 'warning' | 'danger' | 'accent'

interface BadgeProps {
  variant?: BadgeVariant
  children: React.ReactNode
  className?: string
}

const variantStyles: Record<BadgeVariant, string> = {
  default: 'bg-tumtum-surface text-tumtum-text-primary border-tumtum-border',
  success: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  warning: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
  danger: 'bg-red-500/10 text-red-400 border-red-500/20',
  accent: 'bg-tumtum-accent/10 text-tumtum-accent border-tumtum-accent/20',
}

export default function Badge({ variant = 'default', className = '', children }: BadgeProps) {
  return (
    <span
      className={`
        inline-flex items-center rounded-full border px-2.5 py-0.5
        text-xs font-medium
        ${variantStyles[variant]}
        ${className}
      `}
    >
      {children}
    </span>
  )
}
