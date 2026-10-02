/**
 * The consent screen's words, in both languages (26/09, LGPD remediation).
 *
 * Heart rate is sensitive personal data (LGPD art. 5 II, art. 11), and the
 * only lawful basis this product has for it is consent that is specific,
 * separate per purpose, recorded and revocable. So each purpose is one
 * switch with its own sentences, and the version of these words travels
 * with every grant: `CONSENT_TEXT_VERSION` is what the server stores next to
 * the "yes".
 *
 * **The sentences are not written here** (02/10). They live once, in
 * `shared/consent/consent-text.json`, and `scripts/consent_text.py` writes
 * them into `consent-text.generated.ts` (imported below), the Android
 * strings and the server — the same words, verbatim, on every screen that
 * asks. On 02/10 the app and the site disagreed under one version; a
 * backend test now fails when they drift. What stays here is the chrome
 * around the text: tags, button labels, the version line.
 *
 * This is the screen where the brand goes quiet: no jokes, no pink fields,
 * short sentences that say exactly what happens.
 */

import {
  CONSENT_PURPOSES,
  CONSENT_TEXT_EN,
  CONSENT_TEXT_PT,
  CONSENT_TEXT_VERSION,
  CORE_PURPOSES,
  type ConsentPurpose,
  type PurposeText,
} from './consent-text.generated'

export { CONSENT_PURPOSES, CONSENT_TEXT_VERSION, CORE_PURPOSES }
export type { ConsentPurpose }

export function isConsentPurpose(value: unknown): value is ConsentPurpose {
  return typeof value === 'string' && (CONSENT_PURPOSES as readonly string[]).includes(value)
}

/** One purpose's words, as the shared text names them. */
export type PurposeCopy = PurposeText

export interface ConsentCopy {
  lang: 'pt-BR' | 'en'
  title: string
  intro: string
  /** The facts a person needs before any switch: one short line each. */
  facts: readonly string[]
  purposes: Record<ConsentPurpose, PurposeCopy>
  requiredTag: string
  optionalTag: string
  /** Under a switch just saved on. */
  grantedNotice: string
  save: string
  saving: string
  saved: string
  version: string
  birthDate: { label: string; hint: string }
  termsLink: string
  privacyLink: string
}

export const CONSENT_PT: ConsentCopy = {
  lang: 'pt-BR',
  title: 'Seus batimentos, suas regras',
  intro:
    'Antes de tudo, o que a TumTum faz com seu batimento. Cada uso tem um sim separado, e você muda qualquer um quando quiser.',
  facts: CONSENT_TEXT_PT.facts,
  purposes: CONSENT_TEXT_PT.purposes,
  requiredTag: 'Necessário',
  optionalTag: 'Opcional',
  grantedNotice: 'Ligado.',
  save: 'Salvar minhas escolhas',
  saving: 'Salvando…',
  saved: 'Pronto. Suas escolhas estão salvas.',
  version: `Versão do texto: ${CONSENT_TEXT_VERSION}`,
  birthDate: {
    label: 'Data de nascimento',
    hint: 'A TumTum é só para quem tem 18 anos ou mais.',
  },
  termsLink: 'Termos de Uso',
  privacyLink: 'Política de Privacidade',
}

export const CONSENT_EN: ConsentCopy = {
  lang: 'en',
  title: 'Your heartbeat, your rules',
  intro:
    'First, what TumTum does with your heartbeat. Each use has its own yes, and you can change any of them whenever you like.',
  facts: CONSENT_TEXT_EN.facts,
  purposes: CONSENT_TEXT_EN.purposes,
  requiredTag: 'Required',
  optionalTag: 'Optional',
  grantedNotice: 'On.',
  save: 'Save my choices',
  saving: 'Saving…',
  saved: 'Done. Your choices are saved.',
  version: `Text version: ${CONSENT_TEXT_VERSION}`,
  birthDate: {
    label: 'Date of birth',
    hint: 'TumTum is only for people aged 18 or over.',
  },
  termsLink: 'Terms of Use',
  privacyLink: 'Privacy Policy',
}
