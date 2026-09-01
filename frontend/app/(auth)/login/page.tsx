'use client'

import { useState } from 'react'
import { Wordmark } from '@/components/brand'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/stores/useAuthStore'
import { Button, Input, PasswordInput } from '@/components/ui'

export default function LoginPage() {
  const router = useRouter()
  const { login, loading } = useAuthStore()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    try {
      await login(email, password)
      router.push('/events')
    } catch (err: any) {
      setError(err?.detail || err?.message || 'Erro ao fazer login')
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <Wordmark className="h-8 w-auto text-tumtum-white" />
          <p className="mt-2 text-tumtum-muted">Faça login para continuar</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
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
            autoComplete="current-password"
          />

          <p className="text-right text-sm">
            <Link
              href="/esqueci-senha"
              className="text-tumtum-muted hover:text-tumtum-pink hover:underline"
            >
              Esqueci minha senha
            </Link>
          </p>

          {error && (
            <p className="text-sm text-red-500">{error}</p>
          )}

          <Button type="submit" loading={loading} className="w-full">
            Entrar
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-tumtum-muted">
          Não tem conta?{' '}
          <Link href="/signup" className="text-tumtum-pink hover:underline">
            Criar conta
          </Link>
        </p>
      </div>
    </main>
  )
}
