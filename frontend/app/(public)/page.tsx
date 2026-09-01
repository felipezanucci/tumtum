import type { Metadata } from 'next'

import { SiteLanding } from '@/components/marketing/SiteLanding'
import { PT } from '@/lib/site-copy'

export const metadata: Metadata = {
  title: PT.meta.title,
  description: PT.meta.description,
  alternates: {
    canonical: '/',
    languages: { 'pt-BR': '/', en: '/en' },
  },
  openGraph: { title: PT.meta.title, description: PT.meta.description, locale: 'pt_BR' },
}

export default function LandingPage() {
  return <SiteLanding copy={PT} />
}
