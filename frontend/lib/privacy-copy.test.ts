import { describe, it, expect } from 'vitest'

import { DELETE_ACCOUNT_EN, DELETE_ACCOUNT_PT } from './delete-account-copy'
import { PRIVACY_EN, PRIVACY_PT, type PrivacyCopy } from './privacy-copy'
import { TERMS_EN, TERMS_PT } from './terms-copy'

const all = (copy: PrivacyCopy) =>
  [
    copy.intro,
    copy.disclaimer ?? '',
    ...copy.sections.flatMap((s) => [s.heading, ...s.paragraphs, ...(s.items ?? [])]),
  ].join('\n')

describe('the legal pages exist in both languages, section for section', () => {
  it.each([
    ['privacy', PRIVACY_PT, PRIVACY_EN],
    ['terms', TERMS_PT, TERMS_EN],
    ['delete account', DELETE_ACCOUNT_PT, DELETE_ACCOUNT_EN],
  ])('%s', (_name, pt, en) => {
    expect(en.sections).toHaveLength(pt.sections.length)
    pt.sections.forEach((section, i) => {
      expect(en.sections[i].paragraphs).toHaveLength(section.paragraphs.length)
      expect(en.sections[i].items?.length ?? 0).toBe(section.items?.length ?? 0)
    })
  })
})

describe('the privacy policy says what the LGPD audit found missing', () => {
  const pt = all(PRIVACY_PT)

  it.each([
    ['legal basis', 'art. 11'],
    ['sensitive data', 'dado pessoal sensível'],
    ['controller placeholder, visibly pending', '[CONTROLADOR — preencher razão social e CNPJ]'],
    ['officer channel', 'assunto "Privacidade"'],
    ['R-R and motion', 'Intervalos R-R'],
    ['imports', 'Samsung Health'],
    ['crowd threshold', '100 noites'],
    ['no clubs or artists', 'Nenhum dado pessoal vai para clube, artista, produtora, festival ou anunciante'],
    ['raw series retention', '30 dias'],
    ['access logs', '180 dias'],
    ['card cache', '7 dias'],
    ['sign-up code', '24 horas'],
    ['waitlist', 'até você pedir pra sair'],
    ['operators', 'Railway'],
    ['operators', 'Vercel'],
    ['operators', 'Resend'],
    ['operators', 'Sentry'],
    ['international transfer', 'fora do Brasil'],
    ['response time', '15 dias'],
    ['minimum age', '18 anos'],
    ['not medical', 'não é um dispositivo médico'],
  ])('%s', (_what, phrase) => {
    expect(pt).toContain(phrase)
  })

  it('opens with the medical disclaimer in both languages', () => {
    expect(PRIVACY_PT.disclaimer).toBe('A TumTum não é um dispositivo médico e não interpreta saúde.')
    expect(PRIVACY_EN.disclaimer).toBe('TumTum is not a medical device and does not interpret health.')
  })

  it('no longer claims deleting one night does not exist', () => {
    expect(pt).not.toContain('ainda não existe: hoje é tudo ou nada')
  })
})

describe('the terms', () => {
  it('say how they are accepted, the age, and that TumTum is not medical', () => {
    const pt = all(TERMS_PT)
    expect(pt).toContain('Li e aceito')
    expect(pt).toContain('18 anos ou mais')
    expect(pt).toContain('não é um dispositivo médico')
  })
})

describe('the deletion page', () => {
  it('points to the button on the site too', () => {
    expect(all(DELETE_ACCOUNT_PT)).toContain('Apagar pelo site')
    expect(all(DELETE_ACCOUNT_EN)).toContain('Delete on the website')
  })
})
