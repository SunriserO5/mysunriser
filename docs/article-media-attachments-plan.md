# 文章图片展示与附件下载规划

## Summary

为博客新增全站媒体库能力：管理员在文章编辑页上传图片或附件，上传后自动生成可插入正文的 Markdown。文章详情页支持图片展示与内置大图查看器，附件链接支持按权限下载。

第一版使用本地磁盘存储，并抽象存储服务，方便未来替换为 S3、R2、OSS 等对象存储。

## Key Changes

- 新增 `media_asset` 表，记录资源类型、原文件名、存储 key、MIME、大小、访问级别、上传人和创建时间。
- 新增 `post_media_reference` 表，记录文章正文引用的媒体，用于草稿隐藏、发布后开放、删除防误删。
- 新增管理员媒体接口：上传、分页列表、删除未引用资源。
- 新增公开媒体读取接口：图片预览和附件下载统一走后端权限校验。
- 创建或更新文章时，后端解析 Markdown 中的媒体 URL，并重建文章媒体引用关系。
- 前端文章编辑器增加媒体上传面板，上传后生成图片或附件 Markdown 片段。
- 文章详情页中图片可点击打开内置查看器，支持关闭按钮、遮罩关闭和 `Esc` 关闭。
- 附件点击后由后端根据资源权限与文章发布状态决定是否允许下载。

## Security Rules

- 上传目录通过 `MEDIA_STORAGE_ROOT` 配置。
- 图片最大 `50MB`，附件最大 `200MB`。
- 后端校验文件大小、扩展名、MIME 类型。
- 真实存储文件名使用 UUID，不信任用户上传文件名。
- 文件读取必须校验路径归属于上传根目录，防止路径穿越。
- 未被已发布文章引用的资源仅管理员可访问。
- 被已发布文章引用后：
  - `PUBLIC` 资源可匿名访问。
  - `AUTHENTICATED` 资源需要任意登录用户访问。
  - 管理员始终可访问。

## Public Interfaces

- `POST /api/admin/media`：管理员上传媒体。
- `GET /api/admin/media`：管理员分页查看媒体库。
- `DELETE /api/admin/media/{id}`：删除未被引用的媒体。
- `GET /api/media/{id}/content`：读取图片或资源内容。
- `GET /api/media/{id}/download`：附件下载。

## Frontend Types

- `MediaAsset`
- `MediaAccessLevel = 'PUBLIC' | 'AUTHENTICATED'`
- `MediaUploadResponse`
- `AdminMediaListResponse`

## Config

- `MEDIA_STORAGE_ROOT`
- `MEDIA_IMAGE_MAX_BYTES=52428800`
- `MEDIA_ATTACHMENT_MAX_BYTES=209715200`

## Test Plan

- 后端测试管理员上传、匿名拒绝上传、非法类型拒绝、超大小拒绝。
- 测试公开资源、登录资源、草稿资源的访问权限。
- 测试文章更新后媒体引用关系正确刷新。
- 测试已引用资源不可删除，未引用资源可删除并清理磁盘文件。
- 前端执行 `npm run build`。
- 后端执行 `./mvnw test`。
- 手动验证图片插入、图片查看器、附件下载、媒体列表和删除流程。

## Assumptions

- “登录后才能下载”指任意已登录用户，不限管理员。
- 第一版媒体库只做上传、列表、复制 Markdown、删除未引用资源。
- 第一版使用本地磁盘真实存储，但保留存储服务抽象以便未来迁移对象存储。
