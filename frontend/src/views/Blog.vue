<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPage } from '../api'
import Pagination from '../components/Pagination.vue'
import PostCard from '../components/PostCard.vue'
import { useAuth } from '../composables/useAuth'
import { usePagination } from '../composables/usePagination'
import type { PageItem } from '../types'

const route = useRoute()
const router = useRouter()
const auth = useAuth()

const posts = ref<PageItem[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

function parsePage(queryPage: unknown): number {
  const raw = Array.isArray(queryPage) ? queryPage[0] : queryPage
  const value = Number(raw)

  if (!Number.isInteger(value) || value < 1) {
    return 1
  }

  return value
}

const { page, pageSize, canPrev, hasNext, setPage, nextPage, prevPage, syncHasNext } = usePagination(
  parsePage(route.query.page),
  6,
)

async function loadPageData(): Promise<void> {
  loading.value = true
  error.value = null

  try {
    const response = await fetchPage(page.value, pageSize.value)
    posts.value = response.items
    syncHasNext(response.items.length)
  } catch (err) {
    posts.value = []
    error.value = err instanceof Error ? err.message : '加载失败'
    syncHasNext(0)
  } finally {
    loading.value = false
  }
}

function retryLoad() {
  void loadPageData()
}

watch(
  () => route.query.page,
  (queryPage) => {
    const next = parsePage(queryPage)
    if (next !== page.value) {
      setPage(next)
    }
  },
)

watch(
  page,
  async (next, prev) => {
    if (String(route.query.page ?? '') !== String(next)) {
      await router.replace({
        query: {
          ...route.query,
          page: String(next),
        },
      })
    }

    await loadPageData()

    if (prev !== undefined && prev !== next) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  },
  { immediate: true },
)
</script>

<template>
  <section class="fade-in">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">BLOG</p>
        <h1 class="mt-2 text-4xl font-bold tracking-tight text-slate-900">文章列表</h1>
        <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-600">
          聚焦代码实践、项目复盘与思考沉淀。
        </p>
      </div>

      <RouterLink
        v-if="auth.isAdmin.value"
        class="new-post-link focus-ring inline-flex w-fit rounded-md px-4 py-2 text-sm font-semibold transition"
        :to="{ name: 'admin-post-new' }"
      >
        新建文章
      </RouterLink>
    </div>
  </section>

  <section class="mt-8">
    <p v-if="loading" class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">正在加载...</p>
    <div v-else-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
      <p>{{ error }}</p>
      <button type="button" class="retry-button mt-3 rounded-full px-4 py-2 text-sm font-semibold" @click="retryLoad">
        重试
      </button>
    </div>

    <template v-else>
      <div v-if="posts.length" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <PostCard v-for="item in posts" :key="item.slug" :item="item" />
      </div>
      <p v-else class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-500">当前页暂无文章。</p>

      <Pagination :page="page" :can-prev="canPrev" :has-next="hasNext" :loading="loading" @prev="prevPage" @next="nextPage" />
    </template>
  </section>
</template>

<style scoped>
.retry-button {
  background-color: var(--color-primary-bg);
  color: var(--color-primary-fg);
}

.retry-button:hover {
  background-color: var(--color-primary-bg-hover);
}

.new-post-link,
.new-post-link:visited {
  background-color: #0f172a;
  color: #ffffff;
}

.new-post-link:hover {
  background-color: #1e293b;
  color: #ffffff;
}
</style>
