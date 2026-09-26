'use client'

import { useState } from 'react'

import { users, type UserProfile } from '@/lib/api'
import { codeDigits, isCompleteCode } from '@/lib/signup-code'
import { Button, Input, PasswordInput } from '@/components/ui'

/**
 * Correcting the e-mail (LGPD art. 18 III), in two steps like sign-up: the
 * password proves it is you, the code proves the new address is yours. The
 * account's e-mail changes only when the code comes back.
 */
export default function EmailChange({
  current,
  onChanged,
}: {
  current: string
  onChanged: (profile: UserProfile) => void
}) {
  const [open, setOpen] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [sentTo, setSentTo] = useState<string | null>(null)
  const [code, setCode] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  function reset() {
    setOpen(false)
    setEmail('')
    setPassword('')
    setSentTo(null)
    setCode('')
    setError(null)
  }

  async function handleStart(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setNotice(null)
    if (email.trim().toLowerCase() === current.toLowerCase()) {
      setError('Esse já é o e-mail da sua conta.')
      return
    }
    setBusy(true)
    try {
      const started = await users.changeEmail(email.trim(), password)
      setSentTo(started?.email ?? email.trim())
      setPassword('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não deu pra mandar o código.')
    } finally {
      setBusy(false)
    }
  }

  async function handleConfirm(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    if (!isCompleteCode(code)) {
      setError('O código tem 6 números.')
      return
    }
    setBusy(true)
    try {
      const profile = await users.confirmEmail(code)
      onChanged(profile)
      reset()
      setNotice(`Pronto. Sua conta agora usa ${profile.email}.`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não deu pra confirmar o código.')
    } finally {
      setBusy(false)
    }
  }

  if (!open) {
    return (
      <div>
        <p className="text-sm text-tumtum-muted">
          E-mail da conta: <span className="text-tumtum-white">{current}</span>
        </p>
        {notice && (
          <p role="status" className="mt-2 text-sm text-tumtum-white">
            {notice}
          </p>
        )}
        <Button size="sm" variant="secondary" className="mt-3" onClick={() => setOpen(true)}>
          Trocar e-mail
        </Button>
      </div>
    )
  }

  return sentTo ? (
    <form onSubmit={handleConfirm} className="space-y-3">
      <p className="text-sm text-tumtum-white">
        Mandamos um código de 6 números pra <strong>{sentTo}</strong>. Até ele voltar, a conta
        continua com {current}.
      </p>
      <Input
        label="Código"
        id="email-code"
        inputMode="numeric"
        autoComplete="one-time-code"
        placeholder="000000"
        value={code}
        onChange={(e) => setCode(codeDigits(e.target.value))}
        required
      />
      {error && <p role="alert" className="text-sm text-red-400">{error}</p>}
      <div className="flex gap-2">
        <Button type="submit" size="sm" loading={busy}>
          Confirmar
        </Button>
        <Button type="button" size="sm" variant="ghost" onClick={reset} disabled={busy}>
          Cancelar
        </Button>
      </div>
    </form>
  ) : (
    <form onSubmit={handleStart} className="space-y-3">
      <Input
        label="Novo e-mail"
        id="new-email"
        type="email"
        placeholder="seu@email.com"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        required
      />
      <PasswordInput
        label="Sua senha"
        id="email-password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        autoComplete="current-password"
        required
      />
      {error && <p role="alert" className="text-sm text-red-400">{error}</p>}
      <div className="flex gap-2">
        <Button type="submit" size="sm" loading={busy}>
          Mandar o código
        </Button>
        <Button type="button" size="sm" variant="ghost" onClick={reset} disabled={busy}>
          Cancelar
        </Button>
      </div>
    </form>
  )
}
