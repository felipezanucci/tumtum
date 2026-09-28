import { describe, it, expect } from 'vitest'

import {
  USERNAME_BAD_CHARS,
  USERNAME_CHECK_FAILED,
  USERNAME_FIXED,
  USERNAME_TAKEN,
  USERNAME_TOO_LONG,
  USERNAME_TOO_SHORT,
  cleanUsername,
  isUsernameSentence,
  localUsernameProblem,
  usernameStatus,
  usernameStatusText,
} from './username'

describe('cleanUsername, as the field filters what is typed', () => {
  it('drops the @ in front, spaces and upper case, as the server does', () => {
    expect(cleanUsername('  @Felipe ')).toBe('felipe')
    expect(cleanUsername('@@fe')).toBe('fe')
  })

  it('keeps only a-z, 0-9 and _', () => {
    expect(cleanUsername('fe.lipe-z')).toBe('felipez')
    expect(cleanUsername('joão_10')).toBe('joo_10')
    expect(cleanUsername('fe lipe')).toBe('felipe')
    expect(cleanUsername('a@b')).toBe('ab')
  })

  it('stops at twenty characters', () => {
    expect(cleanUsername('a'.repeat(25))).toBe('a'.repeat(20))
  })

  it('leaves nothing of nothing', () => {
    expect(cleanUsername('')).toBe('')
    expect(cleanUsername('@')).toBe('')
  })
})

describe('localUsernameProblem, the rules that need no server', () => {
  it('uses the server sentences, word for word', () => {
    expect(USERNAME_TOO_SHORT).toBe('O @ precisa de pelo menos 3 letras ou números.')
    expect(USERNAME_TOO_LONG).toBe('O @ pode ter até 20 caracteres.')
    expect(USERNAME_BAD_CHARS).toBe('O @ só aceita letras sem acento, números e _.')
  })

  it('refuses a name too short, too long or with other characters', () => {
    expect(localUsernameProblem('fe')).toBe(USERNAME_TOO_SHORT)
    expect(localUsernameProblem('a'.repeat(21))).toBe(USERNAME_TOO_LONG)
    expect(localUsernameProblem('Fel')).toBe(USERNAME_BAD_CHARS)
  })

  it('has nothing to say about a well-formed name — only the server knows if it is free', () => {
    expect(localUsernameProblem('fel')).toBeNull()
    expect(localUsernameProblem('a'.repeat(20))).toBeNull()
    expect(localUsernameProblem('tumtum')).toBeNull()
  })
})

describe('isUsernameSentence', () => {
  it('knows the refusals about an @', () => {
    expect(isUsernameSentence(USERNAME_TAKEN)).toBe(true)
    expect(isUsernameSentence('Esse @ é da TumTum.')).toBe(true)
    expect(isUsernameSentence(USERNAME_FIXED)).toBe(true)
    expect(USERNAME_FIXED).toBe('O @ é fixo: escolhido uma vez, não muda.')
    expect(isUsernameSentence(USERNAME_BAD_CHARS)).toBe(true)
  })

  it('does not take an address or another refusal for one', () => {
    expect(isUsernameSentence('Email já cadastrado')).toBe(false)
    expect(isUsernameSentence('Mandamos pra seu@email.com')).toBe(false)
    expect(isUsernameSentence(undefined)).toBe(false)
  })
})

describe('usernameStatus, the line under the field', () => {
  const yes = { username: 'felipe', available: true, reason: null }
  const no = { username: 'felipe', available: false, reason: USERNAME_TAKEN }

  it('hints before anything is typed', () => {
    expect(usernameStatus('', null, null).kind).toBe('empty')
    expect(usernameStatusText(usernameStatus('', null, null))).toBe(
      'De 3 a 20 letras, números ou _.',
    )
  })

  it('says the local rule while it fails, even with an answer in hand', () => {
    expect(usernameStatus('fe', yes, null)).toEqual({ kind: 'invalid', message: USERNAME_TOO_SHORT })
  })

  it('is checking until the server answers for this very name', () => {
    expect(usernameStatus('felipe', null, null).kind).toBe('checking')
    expect(usernameStatusText({ kind: 'checking' })).toBe('Conferindo…')
  })

  it('never carries an answer over to a name that changed', () => {
    expect(usernameStatus('felipez', yes, null).kind).toBe('checking')
    expect(usernameStatus('felipez', null, 'felipe').kind).toBe('checking')
  })

  it('says disponível only when the server said so for this name', () => {
    const status = usernameStatus('felipe', yes, null)
    expect(status).toEqual({ kind: 'available', username: 'felipe' })
    expect(usernameStatusText(status)).toBe('@felipe disponível')
  })

  it('gives the server reason when it said no', () => {
    expect(usernameStatus('felipe', no, null)).toEqual({ kind: 'unavailable', message: USERNAME_TAKEN })
    const reserved = { username: 'tumtum', available: false, reason: 'Esse @ é da TumTum.' }
    expect(usernameStatusText(usernameStatus('tumtum', reserved, null))).toBe('Esse @ é da TumTum.')
  })

  it('says it could not ask, never disponível, when the check failed', () => {
    const status = usernameStatus('felipe', null, 'felipe')
    expect(status).toEqual({ kind: 'failed', message: USERNAME_CHECK_FAILED })
    expect(usernameStatusText(status)).toBe('Não deu pra conferir agora.')
  })
})
