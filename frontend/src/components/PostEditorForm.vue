<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { PostEditorFormValue } from '../types'

const props = withDefaults(
  defineProps<{
    mode: 'create' | 'edit'
    initialValue: PostEditorFormValue
    saving?: boolean
    error?: string | null
    submitLabel?: string
  }>(),
  {
    saving: false,
    error: null,
    submitLabel: '保存',
  },
)

const emit = defineEmits<{
  submit: [value: PostEditorFormValue]
  cancel: []
}>()

const form = reactive<PostEditorFormValue>({
  slug: '',
  title: '',
  content: '',
  status: 'Draft',
  publishedAt: '',
})

const statusOptions = computed(() => {
  const defaults = ['Published', 'Draft']

  if (form.status && !defaults.includes(form.status)) {
    return [form.status, ...defaults]
  }

  return defaults
})

watch(
  () => props.initialValue,
  (next) => {
    form.slug = next.slug
    form.title = next.title
    form.content = next.content
    form.status = next.status
    form.publishedAt = next.publishedAt
  },
  { immediate: true, deep: true },
)

function submitForm() {
  emit('submit', { ...form })
}
</script>

<template>
  <form class="space-y-6" @submit.prevent="submitForm">
    <header class="space-y-4">
      <div v-if="mode === 'create'">
        <label class="block text-sm font-semibold text-slate-700" for="post-edit-slug">Slug</label>
        <input
          id="post-edit-slug"
          v-model.trim="form.slug"
          class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition focus:border-orange-500"
          maxlength="128"
          pattern="[a-z0-9]+(-[a-z0-9]+)*"
          placeholder="my-new-post"
          required
          type="text"
        />
      </div>

      <div class="grid gap-4 sm:grid-cols-[minmax(0,1fr)_220px]">
        <div>
          <label class="block text-sm font-semibold text-slate-700" for="post-edit-title">标题</label>
          <input
            id="post-edit-title"
            v-model="form.title"
            class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-2xl font-bold text-slate-900 outline-none transition focus:border-orange-500 sm:text-3xl"
            maxlength="200"
            required
            type="text"
          />
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700" for="post-edit-status">状态</label>
          <select
            id="post-edit-status"
            v-model="form.status"
            class="focus-ring mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm outline-none transition focus:border-orange-500"
            required
          >
            <option v-for="status in statusOptions" :key="status" :value="status">{{ status }}</option>
          </select>
        </div>
      </div>

      <div>
        <label class="block text-sm font-semibold text-slate-700" for="post-edit-published-at">发布时间</label>
        <input
          id="post-edit-published-at"
          v-model="form.publishedAt"
          class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500 sm:w-72"
          type="datetime-local"
        />
      </div>
    </header>

    <div>
      <label class="block text-sm font-semibold text-slate-700" for="post-edit-content">正文</label>
      <textarea
        id="post-edit-content"
        v-model="form.content"
        class="focus-ring article-editor mt-2 w-full resize-y rounded-md border border-slate-300 px-3 py-3 font-mono text-sm leading-7 text-slate-800 outline-none transition focus:border-orange-500"
        required
        rows="18"
      />
    </div>

    <p v-if="error" class="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
      {{ error }}
    </p>

    <div class="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
      <button
        class="focus-ring rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
        :disabled="saving"
        type="button"
        @click="emit('cancel')"
      >
        取消
      </button>
      <button
        class="focus-ring rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
        :disabled="saving"
        type="submit"
      >
        {{ saving ? '保存中...' : submitLabel }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.article-editor {
  min-height: 28rem;
}
</style>
