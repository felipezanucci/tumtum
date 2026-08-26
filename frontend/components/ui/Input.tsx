'use client'

import { InputHTMLAttributes, forwardRef } from 'react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  /** Rendered inside the field on the right — a reveal toggle, a unit, a spinner. */
  trailing?: React.ReactNode
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, id, className = '', trailing, ...props }, ref) => {
    const inputId = id || label?.toLowerCase().replace(/\s+/g, '-')

    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label
            htmlFor={inputId}
            className="text-sm font-medium text-tumtum-white"
          >
            {label}
          </label>
        )}
        <div className="relative">
          <input
            ref={ref}
            id={inputId}
            className={`
              w-full rounded-lg border bg-tumtum-surface px-4 py-2.5
              text-tumtum-white placeholder:text-tumtum-muted
              transition-colors duration-150
              focus:outline-none focus:ring-2 focus:ring-tumtum-lime focus:ring-offset-1 focus:ring-offset-tumtum-black
              ${error ? 'border-red-500' : 'border-tumtum-border hover:border-tumtum-muted'}
              ${trailing ? 'pr-12' : ''}
              ${className}
            `}
            {...props}
          />
          {trailing && (
            <div className="absolute inset-y-0 right-1 flex items-center">{trailing}</div>
          )}
        </div>
        {error && (
          <span className="text-sm text-red-500">{error}</span>
        )}
      </div>
    )
  }
)

Input.displayName = 'Input'

export default Input
