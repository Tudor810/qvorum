import { createApiClient } from '@qvorum/shared'

const TOKEN_KEY = 'qvorum.token'

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string | null) => {
    if (token) localStorage.setItem(TOKEN_KEY, token)
    else localStorage.removeItem(TOKEN_KEY)
  },
}

export const api = createApiClient({
  baseUrl: import.meta.env.VITE_API_URL,
  getToken: tokenStore.get,
})
