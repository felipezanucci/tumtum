import type { Metadata, Viewport } from 'next'
import { Bricolage_Grotesque, Instrument_Sans, Martian_Mono } from 'next/font/google'

import './globals.css'

const display = Bricolage_Grotesque({
  subsets: ['latin'],
  variable: '--font-display',
  display: 'swap',
})

const sans = Instrument_Sans({
  subsets: ['latin'],
  variable: '--font-sans',
  display: 'swap',
})

const mono = Martian_Mono({
  subsets: ['latin'],
  variable: '--font-mono',
  display: 'swap',
})

export const metadata: Metadata = {
  title: 'TumTum — Prova que você sentiu',
  description: 'O show que seu coração viu. Conecte seu relógio, vá ao show e leve para casa a batida daquela noite.',
  manifest: '/manifest.json',
  appleWebApp: {
    capable: true,
    statusBarStyle: 'black-translucent',
    title: 'TumTum',
  },
}

export const viewport: Viewport = {
  themeColor: '#FF2E3C',
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
    <html lang="pt-BR" className={`${display.variable} ${sans.variable} ${mono.variable}`}>
      <head>
        <link rel="apple-touch-icon" href="/icons/icon-192.png" />
      </head>
      <body className="bg-tumtum-dark text-tumtum-text-primary antialiased">
        {children}
      </body>
    </html>
  )
}
