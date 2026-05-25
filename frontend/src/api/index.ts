import type {
  AdminCreateUserPayload,
  AdminMediaListResponse,
  AdminPostCreatePayload,
  AdminPostUpdatePayload,
  AdminProjectPayload,
  AdminSecurityConfig,
  AdminSecurityConfigPayload,
  AdminToolPayload,
  AdminUser,
  AccountEmailPayload,
  AccountPasswordPayload,
  AccountProfile,
  AccountProfilePayload,
  AuthCredentials,
  AuthConfig,
  AuthRegisterPayload,
  AuthTokenResponse,
  AuthUser,
  ErrorResponse,
  FooterSettings,
  FooterSettingsPayload,
  HealthResponse,
  MediaAccessLevel,
  MediaUploadResponse,
  MessageResponse,
  PageResponse,
  PasswordForgotPayload,
  PasswordResetPayload,
  PostDetail,
  ProjectItem,
  ProjectListResponse,
  ToolItem,
  ToolListResponse,
  VideoDownloadExtractPayload,
  VideoDownloadExtractResponse,
} from '../types'

const BASE = ''
let accessToken: string | null = null

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
  return accessToken
}

export function setAccessToken(nextToken: string | null) {
  accessToken = nextToken
}

async function refreshAccessToken(): Promise<AuthTokenResponse> {
  const response = await fetch(`${BASE}/api/auth/refresh`, {
    method: 'POST',
    credentials: 'same-origin',
  })

  if (!response.ok) {
    setAccessToken(null)
    throw new ApiError(`Request failed with status ${response.status}`, response.status)
  }

  const payload = (await response.json()) as AuthTokenResponse
  setAccessToken(payload.token)
  return payload
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  return requestOnce<T>(path, options, true)
}

async function requestOnce<T>(path: string, options: RequestOptions, allowRefresh: boolean): Promise<T> {
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
    credentials: 'same-origin',
  })

  if (!response.ok) {
    if (response.status === 401 && allowRefresh && options.auth !== false && path !== '/api/auth/refresh') {
      await refreshAccessToken()
      return requestOnce<T>(path, options, false)
    }

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

async function requestBlob(path: string, options: Omit<RequestOptions, 'body'> = {}): Promise<Blob> {
  const headers = new Headers(options.headers)

  if (options.auth !== false) {
    const token = readStoredToken()
    if (token && !headers.has('Authorization')) {
      headers.set('Authorization', `Bearer ${token}`)
    }
  }

  const response = await fetch(`${BASE}${path}`, {
    method: options.method ?? 'GET',
    headers,
    credentials: 'same-origin',
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

  return response.blob()
}

async function requestForm<T>(path: string, formData: FormData): Promise<T> {
  const headers = new Headers()
  const token = readStoredToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers,
    body: formData,
    credentials: 'same-origin',
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

export function fetchTools(page: number, pageSize: number): Promise<ToolListResponse> {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  })

  return request<ToolListResponse>(`/api/tools?${params.toString()}`)
}

export function fetchTool(slug: string): Promise<ToolItem> {
  return request<ToolItem>(`/api/tools/${encodeURIComponent(slug)}`)
}

export function fetchProjects(page: number, pageSize: number): Promise<ProjectListResponse> {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  })

  return request<ProjectListResponse>(`/api/projects?${params.toString()}`)
}

export function fetchProject(slug: string): Promise<ProjectItem> {
  return request<ProjectItem>(`/api/projects/${encodeURIComponent(slug)}`)
}

export function fetchAdminProjects(page = 1, pageSize = 100): Promise<ProjectListResponse> {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  })

  return request<ProjectListResponse>(`/api/admin/projects?${params.toString()}`)
}

export function createAdminProject(payload: AdminProjectPayload): Promise<ProjectItem> {
  return request<ProjectItem>('/api/admin/projects', {
    method: 'POST',
    body: payload,
  })
}

export function updateAdminProject(slug: string, payload: AdminProjectPayload): Promise<ProjectItem> {
  return request<ProjectItem>(`/api/admin/projects/${encodeURIComponent(slug)}`, {
    method: 'PUT',
    body: payload,
  })
}

export function deleteAdminProject(slug: string): Promise<void> {
  return request<void>(`/api/admin/projects/${encodeURIComponent(slug)}`, {
    method: 'DELETE',
  })
}

export function fetchAdminTools(page = 1, pageSize = 100): Promise<ToolListResponse> {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  })

  return request<ToolListResponse>(`/api/admin/tools?${params.toString()}`)
}

export function createAdminTool(payload: AdminToolPayload): Promise<ToolItem> {
  return request<ToolItem>('/api/admin/tools', {
    method: 'POST',
    body: payload,
  })
}

export function updateAdminTool(slug: string, payload: AdminToolPayload): Promise<ToolItem> {
  return request<ToolItem>(`/api/admin/tools/${encodeURIComponent(slug)}`, {
    method: 'PUT',
    body: payload,
  })
}

export function deleteAdminTool(slug: string): Promise<void> {
  return request<void>(`/api/admin/tools/${encodeURIComponent(slug)}`, {
    method: 'DELETE',
  })
}

export function extractVideoDownload(payload: VideoDownloadExtractPayload): Promise<VideoDownloadExtractResponse> {
  return request<VideoDownloadExtractResponse>('/api/tools/video-download/extract', {
    method: 'POST',
    body: payload,
  })
}

export async function updateAdminPost(slug: string, payload: AdminPostUpdatePayload): Promise<PostDetail> {
  const response = await request<RawPostDetail>(`/api/admin/posts/${encodeURIComponent(slug)}`, {
    method: 'PUT',
    body: payload,
  })

  return {
    ...response,
    publishAt: normalizePublishAt(response),
  }
}

export async function createAdminPost(payload: AdminPostCreatePayload): Promise<PostDetail> {
  const response = await request<RawPostDetail>('/api/admin/posts', {
    method: 'POST',
    body: payload,
  })

  return {
    ...response,
    publishAt: normalizePublishAt(response),
  }
}

export function deleteAdminPost(slug: string): Promise<void> {
  return request<void>(`/api/admin/posts/${encodeURIComponent(slug)}`, {
    method: 'DELETE',
  })
}

export function uploadAdminMedia(file: File, accessLevel: MediaAccessLevel): Promise<MediaUploadResponse> {
  const formData = new FormData()
  formData.set('file', file)
  formData.set('accessLevel', accessLevel)

  return requestForm<MediaUploadResponse>('/api/admin/media', formData)
}

export function fetchAdminMedia(page = 1, pageSize = 20): Promise<AdminMediaListResponse> {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  })

  return request<AdminMediaListResponse>(`/api/admin/media?${params.toString()}`)
}

export function deleteAdminMedia(id: number): Promise<void> {
  return request<void>(`/api/admin/media/${id}`, {
    method: 'DELETE',
  })
}

export function fetchMediaBlob(path: string): Promise<Blob> {
  return requestBlob(path)
}

export function login(credentials: AuthCredentials): Promise<AuthTokenResponse> {
  return request<AuthTokenResponse>('/api/auth/login', {
    method: 'POST',
    body: credentials,
    auth: false,
  })
}

export function requestRegistration(payload: AuthRegisterPayload): Promise<MessageResponse> {
  return request<MessageResponse>('/api/auth/register/request', {
    method: 'POST',
    body: payload,
    auth: false,
  })
}

export function confirmRegistration(token: string): Promise<AuthTokenResponse> {
  return request<AuthTokenResponse>('/api/auth/register/confirm', {
    method: 'POST',
    body: { token },
    auth: false,
  })
}

export function requestPasswordReset(payload: PasswordForgotPayload): Promise<MessageResponse> {
  return request<MessageResponse>('/api/auth/password/forgot', {
    method: 'POST',
    body: payload,
    auth: false,
  })
}

export function resetPassword(payload: PasswordResetPayload): Promise<MessageResponse> {
  return request<MessageResponse>('/api/auth/password/reset', {
    method: 'POST',
    body: payload,
    auth: false,
  })
}

export function confirmEmailChange(token: string): Promise<MessageResponse> {
  return request<MessageResponse>('/api/auth/email-change/confirm', {
    method: 'POST',
    body: { token },
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

export function fetchAdminSecurityConfig(): Promise<AdminSecurityConfig> {
  return request<AdminSecurityConfig>('/api/admin/settings/security')
}

export function updateAdminSecurityConfig(payload: AdminSecurityConfigPayload): Promise<AdminSecurityConfig> {
  return request<AdminSecurityConfig>('/api/admin/settings/security', {
    method: 'PUT',
    body: payload,
  })
}

export function fetchFooterSettings(): Promise<FooterSettings> {
  return request<FooterSettings>('/api/settings/footer', {
    auth: false,
  })
}

export function fetchAdminFooterSettings(): Promise<FooterSettings> {
  return request<FooterSettings>('/api/admin/settings/footer')
}

export function updateAdminFooterSettings(payload: FooterSettingsPayload): Promise<FooterSettings> {
  return request<FooterSettings>('/api/admin/settings/footer', {
    method: 'PUT',
    body: payload,
  })
}

export function fetchMe(): Promise<AuthUser> {
  return request<AuthUser>('/api/auth/me')
}

export function logout(): Promise<{ message: string }> {
  return request<MessageResponse>('/api/auth/logout', {
    method: 'POST',
  })
}

export function refreshSession(): Promise<AuthTokenResponse> {
  return refreshAccessToken()
}

export function fetchAccountProfile(): Promise<AccountProfile> {
  return request<AccountProfile>('/api/account/profile')
}

export function updateAccountProfile(payload: AccountProfilePayload): Promise<AccountProfile> {
  return request<AccountProfile>('/api/account/profile', {
    method: 'PUT',
    body: payload,
  })
}

export function updateAccountPassword(payload: AccountPasswordPayload): Promise<MessageResponse> {
  return request<MessageResponse>('/api/account/password', {
    method: 'PUT',
    body: payload,
  })
}

export function requestAccountEmailChange(payload: AccountEmailPayload): Promise<MessageResponse> {
  return request<MessageResponse>('/api/account/email', {
    method: 'PUT',
    body: payload,
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
