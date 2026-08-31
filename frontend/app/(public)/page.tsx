import type { Metadata } from 'next'

import { Wordmark } from '@/components/brand'
import { LandingNav } from '@/components/marketing/LandingNav'
import { Reveal } from '@/components/marketing/Reveal'
import { VideoSlot } from '@/components/marketing/VideoSlot'
import { WaitlistForm } from '@/components/marketing/WaitlistForm'

/**
 * The public face of TumTum.
 *
 * This route is the whole of what a stranger sees. Everything behind it —
 * events, cards, sessions, profile — lives under `(app)` and is what the
 * Android app loads into its WebView, so the two can change independently:
 * the site can be rewritten as a sales page without the capture app noticing.
 *
 * Ported from a standalone HTML draft. Two things were rebuilt rather than
 * copied: the waitlist form, which had no handler at all, and the reveal
 * animation, which hid every section until script ran.
 */

export const metadata: Metadata = {
  title: 'TumTum — Você sentiu. Agora tem prova.',
  description:
    'A TumTum transforma o que você sentiu num show ou jogo em uma história que dá para ver, provar e compartilhar.',
}

const STEPS = [
  {
    n: '01',
    title: 'Conecta o relógio que você já usa',
    body: 'Sem aparelho novo, sem complicação. Seu smartwatch de sempre já registra tudo o que a gente precisa.',
  },
  {
    n: '02',
    title: 'Vai pro evento e esquece da gente',
    body: 'Canta, grita, sofre no pênalti. O relógio fica no pulso fazendo o trabalho dele, como sempre fez.',
  },
  {
    n: '03',
    title: 'Depois, a revelação',
    body: 'A TumTum cruza sua noite com a timeline do evento e te mostra qual momento foi o seu. Aí é só escolher o card e postar.',
  },
]

const REELS = [
  {
    label: 'Vídeo · torcida no gol',
    bpm: '176',
    caption: 'A torcida foi junto.',
    meta: 'Pênalti · 94:12',
    offset: '',
  },
  {
    label: 'Vídeo · fã na grade',
    bpm: '191',
    caption: 'Quando entrou, meu coração foi junto.',
    meta: 'Abertura do show · 21:03',
    offset: 'md:translate-y-7',
  },
  {
    label: 'Vídeo · drop no festival',
    bpm: '183',
    caption: 'Ninguém tava tranquilo.',
    meta: 'O drop · 23:41',
    offset: '',
  },
]

/**
 * The real accounts.
 *
 * Stored as canonical profile URLs. The links Felipe pasted carried share
 * tokens from his own session — `?igsi=…` on Instagram, `?_r=1&_t=…` on
 * TikTok — which identify the device that generated the share and have no
 * business on a public page.
 */
const SOCIALS = [
  {
    name: 'Instagram',
    href: 'https://www.instagram.com/tumtum.cc',
    path: 'M12 2.2c3.2 0 3.6 0 4.9.1 1.2.1 1.8.2 2.2.4.6.2 1 .5 1.4.9.4.4.7.8.9 1.4.2.4.4 1 .4 2.2.1 1.3.1 1.7.1 4.9s0 3.6-.1 4.9c-.1 1.2-.2 1.8-.4 2.2-.2.6-.5 1-.9 1.4-.4.4-.8.7-1.4.9-.4.2-1 .4-2.2.4-1.3.1-1.7.1-4.9.1s-3.6 0-4.9-.1c-1.2-.1-1.8-.2-2.2-.4-.6-.2-1-.5-1.4-.9-.4-.4-.7-.8-.9-1.4-.2-.4-.4-1-.4-2.2C2.2 15.6 2.2 15.2 2.2 12s0-3.6.1-4.9c.1-1.2.2-1.8.4-2.2.2-.6.5-1 .9-1.4.4-.4.8-.7 1.4-.9.4-.2 1-.4 2.2-.4C8.4 2.2 8.8 2.2 12 2.2m0-2.2C8.7 0 8.3 0 7 .1 5.7.1 4.9.3 4.1.6c-.8.3-1.4.7-2.1 1.4C1.3 2.7.9 3.3.6 4.1.3 4.9.1 5.7.1 7 0 8.3 0 8.7 0 12s0 3.7.1 5c0 1.3.2 2.1.5 2.9.3.8.7 1.4 1.4 2.1.7.7 1.3 1.1 2.1 1.4.8.3 1.6.5 2.9.5 1.3.1 1.7.1 5 .1s3.7 0 5-.1c1.3 0 2.1-.2 2.9-.5.8-.3 1.4-.7 2.1-1.4.7-.7 1.1-1.3 1.4-2.1.3-.8.5-1.6.5-2.9.1-1.3.1-1.7.1-5s0-3.7-.1-5c0-1.3-.2-2.1-.5-2.9-.3-.8-.7-1.4-1.4-2.1C21.3 1.3 20.7.9 19.9.6 19.1.3 18.3.1 17 .1 15.7 0 15.3 0 12 0zm0 5.8a6.2 6.2 0 100 12.4 6.2 6.2 0 000-12.4zm0 10.2a4 4 0 110-8 4 4 0 010 8zm6.4-11.8a1.4 1.4 0 11-2.9 0 1.4 1.4 0 012.9 0z',
  },
  {
    name: 'TikTok',
    href: 'https://www.tiktok.com/@tumtum.cc',
    path: 'M19.6 6.7a5.6 5.6 0 01-3.4-1.1 5.6 5.6 0 01-2.2-3.5h-3.5v13.5a3.2 3.2 0 11-2.3-3.1V8.9a6.7 6.7 0 00-1-.1 6.7 6.7 0 106.8 6.7V9.5a9 9 0 005.6 1.9V7.9c0-.4 0-.8-.1-1.2z',
  },
  {
    name: 'X',
    href: 'https://x.com/TumTumcc',
    path: 'M18.2 2h3.3l-7.2 8.3L22.8 22h-6.7l-5.2-6.8L4.9 22H1.6l7.7-8.8L1.2 2h6.8l4.7 6.2L18.2 2zm-1.2 18h1.8L6.9 3.9H5L17 20z',
  },
  {
    name: 'Facebook',
    href: 'https://www.facebook.com/tumtum.ccc',
    path: 'M24 12a12 12 0 10-13.9 11.9v-8.4H7.1V12h3V9.4c0-3 1.8-4.7 4.5-4.7 1.3 0 2.7.2 2.7.2v3h-1.5c-1.5 0-2 .9-2 1.9V12h3.3l-.5 3.5h-2.8v8.4A12 12 0 0024 12z',
  },
  {
    name: 'LinkedIn',
    href: 'https://www.linkedin.com/company/tumtumcc',
    path: 'M20.4 20.5h-3.6v-5.6c0-1.3 0-3-1.9-3-1.9 0-2.1 1.4-2.1 2.9v5.7H9.2V9h3.4v1.6h.1c.5-.9 1.6-1.9 3.4-1.9 3.6 0 4.3 2.4 4.3 5.5v6.3zM5.3 7.4a2.1 2.1 0 110-4.2 2.1 2.1 0 010 4.2zM7.1 20.5H3.5V9h3.6v11.5z',
  },
]

export default function LandingPage() {
  return (
    <main className="bg-tumtum-black text-tumtum-white">
      <LandingNav />

      {/* Hero */}
      <header className="relative flex min-h-[100svh] items-end overflow-hidden">
        <VideoSlot
          label="Vídeo · multidão no show, plano aberto"
          className="absolute inset-0"
        />
        <div className="absolute inset-0 z-[1] bg-[linear-gradient(180deg,rgba(0,0,0,0.32)_0%,rgba(0,0,0,0.08)_45%,rgba(0,0,0,0.78)_90%,#000_100%)]" />

        <div className="relative z-[2] mx-auto grid w-full max-w-[1120px] grid-cols-1 items-end gap-12 px-6 pb-14 pt-[120px] md:grid-cols-[1.2fr_0.8fr]">
          <div>
            <h1 className="text-[clamp(34px,5vw,62px)] font-hero uppercase leading-none tracking-[-0.02em] [text-shadow:0_2px_24px_rgba(0,0,0,0.5)]">
              Você sentiu.
              <br />
              <span className="text-tumtum-pink">Agora tem prova.</span>
            </h1>
            <p className="mt-5 max-w-[40ch] text-[clamp(15px,1.6vw,18px)] text-white/80 [text-shadow:0_1px_16px_rgba(0,0,0,0.6)]">
              Os momentos mais marcantes da sua vida têm hora, lugar e batida. A
              TumTum guarda os três.
            </p>
            <div className="mt-9 flex flex-wrap items-center gap-[18px]">
              <a
                href="#lista"
                className="rounded-full bg-tumtum-pink px-8 py-4 text-base font-headline text-tumtum-black transition-transform hover:scale-105 motion-reduce:transition-none motion-reduce:hover:scale-100"
              >
                Quero no meu próximo show
              </a>
              <a
                href="#como"
                className="border-b-2 border-tumtum-yellow pb-0.5 text-base font-label text-tumtum-white"
              >
                Como funciona
              </a>
            </div>
          </div>

          {/* The card, as the product makes it */}
          <div className="flex justify-center md:justify-end">
            <div className="flex aspect-[9/16] w-[min(248px,74vw)] rotate-[2.5deg] flex-col rounded-3xl border border-tumtum-pink/40 bg-black/80 px-[22px] py-6 shadow-[0_0_80px_rgba(198,255,0,0.15),0_24px_60px_rgba(0,0,0,0.8)] backdrop-blur-[6px] motion-reduce:rotate-0">
              <Wordmark className="w-[82px] text-tumtum-white" />
              <p className="mt-[22px] text-[19px] font-hero uppercase leading-[1.12]">
                Eu tava tranquilo.
                <br />
                Aí veio isso.
              </p>
              <p className="mt-auto text-[100px] font-hero leading-[0.9] tracking-[-0.04em] text-tumtum-pink tabular-nums">
                187
                <span className="ml-1.5 text-[17px] font-headline tracking-[0.08em] text-tumtum-muted">
                  BPM
                </span>
              </p>
              <span className="mt-3 self-start rounded-full bg-tumtum-pink px-[13px] py-[5px] text-[13.5px] font-headline text-tumtum-black tabular-nums">
                22:47
              </span>
              <p className="mt-3.5 text-base font-headline leading-tight">
                A Sky Full of Stars
                <span className="mt-0.5 block text-[12.5px] font-body text-tumtum-muted">
                  Coldplay
                </span>
              </p>
              <p className="mt-4 text-[11.5px] italic text-tumtum-faint">
                era óbvio que ia ser essa.
              </p>
            </div>
          </div>
        </div>
      </header>

      {/* A comoção */}
      <section className="border-t border-tumtum-border py-20 pb-[120px] md:py-[104px]">
        <div className="mx-auto max-w-[1120px] px-6">
          <Reveal>
            <p className="mb-5 text-[13px] font-headline uppercase tracking-[0.16em] text-tumtum-pink">
              A comoção é real
            </p>
            <h2 className="max-w-[20ch] text-[clamp(32px,4.6vw,56px)] font-hero uppercase leading-[1.02] tracking-[-0.02em]">
              Quarenta mil pessoas. Um resultado que é só seu.
            </h2>
            <p className="mt-5 max-w-[56ch] text-[clamp(16px,1.8vw,19px)] text-tumtum-muted">
              Gol no último minuto. O refrão que todo mundo gritou junto. A
              entrada do ídolo. Você já viveu isso —{' '}
              <strong className="font-headline text-tumtum-white">
                a TumTum guarda o que aconteceu dentro de você nesses segundos.
              </strong>
            </p>

            <div className="mx-auto mt-14 grid max-w-[340px] grid-cols-1 gap-5 md:max-w-none md:grid-cols-3 md:gap-6">
              {REELS.map((reel) => (
                <div
                  key={reel.label}
                  className={`relative aspect-[9/16] overflow-hidden rounded-[20px] border border-tumtum-border ${reel.offset}`}
                >
                  <VideoSlot label={reel.label} className="absolute inset-0" />
                  <div className="absolute inset-0 z-[1] bg-[linear-gradient(180deg,transparent_45%,rgba(0,0,0,0.85))]" />
                  <span className="absolute right-4 top-4 z-[2] rounded-full bg-tumtum-pink px-[13px] py-1.5 text-[15px] font-hero text-tumtum-black tabular-nums">
                    {reel.bpm}
                  </span>
                  <p className="absolute inset-x-5 bottom-5 z-[2] text-xl font-hero uppercase leading-[1.12]">
                    {reel.caption}
                    <span className="mt-2 block text-[12.5px] font-label uppercase tracking-[0.1em] text-tumtum-pink">
                      {reel.meta}
                    </span>
                  </p>
                </div>
              ))}
            </div>
          </Reveal>
        </div>
      </section>

      {/* A promessa */}
      <section className="border-t border-tumtum-border py-20 md:py-[104px]">
        <div className="mx-auto max-w-[1120px] px-6">
          <Reveal>
            <p className="mb-5 text-[13px] font-headline uppercase tracking-[0.16em] text-tumtum-pink">
              O que a TumTum faz
            </p>
            <h2 className="max-w-[24ch] text-[clamp(32px,4.6vw,56px)] font-hero uppercase leading-[1.02] tracking-[-0.02em]">
              Todo mundo filmou o show.{' '}
              <em className="not-italic text-tumtum-yellow">
                Ninguém registrou o que sentiu.
              </em>
            </h2>
            <p className="mt-5 max-w-[56ch] text-[clamp(16px,1.8vw,19px)] text-tumtum-muted">
              Você guarda o ingresso, a pulseira, o print da setlist. A TumTum
              guarda a parte que importa:{' '}
              <strong className="font-headline text-tumtum-white">
                o exato momento em que você perdeu a compostura
              </strong>{' '}
              — com hora, música e prova.
            </p>
          </Reveal>
        </div>
      </section>

      {/* Como funciona */}
      <section id="como" className="border-t border-tumtum-border py-20 md:py-[104px]">
        <div className="mx-auto max-w-[1120px] px-6">
          <Reveal>
            <p className="mb-5 text-[13px] font-headline uppercase tracking-[0.16em] text-tumtum-pink">
              Como funciona
            </p>
            <h2 className="max-w-[20ch] text-[clamp(32px,4.6vw,56px)] font-hero uppercase leading-[1.02] tracking-[-0.02em]">
              Você vive a noite. A gente cuida do resto.
            </h2>
            <div className="mt-14 grid grid-cols-1 gap-px border border-tumtum-border bg-tumtum-border md:grid-cols-3">
              {STEPS.map((step) => (
                <div key={step.n} className="bg-tumtum-black px-7 pb-11 pt-9">
                  <p className="text-[15px] font-hero tracking-[0.06em] text-tumtum-pink tabular-nums">
                    {step.n}
                  </p>
                  <h3 className="mt-4 text-[21px] font-headline leading-tight">
                    {step.title}
                  </h3>
                  <p className="mt-3 text-[15px] text-tumtum-muted">{step.body}</p>
                </div>
              ))}
            </div>
          </Reveal>
        </div>
      </section>

      {/* Os cards */}
      <section className="border-t border-tumtum-border py-20 md:py-[104px]">
        <div className="mx-auto max-w-[1120px] px-6">
          <Reveal>
            <p className="mb-5 text-[13px] font-headline uppercase tracking-[0.16em] text-tumtum-pink">
              O card é a prova
            </p>
            <h2 className="max-w-[20ch] text-[clamp(32px,4.6vw,56px)] font-hero uppercase leading-[1.02] tracking-[-0.02em]">
              Um momento. Vários jeitos de contar.
            </h2>
            <p className="mt-5 max-w-[56ch] text-[clamp(16px,1.8vw,19px)] text-tumtum-muted">
              Cada noite gera cards diferentes do mesmo instante. Você escolhe
              qual história vai pro feed.
            </p>

            <div className="mx-auto mt-16 grid max-w-[340px] grid-cols-1 items-start gap-7 md:max-w-none md:grid-cols-3">
              {/* 01 — Só o momento */}
              <article className="flex aspect-[9/14] flex-col rounded-[20px] border border-tumtum-border bg-tumtum-black px-5 py-[22px]">
                <Wordmark className="w-16 text-tumtum-white" />
                <p className="mt-auto text-xs font-headline uppercase tracking-[0.14em] text-tumtum-faint">
                  Só o momento
                </p>
                <p className="mt-2.5 text-[17px] font-hero uppercase leading-[1.15]">
                  Aqui acabou meu psicológico.
                </p>
                <p className="mt-3.5 text-[52px] font-hero leading-[0.9] tracking-[-0.03em] text-tumtum-pink tabular-nums">
                  187
                </p>
                <p className="mt-2.5 text-[12.5px] text-tumtum-muted">
                  22:47 · A Sky Full of Stars
                </p>
              </article>

              {/* 03 — Minha noite */}
              <article className="flex aspect-[9/14] flex-col rounded-[20px] border border-tumtum-border bg-tumtum-black px-5 py-[22px]">
                <Wordmark className="w-16 text-tumtum-white" />
                <p className="mt-auto text-xs font-headline uppercase tracking-[0.14em] text-tumtum-faint">
                  Minha noite
                </p>
                <p className="mt-2.5 text-[17px] font-hero uppercase leading-[1.15]">
                  Do início ao fim, tudo fez sentido.
                </p>
                {/* BPM over time with the peak marked — the one chart the brand
                    manual allows, and only because it is product information
                    rather than decoration. Never an ECG trace, never a zone. */}
                <svg
                  viewBox="0 0 240 90"
                  className="mt-3.5 h-auto w-full"
                  aria-hidden="true"
                >
                  <polyline
                    points="0,72 22,68 40,70 58,62 76,66 96,58 112,62 130,50 148,56 160,18 172,54 190,60 210,56 240,62"
                    fill="none"
                    stroke="#FF6F91"
                    strokeWidth="2.5"
                    strokeLinejoin="round"
                  />
                  <circle cx="160" cy="18" r="4.5" fill="#FF6F91" />
                </svg>
                <p className="mt-2.5 text-[12.5px] text-tumtum-muted">
                  O pico marcado no seu tempo real da noite.
                </p>
              </article>

              {/* 04 — A galera */}
              <article className="flex aspect-[9/14] flex-col rounded-[20px] border border-tumtum-border bg-tumtum-black px-5 py-[22px]">
                <Wordmark className="w-16 text-tumtum-white" />
                <p className="mt-auto text-xs font-headline uppercase tracking-[0.14em] text-tumtum-faint">
                  A galera
                </p>
                <p className="mt-2.5 text-[17px] font-hero uppercase leading-[1.15]">
                  Ninguém tava tranquilo.
                </p>
                <div className="mt-3.5 flex gap-5">
                  <div>
                    <b className="block text-[34px] font-hero tracking-[-0.02em] text-tumtum-pink tabular-nums">
                      187
                    </b>
                    <span className="text-[11px] uppercase tracking-[0.1em] text-tumtum-muted">
                      Você
                    </span>
                  </div>
                  <div>
                    <b className="block text-[34px] font-hero tracking-[-0.02em] text-tumtum-yellow tabular-nums">
                      172
                    </b>
                    <span className="text-[11px] uppercase tracking-[0.1em] text-tumtum-muted">
                      A torcida
                    </span>
                  </div>
                </div>
                <p className="mt-2.5 text-[12.5px] text-tumtum-muted">
                  Seu momento comparado com todo mundo que estava lá.
                </p>
              </article>
            </div>

            <p className="mt-9 max-w-[60ch] text-sm text-tumtum-faint">
              Os cards acima são exemplos ilustrativos. Cada formato aparece pra
              você quando os dados daquela noite permitem.
            </p>
          </Reveal>
        </div>
      </section>

      {/* Na mesma vibe */}
      <section className="relative overflow-hidden py-28 md:py-[140px]">
        <VideoSlot
          label="Vídeo · artista no palco, visto da plateia"
          className="absolute inset-0"
        />
        <div className="absolute inset-0 z-[1] bg-[linear-gradient(90deg,rgba(0,0,0,0.92)_0%,rgba(0,0,0,0.7)_55%,rgba(0,0,0,0.45)_100%)]" />
        <div className="relative z-[2] mx-auto max-w-[1120px] px-6">
          <Reveal>
            <p className="mb-5 text-[13px] font-headline uppercase tracking-[0.16em] text-tumtum-pink">
              O próximo nível
            </p>
            <h2 className="max-w-[20ch] text-[clamp(32px,4.6vw,56px)] font-hero uppercase leading-[1.02] tracking-[-0.02em]">
              Na mesma vibe. Literalmente.
              <span className="ml-3.5 inline-block rounded-full border border-tumtum-yellow px-3 py-[5px] align-middle text-xs font-headline uppercase tracking-[0.12em] text-tumtum-yellow">
                Em breve
              </span>
            </h2>
            <p className="mt-5 max-w-[56ch] text-[clamp(16px,1.8vw,19px)] text-tumtum-muted">
              Quando o artista ou o jogador topa entrar na brincadeira, você
              compara o seu momento com o de quem estava no palco ou no campo.{' '}
              <strong className="font-headline text-tumtum-white">
                Você sentiu mais que o batedor do pênalti?
              </strong>{' '}
              Agora dá pra saber.
            </p>
          </Reveal>
        </div>
      </section>

      {/* Lista de espera */}
      <section
        id="lista"
        className="border-t border-tumtum-border bg-tumtum-pink py-20 text-tumtum-black md:py-[104px]"
      >
        <div className="mx-auto max-w-[1120px] px-6">
          <Reveal>
            <p className="mb-5 text-[13px] font-headline uppercase tracking-[0.16em] text-tumtum-black">
              Lista de espera
            </p>
            <h2 className="max-w-[16ch] text-[clamp(32px,4.6vw,56px)] font-hero uppercase leading-[1.02] tracking-[-0.02em]">
              Quero isso no meu próximo show.
            </h2>
            <p className="mt-5 max-w-[56ch] text-[clamp(16px,1.8vw,19px)] text-black/65">
              A TumTum está começando em São Paulo, evento por evento. Entra na
              lista e a gente te chama quando for a sua vez.
            </p>

            <WaitlistForm source="landing" />

            <p className="mt-5 max-w-[52ch] text-[13px] text-black/55">
              A gente só usa seu e-mail pra te avisar dos próximos eventos. Nada
              além disso.
            </p>
          </Reveal>
        </div>
      </section>

      <footer className="border-t border-tumtum-border pb-[72px] pt-14">
        <div className="mx-auto flex max-w-[1120px] flex-col gap-7 px-6">
          <Wordmark className="w-[200px] text-tumtum-white" />
          <div className="flex flex-wrap items-center gap-7">
            <div className="flex gap-2.5">
              {SOCIALS.map((social) => (
                <a
                  key={social.name}
                  href={social.href}
                  aria-label={social.name}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex h-[42px] w-[42px] items-center justify-center rounded-full border border-tumtum-border text-tumtum-white transition-colors hover:border-tumtum-pink hover:text-tumtum-pink"
                >
                  <svg viewBox="0 0 24 24" className="h-[18px] w-[18px]" fill="currentColor">
                    <path d={social.path} />
                  </svg>
                </a>
              ))}
            </div>
            <a
              href="mailto:oi@tumtum.cc"
              className="border-b-2 border-tumtum-yellow pb-0.5 text-[15px] font-headline text-tumtum-white transition-colors hover:text-tumtum-yellow"
            >
              oi@tumtum.cc
            </a>
          </div>
          <p className="max-w-[64ch] text-[13.5px] leading-relaxed text-tumtum-faint">
            Seus dados de batimento são seus. A TumTum só acessa o que você
            autorizar, só no intervalo do evento, e transforma em história apenas
            com a sua permissão.
          </p>
          <p className="text-[12.5px] text-tumtum-faint">TumTum · São Paulo · 2026</p>
        </div>
      </footer>
    </main>
  )
}
