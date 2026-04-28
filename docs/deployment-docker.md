# Docker 部署指南

更新时间：2026-04-28

本文档说明 MySunriser 在单台 VPS 上使用 Docker Compose、Caddy、MySQL 和 Cloudflare Turnstile 部署的流程。

## 一、服务器准备

服务器需要安装：

- Docker Engine
- Docker Compose v2
- Git

建议防火墙只开放：

- `22/tcp`：SSH
- `80/tcp`：HTTP，给 Caddy 和 Cloudflare 证书签发使用
- `443/tcp`：HTTPS

后端 `8080` 和 MySQL `3306` 不需要开放公网端口，Compose 会让它们只在 Docker 内网通信。

## 二、编写 `.env`

进入 `deploy/` 目录并复制示例文件：

```bash
cd deploy
cp .env.example .env
```

然后编辑当前目录下的 `.env`，也就是 `deploy/.env`：

```env
COMPOSE_PROJECT_NAME=mysunriser

MYSQL_DATABASE=mysunriser
MYSQL_USER=mysunriser_app
MYSQL_PASSWORD=换成强密码
MYSQL_ROOT_PASSWORD=换成更强的root密码

SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/mysunriser?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=mysunriser_app
SPRING_DATASOURCE_PASSWORD=同上面的MYSQL_PASSWORD

JWT_SECRET=至少32位以上随机字符串
JWT_EXPIRE_MINUTES=120

TURNSTILE_ENABLED=true
TURNSTILE_SITE_KEY=你的TurnstileSiteKey
TURNSTILE_SECRET_KEY=你的TurnstileSecretKey

LOGIN_MAX_ATTEMPTS=5
LOGIN_WINDOW_SECONDS=900

APP_DOMAIN=你的域名
```

注意：

- `.env` 不要提交到 git。
- `TURNSTILE_SITE_KEY` 可以公开，`TURNSTILE_SECRET_KEY` 只能放后端环境变量。
- `JWT_SECRET` 必须至少 32 位，不能使用示例值。
- `SPRING_DATASOURCE_PASSWORD` 要和 `MYSQL_PASSWORD` 一致。
- Compose 内网连接 MySQL 时使用 `allowPublicKeyRetrieval=true`；如果以后改为公网/云数据库，应优先启用 TLS。

## 三、启动与更新

如果需要 Compose 一起启动 MySQL，首次启动在 `deploy/` 目录执行：

```bash
docker compose up -d --build
```

如果已经有可用的 MySQL 容器或外部 MySQL，只启动应用和 Caddy：

```bash
docker compose -f docker-compose.app.yml up -d --build
```

此时 `.env` 里的 `SPRING_DATASOURCE_URL` 必须指向已有数据库。例如已有 MySQL 通过宿主机 `3306` 暴露：

```env
SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/mysunriser?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
```

查看状态：

```bash
docker compose ps
```

查看日志：

```bash
docker compose logs -f caddy
docker compose logs -f backend
docker compose logs -f mysql
```

更新代码后重新构建：

```bash
cd ..
git pull
cd deploy
docker compose up -d --build
```

停止服务：

```bash
docker compose down
```

不要随意执行 `docker compose down -v`，它会删除 MySQL 数据卷。
如果使用 `docker-compose.app.yml`，停止时也要带上同一个文件名：

```bash
docker compose -f docker-compose.app.yml down
```

## 四、Cloudflare 接入

1. 在 Cloudflare 添加站点域名。
2. DNS 记录指向 VPS 公网 IP，并开启橙云代理。
3. SSL/TLS 模式选择 `Full (strict)`。
4. 在 Turnstile 中创建 widget，域名填写生产域名。
5. 将 Turnstile 的 site key 和 secret key 写入服务器 `deploy/.env`。
6. 重启服务：

```bash
docker compose up -d
```

更进一步的源站防护：上线稳定后，可以在服务器防火墙中只允许 Cloudflare IP 访问 `80/443`，避免绕过 Cloudflare 直连源站。

## 五、MySQL 备份

创建备份目录：

```bash
cd deploy
mkdir -p backups
```

导出数据库：

```bash
docker compose exec mysql mysqldump -u root -p mysunriser > backups/mysunriser-$(date +%F).sql
```

恢复数据库前请先确认目标环境，避免覆盖生产数据：

```bash
docker compose exec -T mysql mysql -u root -p mysunriser < backups/mysunriser-YYYY-MM-DD.sql
```

## 六、常见排错

后端启动失败并提示 `JWT_SECRET`：

- 检查 `.env` 中是否配置了至少 32 位的 `JWT_SECRET`。

登录/注册提示 Turnstile 校验失败：

- 检查 `TURNSTILE_SITE_KEY` 和 `TURNSTILE_SECRET_KEY` 是否来自同一个 widget。
- 检查 Cloudflare Turnstile widget 的允许域名是否包含当前访问域名。
- 检查服务器是否能访问 `https://challenges.cloudflare.com`。

数据库连接失败：

- 检查 `SPRING_DATASOURCE_URL` 的 host 是否是 `mysql`。
- 检查 `SPRING_DATASOURCE_PASSWORD` 是否和 `MYSQL_PASSWORD` 一致。
- 查看 `docker compose logs -f mysql`。

Caddy 没有签发 HTTPS 证书：

- 检查 `APP_DOMAIN` 是否是真实域名。
- 检查 DNS 是否指向当前 VPS。
- 检查 80/443 端口是否开放。
