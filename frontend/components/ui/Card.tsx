import { HTMLAttributes } from 'react'

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  hoverable?: boolean
}

export default function Card({ hoverable, className = '', children, ...props }: CardProps) {
  return (
    <div
      className={`
        rounded-xl border border-tumtum-border bg-tumtum-surface p-6
        ${hoverable ? 'transition-colors duration-150 hover:border-tumtum-text-muted cursor-pointer' : ''}
        ${className}
      `}
      {...props}
    >
      {children}
    </div>
  )
}

function CardHeader({ className = '', children, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div className={`mb-4 ${className}`} {...props}>
      {children}
    </div>
  )
}

function CardTitle({ className = '', children, ...props }: HTMLAttributes<HTMLHeadingElement>) {
  return (
    <h3 className={`text-lg font-semibold text-tumtum-text-primary ${className}`} {...props}>
      {children}
    </h3>
  )
}

function CardContent({ className = '', children, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div className={`text-tumtum-text-muted ${className}`} {...props}>
      {children}
    </div>
  )
}

Card.Header = CardHeader
Card.Title = CardTitle
Card.Content = CardContent
