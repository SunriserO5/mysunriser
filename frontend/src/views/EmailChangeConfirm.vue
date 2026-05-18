<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ApiError, confirmEmailChange, fetchMe } from '../api'
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
    await confirmEmailChange(token)
    if (auth.isAuthenticated.value) {
      auth.user.value = await fetchMe()
    }
    success.value = true
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : '邮箱确认失败'
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
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ACCOUNT</p>
    <h1 class="mt-2 text-3xl font-bold text-slate-900">邮箱变更确认</h1>

    <div class="mt-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <p v-if="loading" class="text-sm text-slate-500">正在确认...</p>
      <p v-else-if="error" class="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        {{ error }}
      </p>
      <div v-else-if="success">
        <p class="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
          邮箱已更新。
        </p>
        <button
          class="focus-ring mt-5 w-full rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800"
          type="button"
          @click="goAccount"
        >
          返回账户设置
        </button>
      </div>
    </div>
  </section>
</template>
