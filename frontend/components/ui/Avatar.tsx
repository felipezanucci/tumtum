'use client'

import { useState } from 'react'

import { safeHttpsUrl } from '@/lib/sharing'

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

/**
 * A person's photo, or their initials. The photo is a plain `<img>` and only
 * from an `https:` address: next/image no longer proxies arbitrary remote
 * hosts (security review, 26/09), and anything else — or a photo that fails
 * to load — falls back to the initials rather than a broken image.
 */
export default function Avatar({ src, name, size = 'md', className = '' }: AvatarProps) {
  const [failed, setFailed] = useState(false)
  const safeSrc = safeHttpsUrl(src)

  if (safeSrc && !failed) {
    return (
      // eslint-disable-next-line @next/next/no-img-element -- a remote avatar, deliberately not proxied through next/image
      <img
        src={safeSrc}
        alt={name}
        width={sizePx[size]}
        height={sizePx[size]}
        referrerPolicy="no-referrer"
        onError={() => setFailed(true)}
        className={`rounded-full object-cover ${sizeStyles[size]} ${className}`}
      />
    )
  }

  return (
    <div
      className={`
        inline-flex items-center justify-center rounded-full
        bg-tumtum-pink font-medium text-tumtum-black
        ${sizeStyles[size]}
        ${className}
      `}
    >
      {getInitials(name)}
    </div>
  )
}
