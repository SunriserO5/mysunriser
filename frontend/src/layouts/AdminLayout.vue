<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'
import Navbar from '../components/Navbar.vue'

const route = useRoute()
</script>

<template>
  <div class="min-h-screen bg-slate-100 text-slate-900">
    <Navbar />
    <div class="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[220px_minmax(0,1fr)] lg:px-8">
      <aside class="h-fit rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
        <RouterLink
          to="/admin/users"
          class="admin-menu-link block rounded-md px-3 py-2 text-sm font-semibold transition"
          :class="{ 'is-active': route.path.startsWith('/admin/users') }"
        >
          账号管理
        </RouterLink>
      </aside>

      <main class="min-w-0">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-menu-link {
  color: #334155;
}

.admin-menu-link:hover {
  background-color: #f1f5f9;
  color: #020617;
}

.admin-menu-link.is-active,
.admin-menu-link.is-active:hover {
  background-color: #0f172a;
  color: #ffffff;
  -webkit-text-fill-color: #ffffff;
}
</style>
