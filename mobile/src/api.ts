import { createApiClient } from '@qvorum/shared'

// Token ținut în memorie deocamdată; se va muta în expo-secure-store la autentificare.
let token: string | null = null

export const tokenStore = {
  get: () => token,
  set: (value: string | null) => {
    token = value
  },
}

export const api = createApiClient({
  baseUrl: process.env.EXPO_PUBLIC_API_URL ?? '',
  getToken: tokenStore.get,
})
