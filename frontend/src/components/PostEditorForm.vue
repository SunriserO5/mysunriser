<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ApiError, deleteAdminMedia, fetchAdminMedia, uploadAdminMedia } from '../api'
import type { MediaAccessLevel, MediaAsset, PostEditorFormValue } from '../types'

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

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const mediaAccessLevel = ref<MediaAccessLevel>('PUBLIC')
const mediaItems = ref<MediaAsset[]>([])
const mediaLoading = ref(false)
const mediaUploading = ref(false)
const mediaDeletingId = ref<number | null>(null)
const mediaMessage = ref<string | null>(null)
const mediaError = ref<string | null>(null)

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

onMounted(() => {
  void loadMediaLibrary()
})

function submitForm() {
  emit('submit', { ...form })
}

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  selectedFile.value = input.files?.[0] ?? null
  mediaMessage.value = null
  mediaError.value = null
}

async function loadMediaLibrary() {
  mediaLoading.value = true
  mediaError.value = null

  try {
    const response = await fetchAdminMedia(1, 20)
    mediaItems.value = response.items
  } catch (err) {
    mediaError.value = err instanceof ApiError ? err.message : '媒体列表加载失败'
  } finally {
    mediaLoading.value = false
  }
}

async function uploadMedia() {
  if (!selectedFile.value || mediaUploading.value) {
    return
  }

  mediaUploading.value = true
  mediaMessage.value = null
  mediaError.value = null

  try {
    const uploaded = await uploadAdminMedia(selectedFile.value, mediaAccessLevel.value)
    insertMarkdown(uploaded.markdown)
    selectedFile.value = null
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
    mediaMessage.value = '已上传并插入正文'
    await loadMediaLibrary()
  } catch (err) {
    mediaError.value = err instanceof ApiError ? err.message : '媒体上传失败'
  } finally {
    mediaUploading.value = false
  }
}

function insertMarkdown(markdown: string) {
  const textarea = textareaRef.value
  const snippet = `\n${markdown}\n`

  if (!textarea) {
    form.content = `${form.content}${snippet}`
    return
  }

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  form.content = `${form.content.slice(0, start)}${snippet}${form.content.slice(end)}`

  void nextTick(() => {
    textarea.focus()
    const cursor = start + snippet.length
    textarea.setSelectionRange(cursor, cursor)
  })
}

async function copyMarkdown(markdown: string) {
  mediaMessage.value = null
  mediaError.value = null

  try {
    await navigator.clipboard.writeText(markdown)
    mediaMessage.value = 'Markdown 已复制'
  } catch {
    mediaError.value = '复制失败，请直接插入正文'
  }
}

async function deleteMedia(item: MediaAsset) {
  if (mediaDeletingId.value !== null) {
    return
  }

  const confirmed = window.confirm(`删除未引用媒体：${item.originalFilename}？`)
  if (!confirmed) {
    return
  }

  mediaDeletingId.value = item.id
  mediaMessage.value = null
  mediaError.value = null

  try {
    await deleteAdminMedia(item.id)
    mediaItems.value = mediaItems.value.filter((media) => media.id !== item.id)
    mediaMessage.value = '媒体已删除'
  } catch (err) {
    mediaError.value = err instanceof ApiError ? err.message : '媒体删除失败'
  } finally {
    mediaDeletingId.value = null
  }
}

function formatSize(bytes: number) {
  if (bytes < 1024) {
    return `${bytes} B`
  }

  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }

  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
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
        ref="textareaRef"
        v-model="form.content"
        class="focus-ring article-editor mt-2 w-full resize-y rounded-md border border-slate-300 px-3 py-3 font-mono text-sm leading-7 text-slate-800 outline-none transition focus:border-orange-500"
        required
        rows="18"
      />
    </div>

    <section class="rounded-lg border border-slate-200 bg-slate-50 p-4">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end">
        <div class="min-w-0 flex-1">
          <label class="block text-sm font-semibold text-slate-700" for="post-media-file">媒体文件</label>
          <input
            id="post-media-file"
            ref="fileInputRef"
            class="focus-ring mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition file:mr-3 file:rounded-md file:border-0 file:bg-slate-900 file:px-3 file:py-1.5 file:text-sm file:font-semibold file:text-white focus:border-orange-500"
            type="file"
            @change="onFileChange"
          />
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700" for="post-media-access">访问权限</label>
          <select
            id="post-media-access"
            v-model="mediaAccessLevel"
            class="focus-ring mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm outline-none transition focus:border-orange-500 lg:w-44"
          >
            <option value="PUBLIC">公开</option>
            <option value="AUTHENTICATED">登录可访问</option>
          </select>
        </div>

        <button
          class="focus-ring rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
          :disabled="!selectedFile || mediaUploading"
          type="button"
          @click="uploadMedia"
        >
          {{ mediaUploading ? '上传中...' : '上传并插入' }}
        </button>
      </div>

      <p v-if="mediaMessage" class="mt-3 text-sm text-emerald-700">{{ mediaMessage }}</p>
      <p v-if="mediaError" class="mt-3 text-sm text-rose-700">{{ mediaError }}</p>

      <div class="mt-4 border-t border-slate-200 pt-4">
        <div class="flex items-center justify-between gap-3">
          <h2 class="text-sm font-semibold text-slate-800">最近媒体</h2>
          <button
            class="focus-ring rounded-md border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
            :disabled="mediaLoading"
            type="button"
            @click="loadMediaLibrary"
          >
            刷新
          </button>
        </div>

        <p v-if="mediaLoading" class="mt-3 text-sm text-slate-500">正在加载媒体...</p>
        <p v-else-if="mediaItems.length === 0" class="mt-3 text-sm text-slate-500">暂无媒体。</p>
        <ul v-else class="mt-3 divide-y divide-slate-200 rounded-md border border-slate-200 bg-white">
          <li v-for="item in mediaItems" :key="item.id" class="flex flex-col gap-3 p-3 sm:flex-row sm:items-center">
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-semibold text-slate-800">{{ item.originalFilename }}</p>
              <p class="mt-1 text-xs text-slate-500">
                {{ item.assetType === 'IMAGE' ? '图片' : '附件' }} · {{ formatSize(item.sizeBytes) }} ·
                {{ item.accessLevel === 'PUBLIC' ? '公开' : '登录可访问' }}
              </p>
            </div>
            <div class="flex flex-wrap gap-2">
              <button
                class="focus-ring rounded-md border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
                type="button"
                @click="insertMarkdown(item.markdown)"
              >
                插入
              </button>
              <button
                class="focus-ring rounded-md border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
                type="button"
                @click="copyMarkdown(item.markdown)"
              >
                复制 Markdown
              </button>
              <button
                class="focus-ring rounded-md border border-rose-200 bg-white px-3 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:text-rose-300"
                :disabled="mediaDeletingId === item.id"
                type="button"
                @click="deleteMedia(item)"
              >
                {{ mediaDeletingId === item.id ? '删除中...' : '删除' }}
              </button>
            </div>
          </li>
        </ul>
      </div>
    </section>

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
