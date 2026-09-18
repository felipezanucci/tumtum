import type { Metadata } from 'next'

import { PrivacyPage } from '@/components/marketing/PrivacyPage'
import { DELETE_ACCOUNT_PT } from '@/lib/delete-account-copy'

export const metadata: Metadata = {
  title: DELETE_ACCOUNT_PT.meta.title,
  description: DELETE_ACCOUNT_PT.meta.description,
  alternates: {
    canonical: '/apagar-conta',
    languages: { 'pt-BR': '/apagar-conta', en: '/en/delete-account' },
  },
}

export default function ApagarContaPage() {
  return <PrivacyPage copy={DELETE_ACCOUNT_PT} />
}
