'use client'

import { Suspense, useState } from 'react'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'

import { Wordmark } from '@/components/brand'
import { passwordReset } from '@/lib/api'
import { Button, Loading, PasswordInput } from '@/components/ui'

/**
 * Choose a new password, using the token from the email link.
 *
 * Signing in on success rather than bouncing to the login form: the person
 * just proved control of the mailbox and typed a password, so asking them to
 * type it again is ceremony, not security.
 *
 * The confirmation field is here for the same reason it is on signup — a typo
 * would replace a password they cannot remember with one they never saw.
 */
function ResetForm() {
  const router = useRouter()
  const token = useSearchParams().get('token') ?? ''

  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const mismatch = confirmation.length > 0 && confirmation !== password

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    if (saving) return
    setError('')
    if (password !== confirmation) {
      setError('As senhas não são iguais. Confere as duas antes de continuar.')
      return
    }
    setSaving(true)
    try {
      const { access_token } = await passwordReset.complete(token, password)
      localStorage.setItem('access_token', access_token)
      router.push('/events')
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Não deu pra salvar agora. Tenta de novo em um minuto.',
      )
      setSaving(false)
    }
  }

  // A link that arrived without a token is not a form worth showing — it would
  // fail on submit and blame the person for something the link did.
  if (!token) {
    return (
      <div className="rounded-lg border border-tumtum-border bg-tumtum-surface p-4">
        <p className="font-headline text-tumtum-white">Esse link está incompleto</p>
        <p className="mt-2 text-sm text-tumtum-muted">
          Alguns aplicativos cortam links longos. Abra o link do e-mail inteiro,
          ou peça um novo.
        </p>
        <Link href="/esqueci-senha">
          <Button className="mt-4 w-full">Pedir um novo link</Button>
        </Link>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <PasswordInput
        label="Nova senha"
        placeholder="••••••••"
        value={password}
        onChange={(event) => setPassword(event.target.value)}
        autoComplete="new-password"
        required
        minLength={6}
      />
      <PasswordInput
        label="Confirme a nova senha"
        placeholder="••••••••"
        value={confirmation}
        onChange={(event) => setConfirmation(event.target.value)}
        autoComplete="new-password"
        required
        error={mismatch ? 'As senhas não são iguais.' : undefined}
      />

      {error && <p className="text-sm text-red-500">{error}</p>}

      <Button type="submit" loading={saving} disabled={mismatch} className="w-full">
        Salvar e entrar
      </Button>
    </form>
  )
}

export default function ResetPasswordPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <Wordmark className="h-8 w-auto text-tumtum-white" />
          <p className="mt-2 text-tumtum-muted">Escolha uma senha nova</p>
        </div>

        {/* useSearchParams needs a Suspense boundary to prerender. */}
        <Suspense fallback={<Loading />}>
          <ResetForm />
        </Suspense>

        <p className="mt-6 text-center text-sm text-tumtum-muted">
          <Link href="/login" className="text-tumtum-pink hover:underline">
            Voltar para o login
          </Link>
        </p>
      </div>
    </main>
  )
}
