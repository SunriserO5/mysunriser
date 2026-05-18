<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ApiError, createAdminTool, deleteAdminTool, fetchAdminTools, updateAdminTool } from '../api'
import type { AdminToolPayload, ToolItem } from '../types'

const tools = ref<ToolItem[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)
const editingSlug = ref<string | null>(null)

const form = reactive<AdminToolPayload>({
  slug: '',
  title: '',
  summary: '',
  status: 'Draft',
  entryType: 'INTERNAL',
  routePath: '',
  externalUrl: '',
  accessLevel: 'PUBLIC',
  sortOrder: 0,
})

const isEditing = computed(() => editingSlug.value !== null)

function toMessage(err: unknown, fallback: string): string {
  return err instanceof ApiError ? err.message : fallback
}

function resetForm() {
  editingSlug.value = null
  form.slug = ''
  form.title = ''
  form.summary = ''
  form.status = 'Draft'
  form.entryType = 'INTERNAL'
  form.routePath = ''
  form.externalUrl = ''
  form.accessLevel = 'PUBLIC'
  form.sortOrder = 0
}

function payloadFromForm(): AdminToolPayload {
  const slug = form.slug.trim()

  return {
    slug,
    title: form.title.trim(),
    summary: form.summary.trim(),
    status: form.status,
    entryType: form.entryType,
    routePath: form.entryType === 'INTERNAL' ? form.routePath.trim() || `/tools/${slug}` : '',
    externalUrl: form.entryType === 'EXTERNAL' ? form.externalUrl.trim() : '',
    accessLevel: form.accessLevel,
    sortOrder: Number(form.sortOrder) || 0,
  }
}

async function loadTools() {
  loading.value = true
  error.value = null

  try {
    const response = await fetchAdminTools(1, 100)
    tools.value = response.items
  } catch (err) {
    error.value = toMessage(err, '工具列表加载失败')
  } finally {
    loading.value = false
  }
}

async function submit() {
  saving.value = true
  error.value = null
  success.value = null

  try {
    const payload = payloadFromForm()
    if (editingSlug.value) {
      await updateAdminTool(editingSlug.value, payload)
      success.value = '工具已保存'
    } else {
      await createAdminTool(payload)
      success.value = '工具已创建'
    }

    resetForm()
    await loadTools()
  } catch (err) {
    error.value = toMessage(err, '工具保存失败')
  } finally {
    saving.value = false
  }
}

function editTool(tool: ToolItem) {
  editingSlug.value = tool.slug
  form.slug = tool.slug
  form.title = tool.title
  form.summary = tool.summary
  form.status = tool.status
  form.entryType = tool.entryType
  form.routePath = tool.routePath
  form.externalUrl = tool.externalUrl
  form.accessLevel = tool.accessLevel
  form.sortOrder = tool.sortOrder
  success.value = null
  error.value = null
}

async function removeTool(tool: ToolItem) {
  if (!window.confirm(`删除工具「${tool.title}」？`)) {
    return
  }

  error.value = null
  success.value = null

  try {
    await deleteAdminTool(tool.slug)
    if (editingSlug.value === tool.slug) {
      resetForm()
    }
    success.value = '工具已删除'
    await loadTools()
  } catch (err) {
    error.value = toMessage(err, '工具删除失败')
  }
}

onMounted(loadTools)
</script>

<template>
  <section class="fade-in">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ADMIN</p>
        <h1 class="mt-2 text-3xl font-bold text-slate-900">工具管理</h1>
      </div>
      <button
        class="focus-ring inline-flex rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
        :disabled="loading"
        type="button"
        @click="loadTools"
      >
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>

    <div class="mt-6 grid gap-6 xl:grid-cols-[minmax(0,1fr)_400px]">
      <section class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">工具列表</h2>

        <p v-if="loading" class="mt-4 text-sm text-slate-500">正在加载...</p>
        <p v-else-if="!tools.length" class="mt-4 text-sm text-slate-500">暂无工具。</p>

        <div v-else class="mt-4 overflow-x-auto">
          <table class="min-w-full divide-y divide-slate-200 text-left text-sm">
            <thead class="text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th class="px-3 py-2">名称</th>
                <th class="px-3 py-2">状态</th>
                <th class="px-3 py-2">入口</th>
                <th class="px-3 py-2">权限</th>
                <th class="px-3 py-2">排序</th>
                <th class="px-3 py-2 text-right">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="tool in tools" :key="tool.slug">
                <td class="px-3 py-3">
                  <p class="font-semibold text-slate-900">{{ tool.title }}</p>
                  <p class="mt-1 text-xs text-slate-500">{{ tool.slug }}</p>
                </td>
                <td class="px-3 py-3 text-slate-600">{{ tool.status }}</td>
                <td class="px-3 py-3 text-slate-600">{{ tool.entryType }}</td>
                <td class="px-3 py-3 text-slate-600">{{ tool.accessLevel }}</td>
                <td class="px-3 py-3 text-slate-600">{{ tool.sortOrder }}</td>
                <td class="px-3 py-3 text-right">
                  <button class="text-sm font-semibold text-orange-600 hover:text-orange-500" type="button" @click="editTool(tool)">
                    编辑
                  </button>
                  <button class="ml-3 text-sm font-semibold text-rose-600 hover:text-rose-500" type="button" @click="removeTool(tool)">
                    删除
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <form class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm" @submit.prevent="submit">
        <div class="flex items-center justify-between gap-3">
          <h2 class="text-lg font-semibold text-slate-900">{{ isEditing ? '编辑工具' : '新增工具' }}</h2>
          <button v-if="isEditing" class="text-sm font-semibold text-slate-500 hover:text-slate-900" type="button" @click="resetForm">
            取消
          </button>
        </div>

        <div class="mt-5 space-y-4">
          <label class="block">
            <span class="text-sm font-semibold text-slate-700">Slug</span>
            <input
              v-model.trim="form.slug"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
              pattern="^[a-z0-9][a-z0-9-]{0,99}$"
              placeholder="json-format"
              required
            />
          </label>

          <label class="block">
            <span class="text-sm font-semibold text-slate-700">标题</span>
            <input
              v-model.trim="form.title"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
              maxlength="120"
              required
            />
          </label>

          <label class="block">
            <span class="text-sm font-semibold text-slate-700">简介</span>
            <textarea
              v-model.trim="form.summary"
              class="focus-ring mt-2 min-h-24 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
              maxlength="500"
              required
            />
          </label>

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="block">
              <span class="text-sm font-semibold text-slate-700">状态</span>
              <select v-model="form.status" class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none">
                <option value="Draft">Draft</option>
                <option value="Published">Published</option>
              </select>
            </label>

            <label class="block">
              <span class="text-sm font-semibold text-slate-700">权限</span>
              <select v-model="form.accessLevel" class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none">
                <option value="PUBLIC">PUBLIC</option>
                <option value="AUTHENTICATED">AUTHENTICATED</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </label>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="block">
              <span class="text-sm font-semibold text-slate-700">入口类型</span>
              <select v-model="form.entryType" class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none">
                <option value="INTERNAL">INTERNAL</option>
                <option value="EXTERNAL">EXTERNAL</option>
              </select>
            </label>

            <label class="block">
              <span class="text-sm font-semibold text-slate-700">排序</span>
              <input
                v-model.number="form.sortOrder"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                type="number"
              />
            </label>
          </div>

          <label v-if="form.entryType === 'INTERNAL'" class="block">
            <span class="text-sm font-semibold text-slate-700">站内路径</span>
            <input
              v-model.trim="form.routePath"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
              placeholder="/tools/json-format"
            />
          </label>

          <label v-else class="block">
            <span class="text-sm font-semibold text-slate-700">外部链接</span>
            <input
              v-model.trim="form.externalUrl"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
              placeholder="https://example.com/tool"
              required
              type="url"
            />
          </label>
        </div>

        <p v-if="error" class="mt-5 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">{{ error }}</p>
        <p v-if="success" class="mt-5 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
          {{ success }}
        </p>

        <button
          class="focus-ring mt-5 rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
          :disabled="saving"
          type="submit"
        >
          {{ saving ? '保存中...' : isEditing ? '保存工具' : '创建工具' }}
        </button>
      </form>
    </div>
  </section>
</template>
