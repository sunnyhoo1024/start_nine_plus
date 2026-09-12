# 本地开发指南

本文说明如何在本机启动依赖、后端 API、管理端与微信小程序调试环境。

## 前置条件

| 工具 | 建议版本 |
|------|----------|
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| Docker / Docker Compose | 近期稳定版 |
| 微信开发者工具 | 最新稳定版 |

仓库根目录复制环境变量：

```bash
cp .env.example .env
# 按需修改密码；本地可保持 WECHAT_MOCK=true
```

---

## 1. 仅启动 MySQL + Redis（推荐日常开发）

在 `deploy/` 目录用 Compose **只起基础设施**，API 与前端在宿主机运行，便于热重载与调试：

```bash
cd deploy
docker compose --env-file ../.env up -d mysql redis
```

确认：

```bash
docker compose ps
# mysql 健康、3306 可连；redis 6379 可连
```

默认（与 `.env.example` 一致）：

- MySQL：`127.0.0.1:3306`，库 `start_nine_plus`，用户 `snp` / 密码见 `.env`
- 首次启动会挂载执行 `sql/schema.sql`（含管理员种子数据）
- Redis：`127.0.0.1:6379`，本地密码可为空

停止：

```bash
docker compose stop mysql redis
# 或销毁容器但保留数据卷：
docker compose down
```

> 注意：`schema.sql` 仅在 **数据卷首次初始化** 时执行。若需重新灌库，删除卷后再起：  
> `docker compose down -v`（会清空本地 Docker 中的库数据）。

---

## 2. 运行后端 API

父工程在 `services/`：

```bash
cd services
```

建议先准备各模块的 `application-local.yml`（已被 gitignore），或通过环境变量注入（与 `.env` 一致）。

启动管理端 API（默认端口 **8081**，上下文 `/admin/api`）：

```bash
# 需模块 pom 与主类就绪后：
mvn -pl snp-admin-api -am spring-boot:run
```

启动小程序 C 端 API（默认端口 **8082**，上下文 `/app/api`）：

```bash
mvn -pl snp-app-api -am spring-boot:run
```

本地联调健康检查示例：

```bash
curl -s http://127.0.0.1:8081/admin/api/actuator/health   # 若已接入 actuator
curl -s http://127.0.0.1:8082/app/api/actuator/health
```

生产配置示例见：

- `snp-admin-api/.../application-prod.yml.example`
- `snp-app-api/.../application-prod.yml.example`

---

## 3. 管理端 admin-web

```bash
cd apps/admin-web
npm install
npm run dev
```

通常 Vite 开发服务器为 `http://localhost:5173`。  
确保 `.env` 中：

```text
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

并将前端 API baseURL 指向 `http://127.0.0.1:8081/admin/api`（以项目内实际配置为准）。

生产静态构建：

```bash
npm run build
# 产物默认 dist/，可由 deploy/Dockerfile.admin-web 打进 Nginx 镜像
```

---

## 4. 微信小程序（DevTools）

1. 打开微信开发者工具 → 导入项目 → 目录选择 `apps/miniapp`
2. AppID：测试号或正式 AppId（与 `.env` 中 `WECHAT_MINI_APPID` 对应）
3. 本地调试：
   - 详情 → 本地设置 →勾选「不校验合法域名……」（仅开发）
   - 将请求 baseURL 指向 `http://127.0.0.1:8082/app/api`（或本机局域网 IP，真机预览时需可访问）
4. 本地默认 `WECHAT_MOCK=true`：跳过真实 `code2Session`，便于无登录联调；联调正式登录时改为 `false` 并配置真实密钥

页面目录约定：`pages/index`、`lottery`、`points`、`redeem`、`profile` 等。

---

## 5. 一键起全套（可选）

需要接近生产拓扑时：

```bash
cd deploy
docker compose --env-file ../.env up -d --build
```

将启动：MySQL、Redis、admin-api（8081）、app-api（8082）、Nginx（80，静态管理端 + 反代）。

访问：

- 管理端：`http://localhost/`
- 管理 API：`http://localhost/admin/api/`
- 小程序 API：`http://localhost/app/api/`

---

## 6. 常见问题

| 现象 | 处理 |
|------|------|
| MySQL 连不上 | 等 healthcheck 通过；检查端口占用与 `.env` 密码 |
| 库是空的 | 确认是否误删卷后又未挂载 `schema.sql`；或手动 `source sql/schema.sql` |
| CORS 报错 | 把前端 origin 写入 `CORS_ALLOWED_ORIGINS` 并重启 admin-api |
| 小程序真机访问不到本机 API | 用局域网 IP，并关闭仅监听 127.0.0.1 的限制 |
| Redis 需要密码 | `.env` 设置 `REDIS_PASSWORD`，与 API 配置一致 |

更多上线项见 [PRODUCTION_CHECKLIST.md](./PRODUCTION_CHECKLIST.md)。
