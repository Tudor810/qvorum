import type { DataISO, SumaBani } from './types'

const lei = new Intl.NumberFormat('ro-RO', { style: 'currency', currency: 'RON' })
const data = new Intl.DateTimeFormat('ro-RO', { day: '2-digit', month: 'long', year: 'numeric' })

/** 12345 → "123,45 RON" */
export function formatLei(suma: SumaBani): string {
  return lei.format(suma / 100)
}

/** "2026-10-19" → "19 octombrie 2026" */
export function formatData(valoare: DataISO | Date): string {
  return data.format(typeof valoare === 'string' ? new Date(valoare) : valoare)
}
