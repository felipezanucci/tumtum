import type { Metadata } from 'next'

import { PrivacyPage } from '@/components/marketing/PrivacyPage'
import { TERMS_EN } from '@/lib/terms-copy'

export const metadata: Metadata = {
  title: TERMS_EN.meta.title,
  description: TERMS_EN.meta.description,
  alternates: {
    canonical: '/en/terms',
    languages: { 'pt-BR': '/termos', en: '/en/terms' },
  },
}

export default function TermsPageEn() {
  return <PrivacyPage copy={TERMS_EN} />
}
