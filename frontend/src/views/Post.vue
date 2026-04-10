<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { usePost } from '../composables/usePost'

const route = useRoute()
const slug = computed(() => String(route.params.slug ?? ''))

const { post, loading, error, loadPost } = usePost()

watch(
  slug,
  (nextSlug) => {
    if (!nextSlug) {
      return
    }

    loadPost(nextSlug)
  },
  { immediate: true },
)

const publishedAt = computed(() => {
  if (!post.value?.publish_at) {
    return ''
  }

  const date = new Date(post.value.publish_at)
  if (Number.isNaN(date.getTime())) {
    return post.value.publish_at
  }

  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'full',
    timeStyle: 'short',
  }).format(date)
})
</script>

<template>
  <article class="fade-in mx-auto max-w-3xl">
    <p v-if="loading" class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">正在加载文章...</p>
    <p v-else-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">{{ error }}</p>

    <div v-else-if="post" class="rounded-3xl border border-slate-200/80 bg-white p-6 shadow-sm sm:p-10">
      <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">{{ post.status }}</p>
      <h1 class="mt-3 text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">{{ post.title }}</h1>
      <p class="mt-4 text-sm text-slate-500">{{ publishedAt }}</p>

      <div class="prose prose-slate mt-8 max-w-none whitespace-pre-wrap leading-8 text-slate-700">
        {{ post.content }}
      </div>
    </div>

    <p v-else class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">未找到文章。</p>
  </article>
</template>
