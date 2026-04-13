<template>
  <section class="fade-in mx-auto max-w-3xl rounded-3xl border border-slate-200/80 bg-white p-8 shadow-sm sm:p-10">
    <p class="text-xs font-semibold uppercase tracking-[0.2em] text-emerald-600">ABOUT</p>
    <h1 class="mt-3 text-4xl font-bold tracking-tight text-slate-900">关于 MySunriser</h1>

    <div class="mt-6 space-y-4 text-sm leading-8 text-slate-700 sm:text-base">
      <p>
        Welcome to MySunriser! 我是一名来自中国的本科计算机学生,欢迎来到我的小小天地.
      </p>
      <p>
        这个站点从后端开始搭建，目标是打造一个长期维护的个人内容平台：功能不求复杂，但追求结构清晰、迭代稳定。
      </p>
      <p>
        内容上主要聚焦开发过程记录、工程实践、日常笔记与一些社会观察。
      </p>
    </div>

    <div class="mt-8 grid gap-4 sm:grid-cols-3">
      <div class="rounded-2xl bg-amber-50 p-4">
        <p class="text-xs font-semibold uppercase tracking-wider text-amber-700">Frontend</p>
        <p class="mt-2 text-sm font-medium text-slate-800">Vue 3 + Tailwind</p>
      </div>
      <div class="rounded-2xl bg-emerald-50 p-4">
        <p class="text-xs font-semibold uppercase tracking-wider text-emerald-700">Backend</p>
        <p class="mt-2 text-sm font-medium text-slate-800">Spring Boot + MyBatis</p>
      </div>
      <div class="rounded-2xl bg-sky-50 p-4">
        <p class="text-xs font-semibold uppercase tracking-wider text-sky-700">Database</p>
        <p class="mt-2 text-sm font-medium text-slate-800">MySQL + Flyway</p>
      </div>
    </div>
  </section>
</template>
