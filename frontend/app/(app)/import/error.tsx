'use client'

import { useEffect } from 'react'
import { Button } from '@/components/ui'
import { Nav } from '@/components/layout'

export default function ImportError({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  useEffect(() => {
    console.error('Import page error:', error)
  }, [error])

  return (
    <>
      <Nav />
      <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
        <div className="max-w-md text-center">
          <div className="text-4xl">💔</div>
          <h1 className="mt-4 text-2xl font-bold text-tumtum-white">
            Algo deu errado na importação
          </h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            Não conseguimos processar esse arquivo. Tente novamente ou exporte os dados
            em outro formato.
          </p>
          <Button onClick={reset} className="mt-6">
            Tentar de novo
          </Button>
        </div>
      </main>
    </>
  )
}
