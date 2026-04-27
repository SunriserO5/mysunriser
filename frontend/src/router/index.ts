import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import type { AuthRole } from '../types'

declare module 'vue-router' {
  interface RouteMeta {
    layout?: 'default' | 'home' | 'admin'
    requiresAuth?: boolean
    requiresRole?: AuthRole
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
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { layout: 'default', title: '登录 | MySunriser' },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
      meta: { layout: 'default', title: '注册 | MySunriser' },
    },
    {
      path: '/403',
      name: 'forbidden',
      component: () => import('../views/Forbidden.vue'),
      meta: { layout: 'default', title: '无权限 | MySunriser' },
    },
    {
      path: '/admin',
      redirect: '/admin/users',
    },
    {
      path: '/admin/posts/new',
      name: 'admin-post-new',
      component: () => import('../views/AdminPostNew.vue'),
      meta: {
        layout: 'default',
        requiresAuth: true,
        requiresRole: 'admin',
        title: '新建文章 | MySunriser',
      },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('../views/AdminUsers.vue'),
      meta: {
        layout: 'admin',
        requiresAuth: true,
        requiresRole: 'admin',
        title: '账号管理 | MySunriser',
      },
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuth()
  await auth.restore()

  if (to.meta.requiresAuth && !auth.isAuthenticated.value) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.requiresRole && auth.user.value?.role !== to.meta.requiresRole) {
    return { name: 'forbidden' }
  }

  if ((to.name === 'login' || to.name === 'register') && auth.isAuthenticated.value) {
    return { name: 'home' }
  }

  return true
})

router.afterEach((to) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }
})

export default router
