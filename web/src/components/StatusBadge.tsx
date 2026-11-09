// Shows a status (NEW, PAID, PENDING_APPROVAL...) with the tone defined in @qvorum/shared.
import { toneFor } from '@qvorum/shared'

interface StatusBadgeProps {
  status: string
  label?: string
}

export function StatusBadge({ status, label }: StatusBadgeProps) {
  return <span className={`badge badge-${toneFor(status)}`}>{label ?? status.replace(/_/g, ' ').toLowerCase()}</span>
}
