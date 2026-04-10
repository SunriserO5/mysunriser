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

export async function fetchHealth(): Promise<HealthResponse> {
  return request<HealthResponse>('/api/health')
}

export async function fetchPage(page: number, pageSize: number): Promise<PageResponse> {
  const params = new URLSearchParams({
    page: String(page),
    pageSize: String(pageSize),
  })

  return request<PageResponse>(`/api/page?${params.toString()}`)
}

export async function fetchPost(slug: string): Promise<PostDetail> {
  return request<PostDetail>(`/api/blog/${encodeURIComponent(slug)}`)
}

export { ApiError }
