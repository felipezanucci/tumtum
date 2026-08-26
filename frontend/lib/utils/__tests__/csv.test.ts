import { describe, expect, it } from 'vitest'

import { toCsv } from '../csv'

describe('toCsv', () => {
  it('writes a header and one row per entry', () => {
    expect(toCsv(['email'], [['a@b.com'], ['c@d.com']])).toBe(
      'email\na@b.com\nc@d.com',
    )
  })

  it('leaves ordinary values unquoted', () => {
    expect(toCsv(['email', 'origem'], [['a@b.com', 'landing']])).toBe(
      'email,origem\na@b.com,landing',
    )
  })

  it('quotes a value containing a comma', () => {
    // Without this the row gains a column and every later column shifts.
    expect(toCsv(['nota'], [['São Paulo, SP']])).toBe('nota\n"São Paulo, SP"')
  })

  it('doubles quotes inside a quoted value', () => {
    expect(toCsv(['nota'], [['ele disse "oi"']])).toBe('nota\n"ele disse ""oi"""')
  })

  it('quotes a value containing a newline', () => {
    expect(toCsv(['nota'], [['linha1\nlinha2']])).toBe('nota\n"linha1\nlinha2"')
  })

  it('handles an empty row set', () => {
    expect(toCsv(['email'], [])).toBe('email')
  })
})
