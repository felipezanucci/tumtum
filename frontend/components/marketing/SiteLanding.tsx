import Link from 'next/link'

import { Wordmark } from '@/components/brand/Wordmark'
import { SiteWaitlistForm } from '@/components/marketing/SiteWaitlistForm'
import type { SiteCopy } from '@/lib/site-copy'

/**
 * The public site, v0.4 redesign — built from the Claude Design handoff of
 * 2026-09-01, whose README is the spec this file follows. One layout, two
 * languages: the PT and EN routes both render this component and differ only
 * in the copy object they pass, so a section cannot exist in one language
 * and quietly miss the other.
 *
 * Two rules from the handoff worth restating where the code lives:
 * - No scroll animations, no pulse, no heartbeat motion — the brand forbids
 *   ECG-shaped movement, and the design deliberately has none.
 * - On Pink or Yellow, everything is black. A number is pink only on a dark
 *   ground. White never sits on either accent.
 *
 * The clips shipped as ~25 MB of GIF in the handoff and were converted to
 * short muted MP4 loops (~1.4 MB total) exactly as its README instructs; the
 * poster frames cover the moment before the video loads and any browser that
 * refuses autoplay.
 */

const SOCIALS = [
  { label: 'Instagram', href: 'https://www.instagram.com/tumtum.cc' },
  { label: 'TikTok', href: 'https://www.tiktok.com/@tumtum.cc' },
  { label: 'X', href: 'https://x.com/TumTumcc' },
  { label: 'LinkedIn', href: 'https://www.linkedin.com/company/tumtumcc' },
]

function Clip({ src, className = '' }: { src: string; className?: string }) {
  return (
    <video
      className={`h-full w-full object-cover ${className}`}
      src={`/site/${src}.mp4`}
      poster={`/site/${src}-poster.jpg`}
      autoPlay
      muted
      loop
      playsInline
      aria-hidden="true"
    />
  )
}

function LangSwitch({ copy }: { copy: SiteCopy }) {
  const item = (lang: 'PT' | 'EN', href: string, active: boolean) =>
    active ? (
      <span className="font-hero text-tumtum-black underline underline-offset-[3px]">{lang}</span>
    ) : (
      <Link href={href} className="font-label text-[#B4B4B4] transition-colors hover:text-tumtum-black motion-reduce:transition-none">
        {lang}
      </Link>
    )
  return (
    <span className="flex items-center gap-1.5 text-xs">
      {item('PT', '/', copy.lang === 'pt')}
      <span aria-hidden="true" className="text-[#E6E6E6]">/</span>
      {item('EN', '/en', copy.lang === 'en')}
    </span>
  )
}

export function SiteLanding({ copy }: { copy: SiteCopy }) {
  const [soMomento, minhaNoite, aGalera, mesmaVibe] = copy.cards.items

  return (
    <div className="bg-tumtum-white text-tumtum-black">
      {/* ---------------------------------------------------------- nav */}
      <header className="sticky top-0 z-20 border-b border-[#E6E6E6] bg-tumtum-white">
        <nav className="mx-auto flex max-w-[1440px] items-center justify-between gap-3 px-4 py-[18px] md:gap-6 md:px-16 md:py-[22px]">
          <Link href={copy.lang === 'pt' ? '/' : '/en'} aria-label="TumTum">
            <Wordmark className="h-4 w-[106px] text-tumtum-black md:h-5 md:w-[132px]" />
          </Link>
          <div className="hidden items-center gap-7 text-sm font-headline md:flex">
            <a href="#como" className="transition-colors hover:text-tumtum-pink motion-reduce:transition-none">{copy.nav.how}</a>
            <a href="#cards" className="transition-colors hover:text-tumtum-pink motion-reduce:transition-none">{copy.nav.cards}</a>
            <a href="#feed" className="transition-colors hover:text-tumtum-pink motion-reduce:transition-none">{copy.nav.feed}</a>
            <a href="#galeria" className="transition-colors hover:text-tumtum-pink motion-reduce:transition-none">{copy.nav.gallery}</a>
            <Link href="/login" className="text-[#8A8A8A] transition-colors hover:text-tumtum-black motion-reduce:transition-none">
              {copy.nav.signIn}
            </Link>
          </div>
          <div className="flex items-center gap-3 md:gap-5">
            <a
              href="#lista"
              className="whitespace-nowrap rounded-full bg-tumtum-pink px-4 py-2 text-[13px] font-headline md:px-5 md:py-2.5 md:text-sm text-tumtum-black transition-colors hover:bg-tumtum-yellow motion-reduce:transition-none"
            >
              {copy.nav.cta}
            </a>
            <LangSwitch copy={copy} />
          </div>
        </nav>
      </header>

      {/* --------------------------------------------------------- hero */}
      <section className="bg-tumtum-pink">
        <div className="mx-auto flex max-w-[1440px] flex-col items-center gap-14 px-6 pb-24 pt-[88px] md:flex-row md:items-start md:justify-between md:px-16">
          <div className="max-w-[640px]">
            <h1 className="font-hero text-[clamp(54px,6vw,88px)] leading-[0.94] tracking-[-0.035em] text-tumtum-black">
              {copy.hero.title.map((line) => (
                <span key={line} className="block md:whitespace-nowrap">
                  {line}
                </span>
              ))}
            </h1>
            <p className="mt-7 max-w-[480px] text-xl leading-relaxed text-black/75">{copy.hero.sub}</p>
            <div className="mt-10 flex flex-wrap gap-4">
              <a
                href="#lista"
                className="rounded-xl bg-tumtum-black px-7 py-4 text-base font-headline text-tumtum-white transition-colors hover:bg-tumtum-yellow hover:text-tumtum-black motion-reduce:transition-none"
              >
                {copy.hero.ctaPrimary}
              </a>
              <a
                href="#como"
                className="rounded-xl border-2 border-tumtum-black px-7 py-4 text-base font-headline text-tumtum-black transition-colors hover:bg-tumtum-black hover:text-tumtum-white motion-reduce:transition-none"
              >
                {copy.hero.ctaSecondary}
              </a>
            </div>
          </div>

          {/* The 9:16 share-card mock. Everything on the white plate is black:
              the plate is the one light surface inside a pink section. */}
          <div className="w-[310px] shrink-0 overflow-hidden rounded-none bg-tumtum-black shadow-[0_40px_80px_rgba(0,0,0,0.28)]">
            <div className="relative h-[265px]">
              <Clip src="event-clip" />
              <span className="absolute left-4 top-4 rounded-full bg-tumtum-yellow px-3 py-1 text-[10px] font-headline tracking-[0.08em] text-tumtum-black">
                {copy.hero.card.event}
              </span>
            </div>
            <div className="bg-tumtum-white px-6 pb-7 pt-6">
              <p className="text-[13px] font-hero leading-snug text-tumtum-black">
                {copy.hero.card.copy.map((line) => (
                  <span key={line} className="block">
                    {line}
                  </span>
                ))}
              </p>
              <p className="mt-2 font-hero text-[126px] leading-none tracking-[-0.055em] text-tumtum-black tabular-nums">
                187
              </p>
              <p className="mt-1 text-[11px] text-black/60">{copy.hero.card.unit}</p>
              <Wordmark className="mt-5 h-3 w-[74px] text-tumtum-black" />
            </div>
          </div>
        </div>
      </section>

      {/* ------------------------------------------------- proof strip */}
      <section className="flex flex-col md:flex-row">
        {copy.proof.map((item, i) => (
          <div
            key={item.meta}
            className={
              'flex-1 px-8 py-12 ' +
              (i === 0
                ? 'bg-tumtum-black text-tumtum-white'
                : i === 1
                  ? 'bg-tumtum-yellow text-tumtum-black'
                  : 'bg-tumtum-white text-tumtum-black')
            }
          >
            <p className={`font-hero text-[64px] leading-none tracking-[-0.04em] tabular-nums ${i === 0 ? 'text-tumtum-pink' : ''}`}>
              {item.value}
            </p>
            <p className="mt-3 text-[15px] font-headline">{item.caption}</p>
            <p className={`mt-1 text-[12.5px] ${i === 0 ? 'text-[#8A8A8A]' : 'text-black/55'}`}>{item.meta}</p>
          </div>
        ))}
      </section>

      {/* --------------------------------------------------------- does */}
      <section className="bg-tumtum-white px-6 py-28 text-center">
        <div className="mx-auto max-w-[860px]">
          <p className="text-xs font-headline tracking-[0.16em] text-[#8A8A8A]">{copy.does.eyebrow}</p>
          <h2 className="mt-5 font-hero text-[clamp(34px,4vw,52px)] leading-tight tracking-[-0.03em]">
            {copy.does.title.map((line) => (
              <span key={line} className="block">
                {line}
              </span>
            ))}
          </h2>
          <p className="mx-auto mt-7 max-w-[700px] text-lg leading-relaxed text-black/70">
            {copy.does.body}
            <span className="bg-tumtum-yellow px-1 font-headline text-tumtum-black">{copy.does.highlight}</span>
            {copy.does.bodyEnd}
          </p>
        </div>
      </section>

      {/* ---------------------------------------------------- how #como */}
      <section id="como" className="bg-tumtum-black px-6 py-28 text-tumtum-white md:px-16">
        <div className="mx-auto max-w-[1200px]">
          <p className="text-xs font-headline tracking-[0.16em] text-[#8A8A8A]">{copy.how.eyebrow}</p>
          <h2 className="mt-5 font-hero text-[clamp(34px,4vw,52px)] leading-tight tracking-[-0.03em]">
            {copy.how.title.map((line) => (
              <span key={line} className="block">
                {line}
              </span>
            ))}
          </h2>
          <div className="mt-14 flex flex-col gap-0.5 md:flex-row">
            {copy.how.steps.map((step) => (
              <div key={step.n} className="flex-1 bg-[#0F0F0F] px-8 py-10">
                <p className="font-hero text-[44px] leading-none text-tumtum-pink tabular-nums">{step.n}</p>
                <h3 className="mt-5 text-xl font-headline text-tumtum-white">{step.title}</h3>
                <p className="mt-3 text-[15px] leading-relaxed text-[#8A8A8A]">{step.body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ------------------------------------------------- cards #cards */}
      <section id="cards" className="bg-tumtum-white px-6 py-28 text-center">
        <p className="text-xs font-headline tracking-[0.16em] text-[#8A8A8A]">{copy.cards.eyebrow}</p>
        <h2 className="mt-5 font-hero text-[clamp(34px,4vw,52px)] leading-tight tracking-[-0.03em]">
          {copy.cards.title.map((line) => (
            <span key={line} className="block">
              {line}
            </span>
          ))}
        </h2>
        <p className="mx-auto mt-6 max-w-[620px] text-base leading-relaxed text-black/70">{copy.cards.intro}</p>

        <div className="mx-auto mt-14 flex max-w-[1040px] flex-wrap justify-center gap-3.5">
          {/* Só o momento — white, media on top */}
          <article className="flex h-[412px] min-w-[170px] max-w-[248px] flex-1 basis-0 flex-col overflow-hidden border border-[#E6E6E6] bg-tumtum-white text-left">
            <div className="relative h-[226px] shrink-0">
              <Clip src="cold-play" />
              <span className="absolute left-3 top-3 rounded-full bg-black/60 px-2.5 py-1 text-[9px] font-headline tracking-[0.1em] text-tumtum-white">
                {soMomento.label}
              </span>
            </div>
            <div className="flex flex-1 flex-col px-5 py-4">
              <p className="text-[13px] font-hero leading-snug">
                {soMomento.copy.map((line) => (
                  <span key={line} className="block">
                    {line}
                  </span>
                ))}
              </p>
              <p className="mt-auto font-hero text-[72px] leading-none tracking-[-0.04em] tabular-nums">{soMomento.value}</p>
              <p className="mt-1 text-[11px] text-black/55">{soMomento.meta}</p>
            </div>
          </article>

          {/* Minha noite — black, with the continuous sparkline */}
          <article className="flex h-[412px] min-w-[170px] max-w-[248px] flex-1 basis-0 flex-col overflow-hidden bg-tumtum-black px-5 py-5 text-left text-tumtum-white">
            <span className="text-[9px] font-headline tracking-[0.1em] text-[#8A8A8A]">{minhaNoite.label}</span>
            <p className="mt-4 text-[15px] font-hero leading-snug">
              {minhaNoite.copy.map((line) => (
                <span key={line} className="block">
                  {line}
                </span>
              ))}
            </p>
            <p className="mt-auto font-hero text-[72px] leading-none tracking-[-0.04em] text-tumtum-pink tabular-nums">
              {minhaNoite.value}
            </p>
            {/* One continuous line — the moment the night peaked, dot inset so
                it renders whole. A chart of a night, never an ECG. */}
            <svg viewBox="0 0 1000 300" className="mt-4 h-16 w-full" aria-hidden="true">
              <path
                d="M 0 240 C 80 232, 140 246, 210 226 C 290 202, 340 236, 420 214 C 500 192, 560 226, 640 196 C 720 168, 780 208, 850 170 C 900 142, 950 60, 978 48 C 990 44, 996 56, 1000 70"
                fill="none"
                stroke="#FF6F91"
                strokeWidth="8"
                strokeLinecap="round"
              />
              <circle cx="978" cy="48" r="15" fill="#EFFF00" />
            </svg>
            <p className="mt-3 text-[11px] text-[#8A8A8A]">{minhaNoite.meta}</p>
          </article>

          {/* A galera — pink, media on top, black type */}
          <article className="flex h-[412px] min-w-[170px] max-w-[248px] flex-1 basis-0 flex-col overflow-hidden bg-tumtum-pink text-left">
            <div className="relative h-[226px] shrink-0">
              <Clip src="torcida" />
              <span className="absolute left-3 top-3 rounded-full bg-black/60 px-2.5 py-1 text-[9px] font-headline tracking-[0.1em] text-tumtum-white">
                {aGalera.label}
              </span>
            </div>
            <div className="flex flex-1 flex-col px-5 py-4 text-tumtum-black">
              <p className="text-[13px] font-hero leading-snug">
                {aGalera.copy.map((line) => (
                  <span key={line} className="block">
                    {line}
                  </span>
                ))}
              </p>
              <div className="mt-auto flex items-end gap-5">
                <div>
                  <p className="text-[9px] font-headline tracking-[0.1em]">{aGalera.youLabel}</p>
                  <p className="font-hero text-[34px] leading-none tabular-nums">{aGalera.you}</p>
                </div>
                <div>
                  <p className="text-[9px] font-headline tracking-[0.1em] text-black/45">{aGalera.crowdLabel}</p>
                  <p className="font-hero text-[34px] leading-none text-black/45 tabular-nums">{aGalera.crowd}</p>
                </div>
              </div>
              <p className="mt-2 text-[11px] text-black/60">{aGalera.meta}</p>
            </div>
          </article>

          {/* Na mesma vibe — yellow, all black */}
          <article className="flex h-[412px] min-w-[170px] max-w-[248px] flex-1 basis-0 flex-col overflow-hidden bg-tumtum-yellow px-5 py-5 text-left text-tumtum-black">
            <span className="text-[9px] font-headline tracking-[0.1em]">{mesmaVibe.label}</span>
            <p className="mt-4 text-[15px] font-hero leading-snug">
              {mesmaVibe.copy.map((line) => (
                <span key={line} className="block">
                  {line}
                </span>
              ))}
            </p>
            <p className="mt-auto font-hero text-[72px] leading-none tracking-[-0.04em] tabular-nums">{mesmaVibe.value}</p>
            <p className="mt-2 text-[11px] text-black/60">{mesmaVibe.meta}</p>
          </article>
        </div>

        <p className="mx-auto mt-8 max-w-[620px] text-[12.5px] text-[#8A8A8A]">{copy.cards.disclaimer}</p>
      </section>

      {/* --------------------------------------------------- feed #feed */}
      <section id="feed" className="overflow-hidden bg-tumtum-black px-6 py-28 text-tumtum-white md:px-16">
        <div className="mx-auto flex max-w-[1200px] flex-col items-center gap-16 md:flex-row md:justify-between">
          <div className="flex w-full max-w-full shrink-0 justify-center gap-4 md:w-auto md:gap-6">
            {/* eslint-disable-next-line @next/next/no-img-element -- static mockups, exact size, no optimization pipeline needed */}
            <img
              src="/site/shots/09-feed.png"
              alt=""
              width={264}
              height={572}
              className="w-[43vw] max-w-[220px] rounded-[22px] border border-[#2E2E2E] md:w-[264px] md:max-w-none"
            />
            {/* eslint-disable-next-line @next/next/no-img-element -- static mockups, exact size, no optimization pipeline needed */}
            <img
              src="/site/shots/10-feed-evento.png"
              alt=""
              width={264}
              height={572}
              className="mt-14 w-[43vw] max-w-[220px] rounded-[22px] border border-[#2E2E2E] md:w-[264px] md:max-w-none"
            />
          </div>
          <div className="max-w-[520px]">
            <p className="text-xs font-headline tracking-[0.16em] text-[#8A8A8A]">{copy.feed.eyebrow}</p>
            <h2 className="mt-5 font-hero text-[clamp(34px,4vw,52px)] leading-tight tracking-[-0.03em]">{copy.feed.title}</h2>
            <p className="mt-6 text-base leading-relaxed text-[#B4B4B4]">
              {copy.feed.body}
              <span className="bg-tumtum-yellow px-1 font-headline text-tumtum-black">{copy.feed.button}</span>
              {copy.feed.bodyEnd}
            </p>
            <div className="mt-10 space-y-6">
              {copy.feed.stats.map((stat) => (
                <div key={stat.value}>
                  <p className="font-hero text-[26px] leading-none text-tumtum-pink tabular-nums">{stat.value}</p>
                  <p className="mt-1.5 max-w-[440px] text-[14px] leading-relaxed text-[#8A8A8A]">{stat.text}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* --------------------------------------------- gallery #galeria */}
      <section id="galeria" className="overflow-hidden bg-tumtum-pink px-6 py-28 text-tumtum-black md:px-16">
        <div className="mx-auto flex max-w-[1200px] flex-col items-center gap-16 md:flex-row md:justify-between">
          <div className="max-w-[520px]">
            <p className="text-xs font-headline tracking-[0.16em] text-black/55">{copy.gallery.eyebrow}</p>
            <h2 className="mt-5 font-hero text-[clamp(34px,4vw,52px)] leading-tight tracking-[-0.03em]">{copy.gallery.title}</h2>
            <p className="mt-6 text-base leading-relaxed text-black/70">{copy.gallery.body}</p>
            <div className="mt-10 flex gap-10">
              {copy.gallery.stats.map((stat) => (
                <div key={stat.label}>
                  <p className="font-hero text-[40px] leading-none tabular-nums">{stat.value}</p>
                  <p className="mt-1 text-[11px] font-headline tracking-[0.12em] text-black/55">{stat.label}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Four nights, four skins, one silhouette — each mini leans its own
              way. Numbers are pink only over the dark scrim; on the yellow
              mini everything is black. */}
          <div className="grid shrink-0 grid-cols-2 gap-x-[22px] gap-y-[18px]">
            {copy.gallery.minis.map((mini, i) => {
              const rotations = ['-rotate-2', 'rotate-[1.6deg]', 'rotate-[1.2deg]', '-rotate-[1.6deg]']
              const offsets = ['translate-y-[30px]', '-translate-y-[14px]', 'translate-y-[18px]', '']
              const clips = ['event-clip', 'torcida', 'cold-play', null]
              const numberColor = ['text-tumtum-pink', 'text-tumtum-white', 'text-tumtum-white', 'text-tumtum-black']
              const chips = [
                'bg-tumtum-yellow text-tumtum-black',
                'bg-tumtum-pink text-tumtum-black',
                'bg-tumtum-yellow text-tumtum-black',
                'bg-tumtum-black text-tumtum-yellow',
              ]
              return (
                <div
                  key={mini.label}
                  className={`relative aspect-[9/14] w-[160px] overflow-hidden shadow-[0_28px_56px_rgba(0,0,0,0.28)] md:w-[196px] ${rotations[i]} ${offsets[i]} ${clips[i] ? 'bg-tumtum-black' : 'bg-tumtum-yellow'}`}
                >
                  {clips[i] && (
                    <>
                      <div className="absolute inset-0">
                        <Clip src={clips[i]!} />
                      </div>
                      <div className="absolute inset-0 bg-gradient-to-b from-transparent from-[34%] to-black/[.88] to-[84%]" />
                    </>
                  )}
                  <span
                    className={`absolute left-3 top-3 rounded-full px-2.5 py-1 text-[10px] font-headline tabular-nums ${chips[i]}`}
                  >
                    {mini.time}
                  </span>
                  <div className="absolute bottom-4 left-4">
                    <p className={`font-hero text-[56px] leading-none tracking-[-0.04em] tabular-nums ${numberColor[i]}`}>
                      {mini.value}
                    </p>
                    <p
                      className={`mt-1 text-[10px] font-headline tracking-[0.12em] ${i === 3 ? 'text-tumtum-black' : 'text-tumtum-white'}`}
                    >
                      {mini.label}
                    </p>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      </section>

      {/* ----------------------------------------------- waitlist #lista */}
      <section id="lista" className="bg-tumtum-black px-6 py-28 text-center text-tumtum-white">
        <p className="text-xs font-headline tracking-[0.16em] text-[#8A8A8A]">{copy.waitlist.eyebrow}</p>
        <h2 className="mt-5 font-hero text-[clamp(34px,4vw,52px)] leading-tight tracking-[-0.03em]">
          {copy.waitlist.title.map((line) => (
            <span key={line} className="block">
              {line}
            </span>
          ))}
        </h2>
        <p className="mx-auto mt-6 max-w-[520px] text-base leading-relaxed text-[#B4B4B4]">{copy.waitlist.body}</p>
        <SiteWaitlistForm copy={copy.waitlist} />
      </section>

      {/* ------------------------------------------------------- footer */}
      <footer className="border-t border-[#1E1E1E] bg-tumtum-black px-6 py-16 text-tumtum-white md:px-16">
        <div className="mx-auto max-w-[1200px]">
          <Wordmark className="h-4 w-[110px] text-tumtum-white" />
          <div className="mt-8 flex flex-wrap gap-6 text-[13px] text-[#8A8A8A]">
            {SOCIALS.map((social) => (
              <a
                key={social.label}
                href={social.href}
                target="_blank"
                rel="noopener noreferrer"
                className="transition-colors hover:text-tumtum-pink motion-reduce:transition-none"
              >
                {social.label}
              </a>
            ))}
            <a href="mailto:oi@tumtum.cc" className="transition-colors hover:text-tumtum-pink motion-reduce:transition-none">
              oi@tumtum.cc
            </a>
          </div>
          <p className="mt-8 max-w-[420px] text-xs leading-relaxed text-[#4A4A4A]">{copy.footer.privacy}</p>
          <div className="mt-10 flex flex-wrap items-center justify-between gap-4 text-xs text-[#4A4A4A]">
            <span>{copy.footer.line}</span>
            <Link href="/login" className="text-[#8A8A8A] transition-colors hover:text-tumtum-pink motion-reduce:transition-none">
              {copy.footer.signIn}
            </Link>
          </div>
        </div>
      </footer>
    </div>
  )
}
