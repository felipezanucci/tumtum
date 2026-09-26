import Link from 'next/link'

import { Wordmark } from '@/components/brand'

/**
 * The foot of every page inside the app (26/09): the three doors the law
 * says must always be one tap away — what the TumTum does with the data, the
 * rules, and how to leave — and the one sentence the product never stops
 * saying about itself.
 */
const links = [
  { href: '/privacidade', label: 'Privacidade' },
  { href: '/termos', label: 'Termos' },
  { href: '/apagar-conta', label: 'Apagar conta' },
]

export default function Footer() {
  return (
    <footer className="border-t border-tumtum-border bg-tumtum-black py-8">
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">
          <Wordmark className="h-4 w-auto text-tumtum-white" />
          <nav aria-label="Informações legais" className="flex flex-wrap justify-center gap-5 text-sm">
            {links.map(({ href, label }) => (
              <Link key={href} href={href} className="text-tumtum-muted transition-colors hover:text-tumtum-pink">
                {label}
              </Link>
            ))}
          </nav>
        </div>
        <p className="mt-6 text-center text-xs text-tumtum-faint sm:text-left">
          A TumTum não é um dispositivo médico e não interpreta saúde.
        </p>
        <p className="mt-1 text-center text-xs text-tumtum-faint sm:text-left">
          &copy; {new Date().getFullYear()} TumTum. Todos os direitos reservados.
        </p>
      </div>
    </footer>
  )
}
