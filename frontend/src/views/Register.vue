<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ApiError, fetchAuthConfig } from '../api'
import TurnstileWidget from '../components/TurnstileWidget.vue'
import { useAuth } from '../composables/useAuth'

const auth = useAuth()
const router = useRouter()

const form = reactive({
  username: '',
  password: '',
})
const error = ref<string | null>(null)
const registrationEnabled = ref(false)
const turnstileEnabled = ref(false)
const turnstileSiteKey = ref('')
const turnstileToken = ref('')
const turnstileWidget = ref<{ reset: () => void } | null>(null)

async function submit() {
  if (!registrationEnabled.value) {
    error.value = '注册已关闭'
    return
  }

  error.value = null

  if (turnstileEnabled.value && !turnstileToken.value) {
    error.value = '请先完成人机验证'
    return
  }

  try {
    await auth.register({
      username: form.username,
      password: form.password,
      turnstileToken: turnstileToken.value,
    })
    await router.replace('/')
  } catch (err) {
    error.value = err instanceof ApiError ? err.message : '注册失败'
    turnstileWidget.value?.reset()
  }
}

async function loadAuthConfig() {
  try {
    const config = await fetchAuthConfig()
    registrationEnabled.value = config.registrationEnabled
    turnstileEnabled.value = config.turnstileEnabled
    turnstileSiteKey.value = config.turnstileSiteKey

    if (!config.registrationEnabled) {
      error.value = '注册已关闭'
    }
  } catch {
    registrationEnabled.value = false
    turnstileEnabled.value = false
    turnstileSiteKey.value = ''
    error.value = '注册已关闭'
  }
}

onMounted(loadAuthConfig)
</script>

<template>
  <section class="mx-auto max-w-md fade-in">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">ACCOUNT</p>
    <h1 class="mt-2 text-3xl font-bold text-slate-900">注册</h1>

    <form class="mt-6 rounded-lg border border-slate-200 bg-white p-6 shadow-sm" @submit.prevent="submit">
      <label class="block text-sm font-semibold text-slate-700" for="register-username">用户名</label>
      <input
        id="register-username"
        v-model.trim="form.username"
        class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500 disabled:bg-slate-100"
        autocomplete="username"
        maxlength="32"
        minlength="3"
        required
        type="text"
      />

      <label class="mt-4 block text-sm font-semibold text-slate-700" for="register-password">密码</label>
      <input
        id="register-password"
        v-model="form.password"
        class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500 disabled:bg-slate-100"
        autocomplete="new-password"
        maxlength="72"
        minlength="6"
        required
        type="password"
      />

      <p v-if="error" class="mt-4 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
        {{ error }}
      </p>

      <TurnstileWidget
        v-if="turnstileEnabled && turnstileSiteKey"
        ref="turnstileWidget"
        class="mt-4"
        :site-key="turnstileSiteKey"
        @expired="turnstileToken = ''"
        @verified="turnstileToken = $event"
      />

      <button
        class="focus-ring mt-5 w-full rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
        :disabled="auth.loading.value || !registrationEnabled"
        type="submit"
      >
        {{ auth.loading.value ? '注册中...' : '注册' }}
      </button>

      <RouterLink class="mt-4 block text-center text-sm font-semibold text-orange-600 hover:text-orange-500" to="/login">
        已有账号，去登录
      </RouterLink>
    </form>
  </section>
</template>
