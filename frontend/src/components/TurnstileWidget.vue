<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

declare global {
  interface Window {
    turnstile?: {
      render: (
        element: HTMLElement,
        options: {
          sitekey: string
          callback: (token: string) => void
          'expired-callback': () => void
          'error-callback': () => void
        },
      ) => string
      remove: (widgetId: string) => void
      reset: (widgetId: string) => void
    }
  }
}

const props = defineProps<{
  siteKey: string
}>()

const emit = defineEmits<{
  verified: [token: string]
  expired: []
}>()

const container = ref<HTMLElement | null>(null)
const widgetId = ref<string | null>(null)

function loadScript(): Promise<void> {
  if (window.turnstile) {
    return Promise.resolve()
  }

  return new Promise((resolve, reject) => {
    const existingScript = document.getElementById('cf-turnstile-script') as HTMLScriptElement | null
    if (existingScript) {
      existingScript.addEventListener('load', () => resolve(), { once: true })
      existingScript.addEventListener('error', () => reject(new Error('Turnstile failed to load')), { once: true })
      return
    }

    const script = document.createElement('script')
    script.id = 'cf-turnstile-script'
    script.src = 'https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit'
    script.async = true
    script.defer = true
    script.addEventListener('load', () => resolve(), { once: true })
    script.addEventListener('error', () => reject(new Error('Turnstile failed to load')), { once: true })
    document.head.appendChild(script)
  })
}

async function renderWidget() {
  if (!container.value || !props.siteKey) {
    return
  }

  await loadScript()
  if (!window.turnstile || !container.value) {
    return
  }

  if (widgetId.value) {
    window.turnstile.remove(widgetId.value)
  }

  widgetId.value = window.turnstile.render(container.value, {
    sitekey: props.siteKey,
    callback: (token: string) => emit('verified', token),
    'expired-callback': () => emit('expired'),
    'error-callback': () => emit('expired'),
  })
}

function reset() {
  if (window.turnstile && widgetId.value) {
    window.turnstile.reset(widgetId.value)
  }
  emit('expired')
}

watch(() => props.siteKey, renderWidget)

onMounted(renderWidget)

onBeforeUnmount(() => {
  if (window.turnstile && widgetId.value) {
    window.turnstile.remove(widgetId.value)
  }
})

defineExpose({ reset })
</script>

<template>
  <div ref="container" class="min-h-[65px]" />
</template>
