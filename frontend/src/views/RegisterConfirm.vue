<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ApiError } from '../api'
import { useAuth } from '../composables/useAuth'

const route = useRoute()
const router = useRouter()
const auth = useAuth()

const loading = ref(true)
const error = ref<string | null>(null)
const success = ref(false)

async function confirm() {
  const token = typeof route.query.token === 'string' ? route.query.token : ''
  if (!token) {
    error.value = '验证链接无效'
    loading.value = false
    return
  }

  try {
    await auth.confirmRegistration(token)
    success.value = true
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : '邮箱验证失败'
  } finally {
    loading.value = false
  }
}

async function goAccount() {
  await router.replace('/account')
}

onMounted(confirm)
</script>

<template>
  <section class="mx-auto max-w-md fade-in">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">ACCOUNT</p>
    <h1 class="mt-2 text-3xl font-bold text-slate-900">邮箱验证</h1>

    <div class="mt-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <p v-if="loading" class="text-sm text-slate-500">正在验证...</p>
      <p v-else-if="error" class="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        {{ error }}
      </p>
      <div v-else-if="success">
        <p class="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
          注册已完成，欢迎回来。
        </p>
        <button
          class="focus-ring mt-5 w-full rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800"
          type="button"
          @click="goAccount"
        >
          进入账户设置
        </button>
      </div>
    </div>
  </section>
</template>
