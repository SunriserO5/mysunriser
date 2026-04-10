<script setup lang="ts">
import { useRoute } from 'vue-router'

withDefaults(
  defineProps<{
    transparent?: boolean
  }>(),
  {
    transparent: false,
  },
)

const route = useRoute()

const links = [
  { name: '首页', to: '/' },
  { name: '博客', to: '/blog' },
  { name: '关于', to: '/about' },
]

function isActive(path: string): boolean {
  if (path === '/') {
    return route.path === '/'
  }

  return route.path === path || route.path.startsWith(`${path}/`)
}
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

      <ul class="flex items-center gap-2 text-sm font-medium sm:gap-3">
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
    </nav>
  </header>
</template>

<style scoped>
.nav-link {
  color: #0f172a !important;
  -webkit-text-fill-color: #0f172a;
}

.nav-link-active {
  background-color: #0f172a;
  color: #f8fafc !important;
  -webkit-text-fill-color: #f8fafc;
}

.nav-link:hover {
  border-color: #cbd5e1 !important;
  background-color: #f1f5f9 !important;
  color: #020617 !important;
  -webkit-text-fill-color: #020617;
}

.nav-link-active:hover {
  border-color: #1e293b !important;
  background-color: #1e293b !important;
  color: #ffffff !important;
  -webkit-text-fill-color: #ffffff;
}
</style>
