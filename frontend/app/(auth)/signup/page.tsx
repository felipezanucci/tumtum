'use client'

import { useEffect, useState } from 'react'
import { Wordmark } from '@/components/brand'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/stores/useAuthStore'
import { Button, Input, PasswordInput } from '@/components/ui'
import { codeDigits, isCompleteCode, secondsUntil } from '@/lib/signup-code'

/**
 * Criar conta, in two steps since 24/09 (#64). Test 7 made an account with
 * `teste@teste.com`; Felipe's rule is that only a real address makes one. The
 * first step mails a 6-digit code and creates nothing, the second takes the
 * code back — and only then does the account exist.
 */
export default function SignupPage() {
  const router = useRouter()
  const { startSignup, confirmSignup, loading } = useAuthStore()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  // The second step: where the code went (the server's answer), or null.
  const [sentTo, setSentTo] = useState<string | null>(null)
  const [minutes, setMinutes] = useState(15)
  const [code, setCode] = useState('')
  const [resendAt, setResendAt] = useState(0)
  const [now, setNow] = useState(() => Date.now())

  // Only complain once they have actually typed a confirmation. Turning the
  // field red on the first keystroke tells someone they got it wrong while
  // they are still getting it right.
  const mismatch = confirmation.length > 0 && confirmation !== password

  const wait = secondsUntil(resendAt, now)
  useEffect(() => {
    if (!sentTo || wait === 0) return
    const tick = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(tick)
  }, [sentTo, wait])

  async function sendCode(again: boolean) {
    setError('')
    setNotice('')
    try {
      const started = await startSignup(email, name, password)
      setSentTo(started.email)
      setMinutes(Math.ceil(started.expires_in_seconds / 60))
      setCode('')
      setResendAt(Date.now() + started.resend_after_seconds * 1000)
      setNow(Date.now())
      if (again) setNotice(`Código novo a caminho de ${started.email}. Só o mais novo vale.`)
    } catch (err: any) {
      setError(err?.detail || err?.message || 'Não deu pra mandar o código.')
    }
  }

  async function handleStart(e: React.FormEvent) {
    e.preventDefault()
    if (password !== confirmation) {
      // A typo here does not cost a login attempt — it costs the account.
      // This check is the only thing standing between a slip and an address
      // that can never sign in again.
      setError('As senhas não são iguais. Confere as duas antes de continuar.')
      return
    }
    await sendCode(false)
  }

  async function handleConfirm(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setNotice('')
    if (!sentTo) return
    if (!isCompleteCode(code)) {
      setError('O código tem 6 números.')
      return
    }
    try {
      await confirmSignup(sentTo, code)
      router.push('/onboarding')
    } catch (err: any) {
      setError(err?.detail || err?.message || 'Não deu pra confirmar o código.')
    }
  }

  function fixEmail() {
    setSentTo(null)
    setError('')
    setNotice('')
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <Wordmark className="h-8 w-auto text-tumtum-white" />
          <p className="mt-2 text-tumtum-muted">Crie sua conta e sinta o evento</p>
        </div>

        {sentTo ? (
          <form onSubmit={handleConfirm} className="space-y-4">
            <div>
              <h1 className="text-xl font-semibold text-tumtum-white">Confere seu e-mail.</h1>
              <p className="mt-2 text-tumtum-white">
                Mandamos um código de 6 números pra <strong>{sentTo}</strong>. Ele vale por{' '}
                {minutes} minutos.
              </p>
            </div>
            <Input
              label="Código"
              type="text"
              inputMode="numeric"
              autoComplete="one-time-code"
              placeholder="000000"
              value={code}
              onChange={(e) => setCode(codeDigits(e.target.value))}
              required
            />
            <p className="text-sm text-tumtum-muted">Não chegou? Olha no spam e em Promoções.</p>

            {error && <p className="text-sm text-red-500">{error}</p>}
            {notice && <p className="text-sm text-tumtum-white">{notice}</p>}

            <Button type="submit" loading={loading} className="w-full">
              Confirmar e criar a conta
            </Button>
            <Button
              type="button"
              variant="secondary"
              className="w-full"
              disabled={loading || wait > 0}
              onClick={() => void sendCode(true)}
            >
              {wait > 0 ? `Mandar outro em ${wait} s` : 'Mandar outro código'}
            </Button>
            <Button
              type="button"
              variant="ghost"
              className="w-full"
              disabled={loading}
              onClick={fixEmail}
            >
              Corrigir o e-mail
            </Button>
          </form>
        ) : (
          <form onSubmit={handleStart} className="space-y-4">
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

            <p className="text-sm text-tumtum-muted">
              A gente manda um código de 6 números pro seu e-mail. A conta só é criada quando
              você digitar ele aqui.
            </p>

            {error && <p className="text-sm text-red-500">{error}</p>}

            <Button type="submit" loading={loading} disabled={mismatch} className="w-full">
              Mandar o código
            </Button>
          </form>
        )}

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
