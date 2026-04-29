<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchFooterSettings } from '../api'
import type { FooterSettings } from '../types'

const currentYear = new Date().getFullYear()
const settings = ref<FooterSettings>({
  githubEnabled: false,
  githubUrl: '',
  xEnabled: false,
  xUrl: '',
})

const visibleLinks = computed(() =>
  [
    { label: 'GitHub', icon: '/icons.svg#github-icon', url: settings.value.githubUrl, enabled: settings.value.githubEnabled },
    { label: 'X', icon: '/icons.svg#x-icon', url: settings.value.xUrl, enabled: settings.value.xEnabled },
  ].filter((link) => link.enabled && link.url),
)

async function loadFooterSettings() {
  try {
    settings.value = await fetchFooterSettings()
  } catch {
    settings.value = {
      githubEnabled: false,
      githubUrl: '',
      xEnabled: false,
      xUrl: '',
    }
  }
}

onMounted(loadFooterSettings)
</script>

<template>
  <footer class="border-t border-slate-200/80 bg-white/70 py-8">
    <div class="mx-auto flex max-w-6xl flex-col gap-4 px-4 text-sm text-slate-600 sm:px-6 lg:px-8">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div class="space-y-3">
          <p class="font-medium text-slate-800">写作、代码、以及每天的微小思考。</p>
          <p>© {{ currentYear }} MySunriser. Built with Vue and Spring Boot.</p>
        </div>

        <nav v-if="visibleLinks.length" class="flex flex-wrap gap-2" aria-label="社交链接">
          <a
            v-for="link in visibleLinks"
            :key="link.label"
            class="focus-ring inline-flex h-9 w-9 items-center justify-center rounded-full text-slate-500 transition hover:bg-slate-100/70 hover:text-slate-950"
            :href="link.url"
            :aria-label="link.label"
            rel="noopener noreferrer"
            target="_blank"
            :title="link.label"
          >
            <svg class="h-5 w-5" aria-hidden="true">
              <use :href="link.icon" />
            </svg>
          </a>
        </nav>
      </div>
    </div>
  </footer>
</template>
