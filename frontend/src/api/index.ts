import type { ErrorResponse, HealthResponse, PageResponse, PostDetail } from '../types'

const BASE = ''

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

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${BASE}${path}`)

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

export { ApiError }
