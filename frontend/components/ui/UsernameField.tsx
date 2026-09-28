'use client'

import { cleanUsername, usernameStatusText, type UsernameStatus } from '@/lib/username'

interface UsernameFieldProps {
  /** The @ as kept so far, already clean. */
  value: string
  /** Called with what was typed, cleaned as typed. */
  onChange: (clean: string) => void
  /** From `useUsernameCheck`: what the line under the field says. */
  status: UsernameStatus
  /** Ask again after a check that could not be made. */
  onRetry: () => void
  label?: string
  id?: string
  disabled?: boolean
}

const TONE: Record<UsernameStatus['kind'], string> = {
  empty: 'text-tumtum-muted',
  // Too short while still typing is not a mistake yet: no red.
  invalid: 'text-tumtum-muted',
  checking: 'text-tumtum-muted',
  available: 'text-tumtum-pink',
  unavailable: 'text-red-500',
  failed: 'text-red-500',
}

/**
 * "Seu @" (28/09): an @ shown in front, the name filtered as typed (no
 * spaces, no accents, lower case), and one line under it that is always
 * about the name on screen — the rule, "Conferindo…", "disponível", the
 * server's refusal, or that it could not ask.
 */
export default function UsernameField({
  value,
  onChange,
  status,
  onRetry,
  label = 'Seu @',
  id = 'username',
  disabled = false,
}: UsernameFieldProps) {
  const statusId = `${id}-status`
  const refused = status.kind === 'unavailable' || status.kind === 'failed'

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-tumtum-white">
        {label}
      </label>
      <div
        className={`flex items-center rounded-lg border bg-tumtum-surface transition-colors duration-150 focus-within:ring-2 focus-within:ring-tumtum-pink focus-within:ring-offset-1 focus-within:ring-offset-tumtum-black ${
          refused ? 'border-red-500' : 'border-tumtum-border hover:border-tumtum-muted'
        }`}
      >
        <span aria-hidden="true" className="pl-4 text-tumtum-muted">
          @
        </span>
        <input
          id={id}
          type="text"
          inputMode="text"
          autoCapitalize="none"
          autoCorrect="off"
          autoComplete="nickname"
          spellCheck={false}
          placeholder="seunome"
          value={value}
          disabled={disabled}
          onChange={(e) => onChange(cleanUsername(e.target.value))}
          aria-describedby={statusId}
          aria-invalid={refused || undefined}
          className="w-full rounded-lg bg-transparent py-2.5 pl-1 pr-4 text-tumtum-white placeholder:text-tumtum-muted focus:outline-none disabled:opacity-60"
        />
      </div>
      <p id={statusId} aria-live="polite" className={`text-sm ${TONE[status.kind]}`}>
        {usernameStatusText(status)}
        {status.kind === 'failed' && (
          <>
            {' '}
            <button
              type="button"
              onClick={onRetry}
              className="text-tumtum-white underline underline-offset-2"
            >
              Tentar de novo
            </button>
          </>
        )}
      </p>
    </div>
  )
}
