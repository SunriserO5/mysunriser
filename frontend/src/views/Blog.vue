<script setup lang="ts">
import { ref, watch } from 'vue'
import { fetchPage } from '../api'
import Pagination from '../components/Pagination.vue'
import PostCard from '../components/PostCard.vue'
import { usePagination } from '../composables/usePagination'
import type { PageItem } from '../types'

const posts = ref<PageItem[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const { page, pageSize, canPrev, hasNext, nextPage, prevPage, syncHasNext } = usePagination(1, 6)

async function loadPageData() {
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

watch(page, loadPageData, { immediate: true })
</script>

<template>
  <section class="fade-in">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">BLOG</p>
    <h1 class="mt-2 text-4xl font-bold tracking-tight text-slate-900">文章列表</h1>
    <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-600">
      聚焦代码实践、项目复盘与思考沉淀。分页数据来自后端接口 /api/page。
    </p>
  </section>

  <section class="mt-8">
    <p v-if="loading" class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">正在加载...</p>
    <p v-else-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">{{ error }}</p>

    <template v-else>
      <div v-if="posts.length" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <PostCard v-for="item in posts" :key="item.slug" :item="item" />
      </div>
      <p v-else class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-500">当前页暂无文章。</p>

      <Pagination :page="page" :can-prev="canPrev" :has-next="hasNext" :loading="loading" @prev="prevPage" @next="nextPage" />
    </template>
  </section>
</template>
