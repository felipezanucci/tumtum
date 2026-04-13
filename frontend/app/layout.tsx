import type { Metadata, Viewport } from 'next'

import './globals.css'

export const metadata: Metadata = {
  title: 'Tumtum — Seu coração não sabe mentir',
  description: 'Conecta seu relógio, vai pro show e descobre que você quase infartou de emoção. A gente mostra o que seu coração aprontou.',
  icons: {
    icon: '/icons/icon-192.png',
  },
  manifest: '/manifest.json',
  appleWebApp: {
    capable: true,
    statusBarStyle: 'black-translucent',
    title: 'Tumtum',
  },
}

export const viewport: Viewport = {
  themeColor: '#FE2401',
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
      <body className="bg-white text-tumtum-text-primary antialiased">
        {children}
      </body>
    </html>
  )
}
