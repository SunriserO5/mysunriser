<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchProjects } from '../api'
import Pagination from '../components/Pagination.vue'
import ProjectCard from '../components/ProjectCard.vue'
import { usePagination } from '../composables/usePagination'
import type { ProjectItem } from '../types'

const route = useRoute()
const router = useRouter()

const projects = ref<ProjectItem[]>([])
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

async function loadProjects(): Promise<void> {
  loading.value = true
  error.value = null

  try {
    const response = await fetchProjects(page.value, pageSize.value)
    projects.value = response.items
    syncHasNext(response.items.length)
  } catch (err) {
    projects.value = []
    error.value = err instanceof Error ? err.message : '项目列表加载失败'
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

    await loadProjects()

    if (prev !== undefined && prev !== next) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  },
  { immediate: true },
)
</script>

<template>
  <section class="fade-in">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">PROJECTS</p>
    <h1 class="mt-2 text-4xl font-bold tracking-tight text-slate-900">项目</h1>
    <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-600">
      整理正在维护或值得记录的代码项目，并从 GitHub 仓库同步 README 作为详情说明。
    </p>
  </section>

  <section class="mt-8">
    <p v-if="loading" class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">正在加载...</p>
    <div v-else-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
      <p>{{ error }}</p>
      <button type="button" class="retry-button mt-3 rounded-full px-4 py-2 text-sm font-semibold" @click="loadProjects">
        重试
      </button>
    </div>

    <template v-else>
      <div v-if="projects.length" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <ProjectCard v-for="item in projects" :key="item.slug" :item="item" />
      </div>
      <p v-else class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-500">暂无项目。</p>

      <Pagination
        :page="page"
        :can-prev="canPrev"
        :has-next="hasNext"
        :loading="loading"
        @prev="prevPage"
        @next="nextPage"
      />
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
