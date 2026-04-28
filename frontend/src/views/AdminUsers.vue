<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  ApiError,
  createAdminUser,
  fetchAdminUsers,
  resetAdminUserPassword,
  setAdminUserEnabled,
} from '../api'
import type { AdminUser, AuthRole } from '../types'

const users = ref<AdminUser[]>([])
const loading = ref(false)
const saving = ref(false)
const actionId = ref<number | null>(null)
const error = ref<string | null>(null)
const success = ref<string | null>(null)
const resetTarget = ref<AdminUser | null>(null)

const createForm = reactive({
  username: '',
  password: '',
  role: 'user' as AuthRole,
})
const resetForm = reactive({
  newPassword: '',
})

const empty = computed(() => !loading.value && users.value.length === 0)

function toMessage(err: unknown, fallback: string): string {
  return err instanceof ApiError ? err.message : fallback
}

async function loadUsers() {
  loading.value = true
  error.value = null

  try {
    users.value = await fetchAdminUsers()
  } catch (err) {
    error.value = toMessage(err, '账号列表加载失败')
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  saving.value = true
  error.value = null
  success.value = null

  try {
    const created = await createAdminUser({
      username: createForm.username,
      password: createForm.password,
      role: createForm.role,
    })
    users.value = [created, ...users.value]
    createForm.username = ''
    createForm.password = ''
    createForm.role = 'user'
    success.value = '账号已创建'
  } catch (err) {
    error.value = toMessage(err, '账号创建失败')
  } finally {
    saving.value = false
  }
}

function openReset(user: AdminUser) {
  resetTarget.value = user
  resetForm.newPassword = ''
  error.value = null
  success.value = null
}

async function submitReset() {
  if (!resetTarget.value) {
    return
  }

  actionId.value = resetTarget.value.id
  error.value = null
  success.value = null

  try {
    await resetAdminUserPassword(resetTarget.value.id, resetForm.newPassword)
    success.value = '密码已重置'
    resetTarget.value = null
    resetForm.newPassword = ''
  } catch (err) {
    error.value = toMessage(err, '密码重置失败')
  } finally {
    actionId.value = null
  }
}

async function toggleStatus(user: AdminUser) {
  actionId.value = user.id
  error.value = null
  success.value = null

  try {
    const updated = await setAdminUserEnabled(user.id, user.status !== 'active')
    users.value = users.value.map((item) => (item.id === updated.id ? updated : item))
    success.value = updated.status === 'active' ? '账号已启用' : '账号已禁用'
  } catch (err) {
    error.value = toMessage(err, '账号状态更新失败')
  } finally {
    actionId.value = null
  }
}

function formatDate(value: string | null): string {
  if (!value) {
    return '-'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

onMounted(() => {
  void loadUsers()
})
</script>

<template>
  <section class="fade-in">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.2em] text-orange-600">ADMIN</p>
        <h1 class="mt-2 text-3xl font-bold text-slate-900">账号管理</h1>
      </div>
      <button
        class="focus-ring inline-flex rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
        :disabled="loading"
        type="button"
        @click="loadUsers"
      >
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>

    <div class="mt-6 grid gap-6 xl:grid-cols-[360px_minmax(0,1fr)]">
      <div class="space-y-6">
        <section class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-900">创建账号</h2>
        <form class="mt-4 space-y-4" @submit.prevent="submitCreate">
          <div>
            <label class="block text-sm font-semibold text-slate-700" for="admin-create-username">用户名</label>
            <input
              id="admin-create-username"
              v-model.trim="createForm.username"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500 disabled:bg-slate-100"
              maxlength="32"
              minlength="3"
              required
              type="text"
            />
          </div>

          <div>
            <label class="block text-sm font-semibold text-slate-700" for="admin-create-password">初始密码</label>
            <input
              id="admin-create-password"
              v-model="createForm.password"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500 disabled:bg-slate-100"
              maxlength="72"
              minlength="6"
              required
              type="password"
            />
          </div>

          <div>
            <label class="block text-sm font-semibold text-slate-700" for="admin-create-role">角色</label>
            <select
              id="admin-create-role"
              v-model="createForm.role"
              class="focus-ring mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm outline-none transition focus:border-orange-500"
            >
              <option value="user">user</option>
              <option value="admin">admin</option>
            </select>
          </div>

          <button
            class="focus-ring w-full rounded-md bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
            :disabled="saving"
            type="submit"
          >
            {{ saving ? '创建中...' : '创建' }}
          </button>
        </form>
        </section>
      </div>

      <section class="min-w-0 rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-200 p-5">
          <h2 class="text-lg font-semibold text-slate-900">账号列表</h2>
          <p v-if="error" class="mt-3 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
            {{ error }}
          </p>
          <p v-if="success" class="mt-3 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
            {{ success }}
          </p>
        </div>

        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-slate-200 text-left text-sm">
            <thead class="bg-slate-50 text-xs uppercase text-slate-500">
              <tr>
                <th class="px-4 py-3 font-semibold">用户名</th>
                <th class="px-4 py-3 font-semibold">角色</th>
                <th class="px-4 py-3 font-semibold">状态</th>
                <th class="px-4 py-3 font-semibold">最后登录</th>
                <th class="px-4 py-3 font-semibold">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-if="loading">
                <td class="px-4 py-6 text-slate-500" colspan="5">正在加载...</td>
              </tr>
              <tr v-else-if="empty">
                <td class="px-4 py-6 text-slate-500" colspan="5">暂无账号。</td>
              </tr>
              <tr v-for="user in users" v-else :key="user.id">
                <td class="px-4 py-3 font-semibold text-slate-900">{{ user.username }}</td>
                <td class="px-4 py-3 text-slate-600">{{ user.role }}</td>
                <td class="px-4 py-3">
                  <span
                    class="rounded-full px-2 py-1 text-xs font-semibold"
                    :class="user.status === 'active' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'"
                  >
                    {{ user.status }}
                  </span>
                </td>
                <td class="px-4 py-3 text-slate-600">{{ formatDate(user.lastLoginAt) }}</td>
                <td class="whitespace-nowrap px-4 py-3">
                  <button
                    class="focus-ring rounded-md border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
                    :disabled="actionId === user.id"
                    type="button"
                    @click="openReset(user)"
                  >
                    重置密码
                  </button>
                  <button
                    class="focus-ring ml-2 rounded-md border px-3 py-1.5 text-xs font-semibold transition disabled:cursor-not-allowed disabled:text-slate-400"
                    :class="
                      user.status === 'active'
                        ? 'border-rose-200 text-rose-700 hover:bg-rose-50'
                        : 'border-emerald-200 text-emerald-700 hover:bg-emerald-50'
                    "
                    :disabled="actionId === user.id"
                    type="button"
                    @click="toggleStatus(user)"
                  >
                    {{ user.status === 'active' ? '禁用' : '启用' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <section v-if="resetTarget" class="mt-6 max-w-xl rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <h2 class="text-lg font-semibold text-slate-900">重置密码：{{ resetTarget.username }}</h2>
      <form class="mt-4 flex flex-col gap-3 sm:flex-row" @submit.prevent="submitReset">
        <input
          v-model="resetForm.newPassword"
          class="focus-ring min-w-0 flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-orange-500"
          maxlength="72"
          minlength="6"
          required
          type="password"
        />
        <button
          class="focus-ring rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
          :disabled="actionId === resetTarget.id"
          type="submit"
        >
          保存
        </button>
        <button
          class="focus-ring rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
          type="button"
          @click="resetTarget = null"
        >
          取消
        </button>
      </form>
    </section>
  </section>
</template>
