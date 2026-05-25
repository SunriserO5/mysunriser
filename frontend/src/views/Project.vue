<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'
import { fetchProject } from '../api'
import type { ProjectItem } from '../types'

const route = useRoute()
const slug = computed(() => String(route.params.slug ?? ''))

const project = ref<ProjectItem | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: false,
})

const defaultImageRule = markdown.renderer.rules.image
const defaultLinkOpenRule = markdown.renderer.rules.link_open

markdown.renderer.rules.image = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  const src = token.attrGet('src')
  if (src) {
    token.attrSet('src', resolveReadmeUrl(src, true))
  }

  return defaultImageRule ? defaultImageRule(tokens, idx, options, env, self) : self.renderToken(tokens, idx, options)
}

markdown.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  const href = token.attrGet('href')
  if (href) {
    token.attrSet('href', resolveReadmeUrl(href, false))
  }
  token.attrSet('target', '_blank')
  token.attrSet('rel', 'noreferrer')

  return defaultLinkOpenRule ? defaultLinkOpenRule(tokens, idx, options, env, self) : self.renderToken(tokens, idx, options)
}

watch(
  slug,
  (nextSlug) => {
    if (!nextSlug) {
      return
    }

    void loadProject(nextSlug)
  },
  { immediate: true },
)

watch(
  [project, error],
  () => {
    if (project.value?.title) {
      document.title = `${project.value.title} | MySunriser`
      return
    }

    if (error.value) {
      document.title = '未找到项目 | MySunriser'
      return
    }

    document.title = '项目 | MySunriser'
  },
  { immediate: true },
)

async function loadProject(nextSlug = slug.value) {
  loading.value = true
  error.value = null

  try {
    project.value = await fetchProject(nextSlug)
  } catch (err) {
    project.value = null
    error.value = err instanceof Error ? err.message : '项目加载失败'
  } finally {
    loading.value = false
  }
}

function isSpecialUrl(value: string): boolean {
  return /^(?:[a-z][a-z\d+.-]*:|#)/i.test(value)
}

function resolveReadmeUrl(value: string, raw: boolean): string {
  if (!project.value || isSpecialUrl(value)) {
    return value
  }

  const normalized = value.replace(/^\/+/, '')
  const base = raw
    ? `https://raw.githubusercontent.com/${project.value.repoOwner}/${project.value.repoName}/HEAD/`
    : `https://github.com/${project.value.repoOwner}/${project.value.repoName}/blob/HEAD/`

  try {
    return new URL(normalized, base).toString()
  } catch {
    return value
  }
}

const renderedReadme = computed(() => {
  const source = project.value?.readmeMarkdown ?? ''
  if (!source.trim()) {
    return ''
  }

  return DOMPurify.sanitize(markdown.render(source))
})
</script>

<template>
  <article class="fade-in mx-auto max-w-4xl">
    <p v-if="loading" class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">正在加载项目...</p>
    <div v-else-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
      <p>{{ error }}</p>
      <button type="button" class="retry-button mt-3 rounded-full px-4 py-2 text-sm font-semibold" @click="loadProject()">
        重试
      </button>
    </div>

    <div v-else-if="project" class="rounded-3xl border border-slate-200/80 bg-white p-6 shadow-sm sm:p-10">
      <header>
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">{{ project.status }}</p>
        <h1 class="mt-3 text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">{{ project.title }}</h1>
        <p class="mt-4 max-w-3xl text-sm leading-7 text-slate-600">{{ project.summary }}</p>

        <div class="mt-6 flex flex-wrap items-center gap-3">
          <a
            :href="project.repoUrl"
            class="primary-link focus-ring rounded-md px-4 py-2 text-sm font-semibold transition"
            rel="noreferrer"
            target="_blank"
          >
            直达 GitHub
          </a>
          <span class="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs font-semibold text-slate-600">
            {{ project.repoOwner }}/{{ project.repoName }}
          </span>
        </div>
      </header>

      <p v-if="project.readmeError" class="mt-8 rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
        {{ project.readmeError }}
      </p>

      <section class="mt-8 border-t border-slate-200 pt-8">
        <h2 class="text-2xl font-semibold tracking-tight text-slate-900">README</h2>
        <div v-if="renderedReadme" class="readme-content mt-6" v-html="renderedReadme" />
        <p v-else class="mt-6 rounded-md border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">
          暂无可展示的 README 内容。
        </p>
      </section>
    </div>

    <p v-else class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">未找到项目。</p>
  </article>
</template>

<style scoped>
.retry-button,
.primary-link {
  background-color: var(--color-primary-bg);
  color: var(--color-primary-fg);
}

.retry-button:hover,
.primary-link:hover {
  background-color: var(--color-primary-bg-hover);
}

.readme-content {
  line-height: 1.9;
  font-size: 1rem;
  color: #334155;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.readme-content :deep(p) {
  margin: 0;
}

.readme-content :deep(p + p) {
  margin-top: 1rem;
}

.readme-content :deep(h1),
.readme-content :deep(h2),
.readme-content :deep(h3),
.readme-content :deep(h4) {
  margin-top: 1.75rem;
  margin-bottom: 0.75rem;
  line-height: 1.35;
  color: #0f172a;
}

.readme-content :deep(h1) {
  font-size: 1.8rem;
}

.readme-content :deep(h2) {
  font-size: 1.45rem;
}

.readme-content :deep(h3) {
  font-size: 1.2rem;
}

.readme-content :deep(ul),
.readme-content :deep(ol) {
  margin-top: 1rem;
  margin-bottom: 1rem;
  padding-left: 1.35rem;
}

.readme-content :deep(ul) {
  list-style: disc;
}

.readme-content :deep(ol) {
  list-style: decimal;
}

.readme-content :deep(li + li) {
  margin-top: 0.45rem;
}

.readme-content :deep(blockquote) {
  margin: 1.5rem 0;
  padding: 0.8rem 1rem;
  border-left: 4px solid var(--focus-color);
  background: #fff7ed;
  color: #7c2d12;
  border-radius: 0.5rem;
}

.readme-content :deep(a) {
  color: var(--focus-color);
  text-decoration: underline;
  text-decoration-thickness: 2px;
  text-underline-offset: 2px;
}

.readme-content :deep(img) {
  display: block;
  max-width: 100%;
  height: auto;
  margin: 1.5rem auto;
  border-radius: 0.5rem;
}

.readme-content :deep(pre) {
  margin: 1.2rem 0;
  overflow-x: auto;
  overflow-wrap: normal;
  word-break: normal;
  border-radius: 0.75rem;
  background: #111827;
  padding: 1rem;
  line-height: 1.6;
}

.readme-content :deep(pre code) {
  background: transparent;
  padding: 0;
  color: #e5e7eb;
  font-size: 0.9rem;
}

.readme-content :deep(code) {
  border-radius: 0.375rem;
  background: #e2e8f0;
  overflow-wrap: normal;
  word-break: normal;
  padding: 0.1rem 0.35rem;
  font-size: 0.85em;
}

.readme-content :deep(strong),
.readme-content :deep(b) {
  color: var(--focus-color);
  font-weight: 700;
}
</style>
