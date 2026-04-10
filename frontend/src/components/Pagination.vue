<script setup lang="ts">
defineProps<{
  page: number
  canPrev: boolean
  hasNext: boolean
  loading?: boolean
}>()

const emit = defineEmits<{
  prev: []
  next: []
}>()
</script>

<template>
  <nav class="mt-8 flex items-center justify-center gap-3" aria-label="分页">
    <button
      type="button"
      class="rounded-full border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:border-slate-400 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
      :disabled="!canPrev || loading"
      @click="emit('prev')"
    >
      上一页
    </button>

    <span class="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">第 {{ page }} 页</span>

    <button
      type="button"
      class="rounded-full border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:border-slate-400 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
      :disabled="!hasNext || loading"
      @click="emit('next')"
    >
      下一页
    </button>
  </nav>
</template>
