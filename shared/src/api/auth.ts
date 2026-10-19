// Exemplu de fișier de endpoint-uri; căile se aliniază cu serverul.
import type { RaspunsAutentificare } from '../types'
import type { ApiClient } from './client'

export function login(api: ApiClient, email: string, parola: string) {
  return api.post<RaspunsAutentificare>('/auth/login', { email, parola })
}
