<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ApiError, fetchAdminSecurityConfig, updateAdminSecurityConfig } from '../api'

const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)
const turnstileConfigured = ref(false)
const turnstileSiteKey = ref('')
const jwtExpireMinutes = ref(0)

const form = reactive({
  registrationEnabled: false,
  turnstileEnabled: false,
  loginMaxAttempts: 5,
  loginWindowSeconds: 900,
})

function toMessage(err: unknown, fallback: string): string {
  return err instanceof ApiError ? err.message : fallback
}

async function loadSecurityConfig() {
  loading.value = true
  error.value = null

  try {
    const config = await fetchAdminSecurityConfig()
    form.registrationEnabled = config.registrationEnabled
    form.turnstileEnabled = config.turnstileEnabled
    form.loginMaxAttempts = config.loginMaxAttempts
    form.loginWindowSeconds = config.loginWindowSeconds
    turnstileConfigured.value = config.turnstileConfigured
    turnstileSiteKey.value = config.turnstileSiteKey
    jwtExpireMinutes.value = config.jwtExpireMinutes
  } catch (err) {
    error.value = toMessage(err, '安全设置加载失败')
  } finally {
    loading.value = false
  }
}

async function submit() {
  saving.value = true
  error.value = null
  success.value = null

  try {
    const config = await updateAdminSecurityConfig({
      registrationEnabled: form.registrationEnabled,
      turnstileEnabled: form.turnstileEnabled,
      loginMaxAttempts: Number(form.loginMaxAttempts),
      loginWindowSeconds: Number(form.loginWindowSeconds),
    })
    form.registrationEnabled = config.registrationEnabled
    form.turnstileEnabled = config.turnstileEnabled
    form.loginMaxAttempts = config.loginMaxAttempts
    form.loginWindowSeconds = config.loginWindowSeconds
    turnstileConfigured.value = config.turnstileConfigured
    turnstileSiteKey.value = config.turnstileSiteKey
    jwtExpireMinutes.value = config.jwtExpireMinutes
    success.value = '安全设置已保存'
  } catch (err) {
    error.value = toMessage(err, '安全设置保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadSecurityConfig)
</script>

<template>
  <section class="fade-in">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ADMIN</p>
        <h1 class="mt-2 text-3xl font-bold text-slate-900">安全设置</h1>
      </div>
      <button
        class="focus-ring inline-flex rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
        :disabled="loading"
        type="button"
        @click="loadSecurityConfig"
      >
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>

    <form class="mt-6 grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px]" @submit.prevent="submit">
      <section class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">访问防护</h2>

        <div class="mt-5 space-y-5">
          <label class="flex items-start justify-between gap-4 rounded-md border border-slate-200 p-4">
            <span>
              <span class="block text-sm font-semibold text-slate-800">公开注册</span>
              <span class="mt-1 block text-sm text-slate-500">
                {{ form.registrationEnabled ? '允许访客注册账号' : '关闭公开注册入口' }}
              </span>
            </span>
            <input v-model="form.registrationEnabled" class="mt-1 h-5 w-5" type="checkbox" />
          </label>

          <label class="flex items-start justify-between gap-4 rounded-md border border-slate-200 p-4">
            <span>
              <span class="block text-sm font-semibold text-slate-800">Turnstile</span>
              <span class="mt-1 block text-sm text-slate-500">
                {{ turnstileConfigured ? '登录和注册需要通过 Cloudflare 验证' : '需要先在服务器 .env 配置 key' }}
              </span>
            </span>
            <input
              v-model="form.turnstileEnabled"
              class="mt-1 h-5 w-5"
              :disabled="!turnstileConfigured"
              type="checkbox"
            />
          </label>

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="block">
              <span class="text-sm font-semibold text-slate-700">登录失败阈值</span>
              <input
                v-model.number="form.loginMaxAttempts"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                max="20"
                min="3"
                required
                type="number"
              />
            </label>

            <label class="block">
              <span class="text-sm font-semibold text-slate-700">锁定窗口（秒）</span>
              <input
                v-model.number="form.loginWindowSeconds"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                max="3600"
                min="60"
                required
                type="number"
              />
            </label>
          </div>
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
          <h2 class="text-lg font-semibold text-slate-900">当前配置</h2>
          <dl class="mt-4 space-y-3 text-sm">
            <div>
              <dt class="font-semibold text-slate-700">Turnstile Site Key</dt>
              <dd class="mt-1 break-all text-slate-500">{{ turnstileSiteKey || '未配置' }}</dd>
            </div>
            <div>
              <dt class="font-semibold text-slate-700">JWT 有效期</dt>
              <dd class="mt-1 text-slate-500">{{ jwtExpireMinutes }} 分钟</dd>
            </div>
          </dl>
        </section>
      </aside>
    </form>
  </section>
</template>
