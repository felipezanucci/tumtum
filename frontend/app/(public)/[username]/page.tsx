'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'

import { Wordmark } from '@/components/brand'
import { useParams } from 'next/navigation'
import { ApiError, users, type PublicProfile } from '@/lib/api'
import { Avatar, Card, Loading } from '@/components/ui'

export default function PublicProfilePage() {
  const params = useParams()
  const userId = params.username as string

  const [profile, setProfile] = useState<PublicProfile | null>(null)
  const [loading, setLoading] = useState(true)
  // Three outcomes, not two. "Não existe" and "não consegui perguntar" look
  // identical on screen and are completely different facts — the second one
  // rendered as the first is the app claiming something it never checked.
  const [missing, setMissing] = useState(false)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    async function load() {
      setLoading(true)
      try {
        const data = await users.getPublicProfile(userId)
        setProfile(data)
      } catch (err) {
        if (err instanceof ApiError && err.status === 404) {
          setMissing(true)
        } else {
          setFailed(true)
        }
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [userId])

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-tumtum-black">
        <Loading size="lg" />
      </main>
    )
  }

  if (failed) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
        <div className="max-w-sm text-center">
          <p className="text-lg text-tumtum-white">Não deu pra carregar</p>
          <p className="mt-1 text-sm text-tumtum-muted">
            A gente não conseguiu falar com o servidor agora. Isso não quer
            dizer que a página não exista — tenta de novo daqui a pouco.
          </p>
          <Link
            href="/"
            className="mt-6 inline-block text-sm text-tumtum-pink hover:underline"
          >
            Ir para a página inicial
          </Link>
        </div>
      </main>
    )
  }

  if (missing || !profile) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-tumtum-black px-4">
        <div className="max-w-sm text-center">
          <p className="text-lg text-tumtum-white">Nada por aqui</p>
          <p className="mt-1 text-sm text-tumtum-muted">
            Este endereço não é de ninguém — pode ser um perfil que não existe,
            ou uma página que a TumTum não tem.
          </p>
          <Link
            href="/"
            className="mt-6 inline-block text-sm text-tumtum-pink hover:underline"
          >
            Ir para a página inicial
          </Link>
        </div>
      </main>
    )
  }

  const memberSince = new Date(profile.created_at).toLocaleDateString('pt-BR', {
    month: 'long',
    year: 'numeric',
  })

  return (
    <main className="min-h-screen bg-tumtum-black">
      <div className="mx-auto max-w-md px-4 py-12">
        {/* Header */}
        <div className="flex flex-col items-center text-center">
          <Avatar name={profile.name} src={profile.avatar_url} size="lg" />
          <h1 className="mt-4 text-2xl font-bold text-tumtum-white">{profile.name}</h1>
          <p className="text-sm text-tumtum-muted">Membro desde {memberSince}</p>
        </div>

        {/* Stats */}
        <div className="mt-8 grid grid-cols-3 gap-3">
          {[
            { label: 'Sessões', value: profile.total_sessions },
            { label: 'Eventos', value: profile.total_events },
            { label: 'Cards', value: profile.total_cards },
          ].map(({ label, value }) => (
            <Card key={label} className="text-center">
              <p className="text-xl font-bold text-tumtum-white">{value}</p>
              <p className="text-xs text-tumtum-muted">{label}</p>
            </Card>
          ))}
        </div>

        {/* Branding */}
        <div className="mt-12 text-center">
          <Wordmark className="h-4 w-auto text-tumtum-white" />
          <p className="mt-1 text-xs text-tumtum-muted">
            Sinta o evento. Compartilhe a emoção.
          </p>
        </div>
      </div>
    </main>
  )
}
