// Endpoint functions for one feature; the paths match the server controllers.
import type { AuthResponse, User } from '../types'
import type { ApiClient } from './client'

export function login(api: ApiClient, email: string, password: string) {
  return api.post<AuthResponse>('/auth/login', { email, password })
}

/** Ends the session on the server; the token stops working right away. */
export function logout(api: ApiClient) {
  return api.post<void>('/auth/logout')
}

/** The logged-in user, to restore a session from a saved token. Fails with 401 if it is no longer valid. */
export function getCurrentUser(api: ApiClient) {
  return api.get<User>('/auth/me')
}
