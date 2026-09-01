'use client'

import { useState } from 'react'

import { ApiError, waitlist } from '@/lib/api'
import type { SiteCopy } from '@/lib/site-copy'

type State =
  | { kind: 'idle' }
  | { kind: 'sending' }
  | { kind: 'joined' }
  | { kind: 'already' }
  | { kind: 'failed'; message: string }

/**
 * The waitlist form, in the v0.4 redesign's shape: one email field.
 *
 * The name fields the previous form carried are gone on purpose — they were
 * optional in the API all along, and the page right under this form promises
 * "a gente só usa seu e-mail". Asking for less than we promise beats asking
 * for more.
 *
 * What survives from the old form, deliberately, is its honesty about state:
 * this page once shipped a button with no handler at all — type, click,
 * nothing, lead lost, person convinced they signed up. So every outcome here
 * is visible: sending locks the field and says so, success names itself, a
 * repeat submission is a warm message rather than a red one, and a failure
 * keeps what was typed and blames the server, never the person.
 */
export function SiteWaitlistForm({ copy }: { copy: SiteCopy['waitlist'] }) {
  const [email, setEmail] = useState('')
  const [state, setState] = useState<State>({ kind: 'idle' })

  const sending = state.kind === 'sending'

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    if (sending) return

    const trimmed = email.trim()
    if (!trimmed) {
      setState({ kind: 'failed', message: copy.errorEmpty })
      return
    }

    setState({ kind: 'sending' })
    try {
      const result = await waitlist.join({ email: trimmed, source: 'landing' })
      setState({ kind: result.already_joined ? 'already' : 'joined' })
    } catch (error) {
      const message =
        error instanceof ApiError && error.status === 422
          ? copy.errorInvalid
          : error instanceof ApiError
            ? error.message
            : copy.errorServer
      setState({ kind: 'failed', message })
    }
  }

  if (state.kind === 'joined' || state.kind === 'already') {
    return (
      <div className="mx-auto mt-10 max-w-[520px]" role="status">
        <p className="font-hero text-xl uppercase leading-tight text-tumtum-white">
          {state.kind === 'joined' ? copy.joined : copy.already}
        </p>
        <p className="mt-3 text-[15px] text-[#8A8A8A]">
          {state.kind === 'joined' ? copy.joinedBody : copy.alreadyBody}
        </p>
      </div>
    )
  }

  return (
    <form className="mx-auto mt-10 max-w-[520px]" onSubmit={submit} noValidate>
      <div className="flex flex-wrap justify-center gap-3">
        <input
          type="email"
          name="email"
          autoComplete="email"
          inputMode="email"
          placeholder={copy.placeholder}
          aria-label={copy.placeholder}
          value={email}
          disabled={sending}
          onChange={(event) => {
            setEmail(event.target.value)
            if (state.kind === 'failed') setState({ kind: 'idle' })
          }}
          className="w-[340px] max-w-full rounded-xl border border-[#2E2E2E] bg-[#0F0F0F] px-5 py-3.5 text-base text-tumtum-white outline-none placeholder:text-[#6F6F6F] focus-visible:border-tumtum-pink disabled:opacity-60"
        />
        <button
          type="submit"
          disabled={sending}
          className="rounded-xl bg-tumtum-pink px-7 py-3.5 text-base font-headline text-tumtum-black transition-colors hover:bg-tumtum-yellow disabled:opacity-60 motion-reduce:transition-none"
        >
          {sending ? copy.sending : copy.button}
        </button>
      </div>
      {state.kind === 'failed' && (
        <p className="mt-4 text-[15px] text-tumtum-yellow" role="alert">
          {state.message}
        </p>
      )}
      <p className="mt-5 text-[12.5px] text-[#6F6F6F]">{copy.privacy}</p>
    </form>
  )
}
