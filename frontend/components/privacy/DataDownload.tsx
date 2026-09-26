'use client'

import { useState } from 'react'

import { users } from '@/lib/api'
import { Button } from '@/components/ui'

/**
 * "Baixar meus dados" (LGPD art. 18 II and V): everything the account holds
 * as JSON, or just the readings as CSV. The file lands in the browser's
 * downloads, and the line under the buttons says which one did.
 */
export default function DataDownload() {
  const [busy, setBusy] = useState<'json' | 'csv' | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function run(kind: 'json' | 'csv') {
    setBusy(kind)
    setNotice(null)
    setError(null)
    try {
      const name = kind === 'json' ? await users.exportJson() : await users.exportCsv()
      setNotice(`Baixado: ${name}. Está na pasta de downloads do seu navegador.`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não deu pra baixar agora.')
    } finally {
      setBusy(null)
    }
  }

  return (
    <div>
      <p className="text-sm leading-relaxed text-tumtum-muted">
        O arquivo completo (JSON) tem sua conta, seus consentimentos, suas noites com cada leitura
        guardada, momentos, cards, posts, reações, bloqueios, denúncias e pedidos. O CSV tem só as
        leituras: noite, hora e batida.
      </p>
      <div className="mt-3 flex flex-wrap gap-2">
        <Button size="sm" variant="secondary" loading={busy === 'json'} disabled={busy !== null} onClick={() => void run('json')}>
          Baixar tudo (JSON)
        </Button>
        <Button size="sm" variant="secondary" loading={busy === 'csv'} disabled={busy !== null} onClick={() => void run('csv')}>
          Baixar leituras (CSV)
        </Button>
      </div>
      {notice && <p role="status" className="mt-3 text-sm text-tumtum-white">{notice}</p>}
      {error && <p role="alert" className="mt-3 text-sm text-red-400">{error}</p>}
    </div>
  )
}
