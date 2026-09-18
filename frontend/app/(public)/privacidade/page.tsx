import type { Metadata } from 'next'

import { PrivacyPage } from '@/components/marketing/PrivacyPage'
import { PRIVACY_PT } from '@/lib/privacy-copy'

export const metadata: Metadata = {
  title: PRIVACY_PT.meta.title,
  description: PRIVACY_PT.meta.description,
  alternates: {
    canonical: '/privacidade',
    languages: { 'pt-BR': '/privacidade', en: '/en/privacy' },
  },
}

export default function PrivacidadePage() {
  return <PrivacyPage copy={PRIVACY_PT} />
}
