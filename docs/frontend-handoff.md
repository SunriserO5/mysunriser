# 前端构建交接文档

> 更新时间：2026-04-13

---

## 一、项目背景

MySunriser 是一个个人博客项目，后端 API 已可用，前端已完成从模板到可联调版本的落地。

**技术栈：**
- 前端：Vue 3, Vite, TypeScript, Tailwind CSS v4
- 后端：REST API, Spring Boot（Spring Security, MyBatis, Flyway）
- 数据库：MySQL 8

---

## 二、后端 API 摘要

后端运行地址：`http://localhost:8080`  
完整规范：`docs/openapi.yaml`

### 可用接口

| 接口 | 方法 | 说明 | 请求参数 | 响应 |
|------|------|------|----------|------|
| `/api/health` | GET | 健康检查 | 无 | `{ ok: boolean, db: boolean }` |
| `/api/page` | GET | 博客列表分页 | `page` (int, >=1), `pageSize` (int, 1-100) | `PageResponse` |
| `/api/blog` | POST | 新增或更新文章（upsert） | JSON Body: `slug`, `title`, `content`, `status`, `published_at`(可选) | `text/plain` (`Success!` / `Create Failed`) |
| `/api/blog/{slug}` | GET | 获取单篇博文 | `slug` (path, string) | `PostResponse` |

### 响应结构（与当前前端类型一致）

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
      "publishAt": "2026-04-07T12:00:00"
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
  "content": "Markdown/文本内容",
  "status": "published",
  "publishAt": "2026-04-07T12:00:00"
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
- 后端 Security 配置为 `permitAll()`，无需认证。
- CSRF 已禁用。
- `POST /api/blog` 采用 upsert 语义：存在则更新，不存在则插入。
- 当前前端仅依赖读取接口，未改动后端业务代码。

---

## 三、进度状态

### 已完成

- [x] `src/style.css` 切换为 Tailwind 入口并建立全局视觉变量
- [x] Vue Router 完成配置（含布局切换与基础路由标题）
- [x] TypeScript 类型定义完成（`types/index.ts`）
- [x] API 封装完成（`api/index.ts`）
- [x] Composables 完成（`usePagination`, `usePost`）
- [x] 布局组件完成（`DefaultLayout`, `HomeLayout`）
- [x] 公共组件完成（`Navbar`, `Footer`, `PostCard`, `Pagination`）
- [x] 页面视图完成（`Home`, `Blog`, `Post`, `About`）
- [x] Vite 模板默认文件清理完成（`HelloWorld.vue`、示例 assets）

### 当前可用程度

前端已进入可运行、可联调、可继续迭代的状态，不再处于“初始化阶段”。

---

## 四、已落地架构

### 路由与布局

| 路径 | 页面 | 布局 | 状态 |
|------|------|------|------|
| `/` | `Home.vue` | `HomeLayout` | 已实现 |
| `/blog` | `Blog.vue` | `DefaultLayout` | 已实现 |
| `/blog/:slug` | `Post.vue` | `DefaultLayout` | 已实现 |
| `/about` | `About.vue` | `DefaultLayout` | 已实现 |

通过路由 `meta.layout` 在 `App.vue` 中切换布局，首页与内容页已解耦。

### 当前目录结构（已落地）

```text
frontend/src/
├── main.ts
├── App.vue
├── style.css
├── router/
│   └── index.ts
├── api/
│   └── index.ts
├── types/
│   └── index.ts
├── composables/
│   ├── usePagination.ts
│   └── usePost.ts
├── layouts/
│   ├── DefaultLayout.vue
│   └── HomeLayout.vue
├── views/
│   ├── Home.vue
│   ├── Blog.vue
│   ├── Post.vue
│   └── About.vue
└── components/
    ├── Navbar.vue
    ├── Footer.vue
    ├── PostCard.vue
    └── Pagination.vue
```

---

## 五、已实现能力（超出最初计划）

### 1) 错误处理与重试
- 博客列表页请求失败后支持“重试”。
- 文章详情页请求失败后支持“重试”。

### 2) 分页体验增强
- 分页状态与 URL query 参数同步（`?page=`）。
- 切页后自动滚动到顶部。

### 3) 页面标题动态化
- 路由基础标题已配置。
- 文章详情页在数据加载后会动态更新为 `文章标题 | MySunriser`。

### 4) Markdown 文章渲染
- 文章内容支持 Markdown 渲染（含代码块）。
- 渲染结果经过安全清洗后输出。
- 针对正文补充了段落、标题、引用块、列表、链接、代码块样式。
- 加粗文本映射为主题橙色。

### 5) 可读性修正
- 首页按钮与导航在普通态/悬停态均已修复对比度问题。
- 全站颜色关键值已抽离为 CSS 变量，便于后续统一调色。

---

## 六、关键文件状态

| 文件 | 状态 | 说明 |
|------|------|------|
| `frontend/vite.config.ts` | ✅ | Tailwind v4 插件与 `/api` 代理已配置 |
| `frontend/package.json` | ✅ | 依赖完整（含 `vue-router`、`dompurify`、`markdown-it`） |
| `frontend/src/style.css` | ✅ | 已为项目级样式入口，不再是模板默认样式 |
| `frontend/src/main.ts` | ✅ | 已挂载 Router |
| `frontend/src/App.vue` | ✅ | 已实现布局切换逻辑 |
| `frontend/src/components/HelloWorld.vue` | ✅ 已删除 | 模板文件已清理 |
| `frontend/src/assets/*` | ✅ 已清理 | 模板示例资源已移除 |
| `docs/frontend-acceptance.md` | ✅ | 已补充最小验收文档 |

---

## 七、接续开发指南（下一阶段）

### 快速启动

```bash
cd backend
./mvnw spring-boot:run

cd ../frontend
npm run dev
```

- 前端开发地址：`http://localhost:5173`
- 后端 API 地址：`http://localhost:8080`

### 推荐后续路线

1. 内容管理能力
   - 增加文章创建/编辑的后台入口（当前主要通过 SQL 种子数据）。
2. SEO 增强
   - 完善 Meta 信息（description、og tags）。
3. 体验增强
   - 细化移动端排版与可访问性（键盘焦点、语义标签）。
4. 质量保障
   - 增加前端最小测试用例（API 层与关键视图行为）。

---

## 八、参考文档

- API 规范：`docs/openapi.yaml`
- 最小验收：`docs/frontend-acceptance.md`
