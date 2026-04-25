import { computed, ref } from 'vue'
import { AUTH_TOKEN_STORAGE_KEY, fetchMe, login as loginRequest, logout as logoutRequest, register as registerRequest } from '../api'
import type { AuthCredentials, AuthTokenResponse, AuthUser } from '../types'

const token = ref<string | null>(readInitialToken())
const user = ref<AuthUser | null>(null)
const ready = ref(false)
const loading = ref(false)

function readInitialToken(): string | null {
  if (typeof window === 'undefined') {
    return null
  }

  return window.localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
}

function persistToken(nextToken: string | null) {
  token.value = nextToken

  if (typeof window === 'undefined') {
    return
  }

  if (nextToken) {
    window.localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, nextToken)
  } else {
    window.localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY)
  }
}

function applyTokenResponse(response: AuthTokenResponse) {
  persistToken(response.token)
  user.value = {
    username: response.username,
    role: response.role,
    status: 'active',
  }
  ready.value = true
}

async function restore(): Promise<void> {
  if (ready.value || !token.value) {
    ready.value = true
    return
  }

  loading.value = true

  try {
    user.value = await fetchMe()
  } catch {
    persistToken(null)
    user.value = null
  } finally {
    ready.value = true
    loading.value = false
  }
}

async function login(credentials: AuthCredentials): Promise<void> {
  loading.value = true

  try {
    applyTokenResponse(await loginRequest(credentials))
  } finally {
    loading.value = false
  }
}

async function register(credentials: AuthCredentials): Promise<void> {
  loading.value = true

  try {
    applyTokenResponse(await registerRequest(credentials))
  } finally {
    loading.value = false
  }
}

async function logout(): Promise<void> {
  try {
    if (token.value) {
      await logoutRequest()
    }
  } catch {
    // Token revocation is stateless for now; local cleanup is the source of truth.
  } finally {
    persistToken(null)
    user.value = null
    ready.value = true
  }
}

export function useAuth() {
  return {
    token,
    user,
    ready,
    loading,
    isAuthenticated: computed(() => Boolean(token.value && user.value)),
    isAdmin: computed(() => user.value?.role === 'admin'),
    restore,
    login,
    register,
    logout,
  }
}
