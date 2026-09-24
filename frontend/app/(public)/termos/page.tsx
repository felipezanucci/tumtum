import type { Metadata } from 'next'

import { PrivacyPage } from '@/components/marketing/PrivacyPage'
import { TERMS_PT } from '@/lib/terms-copy'

export const metadata: Metadata = {
  title: TERMS_PT.meta.title,
  description: TERMS_PT.meta.description,
  alternates: {
    canonical: '/termos',
    languages: { 'pt-BR': '/termos', en: '/en/terms' },
  },
}

export default function TermosPage() {
  return <PrivacyPage copy={TERMS_PT} />
}
