<script setup lang="ts">
import type { ToolItem } from '../types'

const props = defineProps<{
  item: ToolItem
}>()

const accessLabels: Record<ToolItem['accessLevel'], string> = {
  PUBLIC: '公开',
  AUTHENTICATED: '登录可用',
  ADMIN: '管理员',
}

const isExternal = props.item.entryType === 'EXTERNAL'
const target = props.item.entryType === 'EXTERNAL' ? props.item.externalUrl : props.item.routePath || `/tools/${props.item.slug}`
</script>

<template>
  <article
    class="group rounded-2xl border border-slate-200/80 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg"
  >
    <div class="mb-3 flex items-center justify-between gap-3 text-xs text-slate-500">
      <span class="rounded-full bg-orange-50 px-2.5 py-1 font-semibold text-orange-700">
        {{ accessLabels[item.accessLevel] }}
      </span>
      <span class="font-semibold text-slate-400">{{ item.entryType === 'EXTERNAL' ? '外部' : '站内' }}</span>
    </div>

    <h3 class="text-xl font-semibold tracking-tight text-slate-900">
      <a v-if="isExternal" :href="target" class="focus-ring rounded-sm" rel="noreferrer" target="_blank">
        {{ item.title }}
      </a>
      <RouterLink v-else :to="target" class="focus-ring rounded-sm">{{ item.title }}</RouterLink>
    </h3>

    <p class="mt-3 line-clamp-3 text-sm leading-7 text-slate-600">{{ item.summary }}</p>

    <a
      v-if="isExternal"
      :href="target"
      class="mt-5 inline-flex items-center gap-1 text-sm font-semibold text-orange-600 transition group-hover:text-orange-500"
      rel="noreferrer"
      target="_blank"
    >
      打开工具
      <span aria-hidden="true">→</span>
    </a>
    <RouterLink
      v-else
      :to="target"
      class="mt-5 inline-flex items-center gap-1 text-sm font-semibold text-orange-600 transition group-hover:text-orange-500"
    >
      打开工具
      <span aria-hidden="true">→</span>
    </RouterLink>
  </article>
</template>
