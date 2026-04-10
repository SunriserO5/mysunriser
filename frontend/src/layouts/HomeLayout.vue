<script setup lang="ts">
import Footer from '../components/Footer.vue'
import Navbar from '../components/Navbar.vue'
</script>

<template>
  <div class="relative min-h-screen overflow-hidden bg-amber-50 text-slate-900">
    <div
      class="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(circle_at_top_right,rgba(16,185,129,0.12),transparent_44%),radial-gradient(circle_at_10%_0%,rgba(249,115,22,0.16),transparent_35%),linear-gradient(180deg,#fffaf0_0%,#f8fafc_60%)]"
    />
    <Navbar transparent />
    <main class="mx-auto w-full max-w-6xl px-4 pb-8 pt-6 sm:px-6 lg:px-8">
      <slot />
    </main>
    <Footer />
  </div>
</template>
