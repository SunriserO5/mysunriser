import type {
  AdminCreateUserPayload,
  AdminUser,
  AuthCredentials,
  AuthConfig,
  AuthTokenResponse,
  AuthUser,
  ErrorResponse,
  HealthResponse,
  PageResponse,
  PostDetail,
} from '../types'

const BASE = ''
export const AUTH_TOKEN_STORAGE_KEY = 'mysunriser.auth.token'

class ApiError extends Error {
  public readonly status: number
  public readonly payload?: ErrorResponse

  constructor(message: string, status: number, payload?: ErrorResponse) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.payload = payload
  }
}

type RequestOptions = {
  method?: string
  body?: unknown
  headers?: HeadersInit
  auth?: boolean
}

function readStoredToken(): string | null {
  if (typeof window === 'undefined') {
    return null
  }

  return window.localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers)

  if (options.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (options.auth !== false) {
    const token = readStoredToken()
    if (token && !headers.has('Authorization')) {
      headers.set('Authorization', `Bearer ${token}`)
    }
  }

  const response = await fetch(`${BASE}${path}`, {
    method: options.method ?? 'GET',
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  if (!response.ok) {
    let payload: ErrorResponse | undefined
    try {
      payload = (await response.json()) as ErrorResponse
    } catch {
      payload = undefined
    }

    throw new ApiError(payload?.message ?? `Request failed with status ${response.status}`, response.status, payload)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

type PublishAtSource = {
  publishAt?: string
  publishedAt?: string
  publish_at?: string
  published_at?: string
  publishTime?: string
}

type RawPageItem = Omit<PageResponse['items'][number], 'publishAt'> & PublishAtSource
type RawPageResponse = Omit<PageResponse, 'items'> & {
  items: RawPageItem[]
}
type RawPostDetail = Omit<PostDetail, 'publishAt'> & PublishAtSource

function normalizePublishAt(source: PublishAtSource): string {
  return source.publishAt ?? source.publishedAt ?? source.publish_at ?? source.published_at ?? source.publishTime ?? ''
}

export async function fetchHealth(): Promise<HealthResponse> {
  return request<HealthResponse>('/api/health')
}

export async function fetchPage(page: number, pageSize: number): Promise<PageResponse> {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  })

  const payload = await request<RawPageResponse>(`/api/page?${params.toString()}`)

  return {
    ...payload,
    items: payload.items.map((item) => ({
      ...item,
      publishAt: normalizePublishAt(item),
    })),
  }
}

export async function fetchPost(slug: string): Promise<PostDetail> {
  const payload = await request<RawPostDetail>(`/api/blog/${encodeURIComponent(slug)}`)

  return {
    ...payload,
    publishAt: normalizePublishAt(payload),
  }
}

export function login(credentials: AuthCredentials): Promise<AuthTokenResponse> {
  return request<AuthTokenResponse>('/api/auth/login', {
    method: 'POST',
    body: credentials,
    auth: false,
  })
}

export function register(credentials: AuthCredentials): Promise<AuthTokenResponse> {
  return request<AuthTokenResponse>('/api/auth/register', {
    method: 'POST',
    body: credentials,
    auth: false,
  })
}

export function fetchAuthConfig(): Promise<AuthConfig> {
  return request<AuthConfig>('/api/auth/config', {
    auth: false,
  })
}

export function fetchAdminAuthConfig(): Promise<AuthConfig> {
  return request<AuthConfig>('/api/admin/settings/auth')
}

export function updateAdminAuthConfig(payload: AuthConfig): Promise<AuthConfig> {
  return request<AuthConfig>('/api/admin/settings/auth', {
    method: 'PUT',
    body: payload,
  })
}

export function fetchMe(): Promise<AuthUser> {
  return request<AuthUser>('/api/auth/me')
}

export function logout(): Promise<{ message: string }> {
  return request<{ message: string }>('/api/auth/logout', {
    method: 'POST',
  })
}

export function fetchAdminUsers(): Promise<AdminUser[]> {
  return request<AdminUser[]>('/api/admin/users')
}

export function createAdminUser(payload: AdminCreateUserPayload): Promise<AdminUser> {
  return request<AdminUser>('/api/admin/users', {
    method: 'POST',
    body: payload,
  })
}

export function resetAdminUserPassword(id: number, newPassword: string): Promise<{ message: string }> {
  return request<{ message: string }>(`/api/admin/users/${id}/reset-password`, {
    method: 'POST',
    body: { newPassword },
  })
}

export function setAdminUserEnabled(id: number, enabled: boolean): Promise<AdminUser> {
  return request<AdminUser>(`/api/admin/users/${id}/${enabled ? 'enable' : 'disable'}`, {
    method: 'POST',
  })
}

export { ApiError }
