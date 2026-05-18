<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchTools } from '../api'
import Pagination from '../components/Pagination.vue'
import ToolCard from '../components/ToolCard.vue'
import { usePagination } from '../composables/usePagination'
import type { ToolItem } from '../types'

const route = useRoute()
const router = useRouter()

const tools = ref<ToolItem[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

function parsePage(queryPage: unknown): number {
  const raw = Array.isArray(queryPage) ? queryPage[0] : queryPage
  const value = Number(raw)

  return Number.isInteger(value) && value > 0 ? value : 1
}

const { page, pageSize, canPrev, hasNext, setPage, nextPage, prevPage, syncHasNext } = usePagination(
  parsePage(route.query.page),
  9,
)

async function loadTools(): Promise<void> {
  loading.value = true
  error.value = null

  try {
    const response = await fetchTools(page.value, pageSize.value)
    tools.value = response.items
    syncHasNext(response.items.length)
  } catch (err) {
    tools.value = []
    error.value = err instanceof Error ? err.message : '工具列表加载失败'
    syncHasNext(0)
  } finally {
    loading.value = false
  }
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

    await loadTools()

    if (prev !== undefined && prev !== next) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  },
  { immediate: true },
)
</script>

<template>
  <section class="fade-in">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">TOOLS</p>
    <h1 class="mt-2 text-4xl font-bold tracking-tight text-slate-900">在线小工具</h1>
    <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-600">
      收集一些自研的轻量服务和效率工具。
    </p>
  </section>

  <section class="mt-8">
    <p v-if="loading" class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">正在加载...</p>
    <div v-else-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
      <p>{{ error }}</p>
      <button type="button" class="retry-button mt-3 rounded-full px-4 py-2 text-sm font-semibold" @click="loadTools">
        重试
      </button>
    </div>

    <template v-else>
      <div v-if="tools.length" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <ToolCard v-for="item in tools" :key="item.slug" :item="item" />
      </div>
      <p v-else class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-500">暂无可用工具。</p>

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
</style>
