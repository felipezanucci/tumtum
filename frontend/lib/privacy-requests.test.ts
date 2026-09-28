import { describe, it, expect } from 'vitest'

import {
  ADMIN_REQUEST_KIND_LABELS,
  REQUEST_KINDS,
  adminDueLine,
  adminRequestKindLabel,
  REQUEST_KIND_LABELS,
  requestDueLine,
  requestKindLabel,
  requestStatusLabel,
} from './privacy-requests'

const NOW = new Date('2026-09-26T15:00:00-03:00')

describe('request labels', () => {
  it('has a label for every kind the API accepts', () => {
    expect([...REQUEST_KINDS].sort()).toEqual(
      ['access', 'correction', 'deletion', 'other', 'portability', 'revocation'],
    )
    for (const kind of REQUEST_KINDS) expect(REQUEST_KIND_LABELS[kind]).toBeTruthy()
  })

  it('shows an unknown value as itself rather than nothing', () => {
    expect(requestKindLabel('something_new')).toBe('something_new')
    expect(requestStatusLabel('open')).toBe('Aberto')
    expect(requestStatusLabel('weird')).toBe('weird')
  })
})

describe('requestDueLine', () => {
  it('gives the due date of an open request', () => {
    const line = requestDueLine(
      { status: 'open', due_at: '2026-10-11T15:00:00-03:00', answered_at: null },
      NOW,
    )
    expect(line).toBe('Resposta até 11 de outubro de 2026')
  })

  it('says so when the TumTum is late', () => {
    const line = requestDueLine(
      { status: 'open', due_at: '2026-09-20T15:00:00-03:00', answered_at: null },
      NOW,
    )
    expect(line).toContain('Prazo vencido')
  })

  it('says when an answered request was answered', () => {
    const line = requestDueLine(
      { status: 'answered', due_at: '2026-10-11T15:00:00-03:00', answered_at: '2026-09-30T12:00:00-03:00' },
      NOW,
    )
    expect(line).toBe('Respondido em 30 de setembro de 2026')
  })
})

describe('the operator queue', () => {
  it('names every kind in one word', () => {
    expect(ADMIN_REQUEST_KIND_LABELS).toEqual({
      access: 'Acesso',
      portability: 'Portabilidade',
      correction: 'Correção',
      deletion: 'Exclusão',
      revocation: 'Revogação',
      other: 'Outro',
    })
    expect(adminRequestKindLabel('novo')).toBe('novo')
  })

  it('counts the deadline in São Paulo calendar days', () => {
    // NOW is 26/09 at 15h in São Paulo.
    expect(adminDueLine('2026-09-29T12:00:00-03:00', NOW)).toBe('Vence em 3 dias')
    expect(adminDueLine('2026-09-27T00:30:00-03:00', NOW)).toBe('Vence em 1 dia')
    expect(adminDueLine('2026-09-26T23:59:00-03:00', NOW)).toBe('Vence hoje')
    expect(adminDueLine('2026-09-25T23:00:00-03:00', NOW)).toBe('Atrasado há 1 dia')
    expect(adminDueLine('2026-09-20T10:00:00-03:00', NOW)).toBe('Atrasado há 6 dias')
  })

  it('never says "Vence" about a deadline already past', () => {
    expect(adminDueLine('2026-09-26T09:00:00-03:00', NOW)).toBe('Venceu hoje')
  })

  it('says it does not know rather than inventing a date', () => {
    expect(adminDueLine('not a date', NOW)).toBe('Prazo desconhecido')
  })
})
