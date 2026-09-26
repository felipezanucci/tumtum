'use client'

import { useEffect, useMemo, useState } from 'react'

import { changedChoices, choicesFrom, type ConsentChoices } from '@/lib/consent'
import { useConsentStore } from '@/lib/stores/useConsentStore'
import { Button, Loading } from '@/components/ui'

import ConsentToggles from './ConsentToggles'

/**
 * The profile's consent switches: the same rows as /consentimento, saved
 * with their own button so a change is never sent by accident.
 */
export default function ConsentSettings({ userId }: { userId: string }) {
  const { userId: loadedFor, entries, status, error: loadError, load, save } = useConsentStore()
  const saved = useMemo(() => choicesFrom(entries), [entries])
  const [choices, setChoices] = useState<ConsentChoices>(saved)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  useEffect(() => setChoices(saved), [saved])
  useEffect(() => {
    if (loadedFor !== userId || status === 'idle') void load(userId)
  }, [userId, loadedFor, status, load])

  const changes = changedChoices(saved, choices)
  const hasChanges = Object.keys(changes).length > 0

  async function handleSave() {
    setSaving(true)
    setError(null)
    setNotice(null)
    try {
      await save(changes, 'tap')
      setNotice('Salvo. Vale a partir de agora.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não deu pra salvar. Tenta de novo.')
    } finally {
      setSaving(false)
    }
  }

  if (status === 'failed') {
    return (
      <div className="rounded-lg border border-red-500/40 bg-red-500/10 p-4 text-sm text-red-400">
        <p>Não deu pra ler suas escolhas agora. {loadError}</p>
        <button
          type="button"
          onClick={() => void load(userId)}
          className="mt-2 text-tumtum-white underline underline-offset-2"
        >
          Tentar de novo
        </button>
      </div>
    )
  }
  if (status !== 'loaded') return <Loading className="py-8" />

  return (
    <div>
      <ConsentToggles
        choices={choices}
        onChange={(purpose, granted) => {
          setNotice(null)
          setChoices((prev) => ({ ...prev, [purpose]: granted }))
        }}
        disabled={saving}
        lockedWhenOn={saved.terms ? ['terms'] : []}
      />
      {error && (
        <p role="alert" className="mt-4 text-sm text-red-400">
          {error}
        </p>
      )}
      {notice && (
        <p role="status" className="mt-4 text-sm text-tumtum-white">
          {notice}
        </p>
      )}
      <div className="mt-4 flex items-center gap-3">
        <Button onClick={handleSave} loading={saving} disabled={!hasChanges}>
          Salvar escolhas
        </Button>
        {!hasChanges && !notice && (
          <span className="text-xs text-tumtum-muted">Nada mudou ainda.</span>
        )}
      </div>
    </div>
  )
}
