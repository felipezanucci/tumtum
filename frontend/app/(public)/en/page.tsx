import type { Metadata } from 'next'

import { SiteLanding } from '@/components/marketing/SiteLanding'
import { EN } from '@/lib/site-copy'

export const metadata: Metadata = {
  title: EN.meta.title,
  description: EN.meta.description,
  alternates: {
    canonical: '/en',
    languages: { 'pt-BR': '/', en: '/en' },
  },
  openGraph: { title: EN.meta.title, description: EN.meta.description, locale: 'en_US' },
}

export default function LandingPageEn() {
  return <SiteLanding copy={EN} />
}
