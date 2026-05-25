<script setup lang="ts">
import type { ProjectItem } from '../types'

defineProps<{
  item: ProjectItem
}>()
</script>

<template>
  <article
    class="group rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg"
  >
    <div class="mb-3 flex items-center justify-between gap-3 text-xs text-slate-500">
      <span class="rounded-full bg-emerald-50 px-2.5 py-1 font-semibold text-emerald-700">
        {{ item.repoOwner }}
      </span>
      <span class="font-semibold text-slate-400">{{ item.repoName }}</span>
    </div>

    <h3 class="text-xl font-semibold tracking-tight text-slate-900">
      <RouterLink :to="`/projects/${item.slug}`" class="focus-ring rounded-sm">{{ item.title }}</RouterLink>
    </h3>

    <p class="mt-3 line-clamp-3 text-sm leading-7 text-slate-600">{{ item.summary }}</p>

    <div class="mt-5 flex flex-wrap items-center gap-3">
      <RouterLink
        :to="`/projects/${item.slug}`"
        class="inline-flex items-center gap-1 text-sm font-semibold text-orange-600 transition group-hover:text-orange-500"
      >
        查看项目
        <span aria-hidden="true">→</span>
      </RouterLink>
      <a
        :href="item.repoUrl"
        class="inline-flex items-center gap-1 text-sm font-semibold text-slate-500 transition hover:text-slate-900"
        rel="noreferrer"
        target="_blank"
      >
        GitHub
        <span aria-hidden="true">↗</span>
      </a>
    </div>
  </article>
</template>
