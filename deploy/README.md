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

```bash
# 在仓库根目录准备 .env
cp ../.env.example ../.env   # 若在 deploy/ 下执行，路径按实际调整
cd deploy
cp ../.env.example ../.env

# 仅基础设施（日常开发推荐）
docker compose --env-file ../.env up -d mysql redis

# 全套（构建镜像较慢）
docker compose --env-file ../.env up -d --build
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
- 生产务必 HTTPS、备案、安全组仅开 80/443；DB/Redis 不对公网
- `schema.sql` 只在 MySQL **数据卷首次初始化**时执行；重建库需 `docker compose down -v`（会清空数据）
- 修改 Nginx 后重建 `nginx` 服务或挂载热更（按部署方式）

## 相关

- 上线清单：[../docs/PRODUCTION_CHECKLIST.md](../docs/PRODUCTION_CHECKLIST.md)
- 后端：[../services/README.md](../services/README.md)
