/**
 * "Com quem seus dados estão" (26/09): the labels and the few rules the
 * profile's sharing section applies to what `GET /api/users/me/sharing`
 * answers. Pure, so they are tested.
 */

const AUDIENCE_LABELS: Record<string, string> = {
  evento: 'Só no feed deste evento',
  'turnê': 'No feed de todas as datas da turnê',
}

/** Who can see a feed post: the one night's feed, or the whole tour's. */
export function audienceLabel(audience: string): string {
  return AUDIENCE_LABELS[audience] ?? audience
}

const PLATFORM_LABELS: Record<string, string> = {
  whatsapp: 'WhatsApp',
  instagram: 'Instagram',
  tiktok: 'TikTok',
  x: 'X',
  snapchat: 'Snapchat',
  facebook: 'Facebook',
  link: 'Link copiado',
  native: 'Menu de compartilhar do celular',
}

/** The network a share went to, as a person names it. Unknown ones pass through. */
export function platformLabel(platform: string): string {
  return PLATFORM_LABELS[platform.toLowerCase()] ?? platform
}

/** `3 reações`, `1 reação`, `Nenhuma reação`. */
export function reactionsLabel(count: number): string {
  if (!Number.isFinite(count) || count <= 0) return 'Nenhuma reação'
  return count === 1 ? '1 reação' : `${count} reações`
}

/**
 * A date as the rest of the privacy section writes it, in São Paulo time: a
 * post at 23h on the 25th is the 25th for the person who made it, whatever
 * the server's clock says. An unreadable date says so rather than inventing one.
 */
export function sharingDate(iso: string): string {
  const time = Date.parse(iso)
  if (Number.isNaN(time)) return 'data desconhecida'
  return new Date(time).toLocaleDateString('pt-BR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'America/Sao_Paulo',
  })
}

/**
 * A link the section may put under a person's finger: only `https:`. Anything
 * else (a relative path, `javascript:`, garbage) is not rendered as a link.
 */
export function safeHttpsUrl(url: string | null | undefined): string | null {
  if (!url) return null
  try {
    const parsed = new URL(url)
    return parsed.protocol === 'https:' ? parsed.toString() : null
  } catch {
    return null
  }
}

/** `Nunca vai para: clubes, artistas, anunciantes.` — or null when the list is empty. */
export function neverLine(never: readonly string[]): string | null {
  const names = never.map((n) => n.trim()).filter(Boolean)
  if (names.length === 0) return null
  return `Nunca vai para: ${names.join(', ')}.`
}

/**
 * Split running text into plain parts and `https://` links, so a legal page
 * written as strings can still put the ANPD channel under the reader's finger.
 * A trailing full stop or comma stays text, not part of the URL.
 */
export function splitLinks(text: string): Array<{ text: string; href?: string }> {
  const parts: Array<{ text: string; href?: string }> = []
  const pattern = /https:\/\/[^\s)]+/g
  let last = 0
  let match: RegExpExecArray | null
  while ((match = pattern.exec(text)) !== null) {
    let url = match[0]
    while (/[.,;:!?]$/.test(url)) url = url.slice(0, -1)
    const start = match.index
    if (start > last) parts.push({ text: text.slice(last, start) })
    parts.push({ text: url, href: url })
    last = start + url.length
  }
  if (last < text.length) parts.push({ text: text.slice(last) })
  return parts
}
