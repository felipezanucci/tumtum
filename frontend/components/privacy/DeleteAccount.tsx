'use client'

import { useState } from 'react'

import { users } from '@/lib/api'
import { Button, PasswordInput } from '@/components/ui'

/**
 * "Apagar minha conta", on the site (26/09). The password is asked again: it
 * is the one act here with no way back, so it is never one tap from a
 * logged-in browser someone else is holding.
 */
export default function DeleteAccount({ onDeleted }: { onDeleted: () => void }) {
  const [open, setOpen] = useState(false)
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleDelete(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await users.deleteAccount(password)
      onDeleted()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não deu pra apagar agora. Tenta de novo.')
      setBusy(false)
    }
  }

  return (
    <div>
      <p className="text-sm leading-relaxed text-tumtum-muted">
        Some na hora: a conta, as noites e cada leitura, os momentos, os cards, os posts no feed, os
        consentimentos e os pedidos. Cards que você já postou em outras redes ficam lá; esses são
        seus para apagar lá.
      </p>
      {!open ? (
        <Button variant="danger" size="sm" className="mt-3" onClick={() => setOpen(true)}>
          Apagar minha conta
        </Button>
      ) : (
        <form onSubmit={handleDelete} className="mt-4 space-y-3">
          <PasswordInput
            label="Confirme com sua senha"
            id="delete-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
          {error && <p role="alert" className="text-sm text-red-400">{error}</p>}
          <div className="flex gap-2">
            <Button type="submit" variant="danger" size="sm" loading={busy} disabled={!password}>
              Apagar tudo, de vez
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              disabled={busy}
              onClick={() => {
                setOpen(false)
                setPassword('')
                setError(null)
              }}
            >
              Cancelar
            </Button>
          </div>
        </form>
      )}
    </div>
  )
}
