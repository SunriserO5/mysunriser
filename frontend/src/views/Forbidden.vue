<template>
  <section class="mx-auto max-w-2xl rounded-lg border border-amber-200 bg-amber-50 p-6 text-slate-900 shadow-sm">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-amber-700">403</p>
    <h1 class="mt-2 text-3xl font-bold">无权限访问</h1>
    <p class="mt-3 text-sm leading-6 text-slate-700">当前账号不能访问该页面。</p>
    <RouterLink
      class="focus-ring mt-5 inline-flex rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-800"
      to="/"
    >
      返回首页
    </RouterLink>
  </section>
</template>
