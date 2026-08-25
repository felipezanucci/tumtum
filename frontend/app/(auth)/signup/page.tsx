'use client'

import { useState } from 'react'
import { Wordmark } from '@/components/brand'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/stores/useAuthStore'
import { Button, Input } from '@/components/ui'

export default function SignupPage() {
  const router = useRouter()
  const { register, loading } = useAuthStore()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
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
          <Input
            label="Senha"
            type="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={6}
          />

          {error && (
            <p className="text-sm text-red-500">{error}</p>
          )}

          <Button type="submit" loading={loading} className="w-full">
            Criar conta
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-tumtum-muted">
          Já tem conta?{' '}
          <Link href="/login" className="text-tumtum-lime hover:underline">
            Fazer login
          </Link>
        </p>
      </div>
    </main>
  )
}
