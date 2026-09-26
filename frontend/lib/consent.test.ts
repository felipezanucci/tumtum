import { describe, it, expect } from 'vitest'

import {
  changedChoices,
  choicesFrom,
  consentHref,
  isAdult,
  latestAdultBirthDate,
  needsConsentGate,
  safeNext,
  signupReady,
} from './consent'
import {
  CONSENT_EN,
  CONSENT_PT,
  CONSENT_PURPOSES,
  CONSENT_TEXT_VERSION,
  CORE_PURPOSES,
} from './consent-copy'

const day = (y: number, m: number, d: number) => new Date(y, m - 1, d)

describe('isAdult', () => {
  it('is true on the 18th birthday itself', () => {
    expect(isAdult('2008-09-26', day(2026, 9, 26))).toBe(true)
  })

  it('is false the day before', () => {
    expect(isAdult('2008-09-27', day(2026, 9, 26))).toBe(false)
  })

  it('counts a 29 February birthday from 1 March in a common year', () => {
    expect(isAdult('2008-02-29', day(2026, 2, 28))).toBe(false)
    expect(isAdult('2008-02-29', day(2026, 3, 1))).toBe(true)
  })

  it('returns null for something that is not a date', () => {
    expect(isAdult('', day(2026, 9, 26))).toBeNull()
    expect(isAdult('26/09/2000', day(2026, 9, 26))).toBeNull()
    expect(isAdult('2000-02-31', day(2026, 9, 26))).toBeNull()
  })
})

describe('latestAdultBirthDate', () => {
  it('is today eighteen years back', () => {
    expect(latestAdultBirthDate(day(2026, 9, 26))).toBe('2008-09-26')
  })

  it('falls back to 28 February when 29 February did not exist', () => {
    expect(latestAdultBirthDate(day(2028, 2, 29))).toBe('2010-02-28')
  })

  it('agrees with isAdult at the edge', () => {
    const today = day(2026, 1, 5)
    expect(isAdult(latestAdultBirthDate(today), today)).toBe(true)
  })
})

describe('choices', () => {
  it('starts every purpose off, whatever the server left out', () => {
    const choices = choicesFrom([{ purpose: 'terms', granted: true }])
    expect(choices.terms).toBe(true)
    for (const purpose of CONSENT_PURPOSES.filter((p) => p !== 'terms')) {
      expect(choices[purpose]).toBe(false)
    }
  })

  it('ignores purposes this client does not know', () => {
    const choices = choicesFrom([{ purpose: 'sell_everything', granted: true }])
    expect(Object.values(choices).every((v) => v === false)).toBe(true)
  })

  it('sends only what changed', () => {
    const before = choicesFrom([{ purpose: 'terms', granted: true }])
    const after = { ...before, keep_night: true, marketing: false }
    expect(changedChoices(before, after)).toEqual({ keep_night: true })
    expect(changedChoices(before, before)).toEqual({})
  })
})

describe('needsConsentGate', () => {
  const granted = [{ purpose: 'terms', granted: true }]

  it('stops an account with no birth date', () => {
    expect(needsConsentGate({ birth_date: null }, granted)).toBe(true)
  })

  it('stops an account that has not accepted the Terms', () => {
    expect(needsConsentGate({ birth_date: '1990-01-01' }, [{ purpose: 'terms', granted: false }])).toBe(true)
    expect(needsConsentGate({ birth_date: '1990-01-01' }, [])).toBe(true)
  })

  it('lets through an account with both', () => {
    expect(needsConsentGate({ birth_date: '1990-01-01' }, granted)).toBe(false)
  })

  it('does not treat a failed request as a "no"', () => {
    expect(needsConsentGate({ birth_date: '1990-01-01' }, null)).toBe(false)
  })

  it('does nothing when nobody is signed in', () => {
    expect(needsConsentGate(null, [])).toBe(false)
  })
})

describe('where a refusal sends the person', () => {
  it('focuses the purpose and remembers the way back', () => {
    expect(consentHref('keep_night', '/import')).toBe('/consentimento?focus=keep_night&next=%2Fimport')
  })

  it('drops a purpose it does not know', () => {
    expect(consentHref('whatever', null)).toBe('/consentimento')
  })

  it('never sends anyone off the site', () => {
    expect(safeNext('//evil.example')).toBeNull()
    expect(safeNext('https://evil.example')).toBeNull()
    expect(safeNext('/\\evil.example')).toBeNull()
    expect(safeNext('/consentimento?focus=terms')).toBeNull()
    expect(safeNext('/cards')).toBe('/cards')
  })
})

describe('signupReady', () => {
  it('needs the Terms ticked and a real birth date', () => {
    expect(signupReady({ birthDate: '1990-01-01', termsAccepted: true })).toBe(true)
    expect(signupReady({ birthDate: '1990-01-01', termsAccepted: false })).toBe(false)
    expect(signupReady({ birthDate: '', termsAccepted: true })).toBe(false)
  })
})

describe('the consent text', () => {
  it('carries the version every codebase shares', () => {
    expect(CONSENT_TEXT_VERSION).toBe('2026-09-26')
  })

  it('names exactly the seven purposes of the contract', () => {
    expect([...CONSENT_PURPOSES].sort()).toEqual(
      ['artist_compare', 'crowd_stats', 'improve_detection', 'keep_night', 'marketing', 'read_heart_rate', 'terms'],
    )
    expect(CORE_PURPOSES).toEqual(['terms', 'read_heart_rate'])
  })

  it('has a title and a sentence for every purpose, in both languages', () => {
    for (const copy of [CONSENT_PT, CONSENT_EN]) {
      for (const purpose of CONSENT_PURPOSES) {
        expect(copy.purposes[purpose].title.length).toBeGreaterThan(3)
        expect(copy.purposes[purpose].description.length).toBeGreaterThan(20)
      }
      expect(copy.facts).toHaveLength(CONSENT_PT.facts.length)
    }
  })

  it('says the four things the law needs said before any switch', () => {
    const text = CONSENT_PT.facts.join(' ')
    expect(text).toContain('dado de saúde')
    expect(text).toContain('30 minutos')
    expect(text).toContain('30 dias')
    expect(text).toContain('não é um dispositivo médico')
    expect(text).toContain('clubes, artistas')
  })
})
