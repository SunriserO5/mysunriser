<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'
import { usePost } from '../composables/usePost'

const route = useRoute()
const slug = computed(() => String(route.params.slug ?? ''))

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: false,
})

const { post, loading, error, loadPost } = usePost()

const fallbackTitle = '文章 | MySunriser'

watch(
  slug,
  (nextSlug) => {
    if (!nextSlug) {
      return
    }

    loadPost(nextSlug)
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

const publishedAt = computed(() => {
  if (!post.value?.publish_at) {
    return ''
  }

  const date = new Date(post.value.publish_at)
  if (Number.isNaN(date.getTime())) {
    return post.value.publish_at
  }

  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'full',
    timeStyle: 'short',
  }).format(date)
})

const renderedContent = computed(() => {
  const source = post.value?.content ?? ''
  const html = markdown.render(source)

  return DOMPurify.sanitize(html)
})
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
      <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">{{ post.status }}</p>
      <h1 class="mt-3 text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">{{ post.title }}</h1>
      <p class="mt-4 text-sm text-slate-500">{{ publishedAt }}</p>

      <div class="article-content mt-8" v-html="renderedContent" />
    </div>

    <p v-else class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">未找到文章。</p>
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

.article-content :deep(pre) {
  margin: 1.2rem 0;
  overflow-x: auto;
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
