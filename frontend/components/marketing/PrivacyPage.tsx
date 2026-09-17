import Link from 'next/link'

import { Wordmark } from '@/components/brand'
import type { PrivacyCopy } from '@/lib/privacy-copy'

/**
 * The privacy page: black canvas, white text, nothing pink louder than a
 * link. The manual says the brand goes quiet and careful on exactly this
 * screen, so there is no motion, no accent field, no joke — headings, short
 * paragraphs, and the address to write to.
 */
export function PrivacyPage({ copy }: { copy: PrivacyCopy }) {
  const home = copy.lang === 'en' ? '/en' : '/'

  return (
    <div className="min-h-screen bg-tumtum-black text-tumtum-white">
      <header className="border-b border-[#1E1E1E] px-6 py-6 md:px-16">
        <Link href={home} aria-label="TumTum" className="inline-block">
          <Wordmark className="h-5 w-auto text-tumtum-white" />
        </Link>
      </header>

      <main className="mx-auto max-w-[680px] px-6 py-14 md:px-0 md:py-20">
        <h1 className="text-[40px] font-bold leading-none tracking-tight md:text-[56px]">{copy.title}</h1>
        <p className="mt-6 text-lg leading-relaxed text-tumtum-muted">{copy.intro}</p>
        <p className="mt-3 text-xs text-tumtum-faint">{copy.updated}</p>

        <div className="mt-14 space-y-12">
          {copy.sections.map((section) => (
            <section key={section.heading}>
              <h2 className="text-xl font-semibold leading-snug md:text-2xl">{section.heading}</h2>
              {section.paragraphs.map((paragraph) => (
                <p key={paragraph} className="mt-4 leading-relaxed text-[#CFCFCF]">
                  {paragraph}
                </p>
              ))}
              {section.items && (
                <ul className="mt-4 space-y-3">
                  {section.items.map((item) => (
                    <li key={item} className="flex gap-3 leading-relaxed text-[#CFCFCF]">
                      <span aria-hidden="true" className="mt-[11px] h-1.5 w-1.5 shrink-0 rounded-full bg-tumtum-pink" />
                      <span>{item}</span>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          ))}
        </div>

        <section className="mt-16 border-t border-[#1E1E1E] pt-10">
          <p className="leading-relaxed text-[#CFCFCF]">{copy.contact.lead}</p>
          <a
            href={`mailto:${copy.contact.email}`}
            className="mt-3 inline-block text-lg font-semibold text-tumtum-pink underline-offset-4 hover:underline"
          >
            {copy.contact.email}
          </a>
        </section>

        <p className="mt-16 text-sm">
          <Link href={home} className="text-[#8A8A8A] transition-colors hover:text-tumtum-pink motion-reduce:transition-none">
            {copy.backHome}
          </Link>
        </p>
      </main>
    </div>
  )
}
