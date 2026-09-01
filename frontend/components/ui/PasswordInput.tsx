'use client'

import { InputHTMLAttributes, forwardRef, useState } from 'react'

import Input from './Input'

interface PasswordInputProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: string
  error?: string
}

/**
 * A password field you can read back.
 *
 * Typing a password blind into a phone keyboard is where accounts are lost,
 * and this one has no way back: there is no password reset yet, so a character
 * nobody saw is a permanently unreachable account. The reveal toggle is not a
 * convenience here, it is the recovery mechanism.
 *
 * It starts hidden, and the toggle is a `type="button"` — inside a form, a
 * bare `<button>` submits, so an eye icon that signed you up would be a
 * memorable bug.
 */
const PasswordInput = forwardRef<HTMLInputElement, PasswordInputProps>(
  (props, ref) => {
    const [revealed, setRevealed] = useState(false)

    return (
      <Input
        {...props}
        ref={ref}
        type={revealed ? 'text' : 'password'}
        trailing={
          <button
            type="button"
            onClick={() => setRevealed((current) => !current)}
            aria-label={revealed ? 'Ocultar senha' : 'Mostrar senha'}
            aria-pressed={revealed}
            className="rounded-md px-2.5 py-2 text-tumtum-muted transition-colors hover:text-tumtum-white focus:outline-none focus:ring-2 focus:ring-tumtum-pink"
          >
            {revealed ? (
              // Struck-through eye: hidden again.
              <svg
                className="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={1.8}
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M3.98 8.22A10.5 10.5 0 001.5 12s3.75 7.5 10.5 7.5a10.2 10.2 0 004.13-.87M6.23 6.23A10.2 10.2 0 0112 4.5c6.75 0 10.5 7.5 10.5 7.5a18.7 18.7 0 01-2.79 4.03M6.23 6.23L3 3m3.23 3.23l3.65 3.65m7.89 7.89L21 21m-3.23-3.23l-3.65-3.65m0 0a3 3 0 10-4.24-4.24m4.24 4.24L9.88 9.88"
                />
              </svg>
            ) : (
              <svg
                className="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={1.8}
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M2.04 12.32a1.01 1.01 0 010-.64C3.42 7.51 7.36 4.5 12 4.5c4.64 0 8.57 3.01 9.96 7.18.04.21.04.43 0 .64C20.58 16.49 16.64 19.5 12 19.5c-4.64 0-8.57-3.01-9.96-7.18z"
                />
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
              </svg>
            )}
          </button>
        }
      />
    )
  },
)

PasswordInput.displayName = 'PasswordInput'

export default PasswordInput
