import { describe, expect, it } from 'vitest'

import {
  BIRTH_DAYS,
  MONTHS_PT,
  assembleBirthDate,
  birthYears,
  isImpossibleBirthDate,
} from './birth-date'
import { isAdult, signupReady } from './consent'

const day = (y: number, m: number, d: number) => new Date(y, m - 1, d)

describe('assembleBirthDate', () => {
  it('builds the ISO date the API takes, zero-padded', () => {
    expect(assembleBirthDate('5', '3', '1990')).toBe('1990-03-05')
    expect(assembleBirthDate('31', '12', '2000')).toBe('2000-12-31')
  })

  it('stays empty while any of the three is missing', () => {
    expect(assembleBirthDate('', '3', '1990')).toBe('')
    expect(assembleBirthDate('5', '', '1990')).toBe('')
    expect(assembleBirthDate('5', '3', '')).toBe('')
  })

  it('stays empty for a date that does not exist', () => {
    expect(assembleBirthDate('31', '2', '1990')).toBe('')
    expect(assembleBirthDate('29', '2', '2001')).toBe('')
    expect(assembleBirthDate('31', '4', '1990')).toBe('')
    expect(assembleBirthDate('0', '1', '1990')).toBe('')
    expect(assembleBirthDate('1', '13', '1990')).toBe('')
  })

  it('takes 29 February in a leap year', () => {
    expect(assembleBirthDate('29', '2', '2000')).toBe('2000-02-29')
  })

  it('feeds signupReady and isAdult unchanged', () => {
    const iso = assembleBirthDate('26', '9', '2008')
    expect(isAdult(iso, day(2026, 9, 26))).toBe(true)
    expect(signupReady({ birthDate: iso, termsAccepted: true, readHeartRate: true })).toBe(true)
    expect(
      signupReady({ birthDate: assembleBirthDate('31', '2', '1990'), termsAccepted: true, readHeartRate: true }),
    ).toBe(false)
  })
})

describe('isImpossibleBirthDate', () => {
  it('is true only when all three are picked and make no date', () => {
    expect(isImpossibleBirthDate('31', '2', '1990')).toBe(true)
    expect(isImpossibleBirthDate('28', '2', '1990')).toBe(false)
    expect(isImpossibleBirthDate('31', '2', '')).toBe(false)
  })
})

describe('the lists', () => {
  it('offers years from eighteen back to one hundred and twenty back, newest first', () => {
    const years = birthYears(day(2026, 9, 28))
    expect(years[0]).toBe(2008)
    expect(years[years.length - 1]).toBe(1906)
    expect(years).toHaveLength(103)
  })

  it('has 31 days and twelve months in pt-BR', () => {
    expect(BIRTH_DAYS).toHaveLength(31)
    expect(BIRTH_DAYS[0]).toBe(1)
    expect(MONTHS_PT).toHaveLength(12)
    expect(MONTHS_PT[0]).toBe('janeiro')
    expect(MONTHS_PT[2]).toBe('março')
  })
})
