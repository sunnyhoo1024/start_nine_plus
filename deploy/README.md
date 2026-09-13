# deploy

生产 / 接近生产拓扑的 **Docker Compose + Nginx** 配置。

## 包含服务

| 服务 | 说明 |
|------|------|
| `mysql` | MySQL 8，挂载 `../sql/schema.sql` 初始化 |
| `redis` | Redis 7 |
| `admin-api` | 管理端 API → 8081 |
| `app-api` | 小程序 API → 8082 |
| `nginx` | 管理端静态资源 + 反代 `/admin/api`、`/app/api` → 80 |

## 使用

多环境文件（根目录，**已纳入 Git**；流水线用 `--env-file` 选择）：

| 文件 | 用途 |
|------|------|
| `.env` | 本地开发 |
| `.env.stg` | 预发 / 体验机 |
| `.env.prod` | 生产 |

```bash
cd deploy
# 仅基础设施（日常开发推荐）
docker compose --env-file ../.env up -d mysql redis

# 预发 / 宝塔方案 B
docker compose --env-file ../.env.stg up -d --build mysql redis admin-api app-api

# 助手脚本
./scripts/dc.sh stg up -d --build mysql redis admin-api app-api
./scripts/dc.sh prod up -d --build
```

访问：

- 管理端：http://localhost/
- 管理 API：http://localhost/admin/api/
- 小程序 API：http://localhost/app/api/

## 文件

- `docker-compose.yml` — 编排
- `Dockerfile.admin-api` / `Dockerfile.app-api` — 多阶段 Maven 构建
- `Dockerfile.admin-web` — Node 构建管理端 + Nginx
- `nginx/nginx.conf` — 路由与（可选）管理端 IP 限制注释示例

## 约束

- **禁止**把真实生产密钥写进 Compose 或镜像；用 `env_file` / 编排平台密钥
- 生产务必 HTTPS、备案、安全组仅开 80/443；DB/Redis/API 默认只绑定 `127.0.0.1`
- **宝塔方案 B**（不起 Compose nginx，用宝塔反代）：见 [../docs/DEPLOY_BAOTA.md](../docs/DEPLOY_BAOTA.md)
  ```bash
  ./scripts/dc.sh stg up -d --build mysql redis admin-api app-api
  # 或：docker compose --env-file ../.env.stg up -d --build mysql redis admin-api app-api
  ```
- `schema.sql` 只在 MySQL **数据卷首次初始化**时执行；重建库需 `docker compose down -v`（会清空数据）
- 修改 Nginx 后重建 `nginx` 服务或挂载热更（按部署方式）

## 相关

- 上线清单：[../docs/PRODUCTION_CHECKLIST.md](../docs/PRODUCTION_CHECKLIST.md)
- 后端：[../services/README.md](../services/README.md)
