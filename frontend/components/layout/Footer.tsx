export default function Footer() {
  return (
    <footer className="border-t border-tumtum-border bg-tumtum-dark py-8">
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">
          <span
            className="text-sm font-bold uppercase tracking-widest text-tumtum-red"
            style={{ fontFamily: 'Georgia, serif' }}
          >
            Tumtum
          </span>
          <p className="text-sm text-tumtum-text-muted">
            &copy; {new Date().getFullYear()} Tumtum. Todos os direitos reservados.
          </p>
        </div>
      </div>
    </footer>
  )
}
