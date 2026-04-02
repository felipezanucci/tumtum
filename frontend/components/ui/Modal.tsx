'use client'

import { useEffect, useRef, HTMLAttributes } from 'react'

interface ModalProps {
  open: boolean
  onClose: () => void
  children: React.ReactNode
  className?: string
}

export default function Modal({ open, onClose, className = '', children }: ModalProps) {
  const overlayRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }

    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'

    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
    }
  }, [open, onClose])

  if (!open) return null

  return (
    <div
      ref={overlayRef}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      onClick={(e) => {
        if (e.target === overlayRef.current) onClose()
      }}
    >
      <div
        role="dialog"
        aria-modal="true"
        className={`
          w-full max-w-lg rounded-xl border border-tumtum-border
          bg-tumtum-surface p-6 shadow-2xl
          animate-in fade-in zoom-in-95
          ${className}
        `}
      >
        {children}
      </div>
    </div>
  )
}

function ModalHeader({ className = '', children, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div className={`mb-4 ${className}`} {...props}>
      {children}
    </div>
  )
}

function ModalTitle({ className = '', children, ...props }: HTMLAttributes<HTMLHeadingElement>) {
  return (
    <h2 className={`text-xl font-semibold text-tumtum-text-primary ${className}`} {...props}>
      {children}
    </h2>
  )
}

function ModalFooter({ className = '', children, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div className={`mt-6 flex justify-end gap-3 ${className}`} {...props}>
      {children}
    </div>
  )
}

Modal.Header = ModalHeader
Modal.Title = ModalTitle
Modal.Footer = ModalFooter
