'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuthStore } from '@/lib/stores/useAuthStore'
import { useHRStore } from '@/lib/stores/useHRStore'
import { users, cards, type UserProfile, type CardData } from '@/lib/api'
import { Avatar, Button, Card, Input, Loading, Badge } from '@/components/ui'
import { Nav } from '@/components/layout'

export default function ProfilePage() {
  const router = useRouter()
  const { user, logout } = useAuthStore()
  const { connections, loadConnections } = useHRStore()

  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [recentCards, setRecentCards] = useState<CardData[]>([])
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [editName, setEditName] = useState('')

  useEffect(() => {
    loadData()
    loadConnections()
  }, [loadConnections])

  async function loadData() {
    setLoading(true)
    try {
      const [profileData, cardsData] = await Promise.all([
        users.getProfile(),
        cards.list(),
      ])
      setProfile(profileData)
      setRecentCards(cardsData.slice(0, 6))
      setEditName(profileData.name)
    } finally {
      setLoading(false)
    }
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
        <main className="flex min-h-screen items-center justify-center bg-tumtum-dark">
          <Loading size="lg" />
        </main>
      </>
    )
  }

  if (!profile) return null

  const memberSince = new Date(profile.created_at).toLocaleDateString('pt-BR', {
    month: 'long',
    year: 'numeric',
  })

  return (
    <>
      <Nav />
      <main className="min-h-screen bg-tumtum-dark">
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
                  <h1 className="text-2xl font-bold text-tumtum-text-primary">{profile.name}</h1>
                  <Button size="sm" variant="ghost" onClick={() => setEditing(true)}>Editar</Button>
                </div>
              )}
              <p className="text-sm text-tumtum-text-muted">{profile.email}</p>
              <p className="text-xs text-tumtum-text-muted">Membro desde {memberSince}</p>
            </div>
          </div>

          {/* Stats */}
          <div className="mt-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
            {[
              { label: 'Sessões', value: profile.total_sessions },
              { label: 'Eventos', value: profile.total_events },
              { label: 'Cards', value: profile.total_cards },
              { label: 'Maior BPM', value: profile.highest_bpm ?? '—' },
            ].map(({ label, value }) => (
              <Card key={label} className="text-center">
                <p className="text-2xl font-bold text-tumtum-text-primary">{value}</p>
                <p className="text-xs text-tumtum-text-muted">{label}</p>
              </Card>
            ))}
          </div>

          {/* Wearables */}
          <div className="mt-8">
            <h2 className="mb-4 text-lg font-semibold text-tumtum-text-primary">
              Dispositivos conectados
            </h2>
            {connections.length === 0 ? (
              <Card>
                <p className="text-sm text-tumtum-text-muted">
                  Nenhum dispositivo conectado.{' '}
                  <button
                    onClick={() => router.push('/onboarding')}
                    className="text-tumtum-red hover:underline"
                  >
                    Conectar agora
                  </button>
                </p>
              </Card>
            ) : (
              <div className="space-y-3">
                {connections.map((conn) => (
                  <Card key={conn.id} className="flex items-center justify-between">
                    <div>
                      <p className="font-medium text-tumtum-text-primary capitalize">
                        {conn.provider.replace('_', ' ')}
                      </p>
                      {conn.last_sync_at && (
                        <p className="text-xs text-tumtum-text-muted">
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
                <h2 className="text-lg font-semibold text-tumtum-text-primary">Cards recentes</h2>
                <Button size="sm" variant="ghost" onClick={() => router.push('/cards')}>
                  Ver todos
                </Button>
              </div>
              <div className="grid grid-cols-3 gap-3">
                {recentCards.map((card) => (
                  <div key={card.id} className="overflow-hidden rounded-lg border border-tumtum-border">
                    {card.image_url ? (
                      <img
                        src={cards.getImageUrl(card.id)}
                        alt="Card"
                        className="w-full"
                      />
                    ) : (
                      <div className="flex aspect-[9/16] items-center justify-center bg-tumtum-surface text-tumtum-text-muted">
                        🃏
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Actions */}
          <div className="mt-10 border-t border-tumtum-border pt-6">
            <Button variant="danger" onClick={handleLogout}>Sair da conta</Button>
          </div>
        </div>
      </main>
    </>
  )
}
