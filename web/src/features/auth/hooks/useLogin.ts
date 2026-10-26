// ViewModel: holds the login state and actions; the view only renders what it returns.
import { strings } from '@qvorum/shared'
import { useState } from 'react'
import { tokenStore } from '../../../lib/api'
import { loginRequest } from '../api/authApi'
import type { LoginCredentials } from '../types'

export function useLogin() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit(credentials: LoginCredentials) {
    setLoading(true)
    setError(null)
    try {
      const { token } = await loginRequest(credentials)
      tokenStore.set(token)
    } catch {
      setError(strings.common.genericError)
    } finally {
      setLoading(false)
    }
  }

  return { loading, error, submit }
}
