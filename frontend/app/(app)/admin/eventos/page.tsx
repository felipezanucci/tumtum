'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'

import { ApiError, events, type TumtumEvent } from '@/lib/api'
import { useCurrentUser } from '@/lib/hooks/useCurrentUser'
import { Badge, Loading, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'
import { formatDateOnly } from '@/lib/utils/dates'

/**
 * Operação › Eventos — the events TumTum covers, for the people who register
 * them.
 *
 * Events are TumTum's; the fan never creates one (product rule, 21/09). Until
 * 22/09 the only way to register one was the operator sheet on the phone,
 * with the web admin as open item 40. This is that admin: every event, one
 * tap from its timeline, and the door to a new one.
 *
 * Whether this account may operate is the server's call (`admin_emails`),
 * answered as `is_admin` on the account. The list itself is public, so a fan
 * who types the URL sees the same events they see on the app — the doors to
 * change anything are what this page hides, and the endpoints refuse anyway.
 */

const typeLabel: Record<string, string> = {
  concert: 'Show',
  sports: 'Jogo',
  festival: 'Festival',
}

const typeVariant: Record<string, 'default' | 'accent' | 'success'> = {
  concert: 'accent',
  sports: 'success',
  festival: 'default',
}

function hours(event: TumtumEvent): string {
  const start = event.start_time?.slice(0, 5)
  const end = event.end_time?.slice(0, 5)
  if (start && end) return `${start} – ${end}`
  if (start) return start
  return 'sem horário'
}

export default function AdminEventsPage() {
  const user = useCurrentUser()
  const [list, setList] = useState<TumtumEvent[] | null>(null)
  const [needsSignIn, setNeedsSignIn] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    events
      .list()
      .then(setList)
      .catch((err: unknown) => {
        if (err instanceof ApiError && err.status === 401) {
          setNeedsSignIn(true)
          return
        }
        setError(err instanceof Error ? err.message : 'Não foi possível carregar.')
      })
  }, [])

  const operator = user?.is_admin === true

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-2xl px-4 py-8">
          <p className="text-xs font-medium uppercase tracking-wider text-tumtum-muted">
            Operação
          </p>
          <div className="mt-1 flex items-center justify-between gap-4">
            <h1 className="text-3xl font-hero text-tumtum-white">Eventos</h1>
            {operator && (
              <Link
                href="/admin/eventos/novo"
                className="shrink-0 rounded-lg bg-tumtum-pink px-4 py-2 text-sm font-label text-tumtum-black transition-colors hover:bg-tumtum-yellow"
              >
                Novo evento
              </Link>
            )}
          </div>
          <p className="mt-2 text-sm text-tumtum-muted">
            O que a TumTum cobre. O fã escolhe daqui; ninguém além da operação cadastra.
          </p>

          {user && !operator && (
            <div className="mt-6 rounded-lg border border-tumtum-border bg-tumtum-surface p-4">
              <p className="font-headline text-tumtum-white">
                Sua conta não opera a plataforma.
              </p>
              <p className="mt-2 text-sm text-tumtum-muted">
                Cadastrar e corrigir eventos é liberado conta por conta, no servidor.
                A lista abaixo é a mesma que aparece no app.
              </p>
            </div>
          )}

          {needsSignIn && <SignInRequired what="a operação" />}

          {error && (
            <p className="mt-6 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
              {error}
            </p>
          )}

          {!list && !needsSignIn && !error && (
            <div className="mt-10 flex justify-center">
              <Loading />
            </div>
          )}

          {list && list.length === 0 && (
            <p className="mt-6 text-tumtum-muted">Nenhum evento cadastrado ainda.</p>
          )}

          {list && list.length > 0 && (
            <ul className="mt-6 space-y-2">
              {list.map((event) => (
                <li key={event.id}>
                  <Link
                    href={`/admin/eventos/${event.id}`}
                    className="block rounded-lg border border-tumtum-border bg-tumtum-surface px-4 py-3 transition-colors hover:border-tumtum-pink"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="truncate font-headline text-tumtum-white">{event.name}</p>
                        <p className="mt-1 text-xs text-tumtum-muted tabular-nums">
                          {formatDateOnly(event.date, {
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric',
                          })}
                          {' · '}
                          {hours(event)}
                          {event.venue ? ` · ${event.venue}` : ''}
                        </p>
                      </div>
                      <Badge variant={typeVariant[event.event_type] ?? 'default'}>
                        {typeLabel[event.event_type] ?? event.event_type}
                      </Badge>
                    </div>
                    {event.external_id && (
                      <p className="mt-1 text-xs text-tumtum-muted">
                        Linha do tempo: {event.external_id}
                      </p>
                    )}
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      </main>
    </>
  )
}
