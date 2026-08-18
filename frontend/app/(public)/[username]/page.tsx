'use client'

import { useEffect, useState } from 'react'
import { useParams } from 'next/navigation'
import { users, type PublicProfile } from '@/lib/api'
import { Avatar, Card, Loading } from '@/components/ui'

export default function PublicProfilePage() {
  const params = useParams()
  const userId = params.username as string

  const [profile, setProfile] = useState<PublicProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    async function load() {
      setLoading(true)
      try {
        const data = await users.getPublicProfile(userId)
        setProfile(data)
      } catch {
        setError(true)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [userId])

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-tumtum-dark">
        <Loading size="lg" />
      </main>
    )
  }

  if (error || !profile) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-tumtum-dark">
        <div className="text-center">
          <p className="text-lg text-tumtum-text-primary">Perfil não encontrado</p>
          <p className="mt-1 text-sm text-tumtum-text-muted">Este usuário não existe ou o perfil é privado.</p>
        </div>
      </main>
    )
  }

  const memberSince = new Date(profile.created_at).toLocaleDateString('pt-BR', {
    month: 'long',
    year: 'numeric',
  })

  return (
    <main className="min-h-screen bg-tumtum-dark">
      <div className="mx-auto max-w-md px-4 py-12">
        {/* Header */}
        <div className="flex flex-col items-center text-center">
          <Avatar name={profile.name} src={profile.avatar_url} size="lg" />
          <h1 className="mt-4 text-2xl font-bold text-tumtum-text-primary">{profile.name}</h1>
          <p className="text-sm text-tumtum-text-muted">Membro desde {memberSince}</p>
        </div>

        {/* Stats */}
        <div className="mt-8 grid grid-cols-3 gap-3">
          {[
            { label: 'Sessões', value: profile.total_sessions },
            { label: 'Eventos', value: profile.total_events },
            { label: 'Cards', value: profile.total_cards },
          ].map(({ label, value }) => (
            <Card key={label} className="text-center">
              <p className="text-xl font-bold text-tumtum-text-primary">{value}</p>
              <p className="text-xs text-tumtum-text-muted">{label}</p>
            </Card>
          ))}
        </div>

        {/* Branding */}
        <div className="mt-12 text-center">
          <p
            className="text-sm font-bold uppercase tracking-widest text-tumtum-red"
            style={{ fontFamily: 'Georgia, serif' }}
          >
            Tumtum
          </p>
          <p className="mt-1 text-xs text-tumtum-text-muted">
            Sinta o evento. Compartilhe a emoção.
          </p>
        </div>
      </div>
    </main>
  )
}
