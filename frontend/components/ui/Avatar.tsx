import Image from 'next/image'

type AvatarSize = 'sm' | 'md' | 'lg'

interface AvatarProps {
  src?: string | null
  name: string
  size?: AvatarSize
  className?: string
}

const sizeStyles: Record<AvatarSize, string> = {
  sm: 'h-8 w-8 text-xs',
  md: 'h-10 w-10 text-sm',
  lg: 'h-14 w-14 text-lg',
}

const sizePx: Record<AvatarSize, number> = {
  sm: 32,
  md: 40,
  lg: 56,
}

function getInitials(name: string): string {
  return name
    .split(' ')
    .slice(0, 2)
    .map((word) => word[0])
    .join('')
    .toUpperCase()
}

export default function Avatar({ src, name, size = 'md', className = '' }: AvatarProps) {
  if (src) {
    return (
      <Image
        src={src}
        alt={name}
        width={sizePx[size]}
        height={sizePx[size]}
        className={`rounded-full object-cover ${sizeStyles[size]} ${className}`}
      />
    )
  }

  return (
    <div
      className={`
        inline-flex items-center justify-center rounded-full
        bg-tumtum-red font-medium text-white
        ${sizeStyles[size]}
        ${className}
      `}
    >
      {getInitials(name)}
    </div>
  )
}
