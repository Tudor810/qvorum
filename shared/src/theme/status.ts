// Which tone each status uses, so a status looks the same everywhere (lists, badges, details).
import type { Tone } from './tokens'

export const STATUS_TONES: Record<string, Tone> = {
  // Issue reports
  NEW: 'info',
  IN_PROGRESS: 'warning',
  RESOLVED: 'success',
  // Bills and payments
  UNPAID: 'danger',
  PAID: 'success',
  DRAFT: 'neutral',
  PUBLISHED: 'brand',
  PENDING: 'warning',
  SUCCEEDED: 'success',
  FAILED: 'danger',
  // Invoices, votes and approvals
  VALID: 'success',
  PENDING_APPROVAL: 'warning',
  ACTIVE: 'brand',
  CLOSED: 'neutral',
  APPROVED: 'success',
  REJECTED: 'danger',
}

/** Tone for a status value; unknown values are neutral. */
export function toneFor(status: string): Tone {
  return STATUS_TONES[status] ?? 'neutral'
}
