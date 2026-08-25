'use client'

import { useEffect } from 'react'
import { Button } from '@/components/ui'
import { Nav } from '@/components/layout'

export default function LiveError({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  useEffect(() => {
    console.error('Live capture error:', error)
  }, [error])

  return (
    <>
      <Nav />
      <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
        <div className="max-w-md text-center">
          <div className="text-4xl">💔</div>
          <h1 className="mt-4 text-2xl font-bold text-tumtum-white">
            A captura foi interrompida
          </h1>
          <p className="mt-2 text-sm text-tumtum-muted">
            Se você já tinha leituras gravadas, elas ficam salvas e aparecem para
            recuperar ao voltar para esta tela.
          </p>
          <Button onClick={reset} className="mt-6">
            Tentar de novo
          </Button>
        </div>
      </main>
    </>
  )
}
