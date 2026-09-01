'use client'

import { useState } from 'react'
import { Wordmark } from '@/components/brand'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/stores/useAuthStore'
import { Button, Input, PasswordInput } from '@/components/ui'

export default function SignupPage() {
  const router = useRouter()
  const { register, loading } = useAuthStore()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [error, setError] = useState('')

  // Only complain once they have actually typed a confirmation. Turning the
  // field red on the first keystroke tells someone they got it wrong while
  // they are still getting it right.
  const mismatch = confirmation.length > 0 && confirmation !== password

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    if (password !== confirmation) {
      // There is no password reset yet, so a typo here does not cost a login
      // attempt — it costs the account. This check is the only thing standing
      // between a slip and an address that can never sign in again.
      setError('As senhas não são iguais. Confere as duas antes de continuar.')
      return
    }
    try {
      await register(email, name, password)
      router.push('/onboarding')
    } catch (err: any) {
      setError(err?.detail || err?.message || 'Erro ao criar conta')
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <Wordmark className="h-8 w-auto text-tumtum-white" />
          <p className="mt-2 text-tumtum-muted">Crie sua conta e sinta o evento</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Nome"
            type="text"
            placeholder="Seu nome"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <Input
            label="Email"
            type="email"
            placeholder="seu@email.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <PasswordInput
            label="Senha"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={6}
            autoComplete="new-password"
          />
          <PasswordInput
            label="Confirme a senha"
            placeholder="••••••••"
            value={confirmation}
            onChange={(e) => setConfirmation(e.target.value)}
            required
            autoComplete="new-password"
            error={mismatch ? 'As senhas não são iguais.' : undefined}
          />

          {error && (
            <p className="text-sm text-red-500">{error}</p>
          )}

          <Button
            type="submit"
            loading={loading}
            disabled={mismatch}
            className="w-full"
          >
            Criar conta
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-tumtum-muted">
          Já tem conta?{' '}
          <Link href="/login" className="text-tumtum-pink hover:underline">
            Fazer login
          </Link>
        </p>
      </div>
    </main>
  )
}
