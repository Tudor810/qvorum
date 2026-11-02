import { createApiClient } from '@qvorum/shared';

// Token kept in memory for now; it moves to expo-secure-store when login is implemented.
let token: string | null = null;

export const tokenStore = {
  get: () => token,
  set: (value: string | null) => {
    token = value;
  },
};

export const api = createApiClient({
  baseUrl: process.env.EXPO_PUBLIC_API_URL ?? '',
  getToken: tokenStore.get,
});
