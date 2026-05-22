import { computed, ref } from 'vue'
import {
  confirmRegistration as confirmRegistrationRequest,
  fetchMe,
  login as loginRequest,
  logout as logoutRequest,
  refreshSession,
  requestRegistration as requestRegistrationRequest,
  setAccessToken,
} from '../api'
import type { AuthCredentials, AuthRegisterPayload, AuthTokenResponse, AuthUser } from '../types'

const token = ref<string | null>(null)
const user = ref<AuthUser | null>(null)
const ready = ref(false)
const loading = ref(false)

function persistToken(nextToken: string | null) {
  token.value = nextToken
  setAccessToken(nextToken)
}

function applyTokenResponse(response: AuthTokenResponse) {
  persistToken(response.token)
  user.value = {
    id: response.id,
    username: response.username,
    role: response.role,
    status: 'active',
    email: response.email,
    emailVerified: response.emailVerified,
    nickname: response.nickname,
    avatarUrl: response.avatarUrl,
  }
  ready.value = true
}

async function restore(): Promise<void> {
  if (ready.value && user.value) {
    ready.value = true
    return
  }

  loading.value = true

  try {
    const response = await refreshSession()
    applyTokenResponse(response)
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

async function register(credentials: AuthRegisterPayload): Promise<void> {
  await requestRegistration(credentials)
}

async function requestRegistration(credentials: AuthRegisterPayload): Promise<void> {
  loading.value = true

  try {
    await requestRegistrationRequest(credentials)
  } finally {
    loading.value = false
  }
}

async function confirmRegistration(tokenValue: string): Promise<void> {
  loading.value = true

  try {
    applyTokenResponse(await confirmRegistrationRequest(tokenValue))
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
    requestRegistration,
    confirmRegistration,
    logout,
  }
}
