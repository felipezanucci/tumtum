'use client'

import { useEffect, useMemo, useState } from 'react'

import { changedChoices, choicesFrom, consentNotes, type ConsentChoices } from '@/lib/consent'
import type { ConsentPurpose } from '@/lib/consent-copy'
import { useConsentStore } from '@/lib/stores/useConsentStore'
import { Button, Loading } from '@/components/ui'

import ConsentToggles from './ConsentToggles'

/**
 * The profile's consent switches: the same rows as /consentimento, saved
 * with their own button so a change is never sent by accident.
 *
 * After a save each switch that changed says what happened under itself
 * (28/09) — "Ligado.", or what stops — instead of one generic "Salvo". A
 * purpose whose off state has consequences keeps its line while it is off,
 * on every load, and a line that no longer matches the switch (a change not
 * saved yet) is not shown.
 */
export default function ConsentSettings({ userId }: { userId: string }) {
  const { userId: loadedFor, entries, status, error: loadError, load, save } = useConsentStore()
  const saved = useMemo(() => choicesFrom(entries), [entries])
  const [choices, setChoices] = useState<ConsentChoices>(saved)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [justSaved, setJustSaved] = useState<Partial<ConsentChoices>>({})

  useEffect(() => setChoices(saved), [saved])
  useEffect(() => {
    if (loadedFor !== userId || status === 'idle') void load(userId)
  }, [userId, loadedFor, status, load])

  const changes = changedChoices(saved, choices)
  const hasChanges = Object.keys(changes).length > 0
  const notes = useMemo(() => {
    const all = consentNotes(saved, justSaved)
    for (const purpose of Object.keys(all) as ConsentPurpose[]) {
      if (choices[purpose] !== saved[purpose]) delete all[purpose]
    }
    return all
  }, [saved, justSaved, choices])

  async function handleSave() {
    setSaving(true)
    setError(null)
    setJustSaved({})
    const sending = changes
    try {
      await save(sending, 'tap')
      setJustSaved(sending)
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
          setJustSaved({})
          setChoices((prev) => ({ ...prev, [purpose]: granted }))
        }}
        disabled={saving}
        lockedWhenOn={saved.terms ? ['terms'] : []}
        notes={notes}
      />
      {error && (
        <p role="alert" className="mt-4 text-sm text-red-400">
          {error}
        </p>
      )}
      <div className="mt-4 flex items-center gap-3">
        <Button onClick={handleSave} loading={saving} disabled={!hasChanges}>
          Salvar escolhas
        </Button>
        {!hasChanges && Object.keys(justSaved).length === 0 && (
          <span className="text-xs text-tumtum-muted">Nada mudou ainda.</span>
        )}
      </div>
    </div>
  )
}
