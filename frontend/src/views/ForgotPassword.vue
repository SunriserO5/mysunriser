<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ApiError, requestPasswordReset } from '../api'

const form = reactive({
  email: '',
})
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref(false)

async function submit() {
  loading.value = true
  error.value = null
  success.value = false

  try {
    await requestPasswordReset({ email: form.email })
    success.value = true
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : '提交失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="mx-auto max-w-md fade-in">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ACCOUNT</p>
    <h1 class="mt-2 font-sans text-3xl font-bold text-slate-900">找回密码</h1>

    <form class="mt-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm" @submit.prevent="submit">
      <label class="block text-sm font-semibold text-slate-700" for="forgot-email">邮箱</label>
      <input
        id="forgot-email"
        v-model.trim="form.email"
        class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
        autocomplete="email"
        maxlength="254"
        required
        type="email"
      />

      <p v-if="success" class="mt-4 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
        如果该邮箱已绑定账号，重置链接会发送到邮箱中。
      </p>
      <p v-if="error" class="mt-4 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        {{ error }}
      </p>

      <button
        class="focus-ring mt-5 w-full rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
        :disabled="loading"
        type="submit"
      >
        {{ loading ? '发送中...' : '发送重置邮件' }}
      </button>
    </form>
  </section>
</template>
