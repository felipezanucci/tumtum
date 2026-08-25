import Link from 'next/link'
import { Wordmark } from '@/components/brand'

export default function LandingPage() {
  return (
    <main className="min-h-screen bg-tumtum-black">
      {/* Hero */}
      <section className="relative flex min-h-screen flex-col items-center justify-center px-4 text-center">
        {/* Background glow */}
        <div className="pointer-events-none absolute inset-0 overflow-hidden">
          <div className="absolute left-1/2 top-1/3 h-96 w-96 -translate-x-1/2 rounded-full bg-tumtum-lime/10 blur-[120px]" />
        </div>

        <div className="relative z-10">
          <Wordmark className="mx-auto h-14 w-auto text-tumtum-white sm:h-20" />
          <p className="mt-4 text-xl text-tumtum-white sm:text-2xl">
            Sinta o evento. Compartilhe a emoção.
          </p>
          <p className="mt-3 max-w-md text-tumtum-muted">
            Descubra como seu coração reage nos shows e jogos mais emocionantes.
            Conecte seu wearable e reviva cada batida.
          </p>

          <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
            <Link
              href="/signup"
              className="inline-flex items-center justify-center rounded-lg bg-tumtum-lime px-8 py-3 text-lg font-medium text-tumtum-black transition-colors hover:bg-tumtum-yellow"
            >
              Começar agora
            </Link>
            <Link
              href="/login"
              className="inline-flex items-center justify-center rounded-lg border border-tumtum-border px-8 py-3 text-lg font-medium text-tumtum-white transition-colors hover:bg-tumtum-surface"
            >
              Já tenho conta
            </Link>
          </div>
        </div>

        {/* Scroll indicator */}
        <div className="absolute bottom-8 animate-bounce">
          <svg className="h-6 w-6 text-tumtum-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
          </svg>
        </div>
      </section>

      {/* How it works */}
      <section className="border-t border-tumtum-border px-4 py-20">
        <div className="mx-auto max-w-4xl">
          <h2 className="mb-12 text-center text-3xl font-bold text-tumtum-white">
            Como funciona
          </h2>
          <div className="grid gap-8 sm:grid-cols-3">
            {[
              {
                icon: '⌚',
                title: 'Conecte',
                description: 'Vincule seu Apple Watch, Wear OS ou outro relógio que já registra sua batida.',
              },
              {
                icon: '🎵',
                title: 'Viva',
                description: 'Vá a um show, jogo ou festival. Seu wearable captura cada batida do seu coração.',
              },
              {
                icon: '🔥',
                title: 'Compartilhe',
                description: 'Veja seus picos de emoção sincronizados com o setlist e compartilhe com seus amigos.',
              },
            ].map(({ icon, title, description }) => (
              <div key={title} className="text-center">
                <div className="mb-4 text-5xl">{icon}</div>
                <h3 className="mb-2 text-xl font-semibold text-tumtum-white">{title}</h3>
                <p className="text-sm text-tumtum-muted">{description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="border-t border-tumtum-border px-4 py-20">
        <div className="mx-auto max-w-4xl">
          <div className="grid gap-12 sm:grid-cols-2">
            <div>
              <h3 className="text-2xl font-bold text-tumtum-white">
                Curva de emoção em tempo real
              </h3>
              <p className="mt-3 text-tumtum-muted">
                Veja exatamente quando seu coração disparou — sincronizado com cada música do setlist ou gol do jogo.
              </p>
            </div>
            <div>
              <h3 className="text-2xl font-bold text-tumtum-white">
                Cards compartilháveis
              </h3>
              <p className="mt-3 text-tumtum-muted">
                Gere cards visuais com seus picos de emoção e compartilhe no Instagram, X, WhatsApp e TikTok.
              </p>
            </div>
            <div>
              <h3 className="text-2xl font-bold text-tumtum-white">
                Shows e jogos
              </h3>
              <p className="mt-3 text-tumtum-muted">
                Cobrimos concerts com setlists do Setlist.fm e jogos de futebol com dados em tempo real.
              </p>
            </div>
            <div>
              <h3 className="text-2xl font-bold text-tumtum-white">
                Privacidade primeiro
              </h3>
              <p className="mt-3 text-tumtum-muted">
                Seus dados de saúde são sensíveis. Coleta mínima, controle total e transparência sempre.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="border-t border-tumtum-border px-4 py-20">
        <div className="mx-auto max-w-lg text-center">
          <h2 className="text-3xl font-bold text-tumtum-white">
            Pronto para sentir o evento?
          </h2>
          <p className="mt-3 text-tumtum-muted">
            Crie sua conta grátis e descubra o que seu coração tem a dizer.
          </p>
          <Link
            href="/signup"
            className="mt-8 inline-flex items-center justify-center rounded-lg bg-tumtum-lime px-8 py-3 text-lg font-medium text-tumtum-black transition-colors hover:bg-tumtum-yellow"
          >
            Criar conta grátis
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-tumtum-border py-8 text-center">
        <Wordmark className="mx-auto h-4 w-auto text-tumtum-white" />
        <p className="mt-2 text-xs text-tumtum-muted">
          &copy; {new Date().getFullYear()} TumTum. Todos os direitos reservados.
        </p>
      </footer>
    </main>
  )
}
