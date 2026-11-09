import { strings } from '@qvorum/shared'
import { useState } from 'react'
import { ApiError, NetworkError } from '@qvorum/shared/src/api/client' // adjust to your path
import { tokenStore } from '../../../lib/api'
import { loginRequest } from '../api/authApi'
import type { LoginCredentials } from '../types'

type FieldErrors = { email?: string; password?: string }

// Deliberately loose: just checks the general shape. The server is the real validator.
const EMAIL_SHAPE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validate({ email, password }: LoginCredentials): FieldErrors {
  const errors: FieldErrors = {}
  if (!email.trim()) errors.email = strings.auth.emailRequired
  else if (!EMAIL_SHAPE.test(email.trim())) errors.email = strings.auth.emailInvalid
  if (!password) errors.password = strings.auth.passwordRequired
  return errors
}

function messageFor(err: unknown): string {
  if (err instanceof NetworkError) return strings.auth.networkError
  if (err instanceof ApiError) {
    switch (err.status) {
      case 401:
        return strings.auth.invalidCredentials
      case 400:
      case 422:
        return strings.auth.invalidInput
      case 429:
        return strings.auth.tooManyAttempts
    }
  }
  return strings.common.genericError
}

export function useLogin() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})

  // Resolves to true on success so the view knows when to navigate.
  async function submit(credentials: LoginCredentials): Promise<boolean> {
    const trimmed = { ...credentials, email: credentials.email.trim() }

    const errors = validate(trimmed)
    setFieldErrors(errors)

    setError(null)
    if (Object.keys(errors).length > 0) return false

    setLoading(true)
    try {
      const { token } = await loginRequest(trimmed)
      tokenStore.set(token)
      return true
    } catch (err) {
      setError(messageFor(err))
      return false
    } finally {
      setLoading(false)
    }
  }

  return { loading, error, fieldErrors, submit }
}
