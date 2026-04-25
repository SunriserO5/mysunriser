<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AdminLayout from './layouts/AdminLayout.vue'
import DefaultLayout from './layouts/DefaultLayout.vue'
import HomeLayout from './layouts/HomeLayout.vue'

const route = useRoute()

const layout = computed(() => {
  if (route.meta.layout === 'home') {
    return HomeLayout
  }

  if (route.meta.layout === 'admin') {
    return AdminLayout
  }

  return DefaultLayout
})
</script>

<template>
  <component :is="layout">
    <RouterView />
  </component>
</template>
