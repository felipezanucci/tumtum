'use client'

import { useEffect, useState } from 'react'

import { users, type DataSubjectRequest } from '@/lib/api'
import {
  REQUEST_KINDS,
  REQUEST_KIND_LABELS,
  requestDueLine,
  requestKindLabel,
  requestStatusLabel,
  type RequestKind,
} from '@/lib/privacy-requests'
import { Badge, Button, Loading } from '@/components/ui'

/**
 * "Pedidos ao encarregado": any right the buttons above do not cover, asked
 * in writing, recorded, and answered within 15 days. The list below the form
 * is the receipt — each request with its due date and where it stands.
 */
export default function PrivacyRequests() {
  const [list, setList] = useState<DataSubjectRequest[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [kind, setKind] = useState<RequestKind>('access')
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  async function loadList() {
    setLoadError(null)
    try {
      const items = await users.requests.list()
      setList([...items].sort((a, b) => Date.parse(b.opened_at) - Date.parse(a.opened_at)))
    } catch (err) {
      setLoadError(err instanceof Error ? err.message : 'Não deu pra carregar seus pedidos.')
    }
  }

  useEffect(() => {
    void loadList()
  }, [])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setNotice(null)
    if (!message.trim()) {
      setError('Conta o que você precisa, em uma ou duas frases.')
      return
    }
    setSending(true)
    try {
      const created = await users.requests.create(kind, message.trim())
      setList((prev) => [created, ...(prev ?? [])])
      setMessage('')
      setNotice(`Pedido registrado. ${requestDueLine(created)}.`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não deu pra registrar o pedido.')
    } finally {
      setSending(false)
    }
  }

  return (
    <div>
      <p className="text-sm leading-relaxed text-tumtum-muted">
        Pra qualquer pedido sobre seus dados. O encarregado responde em até 15 dias. Se preferir
        e-mail: <a href="mailto:oi@tumtum.cc?subject=Privacidade" className="text-tumtum-pink underline underline-offset-2">oi@tumtum.cc</a>, assunto
        “Privacidade”.
      </p>

      <form onSubmit={handleSubmit} className="mt-4 space-y-3">
        <div>
          <label htmlFor="request-kind" className="mb-1.5 block text-sm font-medium text-tumtum-white">
            O que você quer
          </label>
          <select
            id="request-kind"
            value={kind}
            onChange={(e) => setKind(e.target.value as RequestKind)}
            className="w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2.5 text-tumtum-white focus:border-tumtum-pink focus:outline-none"
          >
            {REQUEST_KINDS.map((k) => (
              <option key={k} value={k}>
                {REQUEST_KIND_LABELS[k]}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="request-message" className="mb-1.5 block text-sm font-medium text-tumtum-white">
            Mensagem
          </label>
          <textarea
            id="request-message"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            rows={3}
            maxLength={2000}
            className="w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2.5 text-tumtum-white placeholder:text-tumtum-muted focus:border-tumtum-pink focus:outline-none"
            placeholder="Ex.: quero saber com quem meus dados foram compartilhados."
          />
        </div>
        {error && <p role="alert" className="text-sm text-red-400">{error}</p>}
        {notice && <p role="status" className="text-sm text-tumtum-white">{notice}</p>}
        <Button type="submit" size="sm" loading={sending}>
          Enviar pedido
        </Button>
      </form>

      <div className="mt-6">
        <p className="text-sm font-medium text-tumtum-white">Seus pedidos</p>
        {loadError ? (
          <p className="mt-2 text-sm text-red-400">
            {loadError}{' '}
            <button type="button" onClick={() => void loadList()} className="text-tumtum-white underline underline-offset-2">
              Tentar de novo
            </button>
          </p>
        ) : list === null ? (
          <Loading className="py-4" />
        ) : list.length === 0 ? (
          <p className="mt-2 text-sm text-tumtum-muted">Nenhum pedido ainda.</p>
        ) : (
          <ul className="mt-2 space-y-2">
            {list.map((item) => (
              <li key={item.id} className="rounded-lg border border-tumtum-border p-3">
                <div className="flex items-center justify-between gap-3">
                  <p className="text-sm font-medium text-tumtum-white">{requestKindLabel(item.kind)}</p>
                  <Badge variant={item.status === 'open' ? 'warning' : 'success'}>
                    {requestStatusLabel(item.status)}
                  </Badge>
                </div>
                <p className="mt-1 text-sm text-tumtum-muted">{item.message}</p>
                <p className="mt-1 text-xs text-tumtum-muted">{requestDueLine(item)}</p>
                {item.answer && (
                  <p className="mt-2 border-l-2 border-tumtum-pink pl-3 text-sm text-tumtum-white">{item.answer}</p>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
