<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ApiError, resetPassword } from '../api'

const route = useRoute()
const router = useRouter()

const form = reactive({
  newPassword: '',
})
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref(false)

async function submit() {
  const token = typeof route.query.token === 'string' ? route.query.token : ''
  if (!token) {
    error.value = '重置链接无效'
    return
  }

  loading.value = true
  error.value = null
  success.value = false

  try {
    await resetPassword({ token, newPassword: form.newPassword })
    success.value = true
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : '重置失败'
  } finally {
    loading.value = false
  }
}

async function goLogin() {
  await router.replace('/login')
}
</script>

<template>
  <section class="mx-auto max-w-md fade-in">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ACCOUNT</p>
    <h1 class="mt-2 text-3xl font-bold text-slate-900">重置密码</h1>

    <form class="mt-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm" @submit.prevent="submit">
      <label class="block text-sm font-semibold text-slate-700" for="reset-password">新密码</label>
      <input
        id="reset-password"
        v-model="form.newPassword"
        class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
        autocomplete="new-password"
        maxlength="72"
        minlength="10"
        required
        type="password"
      />

      <p v-if="success" class="mt-4 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
        密码已重置，请使用新密码登录。
      </p>
      <p v-if="error" class="mt-4 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        {{ error }}
      </p>

      <button
        class="focus-ring mt-5 w-full rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
        :disabled="loading || success"
        type="submit"
      >
        {{ loading ? '重置中...' : '重置密码' }}
      </button>
      <button
        v-if="success"
        class="focus-ring mt-3 w-full rounded-md border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-800 transition hover:bg-slate-50"
        type="button"
        @click="goLogin"
      >
        去登录
      </button>
    </form>
  </section>
</template>
