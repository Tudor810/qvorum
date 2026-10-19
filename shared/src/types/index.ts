// Entitățile se completează după ce ERD-ul este stabilit cu serverul.
import type { Rol } from '../constants'

export type Id = number

/** Dată calendaristică ISO, ex. "2026-10-19". */
export type DataISO = string

/** Sumă în bani (1 leu = 100 bani), pentru a evita erorile de rotunjire. */
export type SumaBani = number

export interface Utilizator {
  id: Id
  nume: string
  prenume: string
  email: string
  roluri: Rol[]
}

export interface RaspunsAutentificare {
  token: string
  utilizator: Utilizator
}
