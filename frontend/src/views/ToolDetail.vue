<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchTool } from '../api'
import type { ToolItem } from '../types'

const route = useRoute()
const tool = ref<ToolItem | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const slug = computed(() => String(route.params.slug ?? ''))
const target = computed(() => {
  if (!tool.value) {
    return ''
  }

  return tool.value.entryType === 'EXTERNAL' ? tool.value.externalUrl : tool.value.routePath || `/tools/${tool.value.slug}`
})

async function loadTool(): Promise<void> {
  if (!slug.value) {
    return
  }

  loading.value = true
  error.value = null

  try {
    tool.value = await fetchTool(slug.value)
  } catch (err) {
    tool.value = null
    error.value = err instanceof Error ? err.message : '工具加载失败'
  } finally {
    loading.value = false
  }
}

watch(slug, loadTool, { immediate: true })
</script>

<template>
  <section class="fade-in">
    <RouterLink to="/tools" class="text-sm font-semibold text-orange-600 hover:text-orange-500">← 返回工具列表</RouterLink>

    <p v-if="loading" class="mt-6 rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">正在加载...</p>
    <p v-else-if="error" class="mt-6 rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">{{ error }}</p>

    <article v-else-if="tool" class="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex flex-wrap items-center gap-2 text-xs font-semibold">
        <span class="rounded-full bg-orange-50 px-2.5 py-1 text-orange-700">
          {{ tool.entryType === 'EXTERNAL' ? '外部工具' : '站内工具' }}
        </span>
        <span class="rounded-full bg-slate-100 px-2.5 py-1 text-slate-600">{{ tool.accessLevel }}</span>
      </div>

      <h1 class="mt-4 text-3xl font-bold tracking-tight text-slate-900">{{ tool.title }}</h1>
      <p class="mt-4 max-w-3xl text-sm leading-7 text-slate-600">{{ tool.summary }}</p>

      <a
        v-if="tool.entryType === 'EXTERNAL'"
        :href="target"
        class="mt-6 inline-flex rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-800"
        rel="noreferrer"
        target="_blank"
      >
        打开工具
      </a>
      <p v-else class="mt-6 rounded-md border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">
        功能接入中。
      </p>
    </article>
  </section>
</template>
