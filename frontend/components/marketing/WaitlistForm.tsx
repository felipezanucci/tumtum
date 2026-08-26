'use client'

import { useState } from 'react'

import { ApiError, waitlist } from '@/lib/api'

type State =
  | { kind: 'idle' }
  | { kind: 'sending' }
  | { kind: 'joined' }
  | { kind: 'already' }
  | { kind: 'failed'; message: string }

/**
 * The waitlist form.
 *
 * The version this page was ported from had a button with no handler at all:
 * you typed an address, clicked, and nothing happened — no error, no
 * confirmation, no request. The lead was lost and the person believed they had
 * signed up. That is the failure this project keeps meeting from every angle,
 * an interface saying something false about its own state, and a marketing
 * page is the worst place for it because the person never comes back to check.
 *
 * So every outcome here is visible. Sending says it is sending and the field
 * locks. Success names the address back. A repeat submission is a warm
 * message, not a red one — the person wanted to be on the list and they are.
 * A failure keeps what they typed and says the server was the problem.
 */
export function WaitlistForm({ source }: { source?: string }) {
  const [email, setEmail] = useState('')
  const [state, setState] = useState<State>({ kind: 'idle' })

  const sending = state.kind === 'sending'
  const done = state.kind === 'joined' || state.kind === 'already'

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    if (sending) return

    const trimmed = email.trim()
    if (!trimmed) {
      setState({ kind: 'failed', message: 'Escreve seu e-mail primeiro.' })
      return
    }

    setState({ kind: 'sending' })
    try {
      const result = await waitlist.join(trimmed, source)
      setState({ kind: result.already_joined ? 'already' : 'joined' })
    } catch (error) {
      const message =
        error instanceof ApiError && error.status === 422
          ? 'Esse e-mail não parece completo. Confere?'
          : error instanceof ApiError
            ? error.message
            : 'Não deu pra salvar agora. Tenta de novo em um minuto.'
      setState({ kind: 'failed', message })
    }
  }

  if (done) {
    return (
      <div className="mt-11 max-w-[520px]" role="status">
        <p className="text-xl font-hero uppercase leading-tight text-tumtum-black">
          {state.kind === 'joined' ? 'Pronto. Você está na lista.' : 'Você já estava na lista.'}
        </p>
        <p className="mt-3 text-[15px] text-black/60">
          {state.kind === 'joined'
            ? 'A gente te chama quando a TumTum chegar num evento perto de você.'
            : 'Nada mudou, e é uma boa notícia: a gente já sabe onde te achar.'}
        </p>
      </div>
    )
  }

  return (
    <form className="mt-11 max-w-[520px]" onSubmit={submit} noValidate>
      <div className="flex flex-wrap gap-3">
        <input
          type="email"
          name="email"
          autoComplete="email"
          inputMode="email"
          placeholder="seu@email.com"
          aria-label="Seu e-mail"
          value={email}
          disabled={sending}
          onChange={(event) => {
            setEmail(event.target.value)
            if (state.kind === 'failed') setState({ kind: 'idle' })
          }}
          className="min-w-[220px] flex-1 rounded-full border-2 border-tumtum-black bg-transparent px-[22px] py-3.5 text-base text-tumtum-black outline-none placeholder:text-black/50 focus-visible:ring-2 focus-visible:ring-tumtum-black focus-visible:ring-offset-2 focus-visible:ring-offset-tumtum-lime disabled:opacity-60"
        />
        <button
          type="submit"
          disabled={sending}
          className="rounded-full bg-tumtum-black px-[30px] py-3.5 text-base font-headline text-tumtum-lime transition-transform hover:scale-105 focus-visible:ring-2 focus-visible:ring-tumtum-black focus-visible:ring-offset-2 focus-visible:ring-offset-tumtum-lime disabled:scale-100 disabled:opacity-70 motion-reduce:transition-none motion-reduce:hover:scale-100"
        >
          {sending ? 'Entrando…' : 'Entrar na lista'}
        </button>
      </div>

      {state.kind === 'failed' && (
        <p className="mt-4 text-sm font-headline text-tumtum-black" role="alert">
          {state.message}
        </p>
      )}
    </form>
  )
}
