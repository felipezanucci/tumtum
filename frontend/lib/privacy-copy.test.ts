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
    ['controller, named until there is a CNPJ', 'Felipe Zanucci, pessoa física, São Paulo, SP. Até a TumTum ter CNPJ, ele responde como controlador.'],
    ['officer channel', 'assunto "Privacidade"'],
    ['R-R and motion', 'Intervalos R-R'],
    ['imports', 'Samsung Health'],
    ['crowd threshold', '100 noites'],
    [
      'no clubs or artists (v1.1 §20.1)',
      'Seu dado cardíaco individual não é compartilhado com parceiros comerciais, clubes, artistas ou anunciantes',
    ],
    ['contracted providers act on our behalf', 'estritamente em nosso nome'],
    ['raw series retention', '7 dias'],
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
    ['where to see who holds the data', 'Com quem seus dados estão'],
    ['ANPD channel', 'https://www.gov.br/anpd/pt-br/canais_atendimento/cidadao-titular-de-dados'],
  ])('%s', (_what, phrase) => {
    expect(pt).toContain(phrase)
  })

  it('opens with the medical disclaimer in both languages', () => {
    expect(PRIVACY_PT.disclaimer).toBe('A TumTum não é um dispositivo médico e não interpreta saúde.')
    expect(PRIVACY_EN.disclaimer).toBe('TumTum is not a medical device and does not interpret health.')
  })

  it('makes no absolute promise that nobody else touches the data (v1.1 §20.1)', () => {
    const en = all(PRIVACY_EN)
    expect(pt).not.toContain('Nenhum dado pessoal vai para')
    expect(pt).not.toContain('Nunca.')
    expect(en).not.toContain('No personal data goes to')
    expect(en).not.toContain('Ever.')
    expect(en).toContain('strictly on our behalf')
  })

  it('does not promise that no backup exists', () => {
    expect(all(DELETE_ACCOUNT_PT)).not.toContain('sem cópia guardada')
    expect(all(DELETE_ACCOUNT_EN)).not.toContain('no copy kept')
    expect(pt).toContain('As cópias de segurança do banco expiram sozinhas, no prazo da nossa política de retenção')
  })

  it('leaves no placeholder a reader would see as unfinished (28/09)', () => {
    for (const copy of [PRIVACY_PT, PRIVACY_EN, TERMS_PT, TERMS_EN, DELETE_ACCOUNT_PT, DELETE_ACCOUNT_EN]) {
      const text = all(copy)
      expect(text).not.toContain('CONTROLADOR')
      expect(text).not.toMatch(/(Região|prazo|\[) a confirmar/i)
      expect(text).not.toContain('to be confirmed')
      expect(text).not.toContain('TumTum Tecnologia')
    }
    expect(all(DELETE_ACCOUNT_PT)).toContain('A TumTum é feita por Felipe Zanucci (São Paulo, SP).')
    expect(all(DELETE_ACCOUNT_EN)).toContain('TumTum is made by Felipe Zanucci (São Paulo, Brazil).')
    expect(all(PRIVACY_EN)).toContain('Until TumTum has a CNPJ, he is the controller.')
  })

  it('says what stays after a deletion: three things, in both pages', () => {
    for (const text of [pt, all(DELETE_ACCOUNT_PT)]) {
      expect(text).toContain('Ficam três coisas')
      expect(text).toContain('400 dias')
      expect(text).not.toContain('Fica só um registro')
    }
    for (const text of [all(PRIVACY_EN), all(DELETE_ACCOUNT_EN)]) {
      expect(text).toContain('Three things stay')
      expect(text).toContain('400 days')
    }
  })

  it('is honest about the access log, the transfer, the waitlist and Sentry', () => {
    expect(pt).toContain('Consultas diretas ao banco, que só quem administra o servidor pode fazer, não passam por esse registro.')
    expect(pt).toContain('cláusulas-padrão de transferência internacional')
    expect(pt).not.toContain('garantias contratuais')
    expect(pt).toContain('Na lista de espera do site: o e-mail e, se você preencheu, nome e sobrenome.')
    expect(pt).toContain('Sentry, só quando estiver ligado (hoje não está)')
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

  it('say the photos and videos never leave the phone, and make no feed rule about them', () => {
    const pt = all(TERMS_PT)
    expect(pt).toContain('Suas fotos e seus vídeos ficam no seu celular: a TumTum não recebe nenhum.')
    expect(pt).not.toContain('Foto ou vídeo de outra pessoa')
    expect(all(TERMS_EN)).not.toContain('Someone else’s photo or video')
  })
})

describe('the deletion page', () => {
  it('points to the button on the site too', () => {
    expect(all(DELETE_ACCOUNT_PT)).toContain('Apagar pelo site')
    expect(all(DELETE_ACCOUNT_EN)).toContain('Delete on the website')
  })
})
