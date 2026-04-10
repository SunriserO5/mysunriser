<script setup lang="ts">
import Footer from '../components/Footer.vue'
import Navbar from '../components/Navbar.vue'
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-900">
    <Navbar />
    <main class="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
      <slot />
    </main>
    <Footer />
  </div>
</template>
