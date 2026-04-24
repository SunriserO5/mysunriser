<script setup lang="ts">
import type { PageItem } from '../types'

defineProps<{
  item: PageItem
}>()

function formatDate(dateLike: string): string {
  const date = new Date(dateLike)
  if (Number.isNaN(date.getTime())) {
    return dateLike
  }

  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
  }).format(date)
}
</script>

<template>
  <article
    class="group rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg"
  >
    <div class="mb-3 flex items-center justify-between text-xs text-slate-500">
      <span class="rounded-full bg-emerald-50 px-2.5 py-1 font-semibold text-emerald-700">{{ item.status }}</span>
      <time>{{ formatDate(item.publishAt) }}</time>
    </div>

    <h3 class="text-xl font-semibold tracking-tight text-slate-900">
      <RouterLink :to="`/blog/${item.slug}`" class="focus-ring rounded-sm">{{ item.title }}</RouterLink>
    </h3>

    <p class="mt-3 line-clamp-3 text-sm leading-7 text-slate-600">{{ item.summary }}</p>

    <RouterLink
      :to="`/blog/${item.slug}`"
      class="mt-5 inline-flex items-center gap-1 text-sm font-semibold text-orange-600 transition group-hover:text-orange-500"
    >
      阅读全文
      <span aria-hidden="true">→</span>
    </RouterLink>
  </article>
</template>
