import { createRouter, createWebHistory } from 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    layout?: 'default' | 'home'
    title?: string
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Home.vue'),
      meta: { layout: 'home', title: 'MySunriser' },
    },
    {
      path: '/blog',
      name: 'blog',
      component: () => import('../views/Blog.vue'),
      meta: { layout: 'default', title: '博客 | MySunriser' },
    },
    {
      path: '/blog/:slug',
      name: 'post',
      component: () => import('../views/Post.vue'),
      meta: { layout: 'default', title: '文章 | MySunriser' },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/About.vue'),
      meta: { layout: 'default', title: '关于 | MySunriser' },
    },
  ],
})

router.afterEach((to) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }
})

export default router
