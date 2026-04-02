import type { Metadata } from 'next'

import './globals.css'

export const metadata: Metadata = {
  title: 'Tumtum',
  description: 'Live entertainment heart rate platform',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  )
}