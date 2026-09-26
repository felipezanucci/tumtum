import { ConsentGate, Footer } from '@/components/layout'

/**
 * Every page inside the app: the consent gate first (it renders nothing and
 * only redirects), then the page, then the footer with Privacidade, Termos
 * and Apagar conta.
 */
export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <ConsentGate />
      {children}
      <Footer />
    </>
  )
}
