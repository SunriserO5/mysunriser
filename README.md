# MySunriser
开发中的个人网站

## 技术栈
- 前端：Vue 3, Vite, TypeScript, Tailwind CSS
- 后端：REST API, Spring Boot（Spring Security, MyBatis, Flyway）
- 数据库：MySQL 8
- 部署：Docker Compose, Caddy

## 部署

生产环境推荐使用 Docker Compose 单机部署：

- `caddy` 对外暴露 80/443，托管前端静态文件并反代 `/api/*`
- `backend` 只在 Docker 内网监听 8080
- `mysql` 只在 Docker 内网监听 3306，并使用 Docker volume 持久化

先进入部署目录，复制并填写环境变量：

```bash
cd deploy
cp .env.example .env
```

然后启动：

```bash
docker compose up -d --build
```

详细步骤见 [Docker 部署指南](docs/deployment-docker.md)。
