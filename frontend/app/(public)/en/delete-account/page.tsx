import type { Metadata } from 'next'

import { PrivacyPage } from '@/components/marketing/PrivacyPage'
import { DELETE_ACCOUNT_EN } from '@/lib/delete-account-copy'

export const metadata: Metadata = {
  title: DELETE_ACCOUNT_EN.meta.title,
  description: DELETE_ACCOUNT_EN.meta.description,
  alternates: {
    canonical: '/en/delete-account',
    languages: { 'pt-BR': '/apagar-conta', en: '/en/delete-account' },
  },
}

export default function DeleteAccountPageEn() {
  return <PrivacyPage copy={DELETE_ACCOUNT_EN} />
}
