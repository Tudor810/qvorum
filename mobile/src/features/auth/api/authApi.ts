// Model: binds the shared endpoint functions to this app's API client.
import { login } from '@qvorum/shared';
import { api } from '@/lib/api';
import type { LoginCredentials } from '../types';

export function loginRequest({ email, password }: LoginCredentials) {
  return login(api, email, password);
}
