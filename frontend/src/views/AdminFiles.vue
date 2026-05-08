<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ApiError, deleteAdminMedia, fetchAdminMedia, uploadAdminMedia } from '../api'
import type { MediaAccessLevel, MediaAsset } from '../types'

const PAGE_SIZE = 20

const files = ref<MediaAsset[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const uploading = ref(false)
const deleting = ref(false)
const actionId = ref<number | null>(null)
const selectedIds = ref<number[]>([])
const selectedFile = ref<File | null>(null)
const accessLevel = ref<MediaAccessLevel>('PUBLIC')
const fileInputRef = ref<HTMLInputElement | null>(null)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const empty = computed(() => !loading.value && files.value.length === 0)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))
const currentPageIds = computed(() => files.value.map((file) => file.id))
const selectedCount = computed(() => selectedIds.value.length)
const allCurrentPageSelected = computed(
  () => files.value.length > 0 && currentPageIds.value.every((id) => selectedIds.value.includes(id)),
)

function toMessage(err: unknown, fallback: string): string {
  return err instanceof ApiError ? err.message : fallback
}

async function loadFiles(targetPage = page.value) {
  loading.value = true
  error.value = null

  try {
    const nextPage = Math.max(1, targetPage)
    const response = await fetchAdminMedia(nextPage, PAGE_SIZE)
    files.value = response.items
    page.value = response.page
    total.value = response.total
    selectedIds.value = selectedIds.value.filter((id) => response.items.some((file) => file.id === id))
  } catch (err) {
    error.value = toMessage(err, '文件列表加载失败')
  } finally {
    loading.value = false
  }
}

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  selectedFile.value = input.files?.[0] ?? null
  error.value = null
  success.value = null
}

async function uploadFile() {
  if (!selectedFile.value || uploading.value) {
    return
  }

  uploading.value = true
  error.value = null
  success.value = null

  try {
    await uploadAdminMedia(selectedFile.value, accessLevel.value)
    selectedFile.value = null
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
    success.value = '文件已上传'
    await loadFiles(1)
  } catch (err) {
    error.value = toMessage(err, '文件上传失败')
  } finally {
    uploading.value = false
  }
}

function toggleFile(id: number) {
  if (selectedIds.value.includes(id)) {
    selectedIds.value = selectedIds.value.filter((selectedId) => selectedId !== id)
    return
  }

  selectedIds.value = [...selectedIds.value, id]
}

function toggleCurrentPage() {
  if (allCurrentPageSelected.value) {
    selectedIds.value = selectedIds.value.filter((id) => !currentPageIds.value.includes(id))
    return
  }

  selectedIds.value = Array.from(new Set([...selectedIds.value, ...currentPageIds.value]))
}

function clearSelection() {
  selectedIds.value = []
}

async function deleteOne(file: MediaAsset) {
  const confirmed = window.confirm(`删除未引用文件：${file.originalFilename}？`)
  if (!confirmed) {
    return
  }

  actionId.value = file.id
  error.value = null
  success.value = null

  try {
    await deleteAdminMedia(file.id)
    applyDeletedIds([file.id])
    success.value = '文件已删除'
    await reloadAfterDelete()
  } catch (err) {
    error.value = toMessage(err, '文件删除失败')
  } finally {
    actionId.value = null
  }
}

async function deleteSelected() {
  if (selectedIds.value.length === 0 || deleting.value) {
    return
  }

  const confirmed = window.confirm(`删除选中的 ${selectedIds.value.length} 个未引用文件？`)
  if (!confirmed) {
    return
  }

  deleting.value = true
  error.value = null
  success.value = null

  const idsToDelete = [...selectedIds.value]
  const deletedIds: number[] = []
  const failures: string[] = []

  for (const id of idsToDelete) {
    try {
      await deleteAdminMedia(id)
      deletedIds.push(id)
    } catch (err) {
      const file = files.value.find((item) => item.id === id)
      const name = file?.originalFilename ?? `#${id}`
      failures.push(`${name}: ${toMessage(err, '删除失败')}`)
    }
  }

  applyDeletedIds(deletedIds)

  if (deletedIds.length > 0) {
    success.value = `已删除 ${deletedIds.length} 个文件`
  }

  if (failures.length > 0) {
    error.value = `有 ${failures.length} 个文件删除失败：${failures.join('；')}`
  }

  await reloadAfterDelete(failures.length > 0)
  deleting.value = false
}

function applyDeletedIds(ids: number[]) {
  if (ids.length === 0) {
    return
  }

  files.value = files.value.filter((file) => !ids.includes(file.id))
  selectedIds.value = selectedIds.value.filter((id) => !ids.includes(id))
  total.value = Math.max(0, total.value - ids.length)
}

async function reloadAfterDelete(keepFailuresOnPage = false) {
  if (files.value.length > 0 && keepFailuresOnPage) {
    return
  }

  const nextPage = files.value.length === 0 && page.value > 1 ? page.value - 1 : page.value
  await loadFiles(nextPage)
}

async function goToPage(targetPage: number) {
  if (targetPage < 1 || targetPage > totalPages.value || targetPage === page.value || loading.value) {
    return
  }

  await loadFiles(targetPage)
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

function formatDate(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value || '-'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function typeLabel(type: MediaAsset['assetType']) {
  return type === 'IMAGE' ? '图片' : '附件'
}

function accessLabel(level: MediaAccessLevel) {
  return level === 'PUBLIC' ? '公开' : '登录可访问'
}

onMounted(() => {
  void loadFiles()
})
</script>

<template>
  <section class="fade-in">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ADMIN</p>
        <h1 class="mt-2 text-3xl font-bold text-slate-900">文件管理</h1>
      </div>
      <button
        class="focus-ring inline-flex rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
        :disabled="loading"
        type="button"
        @click="loadFiles(page)"
      >
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>

    <div class="mt-6 grid gap-6 xl:grid-cols-[360px_minmax(0,1fr)]">
      <section class="h-fit rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">上传文件</h2>
        <form class="mt-4 space-y-4" @submit.prevent="uploadFile">
          <div>
            <label class="block text-sm font-semibold text-slate-700" for="admin-file-upload">文件</label>
            <input
              id="admin-file-upload"
              ref="fileInputRef"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-700 outline-none transition file:mr-3 file:rounded-md file:border-0 file:bg-slate-900 file:px-3 file:py-1.5 file:text-sm file:font-semibold file:text-white focus:border-orange-500"
              required
              type="file"
              @change="onFileChange"
            />
          </div>

          <div>
            <label class="block text-sm font-semibold text-slate-700" for="admin-file-access">访问权限</label>
            <select
              id="admin-file-access"
              v-model="accessLevel"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm outline-none transition focus:border-orange-500"
            >
              <option value="PUBLIC">公开</option>
              <option value="AUTHENTICATED">登录可访问</option>
            </select>
          </div>

          <button
            class="focus-ring w-full rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
            :disabled="uploading || !selectedFile"
            type="submit"
          >
            {{ uploading ? '上传中...' : '上传' }}
          </button>
        </form>
      </section>

      <section class="min-w-0 rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-200 p-5">
          <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <h2 class="text-lg font-semibold text-slate-900">文件列表</h2>
              <p class="mt-1 text-sm text-slate-500">共 {{ total }} 个文件</p>
            </div>
            <div class="flex flex-wrap gap-2">
              <button
                class="focus-ring rounded-md border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
                :disabled="selectedCount === 0 || deleting"
                type="button"
                @click="clearSelection"
              >
                清空选择
              </button>
              <button
                class="focus-ring rounded-md border border-rose-200 bg-white px-3 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:text-rose-300"
                :disabled="selectedCount === 0 || deleting"
                type="button"
                @click="deleteSelected"
              >
                {{ deleting ? '删除中...' : `删除选中 (${selectedCount})` }}
              </button>
            </div>
          </div>

          <p v-if="error" class="mt-3 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
            {{ error }}
          </p>
          <p v-if="success" class="mt-3 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
            {{ success }}
          </p>
        </div>

        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-slate-200 text-left text-sm">
            <thead class="bg-slate-50 text-xs uppercase text-slate-500">
              <tr>
                <th class="w-12 px-4 py-3">
                  <input
                    aria-label="选择当前页全部文件"
                    class="h-4 w-4"
                    :checked="allCurrentPageSelected"
                    :disabled="files.length === 0 || deleting"
                    type="checkbox"
                    @change="toggleCurrentPage"
                  />
                </th>
                <th class="px-4 py-3 font-semibold">文件名</th>
                <th class="px-4 py-3 font-semibold">类型</th>
                <th class="px-4 py-3 font-semibold">大小</th>
                <th class="px-4 py-3 font-semibold">访问权限</th>
                <th class="px-4 py-3 font-semibold">上传人</th>
                <th class="px-4 py-3 font-semibold">创建时间</th>
                <th class="px-4 py-3 font-semibold">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-if="loading">
                <td class="px-4 py-6 text-slate-500" colspan="8">正在加载...</td>
              </tr>
              <tr v-else-if="empty">
                <td class="px-4 py-6 text-slate-500" colspan="8">暂无文件。</td>
              </tr>
              <tr v-for="file in files" v-else :key="file.id">
                <td class="px-4 py-3">
                  <input
                    class="h-4 w-4"
                    :checked="selectedIds.includes(file.id)"
                    :disabled="deleting"
                    type="checkbox"
                    :aria-label="`选择 ${file.originalFilename}`"
                    @change="toggleFile(file.id)"
                  />
                </td>
                <td class="max-w-[280px] px-4 py-3">
                  <p class="truncate font-semibold text-slate-900" :title="file.originalFilename">{{ file.originalFilename }}</p>
                  <p class="mt-1 truncate text-xs text-slate-500" :title="file.mimeType">{{ file.mimeType }}</p>
                </td>
                <td class="px-4 py-3 text-slate-600">{{ typeLabel(file.assetType) }}</td>
                <td class="whitespace-nowrap px-4 py-3 text-slate-600">{{ formatSize(file.sizeBytes) }}</td>
                <td class="whitespace-nowrap px-4 py-3">
                  <span
                    class="rounded-full px-2 py-1 text-xs font-semibold"
                    :class="
                      file.accessLevel === 'PUBLIC'
                        ? 'bg-emerald-100 text-emerald-700'
                        : 'bg-amber-100 text-amber-700'
                    "
                  >
                    {{ accessLabel(file.accessLevel) }}
                  </span>
                </td>
                <td class="whitespace-nowrap px-4 py-3 text-slate-600">{{ file.uploadedBy }}</td>
                <td class="whitespace-nowrap px-4 py-3 text-slate-600">{{ formatDate(file.createdAt) }}</td>
                <td class="whitespace-nowrap px-4 py-3">
                  <button
                    class="focus-ring rounded-md border border-rose-200 px-3 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:text-rose-300"
                    :disabled="deleting || actionId === file.id"
                    type="button"
                    @click="deleteOne(file)"
                  >
                    {{ actionId === file.id ? '删除中...' : '删除' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex flex-col gap-3 border-t border-slate-200 p-4 sm:flex-row sm:items-center sm:justify-between">
          <p class="text-sm text-slate-500">第 {{ page }} / {{ totalPages }} 页</p>
          <div class="flex gap-2">
            <button
              class="focus-ring rounded-md border border-slate-300 bg-white px-3 py-1.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
              :disabled="page <= 1 || loading"
              type="button"
              @click="goToPage(page - 1)"
            >
              上一页
            </button>
            <button
              class="focus-ring rounded-md border border-slate-300 bg-white px-3 py-1.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
              :disabled="page >= totalPages || loading"
              type="button"
              @click="goToPage(page + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>
