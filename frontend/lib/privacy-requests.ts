/**
 * Requests to the encarregado (the data-protection officer), 26/09.
 *
 * LGPD art. 18 gives a person the right to ask; the contract gives every
 * request a due date fifteen days after it is opened. These are the labels
 * and the one date rule the profile page shows, pure so they are tested.
 */

export type RequestKind =
  | 'access'
  | 'portability'
  | 'correction'
  | 'deletion'
  | 'revocation'
  | 'other'

export type RequestStatus = 'open' | 'answered' | 'closed'

export const REQUEST_KINDS: readonly RequestKind[] = [
  'access',
  'portability',
  'correction',
  'deletion',
  'revocation',
  'other',
]

export const REQUEST_KIND_LABELS: Record<RequestKind, string> = {
  access: 'Saber o que a TumTum tem sobre mim',
  portability: 'Levar meus dados para outro serviço',
  correction: 'Corrigir um dado',
  deletion: 'Apagar um dado',
  revocation: 'Retirar um consentimento',
  other: 'Outro pedido',
}

export const REQUEST_STATUS_LABELS: Record<RequestStatus, string> = {
  open: 'Aberto',
  answered: 'Respondido',
  closed: 'Encerrado',
}

/** The response window the contract promises, in days. */
export const REQUEST_SLA_DAYS = 15

export function requestKindLabel(kind: string): string {
  return (REQUEST_KIND_LABELS as Record<string, string>)[kind] ?? kind
}

export function requestStatusLabel(status: string): string {
  return (REQUEST_STATUS_LABELS as Record<string, string>)[status] ?? status
}

/**
 * The line under an open request: when the answer is due, or that it is
 * late. An answered or closed request says when it was answered instead.
 */
export function requestDueLine(
  request: { status: string; due_at: string; answered_at: string | null },
  now: Date = new Date(),
): string {
  const fmt = (iso: string) =>
    new Date(iso).toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' })
  if (request.status !== 'open') {
    return request.answered_at ? `Respondido em ${fmt(request.answered_at)}` : requestStatusLabel(request.status)
  }
  const due = Date.parse(request.due_at)
  if (Number.isNaN(due)) return 'Resposta em até 15 dias'
  if (due < now.getTime()) return `Prazo vencido em ${fmt(request.due_at)}. A gente te deve essa resposta.`
  return `Resposta até ${fmt(request.due_at)}`
}

// --- The operator's side: /admin/pedidos (28/09) ---

/** Short names for the queue, where the person's own long label would crowd the row. */
export const ADMIN_REQUEST_KIND_LABELS: Record<RequestKind, string> = {
  access: 'Acesso',
  portability: 'Portabilidade',
  correction: 'Correção',
  deletion: 'Exclusão',
  revocation: 'Revogação',
  other: 'Outro',
}

export function adminRequestKindLabel(kind: string): string {
  return (ADMIN_REQUEST_KIND_LABELS as Record<string, string>)[kind] ?? kind
}

const DAY_MS = 86_400_000

/** The calendar day of an instant in São Paulo, as a day number. */
function saoPauloDay(time: number): number {
  const [y, m, d] = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Sao_Paulo',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
    .format(new Date(time))
    .split('-')
    .map(Number)
  return Date.UTC(y, m - 1, d) / DAY_MS
}

/**
 * The deadline of an open request, as the operator must read it: "Vence em
 * 3 dias", "Vence hoje", "Atrasado há 2 dias". Counted in São Paulo calendar
 * days, so a request due tonight reads "hoje" all day and late from tomorrow.
 * A deadline already past today reads "Venceu hoje" — it is never "Vence".
 */
export function adminDueLine(dueAt: string, now: Date = new Date()): string {
  const due = Date.parse(dueAt)
  if (Number.isNaN(due)) return 'Prazo desconhecido'
  const days = saoPauloDay(due) - saoPauloDay(now.getTime())
  const plural = (n: number) => (n === 1 ? '1 dia' : `${n} dias`)
  if (days > 0) return `Vence em ${plural(days)}`
  if (days === 0) return due < now.getTime() ? 'Venceu hoje' : 'Vence hoje'
  return `Atrasado há ${plural(-days)}`
}
