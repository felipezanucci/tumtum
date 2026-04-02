'use client'

import { InputHTMLAttributes, forwardRef } from 'react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, id, className = '', ...props }, ref) => {
    const inputId = id || label?.toLowerCase().replace(/\s+/g, '-')

    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label
            htmlFor={inputId}
            className="text-sm font-medium text-tumtum-text-primary"
          >
            {label}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          className={`
            w-full rounded-lg border bg-tumtum-surface px-4 py-2.5
            text-tumtum-text-primary placeholder:text-tumtum-text-muted
            transition-colors duration-150
            focus:outline-none focus:ring-2 focus:ring-tumtum-red focus:ring-offset-1 focus:ring-offset-tumtum-dark
            ${error ? 'border-red-500' : 'border-tumtum-border hover:border-tumtum-text-muted'}
            ${className}
          `}
          {...props}
        />
        {error && (
          <span className="text-sm text-red-500">{error}</span>
        )}
      </div>
    )
  }
)

Input.displayName = 'Input'

export default Input
