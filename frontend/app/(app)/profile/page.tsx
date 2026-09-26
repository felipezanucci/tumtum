'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/stores/useAuthStore'
import { useConsentStore } from '@/lib/stores/useConsentStore'
import { useHRStore } from '@/lib/stores/useHRStore'
import { ApiError, users, cards, type UserProfile, type CardData } from '@/lib/api'
import { Avatar, Button, Card, Input, Loading, Badge, SignInRequired } from '@/components/ui'
import { Nav } from '@/components/layout'
import {
  ConsentSettings,
  DataDownload,
  DeleteAccount,
  EmailChange,
  PrivacyRequests,
} from '@/components/privacy'

export default function ProfilePage() {
  const router = useRouter()
  const { logout } = useAuthStore()
  const resetConsents = useConsentStore((s) => s.reset)
  const { connections, loadConnections } = useHRStore()

  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [recentCards, setRecentCards] = useState<CardData[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<Error | null>(null)
  const [editing, setEditing] = useState(false)
  const [editName, setEditName] = useState('')
  const [deleted, setDeleted] = useState(false)

  useEffect(() => {
    loadData()
    loadConnections().catch(() => undefined)
  }, [loadConnections])

  async function loadData() {
    setLoading(true)
    setLoadError(null)
    try {
      const [profileData, cardsData] = await Promise.all([
        users.getProfile(),
        cards.list(),
      ])
      setProfile(profileData)
      setRecentCards(cardsData.slice(0, 6))
      setEditName(profileData.name)
    } catch (err) {
      // A blank page here read as "no profile"; it was a request that failed.
      setLoadError(err instanceof Error ? err : new Error('Não deu pra carregar seu perfil.'))
    } finally {
      setLoading(false)
    }
  }

  function handleDeleted() {
    logout()
    resetConsents()
    setDeleted(true)
  }

  async function handleSave() {
    if (!editName.trim()) return
    const updated = await users.updateProfile({ name: editName.trim() })
    setProfile(updated)
    setEditing(false)
  }

  function handleLogout() {
    logout()
    router.push('/')
  }

  if (loading) {
    return (
      <>
        <Nav />
        <main className="flex min-h-screen items-center justify-center bg-tumtum-black">
          <Loading size="lg" />
        </main>
      </>
    )
  }

  if (deleted) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
        <div className="max-w-sm text-center">
          <h1 className="text-2xl font-bold text-tumtum-white">Sua conta foi apagada.</h1>
          <p className="mt-3 text-sm leading-relaxed text-tumtum-muted">
            A conta, as noites, os momentos e os cards saíram dos servidores da TumTum. Este
            navegador também saiu da conta.
          </p>
          <Link href="/" className="mt-6 inline-block text-sm text-tumtum-pink hover:underline">
            Ir para a página inicial
          </Link>
        </div>
      </main>
    )
  }

  if (loadError || !profile) {
    return (
      <>
        <Nav />
        <main className="min-h-screen bg-tumtum-black">
          <div className="mx-auto max-w-3xl px-4 py-8">
            {loadError instanceof ApiError && loadError.status === 401 ? (
              <SignInRequired what="seu perfil" />
            ) : (
              <p className="rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-400">
                {loadError?.message ?? 'Não deu pra carregar seu perfil.'}{' '}
                <button type="button" onClick={() => void loadData()} className="text-tumtum-white underline underline-offset-2">
                  Tentar de novo
                </button>
              </p>
            )}
          </div>
        </main>
      </>
    )
  }

  const memberSince = new Date(profile.created_at).toLocaleDateString('pt-BR', {
    month: 'long',
    year: 'numeric',
  })

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-black">
        <div className="mx-auto max-w-3xl px-4 py-8">
          {/* Header */}
          <div className="flex items-start gap-6">
            <Avatar name={profile.name} src={profile.avatar_url} size="lg" />
            <div className="flex-1">
              {editing ? (
                <div className="flex items-center gap-3">
                  <Input
                    value={editName}
                    onChange={(e) => setEditName(e.target.value)}
                    className="max-w-xs"
                  />
                  <Button size="sm" onClick={handleSave}>Salvar</Button>
                  <Button size="sm" variant="ghost" onClick={() => setEditing(false)}>Cancelar</Button>
                </div>
              ) : (
                <div className="flex items-center gap-3">
                  <h1 className="text-2xl font-bold text-tumtum-white">{profile.name}</h1>
                  <Button size="sm" variant="ghost" onClick={() => setEditing(true)}>Editar</Button>
                </div>
              )}
              <p className="text-sm text-tumtum-muted">{profile.email}</p>
              <p className="text-xs text-tumtum-muted">Membro desde {memberSince}</p>
            </div>
          </div>

          {/* Stats */}
          <div className="mt-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
            {[
              // A count with no list is invisible state; Sessões opens its own.
              { label: 'Sessões', value: profile.total_sessions, href: '/sessions' },
              { label: 'Eventos', value: profile.total_events },
              { label: 'Cards', value: profile.total_cards, href: '/cards' },
              { label: 'Maior BPM', value: profile.highest_bpm ?? '—' },
            ].map(({ label, value, href }) => (
              <Card
                key={label}
                className={`text-center ${href ? 'cursor-pointer transition-colors hover:border-tumtum-pink/50' : ''}`}
                onClick={href ? () => router.push(href) : undefined}
              >
                <p className="text-2xl font-bold text-tumtum-white">{value}</p>
                <p className="text-xs text-tumtum-muted">{label}</p>
              </Card>
            ))}
          </div>

          {/* Wearables */}
          <div className="mt-8">
            <h2 className="mb-4 text-lg font-semibold text-tumtum-white">
              Dispositivos conectados
            </h2>
            {connections.length === 0 ? (
              <Card>
                <p className="text-sm text-tumtum-muted">
                  Nenhum dispositivo conectado.{' '}
                  {/* The site cannot connect a watch (25/09): the app records the night. */}
                  <button
                    onClick={() => router.push('/onboarding')}
                    className="text-tumtum-pink hover:underline"
                  >
                    Como gravar uma noite
                  </button>
                </p>
              </Card>
            ) : (
              <div className="space-y-3">
                {connections.map((conn) => (
                  <Card key={conn.id} className="flex items-center justify-between">
                    <div>
                      <p className="font-medium text-tumtum-white capitalize">
                        {conn.provider.replace('_', ' ')}
                      </p>
                      {conn.last_sync_at && (
                        <p className="text-xs text-tumtum-muted">
                          Última sincronia: {new Date(conn.last_sync_at).toLocaleDateString('pt-BR')}
                        </p>
                      )}
                    </div>
                    <Badge variant={conn.status === 'active' ? 'success' : 'warning'}>
                      {conn.status === 'active' ? 'Ativo' : conn.status}
                    </Badge>
                  </Card>
                ))}
              </div>
            )}
          </div>

          {/* Recent Cards */}
          {recentCards.length > 0 && (
            <div className="mt-8">
              <div className="mb-4 flex items-center justify-between">
                <h2 className="text-lg font-semibold text-tumtum-white">Cards recentes</h2>
                <Button size="sm" variant="ghost" onClick={() => router.push('/cards')}>
                  Ver todos
                </Button>
              </div>
              <div className="grid grid-cols-3 gap-3">
                {recentCards.map((card) => (
                  <div key={card.id} className="overflow-hidden rounded-lg border border-tumtum-border">
                    {/* Served only once the card is public (26/09). */}
                    {card.image_url && card.published_at ? (
                      <img
                        src={cards.getImageUrl(card.id)}
                        alt="Card"
                        className="w-full"
                      />
                    ) : (
                      <div className="flex aspect-[9/16] flex-col items-center justify-center bg-tumtum-surface text-center">
                        <span className="text-2xl font-hero text-tumtum-pink">
                          {(card.metadata as { peak_bpm?: number } | null)?.peak_bpm ?? '—'}
                        </span>
                        <span className="mt-1 text-[10px] uppercase tracking-wider text-tumtum-muted">
                          {card.published_at ? 'sem imagem' : 'só você vê'}
                        </span>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Privacidade (26/09): every right the law gives, one section. */}
          <section id="privacidade" className="mt-12 border-t border-tumtum-border pt-8">
            <h2 className="text-2xl font-bold text-tumtum-white">Privacidade</h2>
            <p className="mt-2 text-sm leading-relaxed text-tumtum-muted">
              Seus batimentos são seus. Aqui você decide o que a TumTum pode fazer com eles, leva
              seus dados, corrige, pede e apaga. A TumTum não é um dispositivo médico e não
              interpreta saúde.{' '}
              <Link href="/privacidade" className="text-tumtum-pink underline underline-offset-2">
                Política de Privacidade
              </Link>
            </p>

            <PrivacyBlock title="O que a TumTum pode fazer">
              <ConsentSettings userId={profile.id} />
            </PrivacyBlock>

            <PrivacyBlock title="Seu e-mail">
              <EmailChange current={profile.email} onChanged={setProfile} />
            </PrivacyBlock>

            <PrivacyBlock title="Baixar meus dados">
              <DataDownload />
            </PrivacyBlock>

            <PrivacyBlock title="Pedidos ao encarregado">
              <PrivacyRequests />
            </PrivacyBlock>

            <PrivacyBlock title="Apagar minha conta">
              <DeleteAccount onDeleted={handleDeleted} />
            </PrivacyBlock>
          </section>

          {/* Actions */}
          <div className="mt-10 border-t border-tumtum-border pt-6">
            <Button variant="secondary" onClick={handleLogout}>Sair da conta</Button>
          </div>
        </div>
      </main>
    </>
  )
}

function PrivacyBlock({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="mt-8">
      <h3 className="mb-3 text-lg font-semibold text-tumtum-white">{title}</h3>
      {children}
    </div>
  )
}
