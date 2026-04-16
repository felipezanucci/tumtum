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

const bgClasses: Record<BackgroundVariant, string> = {
  warm: 'bg-wrapped-warm',
  cool: 'bg-wrapped-cool',
  neon: 'bg-wrapped-neon',
  fire: 'bg-wrapped-fire',
  night: 'bg-wrapped-night',
  ocean: 'bg-wrapped-ocean',
  sunset: 'bg-wrapped-sunset',
  aurora: 'bg-wrapped-aurora',
}

interface StoryCardProps {
  children: React.ReactNode
  background?: BackgroundVariant
  noise?: boolean
  className?: string
}

export default function StoryCard({
  children,
  background = 'warm',
  noise = true,
  className = '',
}: StoryCardProps) {
  return (
    <div
      className={`
        card-story ${bgClasses[background]}
        ${noise ? 'noise-overlay' : ''}
        flex flex-col justify-between p-8
        ${className}
      `}
    >
      <div className="relative z-10 flex flex-col justify-between h-full">
        {children}
      </div>
    </div>
  )
}

function Header({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return <div className={`flex flex-col gap-2 ${className}`}>{children}</div>
}

function Body({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return <div className={`flex-1 flex flex-col items-center justify-center ${className}`}>{children}</div>
}

function Footer({ children, className = '' }: { children: React.ReactNode; className?: string }) {
  return <div className={`flex flex-col gap-2 ${className}`}>{children}</div>
}

StoryCard.Header = Header
StoryCard.Body = Body
StoryCard.Footer = Footer
