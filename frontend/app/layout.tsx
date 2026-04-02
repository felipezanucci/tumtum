import type { Metadata, Viewport } from 'next'

import './globals.css'

export const metadata: Metadata = {
  title: 'Tumtum — Sinta o evento',
  description: 'Descubra como seu coração reage nos momentos mais emocionantes. Conecte seu wearable, vá a um evento e compartilhe a emoção.',
  manifest: '/manifest.json',
  appleWebApp: {
    capable: true,
    statusBarStyle: 'black-translucent',
    title: 'Tumtum',
  },
}

export const viewport: Viewport = {
  themeColor: '#C0392B',
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
    <html lang="pt-BR">
      <head>
        <link rel="apple-touch-icon" href="/icons/icon-192.png" />
      </head>
      <body className="bg-tumtum-dark text-tumtum-text-primary antialiased">
        {children}
      </body>
    </html>
  )
}
