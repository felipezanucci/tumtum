'use client'

import { useEffect, useRef } from 'react'

import {
  CONSENT_PT,
  CONSENT_PURPOSES,
  CORE_PURPOSES,
  type ConsentCopy,
  type ConsentPurpose,
} from '@/lib/consent-copy'
import type { ConsentChoices } from '@/lib/consent'

interface ConsentTogglesProps {
  choices: ConsentChoices
  onChange: (purpose: ConsentPurpose, granted: boolean) => void
  /** The purpose a refusal pointed at (`?focus=`): outlined and scrolled to. */
  focus?: ConsentPurpose | null
  disabled?: boolean
  /**
   * Purposes that can be turned on here but not off. The Terms, once
   * accepted, are withdrawn by deleting the account — an account without
   * them cannot exist — and the row says so instead of offering a switch
   * that could only fail.
   */
  lockedWhenOn?: readonly ConsentPurpose[]
  copy?: ConsentCopy
}

/**
 * One row per purpose, one switch per row (26/09). The consent screen and
 * the profile's Privacidade section both render this, so the same words and
 * the same order appear wherever the person decides.
 *
 * Quiet on purpose: black canvas, white text, pink only on a switch that is
 * on (black knob on it — never white on pink).
 */
export default function ConsentToggles({
  choices,
  onChange,
  focus = null,
  disabled = false,
  lockedWhenOn = [],
  copy = CONSENT_PT,
}: ConsentTogglesProps) {
  const focusRef = useRef<HTMLLIElement | null>(null)

  useEffect(() => {
    focusRef.current?.scrollIntoView({ block: 'center', behavior: 'smooth' })
  }, [focus])

  return (
    <ul className="space-y-3">
      {CONSENT_PURPOSES.map((purpose) => {
        const text = copy.purposes[purpose]
        const on = choices[purpose]
        const core = CORE_PURPOSES.includes(purpose)
        const locked = on && lockedWhenOn.includes(purpose)
        const focused = focus === purpose
        const id = `consent-${purpose}`
        return (
          <li
            key={purpose}
            ref={focused ? focusRef : undefined}
            className={`rounded-xl border p-4 ${
              focused ? 'border-tumtum-pink bg-tumtum-surface' : 'border-tumtum-border'
            }`}
          >
            <div className="flex items-start justify-between gap-4">
              <div className="min-w-0">
                <p className="text-[11px] font-medium uppercase tracking-wider text-tumtum-muted">
                  {core ? copy.requiredTag : copy.optionalTag}
                </p>
                <p id={`${id}-label`} className="mt-1 font-medium text-tumtum-white">
                  {text.title}
                </p>
                <p id={`${id}-desc`} className="mt-1 text-sm leading-relaxed text-tumtum-muted">
                  {text.description}
                  {text.without && !on ? ` ${text.without}` : ''}
                </p>
                {locked && (
                  <p className="mt-2 text-xs text-tumtum-muted">{copy.lockedNote}</p>
                )}
              </div>
              <button
                type="button"
                role="switch"
                id={id}
                aria-checked={on}
                aria-labelledby={`${id}-label`}
                aria-describedby={`${id}-desc`}
                disabled={disabled || locked}
                onClick={() => onChange(purpose, !on)}
                className={`relative mt-1 inline-flex h-7 w-12 shrink-0 items-center rounded-full border transition-colors focus:outline-none focus:ring-2 focus:ring-tumtum-pink focus:ring-offset-2 focus:ring-offset-tumtum-black disabled:cursor-not-allowed disabled:opacity-60 ${
                  on ? 'border-tumtum-pink bg-tumtum-pink' : 'border-tumtum-border bg-tumtum-surface'
                }`}
              >
                <span
                  aria-hidden="true"
                  className={`inline-block h-5 w-5 rounded-full transition-transform ${
                    on ? 'translate-x-6 bg-tumtum-black' : 'translate-x-1 bg-tumtum-muted'
                  }`}
                />
              </button>
            </div>
          </li>
        )
      })}
    </ul>
  )
}
