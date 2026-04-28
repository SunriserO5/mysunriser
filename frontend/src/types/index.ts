export interface PageItem {
  slug: string
  title: string
  summary: string
  status: string
  publishAt: string
}

export interface PageResponse {
  page: number
  pageSize: number
  items: PageItem[]
}

export interface PostDetail {
  id: number
  slug: string
  title: string
  content: string
  status: string
  publishAt: string
}

export interface AdminPostUpdatePayload {
  title: string
  content: string
  status: string
  published_at: string | null
}

export interface AdminPostCreatePayload extends AdminPostUpdatePayload {
  slug: string
}

export interface PostEditorFormValue {
  slug: string
  title: string
  content: string
  status: string
  publishedAt: string
}

export interface ErrorResponse {
  code: number
  message: string
  timeStamp: number
}

export interface HealthResponse {
  ok: boolean
  db: boolean
}

export type AuthRole = 'admin' | 'user'

export interface AuthUser {
  username: string
  role: AuthRole
  status: string
}

export interface AuthTokenResponse {
  token: string
  expireMinutes: number
  username: string
  role: AuthRole
}

export interface AuthConfig {
  registrationEnabled: boolean
  turnstileEnabled: boolean
  turnstileSiteKey: string
}

export interface AuthCredentials {
  username: string
  password: string
  turnstileToken?: string
}

export interface AdminUser {
  id: number
  username: string
  role: AuthRole
  status: string
  createdAt: string
  updatedAt: string
  lastLoginAt: string | null
}

export interface AdminCreateUserPayload {
  username: string
  password: string
  role: AuthRole
}

export interface AdminSecurityConfig {
  registrationEnabled: boolean
  turnstileEnabled: boolean
  turnstileConfigured: boolean
  turnstileSiteKey: string
  loginMaxAttempts: number
  loginWindowSeconds: number
  jwtExpireMinutes: number
}

export interface AdminSecurityConfigPayload {
  registrationEnabled: boolean
  turnstileEnabled: boolean
  loginMaxAttempts: number
  loginWindowSeconds: number
}
