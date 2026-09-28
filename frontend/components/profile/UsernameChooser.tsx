'use client'

import { useState } from 'react'

import { users, type UserProfile } from '@/lib/api'
import { Button, UsernameField } from '@/components/ui'
import { useUsernameCheck } from '@/lib/hooks/useUsernameCheck'
import { USERNAME_FIXED, isUsernameSentence } from '@/lib/username'

interface UsernameChooserProps {
  /**
   * The account now carries its @: the profile shows it fixed from here on.
   * `savedHere` is false when it had been set elsewhere (the app) meanwhile.
   */
  onSaved: (profile: UserProfile, savedHere: boolean) => void
}

/**
 * "Escolhe seu @" (28/09), once, for an account made before the server held
 * the @. The same live check as sign-up, and "Salvar @" only once the server
 * has said the name on screen is free. The server keeps the promise the line
 * under the button makes: an @ once set is refused as a change.
 */
export default function UsernameChooser({ onSaved }: UsernameChooserProps) {
  const [username, setUsername] = useState('')
  const check = useUsernameCheck(username)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function handleSave() {
    if (check.status.kind !== 'available') return
    setSaving(true)
    setError('')
    try {
      onSaved(await users.updateProfile({ username }), true)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Não deu pra salvar seu @.'
      if (message === USERNAME_FIXED) {
        // Set elsewhere (the app) since this page loaded: show the one it has.
        try {
          onSaved(await users.getProfile(), false)
        } catch {
          setError(message)
        }
      } else if (isUsernameSentence(message)) {
        // Taken in the meantime, or refused: said under the field.
        check.reject(username, message)
      } else {
        setError(message || 'Não deu pra salvar seu @.')
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="mt-4 max-w-xs space-y-3">
      <UsernameField
        label="Escolhe seu @"
        id="profile-username"
        value={username}
        onChange={(clean) => {
          setUsername(clean)
          setError('')
        }}
        status={check.status}
        onRetry={check.retry}
        disabled={saving}
      />
      <p className="text-xs text-tumtum-muted">{USERNAME_FIXED}</p>
      {error && <p className="text-sm text-red-500">{error}</p>}
      <Button
        size="sm"
        onClick={() => void handleSave()}
        loading={saving}
        disabled={saving || check.status.kind !== 'available'}
      >
        Salvar @
      </Button>
    </div>
  )
}
