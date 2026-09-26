import { describe, it, expect, vi, afterEach } from 'vitest'
import { cards, filenameFromDisposition } from './api'

afterEach(() => {
  vi.unstubAllGlobals()
  vi.restoreAllMocks()
})

describe('filenameFromDisposition', () => {
  it('reads the unquoted name the export endpoints send', () => {
    expect(filenameFromDisposition('attachment; filename=tumtum-export.json', 'x.json')).toBe(
      'tumtum-export.json',
    )
  })

  it('reads a quoted name, spaces and all', () => {
    expect(filenameFromDisposition('attachment; filename="minha noite.csv"', 'x.csv')).toBe(
      'minha noite.csv',
    )
  })

  it('prefers the RFC 5987 form, which can carry an accent', () => {
    expect(
      filenameFromDisposition(
        "attachment; filename=\"noite.csv\"; filename*=UTF-8''s%C3%A3o-paulo.csv",
        'x.csv',
      ),
    ).toBe('são-paulo.csv')
  })

  it('falls back when the header is missing, empty or names nothing', () => {
    expect(filenameFromDisposition(null, 'tumtum-export.csv')).toBe('tumtum-export.csv')
    expect(filenameFromDisposition('', 'tumtum-export.csv')).toBe('tumtum-export.csv')
    expect(filenameFromDisposition('attachment', 'tumtum-export.csv')).toBe('tumtum-export.csv')
    expect(filenameFromDisposition('attachment; filename=""', 'tumtum-export.csv')).toBe(
      'tumtum-export.csv',
    )
  })

  it('falls back to the plain name when the extended one is malformed', () => {
    expect(
      filenameFromDisposition("attachment; filename=ok.json; filename*=UTF-8''%E0%A4%A", 'x.json'),
    ).toBe('ok.json')
  })
})

describe('cards.previewBlobUrl', () => {
  it('asks the owner-only preview route and hands back an object URL', async () => {
    const fetchMock = vi.fn(async () => ({
      ok: true,
      status: 200,
      headers: new Headers({ 'Content-Type': 'image/png' }),
      blob: async () => new Blob([new Uint8Array([137, 80, 78, 71])], { type: 'image/png' }),
    }))
    vi.stubGlobal('fetch', fetchMock)
    const create = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:card-1')
    const revoke = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined)

    const url = await cards.previewBlobUrl('card-1')
    expect(url).toBe('blob:card-1')
    expect(create).toHaveBeenCalledOnce()
    const calledWith = (fetchMock.mock.calls[0] as unknown as [string])[0]
    expect(calledWith).toMatch(/\/api\/cards\/card-1\/preview\?format=story$/)

    cards.revokePreviewUrl(url)
    expect(revoke).toHaveBeenCalledWith('blob:card-1')
  })

  it('asks for the landscape variant when told to', async () => {
    const fetchMock = vi.fn(async () => ({
      ok: true,
      status: 200,
      headers: new Headers(),
      blob: async () => new Blob([]),
    }))
    vi.stubGlobal('fetch', fetchMock)
    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:og')

    await cards.previewBlobUrl('card-2', 'og')
    const calledWith = (fetchMock.mock.calls[0] as unknown as [string])[0]
    expect(calledWith).toMatch(/\/preview\?format=og$/)
  })

  it('rejects rather than returning an empty picture when the server refuses', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => ({
        ok: false,
        status: 404,
        headers: new Headers(),
        json: async () => ({ detail: 'Card não encontrado' }),
      })),
    )
    await expect(cards.previewBlobUrl('gone')).rejects.toThrow('Card não encontrado')
  })
})
