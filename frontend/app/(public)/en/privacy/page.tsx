import type { Metadata } from 'next'

import { PrivacyPage } from '@/components/marketing/PrivacyPage'
import { PRIVACY_EN } from '@/lib/privacy-copy'

export const metadata: Metadata = {
  title: PRIVACY_EN.meta.title,
  description: PRIVACY_EN.meta.description,
  alternates: {
    canonical: '/en/privacy',
    languages: { 'pt-BR': '/privacidade', en: '/en/privacy' },
  },
}

export default function PrivacyPageEn() {
  return <PrivacyPage copy={PRIVACY_EN} />
}
