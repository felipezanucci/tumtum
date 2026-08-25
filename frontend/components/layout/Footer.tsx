import { Wordmark } from '@/components/brand'

export default function Footer() {
  return (
    <footer className="border-t border-tumtum-border bg-tumtum-black py-8">
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">
          <Wordmark className="h-4 w-auto text-tumtum-white" />
          <p className="text-sm text-tumtum-muted">
            &copy; {new Date().getFullYear()} TumTum. Todos os direitos reservados.
          </p>
        </div>
      </div>
    </footer>
  )
}
