'use client'

import { useRouter } from 'next/navigation'
import { Wordmark } from '@/components/brand'
import { Button } from '@/components/ui'

/**
 * The first screen after a new account on the site (25/09, rebuilt).
 *
 * It used to open on a ❤️, ask to "conectar seu wearable" from a list with
 * fitness emojis, and "connect" a watch by saving a row with the token
 * `placeholder-token` — a Phase-0 simulation never removed. When that failed it
 * went on to "Tudo pronto!" anyway. The site cannot read a watch at all: the
 * night is recorded by the app, on the phone. So this says that, and nothing
 * the account cannot do.
 *
 * The brand manual's NEVER list forbids heartbeat imagery as decoration and the
 * wearable/healthtech vocabulary; the wordmark is the official asset.
 */
export default function OnboardingPage() {
  const router = useRouter()

  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
      {/* Centred, like the sign-up it follows (Felipe, 25/09). */}
      <div className="w-full max-w-md text-center">
        <Wordmark className="mx-auto block h-8 w-auto text-tumtum-white" />

        <h1 className="mt-10 text-3xl font-bold text-tumtum-white">Sua conta está pronta.</h1>

        {/* The watch first: the fan's device is a watch (Felipe, 25/09). */}
        <p className="mt-4 text-tumtum-white">
          Quem grava a noite é o app da TumTum, no seu celular, com o relógio que você já usa.
        </p>
        <p className="mt-3 text-tumtum-muted">
          Aqui no site você vê os eventos e, depois do show, as suas noites. Entra no app com
          esta mesma conta.
        </p>
        <p className="mt-3 text-sm text-tumtum-muted">
          O app está em teste fechado, só pra Android por enquanto.
        </p>

        <Button onClick={() => router.push('/events')} className="mt-10 w-full" size="lg">
          Ver os eventos
        </Button>
      </div>
    </main>
  )
}
