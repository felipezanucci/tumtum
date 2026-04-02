'use client'

import { useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/stores/useAuthStore'
import { Button, Input } from '@/components/ui'

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
      setError(err.detail || 'Erro ao fazer login')
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-dark px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <h1
            className="text-3xl font-bold uppercase tracking-widest text-tumtum-red"
            style={{ fontFamily: 'Georgia, serif' }}
          >
            Tumtum
          </h1>
          <p className="mt-2 text-tumtum-text-muted">Faça login para continuar</p>
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
          <Input
            label="Senha"
            type="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          {error && (
            <p className="text-sm text-red-500">{error}</p>
          )}

          <Button type="submit" loading={loading} className="w-full">
            Entrar
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-tumtum-text-muted">
          Não tem conta?{' '}
          <Link href="/signup" className="text-tumtum-red hover:underline">
            Criar conta
          </Link>
        </p>
      </div>
    </main>
  )
}
