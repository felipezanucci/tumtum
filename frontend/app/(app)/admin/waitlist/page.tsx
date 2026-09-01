'use client'

import { useEffect, useState } from 'react'

import { ApiError, waitlist, type WaitlistEntry } from '@/lib/api'
import { Loading, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'
import { toCsv } from '@/lib/utils/csv'

/**
 * The waitlist, readable.
 *
 * The API endpoint existed before this page did, and that was a gap rather
 * than a stage: reading it needs an Authorization header, so the one person
 * the list is for could not open it. A feature its only intended user cannot
 * reach is not shipped.
 *
 * Access is decided by the API, not here — the server checks the account
 * against its `waitlist_admin_emails` setting and answers 403 otherwise. This
 * page only has to explain that honestly instead of showing an empty table,
 * which would say "nobody signed up" when it means "you may not look".
 */

/** Empty for the entries collected before the form asked for a name. */
function fullName(entry: WaitlistEntry): string {
  return [entry.first_name, entry.last_name].filter(Boolean).join(' ')
}

function formatWhen(iso: string): string {
  const parsed = new Date(iso)
  if (Number.isNaN(parsed.getTime())) return iso
  return parsed.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function WaitlistAdminPage() {
  const [entries, setEntries] = useState<WaitlistEntry[] | null>(null)
  const [denied, setDenied] = useState(false)
  const [needsSignIn, setNeedsSignIn] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    waitlist
      .list()
      .then(setEntries)
      .catch((err: unknown) => {
        if (err instanceof ApiError && err.status === 403) {
          setDenied(true)
          return
        }
        if (err instanceof ApiError && err.status === 401) {
          setNeedsSignIn(true)
          return
        }
        setError(err instanceof Error ? err.message : 'Não foi possível carregar.')
      })
  }, [])

  function download() {
    if (!entries?.length) return
    // A data: URL rather than a blob: the list is small, and this needs no
    // cleanup of an object URL that would otherwise leak on every click.
    const link = document.createElement('a')
    const csv = toCsv(
      ['nome', 'sobrenome', 'email', 'origem', 'cadastrado_em'],
      entries.map((entry) => [
        entry.first_name ?? '',
        entry.last_name ?? '',
        entry.email,
        entry.source ?? '',
        entry.created_at,
      ]),
    )
    link.href = `data:text/csv;charset=utf-8,${encodeURIComponent(csv)}`
    link.download = 'tumtum-lista-de-espera.csv'
    link.click()
  }

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-2xl px-4 py-8">
          <h1 className="text-3xl font-hero text-tumtum-white">Lista de espera</h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            Quem pediu pra ser avisado, o mais recente primeiro.
          </p>

          {denied && (
            <div className="mt-6 rounded-lg border border-tumtum-border bg-tumtum-surface p-4">
              <p className="font-headline text-tumtum-white">
                Sua conta não tem acesso a esta lista.
              </p>
              <p className="mt-2 text-sm text-tumtum-muted">
                São dados de contato de outras pessoas, então o acesso é
                liberado conta por conta, no servidor. Isso não quer dizer que a
                lista esteja vazia — quer dizer que ela não é sua pra ver.
              </p>
            </div>
          )}

          {needsSignIn && <SignInRequired what="a lista" />}

          {error && (
            <p className="mt-6 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
              {error}
            </p>
          )}

          {!entries && !denied && !needsSignIn && !error && (
            <div className="mt-10 flex justify-center">
              <Loading />
            </div>
          )}

          {entries && entries.length === 0 && (
            <p className="mt-6 text-tumtum-muted">
              Ninguém ainda. O formulário fica no rodapé de tumtum.cc.
            </p>
          )}

          {entries && entries.length > 0 && (
            <>
              <div className="mt-6 flex items-center justify-between gap-4">
                <p className="text-tumtum-white">
                  <span className="font-hero text-2xl text-tumtum-pink tabular-nums">
                    {entries.length}
                  </span>{' '}
                  <span className="text-sm text-tumtum-muted">
                    {entries.length === 1 ? 'pessoa' : 'pessoas'}
                  </span>
                </p>
                <button
                  type="button"
                  onClick={download}
                  className="rounded-lg bg-tumtum-pink px-4 py-2 text-sm font-headline text-tumtum-black transition-colors hover:bg-tumtum-yellow"
                >
                  Baixar CSV
                </button>
              </div>

              <ul className="mt-4 space-y-2">
                {entries.map((entry) => (
                  <li
                    key={entry.email}
                    className="rounded-lg border border-tumtum-border bg-tumtum-surface px-4 py-3"
                  >
                    {fullName(entry) && (
                      <p className="font-headline text-tumtum-white">
                        {fullName(entry)}
                      </p>
                    )}
                    <p className="break-all font-label text-tumtum-muted">
                      {entry.email}
                    </p>
                    <p className="mt-1 text-xs text-tumtum-muted tabular-nums">
                      {formatWhen(entry.created_at)}
                      {entry.source ? ` · ${entry.source}` : ''}
                    </p>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>
      </main>
    </>
  )
}
