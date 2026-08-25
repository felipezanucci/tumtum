import type { Metadata, Viewport } from 'next'
import { Instrument_Sans } from 'next/font/google'

import './globals.css'

// The manual's functional family for site, app and communication. Chosmos is
// the wordmark only and never loads as a webfont.
const instrumentSans = Instrument_Sans({
  subsets: ['latin'],
  weight: ['400', '500', '600', '700'],
  variable: '--font-instrument-sans',
  display: 'swap',
})

export const metadata: Metadata = {
  title: 'TumTum — Sinta o evento',
  description: 'Descubra como seu coração reage nos momentos mais emocionantes. Conecte seu wearable, vá a um evento e compartilhe a emoção.',
  manifest: '/manifest.json',
  appleWebApp: {
    capable: true,
    statusBarStyle: 'black-translucent',
    title: 'TumTum',
  },
}

export const viewport: Viewport = {
  themeColor: '#000000',
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="pt-BR" className={instrumentSans.variable}>
      <head>
        <link rel="apple-touch-icon" href="/icons/icon-192.png" />
      </head>
      <body className="bg-tumtum-black text-tumtum-white antialiased">
        {children}
      </body>
    </html>
  )
}
