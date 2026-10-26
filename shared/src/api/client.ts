export interface ApiClientConfig {
  /** Server address */
  baseUrl: string
  /** Current JWT Token. */
  getToken: () => string | null | Promise<string | null>
}

export class ApiError extends Error {
  readonly status: number
  readonly body: unknown

  constructor(status: number, body: unknown) {
    super(`Request failed with status ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export type ApiClient = ReturnType<typeof createApiClient>

export function createApiClient({ baseUrl, getToken }: ApiClientConfig) {
  const root = baseUrl.replace(/\/+$/, '')

  async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
    const token = await getToken()
    const headers: Record<string, string> = { Accept: 'application/json' }
    if (body !== undefined) headers['Content-Type'] = 'application/json'
    if (token) headers.Authorization = `Bearer ${token}`

    const response = await fetch(`${root}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })

    const text = await response.text()
    const data: unknown = text ? JSON.parse(text) : undefined
    if (!response.ok) throw new ApiError(response.status, data)
    return data as T
  }

  return {
    get: <T>(path: string) => request<T>('GET', path),
    post: <T>(path: string, body?: unknown) => request<T>('POST', path, body),
    put: <T>(path: string, body?: unknown) => request<T>('PUT', path, body),
    delete: <T>(path: string) => request<T>('DELETE', path),
  }
}
