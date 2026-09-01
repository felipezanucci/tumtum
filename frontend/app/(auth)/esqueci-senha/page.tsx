'use client'

import { useState } from 'react'
import Link from 'next/link'
import { Wordmark } from '@/components/brand'
import { passwordReset } from '@/lib/api'
import { Button, Input } from '@/components/ui'

/**
 * Ask for a reset link.
 *
 * The confirmation is deliberately vague about whether the address has an
 * account — the API answers identically either way, and a page that said
 * "esse e-mail não está cadastrado" would be a free tool for finding out who
 * has one. The wording has to carry that without sounding evasive, so it says
 * what happens *if* there is a conta rather than pretending to have sent
 * something it may not have.
 */
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [sending, setSending] = useState(false)
  const [sent, setSent] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    if (sending) return
    setError('')
    setSending(true)
    try {
      await passwordReset.request(email.trim())
      setSent(true)
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Não deu pra pedir agora. Tenta de novo em um minuto.',
      )
    } finally {
      setSending(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <Wordmark className="h-8 w-auto text-tumtum-white" />
          <p className="mt-2 text-tumtum-muted">Esqueceu a senha? Acontece.</p>
        </div>

        {sent ? (
          <div className="rounded-lg border border-tumtum-border bg-tumtum-surface p-4">
            <p className="font-headline text-tumtum-white">Olha sua caixa de entrada</p>
            <p className="mt-2 text-sm text-tumtum-muted">
              Se esse e-mail tiver uma conta, o link para criar uma nova senha
              acabou de sair. Ele vale por 30 minutos. Confere o spam também.
            </p>
            <Link href="/login">
              <Button variant="secondary" className="mt-4 w-full">
                Voltar para o login
              </Button>
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Email"
              type="email"
              placeholder="seu@email.com"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              required
            />

            {error && <p className="text-sm text-red-500">{error}</p>}

            <Button type="submit" loading={sending} className="w-full">
              Me manda o link
            </Button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-tumtum-muted">
          Lembrou?{' '}
          <Link href="/login" className="text-tumtum-pink hover:underline">
            Fazer login
          </Link>
        </p>
      </div>
    </main>
  )
}
