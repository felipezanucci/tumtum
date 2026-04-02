'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useHRStore } from '@/lib/stores/useHRStore'
import { Button, Card } from '@/components/ui'

type Step = 'welcome' | 'wearable' | 'ready'

const wearableOptions = [
  { id: 'apple_health', name: 'Apple Watch', icon: '⌚', description: 'Via Apple HealthKit' },
  { id: 'google_fit', name: 'Wear OS / Android', icon: '📱', description: 'Via Google Health Connect' },
  { id: 'garmin', name: 'Garmin', icon: '🏃', description: 'Em breve' },
  { id: 'fitbit', name: 'Fitbit', icon: '💪', description: 'Em breve' },
]

export default function OnboardingPage() {
  const router = useRouter()
  const { connectWearable } = useHRStore()
  const [step, setStep] = useState<Step>('welcome')
  const [selectedProvider, setSelectedProvider] = useState<string | null>(null)
  const [connecting, setConnecting] = useState(false)

  async function handleConnect() {
    if (!selectedProvider) return
    setConnecting(true)
    try {
      // In Phase 0, we simulate the OAuth flow
      // Real implementation will redirect to provider's OAuth page
      await connectWearable(selectedProvider, 'placeholder-token')
      setStep('ready')
    } catch {
      // If connection fails, still allow user to proceed
      setStep('ready')
    } finally {
      setConnecting(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-tumtum-dark px-4">
      <div className="w-full max-w-md">
        {/* Step: Welcome */}
        {step === 'welcome' && (
          <div className="text-center">
            <div className="mb-6 text-6xl">❤️</div>
            <h1 className="text-3xl font-bold text-tumtum-text-primary">
              Bem-vindo ao Tumtum
            </h1>
            <p className="mt-3 text-tumtum-text-muted">
              Descubra como seu coração reage nos momentos mais emocionantes.
              Conecte seu wearable, vá a um evento e veja sua experiência.
            </p>
            <Button onClick={() => setStep('wearable')} className="mt-8 w-full" size="lg">
              Começar
            </Button>
          </div>
        )}

        {/* Step: Connect Wearable */}
        {step === 'wearable' && (
          <div>
            <h2 className="mb-2 text-2xl font-bold text-tumtum-text-primary">
              Conecte seu dispositivo
            </h2>
            <p className="mb-6 text-sm text-tumtum-text-muted">
              Escolha o wearable que você usa para monitorar sua frequência cardíaca.
            </p>

            <div className="space-y-3">
              {wearableOptions.map((option) => {
                const isDisabled = option.id === 'garmin' || option.id === 'fitbit'
                const isSelected = selectedProvider === option.id

                return (
                  <button
                    key={option.id}
                    disabled={isDisabled}
                    onClick={() => setSelectedProvider(option.id)}
                    className={`flex w-full items-center gap-4 rounded-xl border p-4 text-left transition-colors ${
                      isSelected
                        ? 'border-tumtum-red bg-tumtum-red/10'
                        : isDisabled
                        ? 'border-tumtum-border opacity-40 cursor-not-allowed'
                        : 'border-tumtum-border hover:border-tumtum-text-muted'
                    }`}
                  >
                    <span className="text-2xl">{option.icon}</span>
                    <div>
                      <p className="font-medium text-tumtum-text-primary">{option.name}</p>
                      <p className="text-xs text-tumtum-text-muted">{option.description}</p>
                    </div>
                  </button>
                )
              })}
            </div>

            <div className="mt-6 flex gap-3">
              <Button variant="ghost" onClick={() => setStep('ready')} className="flex-1">
                Pular
              </Button>
              <Button
                onClick={handleConnect}
                disabled={!selectedProvider}
                loading={connecting}
                className="flex-1"
              >
                Conectar
              </Button>
            </div>
          </div>
        )}

        {/* Step: Ready */}
        {step === 'ready' && (
          <div className="text-center">
            <div className="mb-6 text-6xl">🎉</div>
            <h2 className="text-2xl font-bold text-tumtum-text-primary">Tudo pronto!</h2>
            <p className="mt-3 text-tumtum-text-muted">
              Agora é só ir a um evento e depois voltar aqui para ver como seu coração reagiu.
            </p>
            <Button onClick={() => router.push('/events')} className="mt-8 w-full" size="lg">
              Explorar eventos
            </Button>
          </div>
        )}

        {/* Progress dots */}
        <div className="mt-8 flex justify-center gap-2">
          {(['welcome', 'wearable', 'ready'] as Step[]).map((s) => (
            <div
              key={s}
              className={`h-2 w-2 rounded-full transition-colors ${
                step === s ? 'bg-tumtum-red' : 'bg-tumtum-border'
              }`}
            />
          ))}
        </div>
      </div>
    </main>
  )
}
