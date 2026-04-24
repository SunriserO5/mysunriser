<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchHealth, fetchPage } from '../api'
import PostCard from '../components/PostCard.vue'
import type { HealthResponse, PageItem } from '../types'

const health = ref<HealthResponse | null>(null)
const latestPosts = ref<PageItem[]>([])
const postsLoading = ref(false)
const postsError = ref<string | null>(null)

async function loadHomeData() {
  postsLoading.value = true
  postsError.value = null

  const [healthResult, postResult] = await Promise.allSettled([fetchHealth(), fetchPage(1, 3)])

  if (healthResult.status === 'fulfilled') {
    health.value = healthResult.value
  }

  if (postResult.status === 'fulfilled') {
    latestPosts.value = postResult.value.items
  } else {
    postsError.value = postResult.reason instanceof Error ? postResult.reason.message : '最新文章加载失败'
  }

  postsLoading.value = false
}

onMounted(loadHomeData)
</script>

<template>
  <section class="fade-in relative overflow-hidden rounded-3xl border border-amber-200/70 bg-white/85 p-8 shadow-xl sm:p-10">
    <div
      class="pointer-events-none absolute -right-24 -top-24 h-64 w-64 rounded-full bg-gradient-to-br from-orange-200/60 to-emerald-200/60 blur-3xl"
    />

    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">MY SUNRISER</p>
    <h1 class="mt-4 text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">积跬步，
      <span class="text-orange-600">以致千里</span>
    </h1>
    <p class="mt-5 max-w-2xl text-base leading-8 text-slate-600 sm:text-lg">
      这里记录技术、学习和生活观察。前端使用 Vue 3，后端基于 Spring Boot，整体保持轻量、清晰、可持续演进。
    </p>

    <div class="mt-8 flex flex-wrap items-center gap-3">
      <RouterLink
        to="/blog"
        class="cta-primary rounded-full bg-slate-900 px-5 py-2.5 text-sm font-semibold text-slate-50 shadow-md shadow-slate-900/20 transition"
      >
        浏览文章
      </RouterLink>
      <RouterLink
        to="/about"
        class="cta-secondary rounded-full border border-slate-300 bg-white px-5 py-2.5 text-sm font-semibold text-slate-900 transition"
      >
        了解作者
      </RouterLink>
      <span
        v-if="health"
        class="rounded-full px-3 py-1 text-xs font-semibold"
        :class="health.ok && health.db ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'"
      >
        {{ health.ok && health.db ? 'online' : 'offline' }}
      </span>
    </div>
  </section>

  <section class="mt-12">
    <div class="mb-5 flex items-end justify-between">
      <h2 class="text-2xl font-semibold tracking-tight text-slate-900">最新文章</h2>
      <RouterLink to="/blog" class="text-sm font-semibold text-orange-600 hover:text-orange-500">查看全部 →</RouterLink>
    </div>

    <p v-if="postsLoading" class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">正在加载文章...</p>
    <p v-else-if="postsError" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
      {{ postsError }}
    </p>

    <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      <PostCard v-for="item in latestPosts" :key="item.slug" :item="item" />
    </div>
  </section>
</template>

<style scoped>
.cta-primary {
  background-color: var(--color-primary-bg);
  color: var(--color-primary-fg) !important;
  -webkit-text-fill-color: var(--color-primary-fg);
}

.cta-primary:hover {
  background-color: var(--color-primary-bg-hover) !important;
  color: #ffffff !important;
  -webkit-text-fill-color: #ffffff;
}

.cta-secondary {
  background-color: var(--color-secondary-bg);
  color: var(--color-secondary-fg) !important;
  -webkit-text-fill-color: var(--color-secondary-fg);
}

.cta-secondary:hover {
  border-color: var(--color-secondary-border-hover) !important;
  background-color: var(--color-secondary-bg-hover) !important;
  color: var(--color-secondary-fg-hover) !important;
  -webkit-text-fill-color: var(--color-secondary-fg-hover);
}
</style>
