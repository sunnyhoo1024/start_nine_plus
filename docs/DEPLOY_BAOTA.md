# 阿里云轻量 + 宝塔：方案 B 部署

**方案 B**：Docker Compose 只跑 `mysql` / `redis` / `admin-api` / `app-api`；**不启** Compose 里的 `nginx`。  
公网入口用 **宝塔 Nginx** 反代，有域名后再在宝塔申请 HTTPS。

适合：已装「宝塔 Linux 面板」镜像、人数少、先打通再备案。

---

## 0. 架构

```text
手机 / 浏览器
    │  http://公网IP   （有域名备案后改 https://域名）
    ▼
宝塔 Nginx :80/:443
    ├── /app/api/*    → 127.0.0.1:8082   (app-api)
    ├── /admin/api/*  → 127.0.0.1:8081   (admin-api)
    └── /             → 管理端静态文件（admin-web build）
    ▲
仅本机回环
Docker: mysql / redis / admin-api / app-api
```

安全组 / 轻量防火墙：**只放行** `22`、`8888`（面板，建议限 IP）、`80`，有证书后再开 `443`。  
**不要**对公网放行 `3306` / `6379` / `8081` / `8082`。

---

## 1. 宝塔准备

1. 用控制台邮件里的地址登录宝塔（端口多为 `8888`），立刻改面板密码；有条件则改面板端口并绑定你的办公 IP。
2. **软件商店**安装：
   - **Nginx**（必装，反代用）
   - **Docker** + **Docker Compose**（若商店无 Compose 插件，SSH 里确认 `docker compose version`）
3. 若宝塔还装了 **MySQL / Redis**：与 Compose 抢端口时，**停用宝塔的 MySQL/Redis**，统一用 Compose（与仓库一致）。或反过来不用 Compose 数据库——本指南按 Compose 数据库写。
4. 确认本机：

```bash
docker -v
docker compose version
```

---

## 2. 拉取代码并配置环境文件

预发 / 体验机用 **`.env.stg`**（生产用 `.env.prod`）：

```bash
mkdir -p /www/wwwroot && cd /www/wwwroot
git clone https://github.com/sunnyhoo1024/start_nine_plus.git
cd start_nine_plus
vi .env.stg   # 改成服务器真实密码 / JWT / CORS（仓库里是占位值）
```

`.env.stg` 最少改这些：

| 变量 | 说明 |
|------|------|
| `MYSQL_*` / `MYSQL_ROOT_PASSWORD` | 强密码 |
| `REDIS_PASSWORD` | 建议设置 |
| `ADMIN_JWT_SECRET` / `APP_JWT_SECRET` | 各 ≥32 位随机串（compose 分别注入为各 API 的 `JWT_SECRET`） |
| `WECHAT_APP_ID` / `WECHAT_SECRET` | 真登录时必填；可先空 + `WECHAT_MOCK=true` |
| `CORS_ORIGINS` | 管理端访问来源，例如追加 `http://你的公网IP`；有域名后改 `https://域名` |

Compose 容器内会把 `MYSQL_HOST`/`REDIS_HOST` 覆盖为服务名，**.env.stg 里写 `127.0.0.1` 即可**（给宿主机工具连库时用）。

---

## 3. 启动四个容器（不要起 nginx）

```bash
cd /www/wwwroot/start_nine_plus/deploy
docker compose --env-file ../.env.stg up -d --build mysql redis admin-api app-api
# 或：../scripts/dc.sh stg up -d --build mysql redis admin-api app-api
docker compose ps
docker compose logs -f --tail=100 app-api
```

期望：`snp-mysql` / `snp-redis` / `snp-admin-api` / `snp-app-api` 为 `Up`（mysql/redis 为 healthy）。

本机自测（在服务器上）：

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8082/app/api/lottery/activities
# 未登录类接口按实际路径测；至少应不是 connection refused
```

API 已绑定 `127.0.0.1`（见 `docker-compose.yml`），外网不能直连 8082，必须走宝塔 Nginx。

---

## 4. 宝塔：建站 + 反代

### 4.1 添加站点

1. 宝塔 → **网站** → **添加站点**
2. 域名：
   - 暂无域名：填 **公网 IP**（有的面板要求填域名，可先填 `api.临时` 再改，或填 IP）
   - 有域名后：改成你的域名并解析 A 记录到该服务器
3. 根目录例如：`/www/wwwroot/snp-web`（放管理端静态文件）
4. PHP 选「纯静态」即可

### 4.2 反向代理（两个）

在该站点 → **反向代理** → **添加反代**：

**小程序 API**

| 项 | 值 |
|----|-----|
| 代理名称 | `app-api` |
| 目标 URL | `http://127.0.0.1:8082` |
| 发送域名 | `$host` |
| 代理目录 | `/app/api` |

**管理 API**

| 项 | 值 |
|----|-----|
| 代理名称 | `admin-api` |
| 目标 URL | `http://127.0.0.1:8081` |
| 发送域名 | `$host` |
| 代理目录 | `/admin/api` |

若宝塔生成的配置把路径剥掉导致 404，改为「自定义配置」保证类似：

```nginx
location /app/api/ {
    proxy_pass http://127.0.0.1:8082/app/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

location /admin/api/ {
    proxy_pass http://127.0.0.1:8081/admin/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

保存后重载 Nginx。

### 4.3 验证

电脑浏览器：

```text
http://公网IP/app/api/lottery/activities
```

能返回 JSON（或业务码包装）即反代成功。

---

## 5. 部署管理端静态页（可选，建议做）

在**本机或服务器**（需 Node 20）：

```bash
cd /www/wwwroot/start_nine_plus/apps/admin-web
npm ci
npm run build
# 产物一般在 dist/
cp -r dist/* /www/wwwroot/snp-web/
```

管理端请求基路径若为 `/admin/api`，与反代一致即可。  
浏览器打开：`http://公网IP/` ，默认账号见根 README（**立刻改密码**）。

若本地 `vite` 配了 `base`，以 `apps/admin-web` 现有配置为准。

---

## 6. 小程序指向这台机

改 `apps/miniapp/src/App.vue`：

```js
baseUrl: 'http://你的公网IP'   // 无尾斜杠；代码会拼 /app/api
```

- 微信开发者工具：勾选 **不校验合法域名**、**不校验 TLS**（若仅 HTTP）。
- **真机体验版**：微信要求 HTTPS + 已备案域名，IP/HTTP 通常不行；人少可先用开发者预览 + 不校验，或等域名备案完成。

重新编译：

```bash
cd apps/miniapp && npm run dev:mp-weixin
```

---

## 7. 有域名之后（同一方案 B 升级）

1. 域名 A 记录 → 服务器 IP  
2. 备案通过  
3. 宝塔站点绑定域名 → **SSL** → Let’s Encrypt  
4. 强制 HTTPS  
5. `.env.stg` / `.env.prod` 里 `CORS_ORIGINS=https://你的域名`  
6. 小程序 `baseUrl: 'https://你的域名'`  
7. 公众平台配置 **request 合法域名**  
8. `WECHAT_MOCK=false`，填正式 AppId/Secret，重启 `app-api`：

```bash
cd /www/wwwroot/start_nine_plus/deploy
docker compose --env-file ../.env.prod up -d app-api
```

---

## 8. 日常运维命令

```bash
cd /www/wwwroot/start_nine_plus/deploy

# 看状态 / 日志
docker compose ps
docker compose logs -f --tail=200 app-api

# 改代码后更新 API
git pull
docker compose --env-file ../.env.stg up -d --build admin-api app-api
# 或生产：--env-file ../.env.prod

# 只重启
docker compose restart app-api admin-api
```

备份 MySQL（示例）：

```bash
docker exec snp-mysql mysqldump -usnp -p'你的密码' start_nine_plus > /www/backup/snp-$(date +%F).sql
```

---

## 9. 常见问题

| 现象 | 处理 |
|------|------|
| 外网 502 | 容器没起来；或反代指错端口；`docker compose ps` / `curl 127.0.0.1:8082` |
| 外网能访问 8082 | 安全组误开了端口；应只开 80/443，API 仅 127.0.0.1 |
| 与宝塔 MySQL 冲突 | 停宝塔 MySQL，或改 Compose 映射端口 |
| 管理端跨域 | 把前端访问源写入 `CORS_ORIGINS` 后重启 `admin-api` |
| 首次库表为空 | `schema.sql` 只在 MySQL **数据卷首次创建**时执行；需要空库重建才 `docker compose down -v`（会丢数据） |

---

## 相关

- 总清单：[PRODUCTION_CHECKLIST.md](./PRODUCTION_CHECKLIST.md)
- Compose 说明：[../deploy/README.md](../deploy/README.md)
