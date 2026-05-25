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

export type ToolStatus = 'Draft' | 'Published'

export type ToolEntryType = 'INTERNAL' | 'EXTERNAL'

export type ToolAccessLevel = 'PUBLIC' | 'AUTHENTICATED' | 'ADMIN'

export interface ToolItem {
  id: number
  slug: string
  title: string
  summary: string
  status: ToolStatus
  entryType: ToolEntryType
  routePath: string
  externalUrl: string
  accessLevel: ToolAccessLevel
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface ToolListResponse {
  page: number
  pageSize: number
  total: number
  items: ToolItem[]
}

export type ProjectStatus = 'Draft' | 'Published'

export interface ProjectItem {
  id: number
  slug: string
  title: string
  summary: string
  status: ProjectStatus
  repoOwner: string
  repoName: string
  repoUrl: string
  sortOrder: number
  readmeMarkdown: string
  readmeError: string
  readmeCachedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface ProjectListResponse {
  page: number
  pageSize: number
  total: number
  items: ProjectItem[]
}

export interface AdminProjectPayload {
  slug: string
  title: string
  summary: string
  status: ProjectStatus
  repoOwner: string
  repoName: string
  repoUrl: string
  sortOrder: number
}

export interface AdminToolPayload {
  slug: string
  title: string
  summary: string
  status: ToolStatus
  entryType: ToolEntryType
  routePath: string
  externalUrl: string
  accessLevel: ToolAccessLevel
  sortOrder: number
}

export interface VideoDownloadExtractPayload {
  url: string
}

export interface VideoDownloadFormat {
  quality: number | null
  videoUrl: string | null
  videoExt: string | null
  videoSize: number | null
  audioUrl: string | null
  audioExt: string | null
  audioSize: number | null
  separate: number | null
  qualityNote: string | null
}

export interface VideoDownloadMedia {
  mediaType: string | null
  resourceUrl: string | null
  previewUrl: string | null
  formats: VideoDownloadFormat[]
  headers: Record<string, string>
}

export interface VideoDownloadExtractResponse {
  text: string | null
  medias: VideoDownloadMedia[]
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

export type MediaAccessLevel = 'PUBLIC' | 'AUTHENTICATED'

export type MediaAssetType = 'IMAGE' | 'ATTACHMENT'

export interface MediaAsset {
  id: number
  assetType: MediaAssetType
  originalFilename: string
  mimeType: string
  sizeBytes: number
  accessLevel: MediaAccessLevel
  uploadedBy: string
  createdAt: string
  contentUrl: string
  downloadUrl: string
  markdown: string
}

export type MediaUploadResponse = MediaAsset

export interface AdminMediaListResponse {
  page: number
  pageSize: number
  total: number
  items: MediaAsset[]
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
  id: number
  username: string
  role: AuthRole
  status: string
  email: string
  emailVerified: boolean
  nickname: string
  avatarUrl: string
}

export interface AuthTokenResponse {
  token: string
  expireMinutes: number
  id: number
  username: string
  role: AuthRole
  email: string
  emailVerified: boolean
  nickname: string
  avatarUrl: string
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

export interface AuthRegisterPayload extends AuthCredentials {
  email: string
}

export interface MessageResponse {
  message: string
}

export interface PasswordForgotPayload {
  email: string
}

export interface PasswordResetPayload {
  token: string
  newPassword: string
}

export interface AccountProfile extends AuthUser {}

export interface AccountProfilePayload {
  nickname: string
}

export interface AccountPasswordPayload {
  currentPassword: string
  newPassword: string
}

export interface AccountEmailPayload {
  email: string
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

export interface FooterSettings {
  githubEnabled: boolean
  githubUrl: string
  xEnabled: boolean
  xUrl: string
}

export type FooterSettingsPayload = FooterSettings
