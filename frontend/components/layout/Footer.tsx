import { Logo } from '@/components/brand'

export default function Footer() {
  return (
    <footer className="border-t border-tumtum-border bg-tumtum-dark py-8">
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">
          <Logo variant="horizontal" className="h-5 w-auto" />
          <p className="text-sm text-tumtum-text-muted">
            &copy; {new Date().getFullYear()} TumTum. Todos os direitos reservados.
          </p>
        </div>
      </div>
    </footer>
  )
}
