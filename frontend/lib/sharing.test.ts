import { describe, expect, it } from 'vitest'

import {
  audienceLabel,
  neverLine,
  platformLabel,
  reactionsLabel,
  safeHttpsUrl,
  sharingDate,
  splitLinks,
} from './sharing'

describe('audienceLabel', () => {
  it('names the one night and the whole tour', () => {
    expect(audienceLabel('evento')).toBe('Só no feed deste evento')
    expect(audienceLabel('turnê')).toBe('No feed de todas as datas da turnê')
  })

  it('passes an unknown audience through rather than guessing', () => {
    expect(audienceLabel('clube')).toBe('clube')
  })
})

describe('platformLabel', () => {
  it('writes networks the way people do', () => {
    expect(platformLabel('whatsapp')).toBe('WhatsApp')
    expect(platformLabel('tiktok')).toBe('TikTok')
    expect(platformLabel('x')).toBe('X')
    expect(platformLabel('native')).toBe('Menu de compartilhar do celular')
    expect(platformLabel('bluesky')).toBe('bluesky')
  })
})

describe('reactionsLabel', () => {
  it('counts in Portuguese', () => {
    expect(reactionsLabel(0)).toBe('Nenhuma reação')
    expect(reactionsLabel(1)).toBe('1 reação')
    expect(reactionsLabel(3)).toBe('3 reações')
  })
})

describe('sharingDate', () => {
  it('reads the date in São Paulo, not in UTC', () => {
    // 02:00 UTC on the 26th is still 23:00 on the 25th in São Paulo.
    expect(sharingDate('2026-09-26T02:00:00Z')).toBe('25 de setembro de 2026')
  })

  it('says it cannot read a date instead of inventing one', () => {
    expect(sharingDate('not a date')).toBe('data desconhecida')
  })
})

describe('safeHttpsUrl', () => {
  it('keeps only https links', () => {
    expect(safeHttpsUrl('https://tumtum.cc/cards/abc')).toBe('https://tumtum.cc/cards/abc')
    expect(safeHttpsUrl('http://tumtum.cc/cards/abc')).toBeNull()
    expect(safeHttpsUrl('javascript:alert(1)')).toBeNull()
    expect(safeHttpsUrl('/cards/abc')).toBeNull()
    expect(safeHttpsUrl('')).toBeNull()
    expect(safeHttpsUrl(null)).toBeNull()
  })
})

describe('neverLine', () => {
  it('is the one sentence the section promises', () => {
    expect(neverLine(['clubes', 'artistas', 'produtoras', 'festivais', 'anunciantes'])).toBe(
      'Nunca vai para: clubes, artistas, produtoras, festivais, anunciantes.',
    )
  })

  it('says nothing when the server sends no list', () => {
    expect(neverLine([])).toBeNull()
    expect(neverLine(['  '])).toBeNull()
  })
})

describe('splitLinks', () => {
  it('turns an https address into a link and leaves the full stop as text', () => {
    expect(splitLinks('Canal: https://www.gov.br/anpd. Fim')).toEqual([
      { text: 'Canal: ' },
      { text: 'https://www.gov.br/anpd', href: 'https://www.gov.br/anpd' },
      { text: '. Fim' },
    ])
  })

  it('leaves text without a link whole', () => {
    expect(splitLinks('Sem link aqui.')).toEqual([{ text: 'Sem link aqui.' }])
  })
})
