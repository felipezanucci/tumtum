import Link from 'next/link'

export default function LandingPage() {
  return (
    <main className="min-h-screen bg-tumtum-dark">
      {/* Hero */}
      <section className="relative flex min-h-screen flex-col items-center justify-center px-4 text-center">
        {/* Background glow */}
        <div className="pointer-events-none absolute inset-0 overflow-hidden">
          <div className="absolute left-1/2 top-1/3 h-96 w-96 -translate-x-1/2 rounded-full bg-tumtum-red/10 blur-[120px]" />
        </div>

        <div className="relative z-10">
          <h1
            className="text-5xl font-bold uppercase tracking-widest text-tumtum-red sm:text-7xl"
            style={{ fontFamily: 'Georgia, serif' }}
          >
            Tumtum
          </h1>
          <p className="mt-4 text-xl text-tumtum-text-primary sm:text-2xl">
            Seu coração já sabe o que você sentiu. A gente mostra.
          </p>
          <p className="mt-3 max-w-md text-tumtum-text-muted">
            Conecta seu relógio, vai pro show e descobre que você quase infartou de emoção naquela música.
          </p>

          <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
            <Link
              href="/signup"
              className="inline-flex items-center justify-center rounded-lg bg-tumtum-red px-8 py-3 text-lg font-medium text-white transition-colors hover:bg-tumtum-red-secondary"
            >
              Bora lá
            </Link>
            <Link
              href="/login"
              className="inline-flex items-center justify-center rounded-lg border border-tumtum-border px-8 py-3 text-lg font-medium text-tumtum-text-primary transition-colors hover:bg-tumtum-surface"
            >
              Já tenho conta
            </Link>
          </div>
        </div>

        {/* Scroll indicator */}
        <div className="absolute bottom-8 animate-bounce">
          <svg className="h-6 w-6 text-tumtum-text-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
          </svg>
        </div>
      </section>

      {/* How it works */}
      <section className="border-t border-tumtum-border px-4 py-20">
        <div className="mx-auto max-w-4xl">
          <h2 className="mb-12 text-center text-3xl font-bold text-tumtum-text-primary">
            Como funciona
          </h2>
          <div className="grid gap-8 sm:grid-cols-3">
            {[
              {
                icon: '⌚',
                title: 'Conecta o relógio',
                description: 'Apple Watch, Wear OS, Garmin... qualquer um que saiba contar seus batimentos. A gente não julga o modelo.',
              },
              {
                icon: '🎵',
                title: 'Vai pro evento',
                description: 'Show, jogo, festival. Seu relógio grava cada batida enquanto você finge que tá de boa.',
              },
              {
                icon: '🔥',
                title: 'Mostra pro mundo',
                description: 'A gente sincroniza seus picos com o setlist e gera um card. Seus amigos vão querer saber por que você tava a 140 bpm.',
              },
            ].map(({ icon, title, description }) => (
              <div key={title} className="text-center">
                <div className="mb-4 text-5xl">{icon}</div>
                <h3 className="mb-2 text-xl font-semibold text-tumtum-text-primary">{title}</h3>
                <p className="text-sm text-tumtum-text-muted">{description}</p>
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
              <h3 className="text-2xl font-bold text-tumtum-text-primary">
                Sua curva de emoção
              </h3>
              <p className="mt-3 text-tumtum-text-muted">
                Aquele arrepio em Evidências? A gente mostra exatamente quando aconteceu. Com gráfico e tudo.
              </p>
            </div>
            <div>
              <h3 className="text-2xl font-bold text-tumtum-text-primary">
                Cards pra causar
              </h3>
              <p className="mt-3 text-tumtum-text-muted">
                Gera um card com seu pico, manda no story e espera os "meu deus" nos replies.
              </p>
            </div>
            <div>
              <h3 className="text-2xl font-bold text-tumtum-text-primary">
                Shows e jogos
              </h3>
              <p className="mt-3 text-tumtum-text-muted">
                Sabe o gol dos acréscimos? A gente sabe o que seu coração fez naquele segundo. Setlists e partidas cobertas.
              </p>
            </div>
            <div>
              <h3 className="text-2xl font-bold text-tumtum-text-primary">
                Seus dados, suas regras
              </h3>
              <p className="mt-3 text-tumtum-text-muted">
                A gente leva privacidade a sério (uma das poucas coisas que a gente leva a sério). Coleta mínima e controle total.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="border-t border-tumtum-border px-4 py-20">
        <div className="mx-auto max-w-lg text-center">
          <h2 className="text-3xl font-bold text-tumtum-text-primary">
            Bora descobrir o que seu coração aprontou?
          </h2>
          <p className="mt-3 text-tumtum-text-muted">
            Cria sua conta. É grátis, rápido e não dói. (Diferente do gol contra do seu time.)
          </p>
          <Link
            href="/signup"
            className="mt-8 inline-flex items-center justify-center rounded-lg bg-tumtum-red px-8 py-3 text-lg font-medium text-white transition-colors hover:bg-tumtum-red-secondary"
          >
            Criar conta de graça
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-tumtum-border py-8 text-center">
        <p
          className="text-sm font-bold uppercase tracking-widest text-tumtum-red"
          style={{ fontFamily: 'Georgia, serif' }}
        >
          Tumtum
        </p>
        <p className="mt-2 text-xs text-tumtum-text-muted">
          &copy; {new Date().getFullYear()} Tumtum. Todos os direitos reservados.
        </p>
      </footer>
    </main>
  )
}
