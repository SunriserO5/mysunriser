<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError, createAdminPost } from '../api'
import PostEditorForm from '../components/PostEditorForm.vue'
import type { AdminPostCreatePayload, PostEditorFormValue } from '../types'
import { makeEmptyPostEditorValue, toApiPublishedAt } from '../utils/postEditor'

const router = useRouter()
const formValue = ref<PostEditorFormValue>(makeEmptyPostEditorValue())
const saving = ref(false)
const error = ref<string | null>(null)

function validate(value: PostEditorFormValue): string | null {
  if (!value.slug.trim() || !value.title.trim() || !value.content.trim() || !value.status.trim()) {
    return 'Slug、标题、正文和状态不能为空'
  }

  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(value.slug.trim())) {
    return 'Slug 只能使用小写字母、数字和短横线'
  }

  return null
}

async function savePost(value: PostEditorFormValue) {
  if (saving.value) {
    return
  }

  const validationError = validate(value)
  if (validationError) {
    error.value = validationError
    return
  }

  const payload: AdminPostCreatePayload = {
    slug: value.slug.trim(),
    title: value.title.trim(),
    content: value.content,
    status: value.status.trim(),
    published_at: toApiPublishedAt(value.publishedAt),
  }

  saving.value = true
  error.value = null

  try {
    const created = await createAdminPost(payload)
    await router.push({ name: 'post', params: { slug: created.slug } })
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : '文章创建失败'
  } finally {
    saving.value = false
  }
}

function cancelCreate() {
  void router.push({ name: 'blog' })
}
</script>

<template>
  <article class="fade-in mx-auto max-w-3xl rounded-3xl border border-slate-200/80 bg-white p-6 shadow-sm sm:p-10">
    <header class="mb-8">
      <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">ADMIN</p>
      <h1 class="mt-3 text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">新建文章</h1>
    </header>

    <PostEditorForm
      mode="create"
      :error="error"
      :initial-value="formValue"
      :saving="saving"
      submit-label="创建"
      @cancel="cancelCreate"
      @submit="savePost"
    />
  </article>
</template>
