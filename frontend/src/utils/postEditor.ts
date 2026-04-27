import type { PostDetail, PostEditorFormValue } from '../types'

export function makeEmptyPostEditorValue(): PostEditorFormValue {
  return {
    slug: '',
    title: '',
    content: '',
    status: 'Draft',
    publishedAt: '',
  }
}

export function postToEditorValue(post: PostDetail): PostEditorFormValue {
  return {
    slug: post.slug,
    title: post.title,
    content: post.content,
    status: post.status,
    publishedAt: toDateTimeLocal(post.publishAt),
  }
}

export function toDateTimeLocal(value: string): string {
  if (!value) {
    return ''
  }

  const exact = value.match(/^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2})/)
  if (exact) {
    return exact[1]
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }

  const pad = (part: number) => String(part).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function toApiPublishedAt(value: string): string | null {
  if (!value) {
    return null
  }

  return value.length === 16 ? `${value}:00` : value
}
