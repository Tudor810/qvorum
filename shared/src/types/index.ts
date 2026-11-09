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
  phone: string | null
  roles: Role[]
  /** null for administrators */
  associationId: Id | null
  /** null for administrators */
  apartmentId: Id | null
  /** true until the temporary password is changed */
  mustChangePassword: boolean
}

/** Error body of every failed request (docs/api-contract.md, section 1.6). */
export interface ApiErrorBody {
  message: string
  /** Only on 400: which fields are wrong, e.g. { email: "Enter a valid email address." } */
  fields?: Record<string, string>
}

export interface AuthResponse {
  token: string
  user: User
}
