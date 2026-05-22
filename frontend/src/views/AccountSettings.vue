<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ApiError,
  fetchAccountProfile,
  requestAccountEmailChange,
  updateAccountPassword,
  updateAccountProfile,
} from '../api'
import { useAuth } from '../composables/useAuth'
import type { AccountProfile } from '../types'

const router = useRouter()
const auth = useAuth()

const loading = ref(false)
const profileSaving = ref(false)
const passwordSaving = ref(false)
const emailSaving = ref(false)
const error = ref<string | null>(null)
const profileSuccess = ref<string | null>(null)
const passwordSuccess = ref<string | null>(null)
const emailSuccess = ref<string | null>(null)
const profile = ref<AccountProfile | null>(null)

const profileForm = reactive({
  nickname: '',
})
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
})
const emailForm = reactive({
  email: '',
})

function toMessage(err: unknown, fallback: string): string {
  return err instanceof ApiError ? err.message : fallback
}

async function loadProfile() {
  loading.value = true
  error.value = null

  try {
    profile.value = await fetchAccountProfile()
    profileForm.nickname = profile.value.nickname
    emailForm.email = profile.value.email
    auth.user.value = profile.value
  } catch (err) {
    error.value = toMessage(err, '账户信息加载失败')
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  profileSaving.value = true
  error.value = null
  profileSuccess.value = null

  try {
    profile.value = await updateAccountProfile({ nickname: profileForm.nickname })
    auth.user.value = profile.value
    profileSuccess.value = '资料已保存'
  } catch (err) {
    error.value = toMessage(err, '资料保存失败')
  } finally {
    profileSaving.value = false
  }
}

async function saveEmail() {
  emailSaving.value = true
  error.value = null
  emailSuccess.value = null

  try {
    await requestAccountEmailChange({ email: emailForm.email })
    emailSuccess.value = '确认邮件已发送，请打开邮箱中的链接完成变更'
  } catch (err) {
    error.value = toMessage(err, '邮箱变更失败')
  } finally {
    emailSaving.value = false
  }
}

async function savePassword() {
  passwordSaving.value = true
  error.value = null
  passwordSuccess.value = null

  try {
    await updateAccountPassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    })
    passwordSuccess.value = '密码已修改，请重新登录'
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    await auth.logout()
    await router.replace('/login')
  } catch (err) {
    error.value = toMessage(err, '密码修改失败')
  } finally {
    passwordSaving.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <section class="fade-in">
    <div>
      <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ACCOUNT</p>
      <h1 class="mt-2 text-3xl font-bold text-slate-900">账户设置</h1>
    </div>

    <p v-if="error" class="mt-5 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
      {{ error }}
    </p>

    <div class="mt-6 grid gap-6 lg:grid-cols-[260px_minmax(0,1fr)]">
      <aside class="h-fit rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div class="flex items-center gap-4">
          <div class="grid h-16 w-16 place-items-center rounded-full bg-slate-900 text-xl font-bold text-white">
            {{ (profile?.nickname || profile?.username || '?').slice(0, 1).toUpperCase() }}
          </div>
          <div class="min-w-0">
            <p class="truncate text-base font-semibold text-slate-900">
              {{ profile?.nickname || profile?.username || '加载中...' }}
            </p>
            <p class="mt-1 text-sm text-slate-500">{{ profile?.role || '' }}</p>
          </div>
        </div>
        <dl class="mt-5 space-y-3 text-sm">
          <div>
            <dt class="font-semibold text-slate-700">用户名</dt>
            <dd class="mt-1 break-all text-slate-500">{{ profile?.username || '-' }}</dd>
          </div>
          <div>
            <dt class="font-semibold text-slate-700">邮箱</dt>
            <dd class="mt-1 break-all text-slate-500">{{ profile?.email || '未设置' }}</dd>
          </div>
          <div>
            <dt class="font-semibold text-slate-700">邮箱状态</dt>
            <dd class="mt-1 text-slate-500">{{ profile?.emailVerified ? '已验证' : '未验证' }}</dd>
          </div>
        </dl>
      </aside>

      <div class="space-y-6">
        <form class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm" @submit.prevent="saveProfile">
          <h2 class="text-lg font-semibold text-slate-900">基本资料</h2>
          <label class="mt-5 block">
            <span class="text-sm font-semibold text-slate-700">昵称</span>
            <input
              v-model.trim="profileForm.nickname"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
              maxlength="64"
              type="text"
            />
          </label>
          <p v-if="profileSuccess" class="mt-4 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
            {{ profileSuccess }}
          </p>
          <button
            class="focus-ring mt-5 rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
            :disabled="loading || profileSaving"
            type="submit"
          >
            {{ profileSaving ? '保存中...' : '保存资料' }}
          </button>
        </form>

        <form class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm" @submit.prevent="saveEmail">
          <h2 class="text-lg font-semibold text-slate-900">邮箱</h2>
          <label class="mt-5 block">
            <span class="text-sm font-semibold text-slate-700">新邮箱</span>
            <input
              v-model.trim="emailForm.email"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
              autocomplete="email"
              maxlength="254"
              required
              type="email"
            />
          </label>
          <p v-if="emailSuccess" class="mt-4 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
            {{ emailSuccess }}
          </p>
          <button
            class="focus-ring mt-5 rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
            :disabled="loading || emailSaving"
            type="submit"
          >
            {{ emailSaving ? '发送中...' : '发送确认邮件' }}
          </button>
        </form>

        <form class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm" @submit.prevent="savePassword">
          <h2 class="text-lg font-semibold text-slate-900">密码</h2>
          <div class="mt-5 grid gap-4 sm:grid-cols-2">
            <label class="block">
              <span class="text-sm font-semibold text-slate-700">当前密码</span>
              <input
                v-model="passwordForm.currentPassword"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                autocomplete="current-password"
                required
                type="password"
              />
            </label>
            <label class="block">
              <span class="text-sm font-semibold text-slate-700">新密码</span>
              <input
                v-model="passwordForm.newPassword"
                class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
                autocomplete="new-password"
                maxlength="72"
                minlength="10"
                required
                type="password"
              />
            </label>
          </div>
          <p v-if="passwordSuccess" class="mt-4 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
            {{ passwordSuccess }}
          </p>
          <button
            class="focus-ring mt-5 rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
            :disabled="loading || passwordSaving"
            type="submit"
          >
            {{ passwordSaving ? '保存中...' : '修改密码' }}
          </button>
        </form>
      </div>
    </div>
  </section>
</template>
