import type { IsoDate, Money } from './types'

const ron = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'RON' })
const date = new Intl.DateTimeFormat('en-GB', { day: '2-digit', month: 'long', year: 'numeric' })

/** 12345 → "RON 123.45" */
export function formatMoney(amount: Money): string {
  return ron.format(amount / 100)
}

/** "2026-10-19" → "19 October 2026" */
export function formatDate(value: IsoDate | Date): string {
  return date.format(typeof value === 'string' ? new Date(value) : value)
}
