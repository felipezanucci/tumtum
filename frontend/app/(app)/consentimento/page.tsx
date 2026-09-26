'use client'

import { Suspense, useEffect, useMemo, useState } from 'react'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'

import { users } from '@/lib/api'
import { CONSENT_PT, isConsentPurpose, type ConsentPurpose } from '@/lib/consent-copy'
import {
  changedChoices,
  choicesFrom,
  isAdult,
  latestAdultBirthDate,
  needsConsentGate,
  safeNext,
  UNDER_AGE_MESSAGE,
  type ConsentChoices,
} from '@/lib/consent'
import { useAuthStore } from '@/lib/stores/useAuthStore'
import { useConsentStore } from '@/lib/stores/useConsentStore'
import { useCurrentUser } from '@/lib/hooks/useCurrentUser'
import { Button, Input, Loading, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'
import { ConsentToggles } from '@/components/privacy'

const copy = CONSENT_PT

/**
 * /consentimento (26/09, LGPD remediation).
 *
 * Where a person decides, purpose by purpose, what the TumTum may do with
 * their heartbeat — and where every refusal of the server
 * (`consent_required`) sends them, with `?focus=` on the purpose it named.
 * The gate in the app layout sends here first anyone signed in without a
 * birth date or without the Terms accepted, and `?next=` takes them back.
 *
 * Optional purposes start off; nothing is sent until the button is pressed,
 * and only what changed is sent.
 */
export default function ConsentPage() {
  return (
    <Suspense
      fallback={
        <>
          <Nav />
          <main className="flex min-h-screen items-center justify-center bg-tumtum-black">
            <Loading size="lg" />
          </main>
        </>
      }
    >
      <ConsentScreen />
    </Suspense>
  )
}

function ConsentScreen() {
  const router = useRouter()
  const params = useSearchParams()
  const focusParam = params.get('focus')
  const focus: ConsentPurpose | null = isConsentPurpose(focusParam) ? focusParam : null
  const next = safeNext(params.get('next'))

  const token = useAuthStore((s) => s.token)
  const loadUser = useAuthStore((s) => s.loadUser)
  const user = useCurrentUser()
  const { userId, entries, status, error: loadError, load, save } = useConsentStore()

  const saved = useMemo(() => choicesFrom(entries), [entries])
  const [choices, setChoices] = useState<ConsentChoices>(saved)
  const [birthDate, setBirthDate] = useState('')
  const [maxBirthDate] = useState(() => latestAdultBirthDate())
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  // What the server holds is the starting point, every time it changes.
  useEffect(() => setChoices(saved), [saved])

  useEffect(() => {
    if (user && (userId !== user.id || status === 'idle')) void load(user.id)
  }, [user, userId, status, load])

  if (!token) {
    return (
      <>
        <Nav />
        <main className="min-h-screen bg-tumtum-black">
          <div className="mx-auto max-w-2xl px-4 py-8">
            <SignInRequired what="suas escolhas de privacidade" />
          </div>
        </main>
      </>
    )
  }

  const needsBirthDate = user !== null && !user.birth_date
  const changes = changedChoices(saved, choices)
  const hasChanges = Object.keys(changes).length > 0
  const canSave = !saving && status === 'loaded' && (hasChanges || (needsBirthDate && birthDate !== ''))

  async function handleSave() {
    setError(null)
    setNotice(null)
    if (needsBirthDate) {
      if (!birthDate) {
        setError('Falta a sua data de nascimento.')
        return
      }
      if (isAdult(birthDate) === false) {
        setError(UNDER_AGE_MESSAGE)
        return
      }
    }
    setSaving(true)
    try {
      if (needsBirthDate) {
        await users.updateProfile({ birth_date: birthDate })
        await loadUser()
      }
      const after = hasChanges ? await save(changes, 'tap') : entries
      const stillBlocked = needsConsentGate(useAuthStore.getState().user, after)
      if (stillBlocked) {
        setNotice('Salvo. Pra usar a TumTum, falta aceitar os Termos de Uso e a Política de Privacidade.')
      } else if (next) {
        router.replace(next)
      } else {
        setNotice(copy.saved)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não deu pra salvar. Tenta de novo.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-2xl px-4 py-10">
          <h1 className="text-3xl font-bold leading-tight text-tumtum-white">{copy.title}</h1>
          <p className="mt-3 leading-relaxed text-tumtum-muted">{copy.intro}</p>

          <ul className="mt-8 space-y-3">
            {copy.facts.map((fact) => (
              <li key={fact} className="flex gap-3 text-sm leading-relaxed text-[#CFCFCF]">
                <span aria-hidden="true" className="mt-[9px] h-1.5 w-1.5 shrink-0 rounded-full bg-tumtum-pink" />
                <span>{fact}</span>
              </li>
            ))}
          </ul>

          {needsBirthDate && (
            <div className="mt-10">
              <Input
                id="birth-date"
                label={copy.birthDate.label}
                type="date"
                value={birthDate}
                max={maxBirthDate}
                min="1900-01-01"
                onChange={(e) => setBirthDate(e.target.value)}
              />
              <p className="mt-1.5 text-xs text-tumtum-muted">{copy.birthDate.hint}</p>
            </div>
          )}

          <div className="mt-10">
            {status === 'failed' ? (
              <div className="rounded-lg border border-red-500/40 bg-red-500/10 p-4 text-sm text-red-400">
                <p>Não deu pra ler suas escolhas agora. {loadError}</p>
                <button
                  type="button"
                  onClick={() => user && void load(user.id)}
                  className="mt-2 text-tumtum-white underline underline-offset-2"
                >
                  Tentar de novo
                </button>
              </div>
            ) : status !== 'loaded' ? (
              <Loading className="py-10" />
            ) : (
              <ConsentToggles
                choices={choices}
                onChange={(purpose, granted) => {
                  setNotice(null)
                  setChoices((prev) => ({ ...prev, [purpose]: granted }))
                }}
                focus={focus}
                disabled={saving}
                lockedWhenOn={saved.terms ? ['terms'] : []}
              />
            )}
          </div>

          <p className="mt-6 text-sm text-tumtum-muted">
            Tudo em detalhe:{' '}
            <Link href="/privacidade" className="text-tumtum-pink underline underline-offset-2">
              {copy.privacyLink}
            </Link>{' '}
            e{' '}
            <Link href="/termos" className="text-tumtum-pink underline underline-offset-2">
              {copy.termsLink}
            </Link>
            .
          </p>

          {error && (
            <p role="alert" className="mt-6 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
              {error}
            </p>
          )}
          {notice && (
            <p role="status" className="mt-6 rounded-lg border border-tumtum-border bg-tumtum-surface p-3 text-sm text-tumtum-white">
              {notice}
            </p>
          )}

          <Button onClick={handleSave} loading={saving} disabled={!canSave} size="lg" className="mt-6 w-full">
            {saving ? copy.saving : copy.save}
          </Button>
          {!hasChanges && !(needsBirthDate && birthDate) && status === 'loaded' && (
            <p className="mt-2 text-center text-xs text-tumtum-muted">
              Nada mudou desde a última vez que você salvou.
            </p>
          )}
          <p className="mt-6 text-center text-xs text-tumtum-faint">{copy.version}</p>
        </div>
      </main>
    </>
  )
}
