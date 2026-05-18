<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ApiError, extractVideoDownload } from '../api'
import type { VideoDownloadExtractResponse, VideoDownloadFormat, VideoDownloadMedia } from '../types'

const route = useRoute()

const url = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const authRequired = ref(false)
const result = ref<VideoDownloadExtractResponse | null>(null)

const loginTarget = computed(() => ({
  name: 'login',
  query: {
    redirect: route.fullPath,
  },
}))

async function submit() {
  const sourceUrl = url.value.trim()
  if (!sourceUrl) {
    error.value = '请输入链接'
    return
  }

  loading.value = true
  error.value = null
  authRequired.value = false
  result.value = null

  try {
    result.value = await extractVideoDownload({ url: sourceUrl })
  } catch (err) {
    if (err instanceof ApiError && err.status === 401) {
      authRequired.value = true
      error.value = '登录后可使用解析功能'
    } else {
      error.value = err instanceof Error ? err.message : '解析失败'
    }
  } finally {
    loading.value = false
  }
}

function mediaLabel(mediaType: string | null): string {
  if (mediaType === 'video') {
    return '视频'
  }

  if (mediaType === 'image') {
    return '图片'
  }

  if (mediaType === 'audio') {
    return '音频'
  }

  return '资源'
}

function sizeLabel(size: number | null): string {
  if (!size || size < 1) {
    return ''
  }

  const units = ['B', 'KB', 'MB', 'GB']
  let value = size
  let unitIndex = 0
  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex += 1
  }

  return `${value.toFixed(value >= 10 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`
}

function formatTitle(format: VideoDownloadFormat): string {
  return format.qualityNote || (format.quality ? `${format.quality}P` : '格式')
}

function hasSpecialHeaders(media: VideoDownloadMedia): boolean {
  return Object.keys(media.headers ?? {}).length > 0
}
</script>

<template>
  <section class="fade-in">
    <RouterLink to="/tools" class="text-sm font-semibold text-orange-600 hover:text-orange-500">← 返回工具列表</RouterLink>

    <div class="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">VIDEO DOWNLOAD</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">在线视频解析下载器</h1>
      <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-600">
        支持解析并下载 YouTube、Twitter、Instagram、Facebook 等平台的视频资源。
      </p>

      <form class="mt-6 flex flex-col gap-3 sm:flex-row" @submit.prevent="submit">
        <label class="min-w-0 flex-1">
          <span class="sr-only">视频链接</span>
          <input
            v-model.trim="url"
            class="focus-ring w-full rounded-md border border-slate-300 px-3 py-2.5 text-sm outline-none transition focus:border-orange-500"
            placeholder="https://example.com/detail/..."
            type="url"
          />
        </label>
        <button
          class="tool-primary-button focus-ring rounded-md px-5 py-2.5 text-sm font-semibold transition disabled:cursor-not-allowed"
          :disabled="loading"
          type="submit"
        >
          {{ loading ? '解析中...' : '解析' }}
        </button>
      </form>

      <div v-if="error" class="mt-5 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        <p>{{ error }}</p>
        <RouterLink v-if="authRequired" :to="loginTarget" class="mt-2 inline-flex font-semibold text-rose-800 underline">
          去登录
        </RouterLink>
      </div>
    </div>

    <section v-if="result" class="mt-6 space-y-4">
      <article v-if="result.text" class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">解析文本</h2>
        <p class="mt-3 whitespace-pre-wrap text-sm leading-7 text-slate-600">{{ result.text }}</p>
      </article>

      <p v-if="!result.medias.length" class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-500">
        未解析到可用资源。
      </p>

      <article
        v-for="(media, index) in result.medias"
        :key="`${media.resourceUrl ?? media.previewUrl ?? index}`"
        class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm"
      >
        <div class="grid gap-0 lg:grid-cols-[280px_minmax(0,1fr)]">
          <div class="border-b border-slate-200 bg-slate-50 lg:border-b-0 lg:border-r">
            <img
              v-if="media.previewUrl"
              :src="media.previewUrl"
              alt=""
              class="aspect-video h-full w-full object-cover"
              loading="lazy"
            />
            <div v-else class="flex aspect-video items-center justify-center px-4 text-sm font-semibold text-slate-400">
              {{ mediaLabel(media.mediaType) }}
            </div>
          </div>

          <div class="p-5">
            <div class="flex flex-wrap items-center gap-2">
              <span class="rounded-full bg-orange-50 px-2.5 py-1 text-xs font-semibold text-orange-700">
                {{ mediaLabel(media.mediaType) }}
              </span>
              <span v-if="hasSpecialHeaders(media)" class="rounded-full bg-amber-50 px-2.5 py-1 text-xs font-semibold text-amber-700">
                可能需要特殊请求头
              </span>
            </div>

            <div class="mt-4 flex flex-wrap gap-3">
              <a
                v-if="media.resourceUrl"
                :href="media.resourceUrl"
                class="tool-primary-button focus-ring rounded-md px-4 py-2 text-sm font-semibold transition"
                rel="noreferrer"
                target="_blank"
              >
                打开原链
              </a>
              <a
                v-if="media.previewUrl"
                :href="media.previewUrl"
                class="tool-secondary-button focus-ring rounded-md border px-4 py-2 text-sm font-semibold transition"
                rel="noreferrer"
                target="_blank"
              >
                打开封面
              </a>
            </div>

            <div v-if="media.formats.length" class="mt-5">
              <h3 class="text-sm font-semibold text-slate-900">清晰度</h3>
              <div class="mt-3 grid gap-3">
                <section v-for="(format, formatIndex) in media.formats" :key="formatIndex" class="rounded-lg border border-slate-200 p-3">
                  <div class="flex flex-wrap items-center justify-between gap-2">
                    <p class="font-semibold text-slate-900">{{ formatTitle(format) }}</p>
                    <p v-if="format.separate === 1" class="text-xs font-semibold text-slate-500">音视频分离</p>
                  </div>

                  <div class="mt-3 flex flex-wrap gap-2">
                    <a
                      v-if="format.videoUrl"
                      :href="format.videoUrl"
                      class="tool-secondary-button rounded-md border px-3 py-1.5 text-sm font-semibold transition"
                      rel="noreferrer"
                      target="_blank"
                    >
                      视频 {{ format.videoExt || '' }} {{ sizeLabel(format.videoSize) }}
                    </a>
                    <a
                      v-if="format.audioUrl"
                      :href="format.audioUrl"
                      class="tool-secondary-button rounded-md border px-3 py-1.5 text-sm font-semibold transition"
                      rel="noreferrer"
                      target="_blank"
                    >
                      音频 {{ format.audioExt || '' }} {{ sizeLabel(format.audioSize) }}
                    </a>
                  </div>
                </section>
              </div>
            </div>
          </div>
        </div>
      </article>
    </section>
  </section>
</template>

<style scoped>
.tool-primary-button {
  background-color: var(--color-primary-bg) !important;
  color: var(--color-primary-fg) !important;
  -webkit-text-fill-color: var(--color-primary-fg);
}

.tool-primary-button:hover {
  background-color: var(--color-primary-bg-hover) !important;
  color: #ffffff !important;
  -webkit-text-fill-color: #ffffff;
}

.tool-primary-button:disabled {
  background-color: #94a3b8 !important;
  color: #ffffff !important;
  -webkit-text-fill-color: #ffffff;
}

.tool-secondary-button {
  border-color: #cbd5e1 !important;
  background-color: #ffffff !important;
  color: #0f172a !important;
  -webkit-text-fill-color: #0f172a;
}

.tool-secondary-button:hover {
  border-color: #94a3b8 !important;
  background-color: #f8fafc !important;
  color: #020617 !important;
  -webkit-text-fill-color: #020617;
}
</style>
