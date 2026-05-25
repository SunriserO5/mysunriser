<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

withDefaults(
  defineProps<{
    transparent?: boolean
  }>(),
  {
    transparent: false,
  },
)

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const userMenuOpen = ref(false)
const mobileMenuOpen = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)

const links = [
  { name: '首页', to: '/' },
  { name: '博客', to: '/blog' },
  { name: '工具', to: '/tools' },
  { name: '项目', to: '/projects' },
  { name: '关于', to: '/about' },
]

const accountLinks = computed(() => {
  if (!auth.isAuthenticated.value) {
    return [{ name: '登录', to: '/login' }]
  }

  const items = []
  if (auth.isAdmin.value) {
    items.push({ name: '后台', to: '/admin/users' })
  }
  items.push({ name: '账户', to: '/account' })

  return items
})

function isActive(path: string): boolean {
  if (path === '/') {
    return route.path === '/'
  }

  return route.path === path || route.path.startsWith(`${path}/`)
}

function closeMenus() {
  userMenuOpen.value = false
  mobileMenuOpen.value = false
}

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value
  mobileMenuOpen.value = false
}

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value
  userMenuOpen.value = false
}

async function logout() {
  await auth.logout()
  closeMenus()

  if (route.meta.requiresAuth) {
    await router.push({ name: 'login' })
  }
}

function onDocumentClick(event: MouseEvent) {
  if (!userMenuOpen.value) {
    return
  }

  const target = event.target
  if (target instanceof Node && userMenuRef.value?.contains(target)) {
    return
  }

  userMenuOpen.value = false
}

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeMenus()
  }
}

onMounted(() => {
  void auth.restore()
  document.addEventListener('click', onDocumentClick)
  window.addEventListener('keydown', onKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocumentClick)
  window.removeEventListener('keydown', onKeydown)
  document.body.classList.remove('overflow-hidden')
})

watch(
  () => route.fullPath,
  () => {
    closeMenus()
  },
)

watch(mobileMenuOpen, (open) => {
  document.body.classList.toggle('overflow-hidden', open)
})
</script>

<template>
  <header
    class="sticky top-0 z-30 border-b backdrop-blur-xl"
    :class="
      transparent
        ? 'border-amber-100/40 bg-amber-50/40'
        : 'border-slate-200/70 bg-white/85 supports-[backdrop-filter]:bg-white/70'
    "
  >
    <nav class="mx-auto flex max-w-6xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
      <RouterLink to="/" class="group inline-flex items-center gap-2">
        <span
          class="inline-block h-3 w-3 rounded-full bg-gradient-to-r from-amber-500 to-orange-500 shadow-[0_0_0_5px_rgba(251,146,60,0.15)]"
        />
        <span class="text-lg font-semibold tracking-tight text-slate-900">MySunriser</span>
      </RouterLink>

      <div class="hidden flex-1 items-center justify-end gap-3 md:flex">
        <ul class="flex items-center justify-end gap-2 text-sm font-medium">
          <li v-for="link in links" :key="link.to">
            <RouterLink
              :to="link.to"
              class="nav-link rounded-full border border-transparent px-3 py-1.5 transition"
              :class="
                isActive(link.to)
                  ? 'nav-link-active border-slate-800 bg-slate-900 text-slate-50 shadow-md'
                  : 'text-slate-900'
              "
            >
              {{ link.name }}
            </RouterLink>
          </li>
        </ul>

        <div ref="userMenuRef" class="relative">
          <button
            class="icon-button focus-ring inline-flex"
            :class="{ 'is-active': userMenuOpen || isActive('/admin') || isActive('/account') || isActive('/login') }"
            type="button"
            aria-label="用户菜单"
            :aria-expanded="userMenuOpen"
            aria-haspopup="menu"
            @click.stop="toggleUserMenu"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M12 12.5a4.25 4.25 0 1 0 0-8.5 4.25 4.25 0 0 0 0 8.5Zm-7 7.25c.8-3.45 3.35-5.5 7-5.5s6.2 2.05 7 5.5"
                fill="none"
                stroke="currentColor"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
              />
            </svg>
          </button>

          <div v-if="userMenuOpen" class="user-menu" role="menu">
            <RouterLink
              v-for="link in accountLinks"
              :key="link.to"
              :to="link.to"
              class="menu-link"
              :class="{ 'is-active': isActive(link.to) }"
              role="menuitem"
            >
              {{ link.name }}
            </RouterLink>
            <button v-if="auth.isAuthenticated.value" class="menu-link text-left" type="button" role="menuitem" @click="logout">
              登出
            </button>
          </div>
        </div>
      </div>

      <button
        class="icon-button focus-ring inline-flex md:hidden"
        type="button"
        aria-label="打开菜单"
        :aria-expanded="mobileMenuOpen"
        @click="toggleMobileMenu"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path
            d="M4 7h16M4 12h16M4 17h16"
            fill="none"
            stroke="currentColor"
            stroke-linecap="round"
            stroke-width="2"
          />
        </svg>
      </button>
    </nav>

    <div v-if="mobileMenuOpen" class="fixed inset-0 z-40 bg-slate-950/40 md:hidden" @click="closeMenus" />
    <aside v-if="mobileMenuOpen" class="mobile-drawer md:hidden" aria-label="移动端导航">
      <div class="flex items-center justify-between gap-4 border-b border-slate-200 px-5 py-4">
        <span class="text-base font-semibold tracking-tight text-slate-900">菜单</span>
        <button class="icon-button focus-ring inline-flex" type="button" aria-label="关闭菜单" @click="closeMenus">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="m6 6 12 12M18 6 6 18" fill="none" stroke="currentColor" stroke-linecap="round" stroke-width="2" />
          </svg>
        </button>
      </div>

      <nav class="flex flex-col gap-2 p-4 text-sm font-semibold">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="drawer-link"
          :class="{ 'is-active': isActive(link.to) }"
        >
          {{ link.name }}
        </RouterLink>

        <div class="my-2 border-t border-slate-200" />

        <RouterLink
          v-for="link in accountLinks"
          :key="link.to"
          :to="link.to"
          class="drawer-link"
          :class="{ 'is-active': isActive(link.to) }"
        >
          {{ link.name }}
        </RouterLink>
        <button v-if="auth.isAuthenticated.value" class="drawer-link text-left" type="button" @click="logout">登出</button>
      </nav>
    </aside>
  </header>
</template>

<style scoped>
.nav-link {
  color: var(--color-secondary-fg) !important;
  -webkit-text-fill-color: var(--color-secondary-fg);
}

.nav-link-active {
  background-color: var(--color-primary-bg);
  color: var(--color-primary-fg) !important;
  -webkit-text-fill-color: var(--color-primary-fg);
}

.nav-link:hover {
  border-color: var(--color-nav-hover-border) !important;
  background-color: var(--color-secondary-bg-hover) !important;
  color: var(--color-secondary-fg-hover) !important;
  -webkit-text-fill-color: var(--color-secondary-fg-hover);
}

.nav-link-active:hover {
  border-color: var(--color-primary-bg-hover) !important;
  background-color: var(--color-primary-bg-hover) !important;
  color: #ffffff !important;
  -webkit-text-fill-color: #ffffff;
}

.icon-button {
  width: 2.5rem;
  height: 2.5rem;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
  border-radius: 9999px;
  background: var(--color-secondary-bg);
  color: var(--color-secondary-fg);
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease,
    box-shadow 0.15s ease;
}

.icon-button svg {
  width: 1.25rem;
  height: 1.25rem;
}

.icon-button:hover {
  border-color: var(--color-nav-hover-border);
  background: var(--color-secondary-bg-hover);
  color: var(--color-secondary-fg-hover);
}

.icon-button.is-active {
  border-color: var(--color-primary-bg);
  background: var(--color-primary-bg);
  color: var(--color-primary-fg);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.14);
}

.user-menu {
  position: absolute;
  top: calc(100% + 0.75rem);
  right: 0;
  z-index: 50;
  min-width: 9rem;
  border: 1px solid rgba(203, 213, 225, 0.9);
  border-radius: 0.75rem;
  background: rgba(255, 255, 255, 0.96);
  padding: 0.35rem;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.16);
  backdrop-filter: blur(16px);
}

.menu-link,
.drawer-link {
  display: block;
  border-radius: 0.5rem;
  color: #334155;
  transition:
    background-color 0.15s ease,
    color 0.15s ease;
}

.menu-link {
  width: 100%;
  padding: 0.65rem 0.75rem;
  font-size: 0.9rem;
  font-weight: 650;
}

.menu-link:hover,
.drawer-link:hover {
  background: #f1f5f9;
  color: #020617;
}

.menu-link.is-active,
.drawer-link.is-active {
  background: #0f172a;
  color: #ffffff;
  -webkit-text-fill-color: #ffffff;
}

.mobile-drawer {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 50;
  width: min(20rem, calc(100vw - 2rem));
  height: 100vh;
  overflow-y: auto;
  border-left: 1px solid rgba(203, 213, 225, 0.9);
  background: rgba(255, 255, 255, 0.98);
  box-shadow: -24px 0 60px rgba(15, 23, 42, 0.22);
  backdrop-filter: blur(18px);
}

.drawer-link {
  padding: 0.85rem 1rem;
}
</style>
