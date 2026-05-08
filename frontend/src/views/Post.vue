<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'
import { ApiError, deleteAdminPost, fetchMediaBlob, updateAdminPost } from '../api'
import PostEditorForm from '../components/PostEditorForm.vue'
import { useAuth } from '../composables/useAuth'
import { usePost } from '../composables/usePost'
import type { AdminPostUpdatePayload, PostEditorFormValue } from '../types'
import { postToEditorValue, toApiPublishedAt } from '../utils/postEditor'

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const slug = computed(() => String(route.params.slug ?? ''))

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: false,
})

markdown.renderer.rules.image = (tokens, idx, options, _env, self) => {
  const token = tokens[idx]
  const src = token.attrGet('src') ?? ''
  const alt = token.content ? markdown.utils.escapeHtml(token.content) : ''
  const title = token.attrGet('title')

  if (/^\/api\/media\/\d+\/content$/.test(src)) {
    const titleAttribute = title ? ` title="${markdown.utils.escapeHtml(title)}"` : ''
    return `<img data-media-src="${markdown.utils.escapeHtml(src)}" alt="${alt}"${titleAttribute}>`
  }

  return self.renderToken(tokens, idx, options)
}

const { post, loading, error, loadPost } = usePost()

const fallbackTitle = '文章 | MySunriser'
const isEditing = ref(false)
const saving = ref(false)
const deleting = ref(false)
const saveError = ref<string | null>(null)
const editValue = ref<PostEditorFormValue | null>(null)
const mediaReadError = ref<string | null>(null)
const articleContentRef = ref<HTMLElement | null>(null)
const mediaObjectUrls = ref<string[]>([])
const viewerImage = ref<{ src: string; alt: string } | null>(null)

watch(
  slug,
  (nextSlug) => {
    if (!nextSlug) {
      return
    }

    isEditing.value = false
    saveError.value = null
    editValue.value = null
    void loadPost(nextSlug)
  },
  { immediate: true },
)

watch(
  [post, error],
  () => {
    if (post.value?.title) {
      document.title = `${post.value.title} | MySunriser`
      return
    }

    if (error.value) {
      document.title = `未找到文章 | MySunriser`
      return
    }

    document.title = fallbackTitle
  },
  { immediate: true },
)

function retryLoad() {
  if (!slug.value) {
    return
  }

  void loadPost(slug.value)
}

function beginEdit() {
  if (!post.value) {
    return
  }

  editValue.value = postToEditorValue(post.value)
  isEditing.value = true
  saveError.value = null
}

function cancelEdit() {
  isEditing.value = false
  saveError.value = null
  editValue.value = null
}

async function saveEdit(value: PostEditorFormValue) {
  if (!post.value || saving.value) {
    return
  }

  const payload: AdminPostUpdatePayload = {
    title: value.title.trim(),
    content: value.content,
    status: value.status.trim(),
    published_at: toApiPublishedAt(value.publishedAt),
  }

  if (!payload.title || !payload.content.trim() || !payload.status) {
    saveError.value = '标题、正文和状态不能为空'
    return
  }

  saving.value = true
  saveError.value = null

  try {
    post.value = await updateAdminPost(post.value.slug, payload)
    isEditing.value = false
    editValue.value = null
  } catch (err) {
    saveError.value = err instanceof ApiError ? err.message : '文章保存失败'
  } finally {
    saving.value = false
  }
}

async function deletePost() {
  if (!post.value || deleting.value) {
    return
  }

  const firstConfirmed = window.confirm(`确定要删除文章「${post.value.title}」吗？`)
  if (!firstConfirmed) {
    return
  }

  const secondConfirmed = window.confirm('删除后不可恢复。请再次确认是否删除这篇文章。')
  if (!secondConfirmed) {
    return
  }

  deleting.value = true
  saveError.value = null

  try {
    await deleteAdminPost(post.value.slug)
    await router.push({ name: 'blog' })
  } catch (err) {
    saveError.value = err instanceof ApiError ? err.message : '文章删除失败'
  } finally {
    deleting.value = false
  }
}

const publishedAt = computed(() => {
  if (!post.value?.publishAt) {
    return ''
  }

  const date = new Date(post.value.publishAt)
  if (Number.isNaN(date.getTime())) {
    return post.value.publishAt
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
})

const publishedAtText = computed(() => publishedAt.value || '暂未设置')
const publishedAtDateTime = computed(() => post.value?.publishAt ?? null)

const renderedContent = computed(() => {
  const source = post.value?.content ?? ''
  const html = markdown.render(source)

  return DOMPurify.sanitize(html)
})

watch(
  [renderedContent, isEditing],
  () => {
    if (!isEditing.value) {
      void nextTick(hydrateMediaImages)
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  revokeMediaObjectUrls()
  window.removeEventListener('keydown', handleViewerKeydown)
})

watch(viewerImage, (next) => {
  if (next) {
    window.addEventListener('keydown', handleViewerKeydown)
  } else {
    window.removeEventListener('keydown', handleViewerKeydown)
  }
})

function mediaPathFromUrl(value: string | null) {
  if (!value) {
    return null
  }

  try {
    const url = new URL(value, window.location.origin)
    if (url.origin !== window.location.origin) {
      return null
    }

    return /^\/api\/media\/\d+\/(?:content|download)$/.test(url.pathname) ? url.pathname : null
  } catch {
    return null
  }
}

async function hydrateMediaImages() {
  const container = articleContentRef.value
  if (!container) {
    return
  }

  revokeMediaObjectUrls()
  const images = Array.from(container.querySelectorAll<HTMLImageElement>('img'))

  await Promise.all(
    images.map(async (image) => {
      const mediaPath = mediaPathFromUrl(image.dataset.mediaSrc ?? image.getAttribute('src'))
      if (!mediaPath) {
        return
      }

      image.dataset.mediaPath = mediaPath
      image.removeAttribute('src')
      image.classList.add('media-image-loading')

      try {
        const blob = await fetchMediaBlob(mediaPath)
        const objectUrl = URL.createObjectURL(blob)
        mediaObjectUrls.value.push(objectUrl)
        image.src = objectUrl
        image.classList.remove('media-image-loading')
      } catch (err) {
        image.replaceWith(createMediaImageError(mediaErrorMessage(err)))
      }
    }),
  )
}

function createMediaImageError(message: string) {
  const placeholder = document.createElement('div')
  placeholder.className = 'media-image-error'
  placeholder.textContent = message

  return placeholder
}

function mediaErrorMessage(err: unknown) {
  if (err instanceof ApiError) {
    if (err.status === 401) {
      return '请登录后访问图片'
    }

    if (err.status === 403) {
      return '你没有权限访问这张图片'
    }

    if (err.status === 404) {
      return '图片不存在或已被删除'
    }
  }

  return '图片加载失败'
}

function revokeMediaObjectUrls() {
  for (const objectUrl of mediaObjectUrls.value) {
    URL.revokeObjectURL(objectUrl)
  }
  mediaObjectUrls.value = []
}

function handleArticleClick(event: MouseEvent) {
  const target = event.target
  if (!(target instanceof Element)) {
    return
  }

  const image = target.closest('img')
  if (image instanceof HTMLImageElement) {
    event.preventDefault()
    viewerImage.value = {
      src: image.currentSrc || image.src,
      alt: image.alt || '文章图片',
    }
    mediaReadError.value = null
    return
  }

  const link = target.closest('a')
  if (link instanceof HTMLAnchorElement) {
    const mediaPath = mediaPathFromUrl(link.getAttribute('href'))
    if (mediaPath?.endsWith('/download')) {
      event.preventDefault()
      void downloadMedia(mediaPath, link.textContent?.trim() || 'attachment')
    }
  }
}

async function downloadMedia(path: string, filename: string) {
  mediaReadError.value = null

  try {
    const blob = await fetchMediaBlob(path)
    const objectUrl = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = objectUrl
    anchor.download = filename
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
  } catch (err) {
    mediaReadError.value = err instanceof ApiError ? err.message : '附件下载失败'
  }
}

function closeViewer() {
  viewerImage.value = null
}

function handleViewerKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeViewer()
  }
}
</script>

<template>
  <article class="fade-in mx-auto max-w-3xl">
    <p v-if="loading" class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">正在加载文章...</p>
    <div v-else-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
      <p>{{ error }}</p>
      <button type="button" class="retry-button mt-3 rounded-full px-4 py-2 text-sm font-semibold" @click="retryLoad">
        重试
      </button>
    </div>

    <div v-else-if="post" class="rounded-3xl border border-slate-200/80 bg-white p-6 shadow-sm sm:p-10">
      <template v-if="isEditing && editValue">
        <PostEditorForm
          mode="edit"
          danger-label="删除文章"
          danger-loading-label="删除中..."
          :danger-loading="deleting"
          :error="saveError"
          :initial-value="editValue"
          :saving="saving || deleting"
          submit-label="保存"
          @cancel="cancelEdit"
          @danger="deletePost"
          @submit="saveEdit"
        />
      </template>

      <template v-else>
        <header>
          <div class="flex items-start justify-between gap-4">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">{{ post.status }}</p>
            <button
              v-if="auth.isAdmin.value"
              class="focus-ring shrink-0 rounded-md border border-slate-300 bg-white px-3 py-1.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              type="button"
              @click="beginEdit"
            >
              编辑
            </button>
          </div>
          <h1 class="mt-3 text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">{{ post.title }}</h1>
          <p
            class="mt-4 inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs font-medium text-slate-600 sm:text-sm"
          >
            <span class="text-slate-500">发布于</span>
            <time v-if="publishedAtDateTime" :datetime="publishedAtDateTime" class="text-slate-700">{{ publishedAtText }}</time>
            <span v-else class="text-slate-700">{{ publishedAtText }}</span>
          </p>
        </header>

        <p v-if="mediaReadError" class="mt-6 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
          {{ mediaReadError }}
        </p>
        <div ref="articleContentRef" class="article-content mt-8" @click="handleArticleClick" v-html="renderedContent" />
      </template>
    </div>

    <p v-else class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">未找到文章。</p>

    <div
      v-if="viewerImage"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/90 p-4"
      role="dialog"
      aria-modal="true"
      @click.self="closeViewer"
    >
      <button
        class="focus-ring absolute right-4 top-4 rounded-md bg-white/95 px-3 py-2 text-sm font-semibold text-slate-900 shadow-sm transition hover:bg-white"
        type="button"
        @click="closeViewer"
      >
        关闭
      </button>
      <img class="max-h-[88vh] max-w-full rounded-md object-contain shadow-2xl" :alt="viewerImage.alt" :src="viewerImage.src" />
    </div>
  </article>
</template>

<style scoped>
.retry-button {
  background-color: var(--color-primary-bg);
  color: var(--color-primary-fg);
}

.retry-button:hover {
  background-color: var(--color-primary-bg-hover);
}

.article-content {
  line-height: 1.95;
  font-size: 1.05rem;
  color: #334155;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.article-content :deep(p) {
  margin: 0;
}

.article-content :deep(p + p) {
  margin-top: 1.15rem;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4) {
  margin-top: 2rem;
  margin-bottom: 0.75rem;
  line-height: 1.35;
  color: #0f172a;
}

.article-content :deep(h1) {
  font-size: 1.9rem;
}

.article-content :deep(h2) {
  font-size: 1.5rem;
}

.article-content :deep(h3) {
  font-size: 1.25rem;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  margin-top: 1rem;
  margin-bottom: 1rem;
  padding-left: 1.35rem;
}

.article-content :deep(ul) {
  list-style: disc;
}

.article-content :deep(ol) {
  list-style: decimal;
}

.article-content :deep(li + li) {
  margin-top: 0.45rem;
}

.article-content :deep(blockquote) {
  margin: 1.5rem 0;
  padding: 0.8rem 1rem;
  border-left: 4px solid var(--focus-color);
  background: #fff7ed;
  color: #7c2d12;
  border-radius: 0.5rem;
}

.article-content :deep(a) {
  color: var(--focus-color);
  text-decoration: underline;
  text-decoration-thickness: 2px;
  text-underline-offset: 2px;
}

.article-content :deep(img) {
  display: block;
  max-width: 100%;
  height: auto;
  margin: 1.5rem auto;
  border-radius: 0.5rem;
  cursor: zoom-in;
}

.article-content :deep(.media-image-error) {
  display: flex;
  min-height: 6rem;
  align-items: center;
  justify-content: center;
  margin: 1.5rem 0;
  border: 1px dashed #cbd5e1;
  border-radius: 0.5rem;
  background: #f8fafc;
  padding: 1rem;
  color: #64748b;
  font-size: 0.95rem;
}

.article-content :deep(.media-image-loading) {
  min-height: 6rem;
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
}

.article-content :deep(pre) {
  margin: 1.2rem 0;
  overflow-x: auto;
  overflow-wrap: normal;
  word-break: normal;
  border-radius: 0.75rem;
  background: #111827;
  padding: 1rem;
  line-height: 1.6;
}

.article-content :deep(pre code) {
  color: #e5e7eb;
  font-size: 0.9rem;
}

.article-content :deep(code) {
  border-radius: 0.375rem;
  background: #e2e8f0;
  overflow-wrap: normal;
  word-break: normal;
  padding: 0.1rem 0.35rem;
  font-size: 0.85em;
}

.article-content :deep(strong),
.article-content :deep(b) {
  color: var(--focus-color);
  font-weight: 700;
}

.article-content :deep(pre code) {
  background: transparent;
  padding: 0;
}
</style>
