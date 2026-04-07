# 前端构建交接文档

> 更新时间：2026-04-07

---

## 一、项目背景

MySunriser 是一个个人博客项目，后端已完成，现在需要构建前端。

**README 中声明的技术栈：**
- 前端：Vue 3, Vite, TypeScript, Tailwind CSS
- 后端：REST API, Spring Boot（Spring Security, MyBatis, Flyway）
- 数据库：MySQL 8

---

## 二、后端 API 总结

后端运行在 `http://localhost:8080`，完整 OpenAPI 规范见 `docs/openapi.yaml`。

### 可用接口

| 接口 | 方法 | 说明 | 请求参数 | 响应 |
|------|------|------|----------|------|
| `/api/health` | GET | 健康检查 | 无 | `{ ok: boolean, db: boolean }` |
| `/api/page` | GET | 博客列表分页 | `page` (int, ≥1), `pageSize` (int, 1-100) | `PageResponse` |
| `/api/blog/{slug}` | GET | 获取单篇博文 | `slug` (path, string) | `PostResponse` |

### 响应数据结构

**PageResponse：**
```json
{
  "page": 1,
  "pageSize": 10,
  "items": [
    {
      "slug": "testblog2",
      "title": "testtitle2",
      "summary": "testsummary2",
      "status": "published",
      "publishTime": "2026-04-07T12:00:00"
    }
  ]
}
```

**PostResponse：**
```json
{
  "id": 1,
  "slug": "testblog2",
  "title": "testtitle2",
  "content": "This is a test blog, hello mysunriser2",
  "status": "published",
  "publish_at": "2026-04-07T12:00:00"
}
```

**ErrorResponse：**
```json
{
  "code": 40001,
  "message": "错误信息",
  "timeStamp": 1712476800000
}
```

### 注意事项
- 后端 Security 配置为 `permitAll()`，无需认证
- CSRF 已禁用
- 后端代码不要修改（除非万不得已）

---

## 三、已完成的工作

### ✅ 已完成

1. **Vite 项目初始化**
   - 使用 `npm create vite@latest . -- --template vue-ts` 在 `frontend/` 目录下创建
   - Vue 3 + TypeScript 模板

2. **依赖安装**
   - `vue-router@4` — 路由
   - `tailwindcss` + `@tailwindcss/vite` — Tailwind CSS v4（Vite 插件模式）
   - 所有依赖已 `npm install` 完成

3. **Vite 配置更新** (`frontend/vite.config.ts`)
   - 添加了 Tailwind CSS Vite 插件
   - 添加了开发代理：`/api` → `http://localhost:8080`

### ⬜ 尚未完成

以下所有工作均未开始：

- [ ] 替换 `src/style.css` 为 Tailwind 入口样式（`@import "tailwindcss"`）
- [ ] 配置 Vue Router
- [ ] 定义 TypeScript 类型
- [ ] 封装 API 请求层
- [ ] 实现 Composables
- [ ] 实现布局组件
- [ ] 实现公共组件
- [ ] 实现页面视图
- [ ] 清理 Vite 模板默认文件

---

## 四、计划的前端架构

### 设计原则
- **可扩展性**：首页使用独立布局，方便后续自定义创意
- **关注点分离**：布局、页面、组件、API、类型各自独立
- **组合式 API**：使用 Vue 3 Composition API + Composables

### 路由规划

| 路径 | 页面 | 布局 | 说明 |
|------|------|------|------|
| `/` | `Home.vue` | `HomeLayout` | 着陆页，独立布局，便于创意自定义 |
| `/blog` | `Blog.vue` | `DefaultLayout` | 博客文章列表 + 分页 |
| `/blog/:slug` | `Post.vue` | `DefaultLayout` | 文章详情页 |
| `/about` | `About.vue` | `DefaultLayout` | 关于页（静态） |

### 目标目录结构

```
frontend/src/
├── main.ts                    # 入口：挂载 App + Router
├── App.vue                    # 根组件：根据路由 meta 切换布局
├── style.css                  # Tailwind 入口（@import "tailwindcss"）
├── router/
│   └── index.ts               # Vue Router 路由配置，meta.layout 控制布局
├── api/
│   └── index.ts               # fetch 封装，调用后端 /api/*
├── types/
│   └── index.ts               # TS 类型定义（PageItem, PostDetail, PageResponse 等）
├── composables/
│   ├── usePagination.ts       # 分页状态管理
│   └── usePost.ts             # 文章获取逻辑
├── layouts/
│   ├── DefaultLayout.vue      # 标准布局：Navbar + <slot> + Footer
│   └── HomeLayout.vue         # 首页专属布局：自由创意空间
├── views/
│   ├── Home.vue               # 首页着陆页（Hero + 最新文章预览）
│   ├── Blog.vue               # 博客列表页（调用 /api/page，含分页）
│   ├── Post.vue               # 文章详情页（调用 /api/blog/{slug}）
│   └── About.vue              # 关于页
└── components/
    ├── Navbar.vue             # 顶部导航栏
    ├── Footer.vue             # 底部组件
    ├── PostCard.vue           # 文章卡片（用于列表展示）
    └── Pagination.vue         # 分页组件
```

### 布局系统设计

通过路由 `meta.layout` 字段控制每个页面使用哪个布局：

```ts
// router/index.ts 示例
{
  path: '/',
  component: () => import('../views/Home.vue'),
  meta: { layout: 'home' }
},
{
  path: '/blog',
  component: () => import('../views/Blog.vue'),
  meta: { layout: 'default' }
}
```

```vue
<!-- App.vue 示例 -->
<template>
  <component :is="layout">
    <RouterView />
  </component>
</template>
```

这样首页（`HomeLayout`）和其他页面（`DefaultLayout`）完全解耦，后续改造首页不影响其他页面。

### TypeScript 类型定义（对应后端 DTO）

```ts
// types/index.ts
export interface PageItem {
  slug: string
  title: string
  summary: string
  status: string
  publishTime: string
}

export interface PageResponse {
  page: number
  pageSize: number
  items: PageItem[]
}

export interface PostDetail {
  id: number
  slug: string
  title: string
  content: string
  status: string
  publish_at: string
}

export interface ErrorResponse {
  code: number
  message: string
  timeStamp: number
}
```

### API 封装方案

使用原生 `fetch`（不引入 axios），保持简洁：

```ts
// api/index.ts
const BASE = ''  // 开发环境由 Vite 代理处理

export async function fetchPage(page: number, pageSize: number): Promise<PageResponse> { ... }
export async function fetchPost(slug: string): Promise<PostDetail> { ... }
```

---

## 五、当前文件状态

| 文件 | 状态 | 说明 |
|------|------|------|
| `frontend/vite.config.ts` | ✅ 已更新 | 添加了 Tailwind 插件 + API 代理 |
| `frontend/package.json` | ✅ 依赖已装 | vue-router, tailwindcss, @tailwindcss/vite |
| `frontend/src/style.css` | ⚠️ 待替换 | 当前是 Vite 模板默认样式，需替换为 Tailwind 入口 |
| `frontend/src/main.ts` | ⚠️ 待更新 | 需添加 Router 挂载 |
| `frontend/src/App.vue` | ⚠️ 待重写 | 需实现布局切换逻辑 |
| `frontend/src/components/HelloWorld.vue` | 🗑️ 待删除 | Vite 模板文件，不需要 |
| `frontend/src/assets/*` | 🗑️ 待清理 | hero.png, vite.svg, vue.svg 为模板文件 |

---

## 六、接续开发指南

### 快速开始

```bash
cd frontend
npm install    # 如果需要
npm run dev    # 启动开发服务器 http://localhost:5173
```

### 推荐的实施顺序

1. 替换 `style.css` → `@import "tailwindcss";`
2. 创建 `types/index.ts`（类型定义）
3. 创建 `api/index.ts`（API 封装）
4. 创建 `router/index.ts`（路由配置）
5. 更新 `main.ts`（挂载 Router）
6. 创建布局组件 `layouts/DefaultLayout.vue` 和 `HomeLayout.vue`
7. 重写 `App.vue`（布局切换）
8. 创建公共组件 `Navbar.vue`, `Footer.vue`, `PostCard.vue`, `Pagination.vue`
9. 创建 composables `usePagination.ts`, `usePost.ts`
10. 实现页面 `Home.vue`, `Blog.vue`, `Post.vue`, `About.vue`
11. 清理模板默认文件

### 设计风格建议
- 简洁干净的博客风格
- Tailwind CSS 实现响应式
- 后续可考虑深色/浅色主题切换
