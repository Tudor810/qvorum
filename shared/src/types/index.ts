// Entities are added once the ERD is agreed with the server.
import type { Role } from '../constants'

export type Id = number

/** ISO calendar date, e.g. "2026-10-19". */
export type IsoDate = string

/** Amount in bani (1 RON = 100 bani), stored as an integer to avoid rounding errors. */
export type Money = number

export interface User {
  id: Id
  firstName: string
  lastName: string
  email: string
  roles: Role[]
}

export interface AuthResponse {
  token: string
  user: User
}
