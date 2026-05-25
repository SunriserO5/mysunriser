<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ApiError, createAdminProject, deleteAdminProject, fetchAdminProjects, updateAdminProject } from '../api'
import type { AdminProjectPayload, ProjectItem } from '../types'

const projects = ref<ProjectItem[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)
const editingSlug = ref<string | null>(null)

const form = reactive<AdminProjectPayload>({
  slug: '',
  title: '',
  summary: '',
  status: 'Draft',
  repoOwner: '',
  repoName: '',
  repoUrl: '',
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
  form.repoOwner = ''
  form.repoName = ''
  form.repoUrl = ''
  form.sortOrder = 0
}

function payloadFromForm(): AdminProjectPayload {
  return {
    slug: form.slug.trim(),
    title: form.title.trim(),
    summary: form.summary.trim(),
    status: form.status,
    repoOwner: form.repoOwner.trim(),
    repoName: form.repoName.trim(),
    repoUrl: form.repoUrl.trim(),
    sortOrder: Number(form.sortOrder) || 0,
  }
}

async function loadProjects() {
  loading.value = true
  error.value = null

  try {
    const response = await fetchAdminProjects(1, 100)
    projects.value = response.items
  } catch (err) {
    error.value = toMessage(err, '项目列表加载失败')
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
      await updateAdminProject(editingSlug.value, payload)
      success.value = '项目已保存'
    } else {
      await createAdminProject(payload)
      success.value = '项目已创建'
    }

    resetForm()
    await loadProjects()
  } catch (err) {
    error.value = toMessage(err, '项目保存失败')
  } finally {
    saving.value = false
  }
}

function editProject(project: ProjectItem) {
  editingSlug.value = project.slug
  form.slug = project.slug
  form.title = project.title
  form.summary = project.summary
  form.status = project.status
  form.repoOwner = project.repoOwner
  form.repoName = project.repoName
  form.repoUrl = project.repoUrl
  form.sortOrder = project.sortOrder
  success.value = null
  error.value = null
}

async function removeProject(project: ProjectItem) {
  if (!window.confirm(`删除项目「${project.title}」？`)) {
    return
  }

  error.value = null
  success.value = null

  try {
    await deleteAdminProject(project.slug)
    if (editingSlug.value === project.slug) {
      resetForm()
    }
    success.value = '项目已删除'
    await loadProjects()
  } catch (err) {
    error.value = toMessage(err, '项目删除失败')
  }
}

onMounted(loadProjects)
</script>

<template>
  <section class="fade-in">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ADMIN</p>
        <h1 class="mt-2 text-3xl font-bold text-slate-900">项目管理</h1>
      </div>
      <button
        class="focus-ring inline-flex rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
        :disabled="loading"
        type="button"
        @click="loadProjects"
      >
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>

    <div class="mt-6 grid gap-6 xl:grid-cols-[minmax(0,1fr)_400px]">
      <section class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">项目列表</h2>

        <p v-if="loading" class="mt-4 text-sm text-slate-500">正在加载...</p>
        <p v-else-if="!projects.length" class="mt-4 text-sm text-slate-500">暂无项目。</p>

        <div v-else class="mt-4 overflow-x-auto">
          <table class="min-w-full divide-y divide-slate-200 text-left text-sm">
            <thead class="text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th class="px-3 py-2">名称</th>
                <th class="px-3 py-2">状态</th>
                <th class="px-3 py-2">仓库</th>
                <th class="px-3 py-2">排序</th>
                <th class="px-3 py-2 text-right">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="project in projects" :key="project.slug">
                <td class="px-3 py-3">
                  <p class="font-semibold text-slate-900">{{ project.title }}</p>
                  <p class="mt-1 text-xs text-slate-500">{{ project.slug }}</p>
                </td>
                <td class="px-3 py-3 text-slate-600">{{ project.status }}</td>
                <td class="px-3 py-3 text-slate-600">
                  <a :href="project.repoUrl" class="font-semibold text-orange-600 hover:text-orange-500" rel="noreferrer" target="_blank">
                    {{ project.repoOwner }}/{{ project.repoName }}
                  </a>
                </td>
                <td class="px-3 py-3 text-slate-600">{{ project.sortOrder }}</td>
                <td class="px-3 py-3 text-right">
                  <button class="text-sm font-semibold text-orange-600 hover:text-orange-500" type="button" @click="editProject(project)">
                    编辑
                  </button>
                  <button class="ml-3 text-sm font-semibold text-rose-600 hover:text-rose-500" type="button" @click="removeProject(project)">
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
          <h2 class="text-lg font-semibold text-slate-900">{{ isEditing ? '编辑项目' : '新增项目' }}</h2>
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
              placeholder="mysunriser"
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
              <span class="text-sm font-semibold text-slate-700">排序</span>
              <input
                v-model.number="form.sortOrder"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                type="number"
              />
            </label>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="block">
              <span class="text-sm font-semibold text-slate-700">仓库 Owner</span>
              <input
                v-model.trim="form.repoOwner"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                placeholder="owner"
                required
              />
            </label>

            <label class="block">
              <span class="text-sm font-semibold text-slate-700">仓库 Name</span>
              <input
                v-model.trim="form.repoName"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                placeholder="repo"
                required
              />
            </label>
          </div>

          <label class="block">
            <span class="text-sm font-semibold text-slate-700">仓库链接</span>
            <input
              v-model.trim="form.repoUrl"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
              placeholder="留空则自动生成 https://github.com/owner/repo"
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
          {{ saving ? '保存中...' : isEditing ? '保存项目' : '创建项目' }}
        </button>
      </form>
    </div>
  </section>
</template>
