<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ApiError, fetchAdminFooterSettings, updateAdminFooterSettings } from '../api'

const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const form = reactive({
  githubEnabled: false,
  githubUrl: '',
  xEnabled: false,
  xUrl: '',
})

function toMessage(err: unknown, fallback: string): string {
  return err instanceof ApiError ? err.message : fallback
}

async function loadFooterSettings() {
  loading.value = true
  error.value = null

  try {
    const settings = await fetchAdminFooterSettings()
    form.githubEnabled = settings.githubEnabled
    form.githubUrl = settings.githubUrl
    form.xEnabled = settings.xEnabled
    form.xUrl = settings.xUrl
  } catch (err) {
    error.value = toMessage(err, '页脚链接加载失败')
  } finally {
    loading.value = false
  }
}

async function submit() {
  saving.value = true
  error.value = null
  success.value = null

  try {
    const settings = await updateAdminFooterSettings({
      githubEnabled: form.githubEnabled,
      githubUrl: form.githubUrl,
      xEnabled: form.xEnabled,
      xUrl: form.xUrl,
    })
    form.githubEnabled = settings.githubEnabled
    form.githubUrl = settings.githubUrl
    form.xEnabled = settings.xEnabled
    form.xUrl = settings.xUrl
    success.value = '页脚链接已保存'
  } catch (err) {
    error.value = toMessage(err, '页脚链接保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadFooterSettings)
</script>

<template>
  <section class="fade-in">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ADMIN</p>
        <h1 class="mt-2 text-3xl font-bold text-slate-900">页脚链接</h1>
      </div>
      <button
        class="focus-ring inline-flex rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
        :disabled="loading"
        type="button"
        @click="loadFooterSettings"
      >
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>

    <form class="mt-6 grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]" @submit.prevent="submit">
      <section class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">外部链接</h2>

        <div class="mt-5 space-y-5">
          <section class="rounded-md border border-slate-200 p-4">
            <div class="flex items-start justify-between gap-4">
              <div>
                <h3 class="text-base font-semibold text-slate-800">GitHub</h3>
                <p class="mt-1 text-sm text-slate-500">
                  {{ form.githubEnabled ? '页脚展示 GitHub 链接' : '页脚不展示 GitHub 链接' }}
                </p>
              </div>
              <input v-model="form.githubEnabled" class="mt-1 h-5 w-5" type="checkbox" />
            </div>

            <label class="mt-4 block">
              <span class="text-sm font-semibold text-slate-700">链接地址</span>
              <input
                v-model.trim="form.githubUrl"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                placeholder="https://github.com/username"
                :required="form.githubEnabled"
                type="url"
              />
            </label>
          </section>

          <section class="rounded-md border border-slate-200 p-4">
            <div class="flex items-start justify-between gap-4">
              <div>
                <h3 class="text-base font-semibold text-slate-800">X</h3>
                <p class="mt-1 text-sm text-slate-500">
                  {{ form.xEnabled ? '页脚展示 X 链接' : '页脚不展示 X 链接' }}
                </p>
              </div>
              <input v-model="form.xEnabled" class="mt-1 h-5 w-5" type="checkbox" />
            </div>

            <label class="mt-4 block">
              <span class="text-sm font-semibold text-slate-700">链接地址</span>
              <input
                v-model.trim="form.xUrl"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                placeholder="https://x.com/username"
                :required="form.xEnabled"
                type="url"
              />
            </label>
          </section>
        </div>

        <p v-if="error" class="mt-5 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
          {{ error }}
        </p>
        <p v-if="success" class="mt-5 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
          {{ success }}
        </p>

        <button
          class="focus-ring mt-5 rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
          :disabled="saving || loading"
          type="submit"
        >
          {{ saving ? '保存中...' : '保存设置' }}
        </button>
      </section>

      <aside class="space-y-4">
        <section class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <h2 class="text-lg font-semibold text-slate-900">展示状态</h2>
          <dl class="mt-4 space-y-3 text-sm">
            <div>
              <dt class="font-semibold text-slate-700">GitHub</dt>
              <dd class="mt-1 break-all text-slate-500">
                {{ form.githubEnabled && form.githubUrl ? form.githubUrl : '未展示' }}
              </dd>
            </div>
            <div>
              <dt class="font-semibold text-slate-700">X</dt>
              <dd class="mt-1 break-all text-slate-500">
                {{ form.xEnabled && form.xUrl ? form.xUrl : '未展示' }}
              </dd>
            </div>
          </dl>
        </section>
      </aside>
    </form>
  </section>
</template>
