export interface PageItem {
  slug: string
  title: string
  summary: string
  status: string
  publishTime: string
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
  publish_at: string
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
