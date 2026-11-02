// Endpoint functions for one feature; the paths match the server controllers.
import type { AuthResponse } from '../types'
import type { ApiClient } from './client'

export function login(api: ApiClient, email: string, password: string) {
  return api.post<AuthResponse>('/auth/login', { email, password })
}
