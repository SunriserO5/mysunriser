<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ApiError, fetchAuthConfig } from '../api'
import { useAuth } from '../composables/useAuth'

const auth = useAuth()
const route = useRoute()
const router = useRouter()

const form = reactive({
  username: '',
  password: '',
})
const error = ref<string | null>(null)
const registrationEnabled = ref(false)

async function submit() {
  error.value = null

  try {
    await auth.login({
      username: form.username,
      password: form.password,
    })

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : '登录失败'
  }
}

async function loadAuthConfig() {
  try {
    const config = await fetchAuthConfig()
    registrationEnabled.value = config.registrationEnabled
  } catch {
    registrationEnabled.value = false
  }
}

onMounted(loadAuthConfig)
</script>

<template>
  <section class="mx-auto max-w-md fade-in">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ACCOUNT</p>
    <h1 class="mt-2 text-3xl font-bold text-slate-900">登录</h1>

    <form class="mt-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm" @submit.prevent="submit">
      <label class="block text-sm font-semibold text-slate-700" for="login-username">用户名</label>
      <input
        id="login-username"
        v-model.trim="form.username"
        class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500 disabled:bg-slate-100"
        autocomplete="username"
        required
        type="text"
      />

      <label class="mt-4 block text-sm font-semibold text-slate-700" for="login-password">密码</label>
      <input
        id="login-password"
        v-model="form.password"
        class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500 disabled:bg-slate-100"
        autocomplete="current-password"
        required
        type="password"
      />

      <p v-if="error" class="mt-4 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        {{ error }}
      </p>

      <button
        class="focus-ring mt-5 w-full rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
        :disabled="auth.loading.value"
        type="submit"
      >
        {{ auth.loading.value ? '登录中...' : '登录' }}
      </button>

      <RouterLink
        v-if="registrationEnabled"
        class="mt-4 block text-center text-sm font-semibold text-orange-600 hover:text-orange-500"
        to="/register"
      >
        创建账号
      </RouterLink>
    </form>
  </section>
</template>
