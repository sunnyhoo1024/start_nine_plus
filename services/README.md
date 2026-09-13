# services

Start Nine Plus **后端**：Maven 多模块，Java 17 + Spring Boot 3.2 + MyBatis-Plus + Redis + JWT。

## 模块

| 模块 | 说明 | 端口 / 前缀 |
|------|------|-------------|
| `snp-common` | `ApiResponse` / `ErrorCode`、异常、JWT 工具、枚举 | — |
| `snp-service` | 实体 / Mapper / 领域服务、AdminBootstrap | — |
| `snp-admin-api` | 管理端 REST + Security | **8081** `/admin/api` |
| `snp-app-api` | 小程序 REST + 微信登录 | **8082** `/app/api` |

不做独立 BFF 进程：`app-api` 承担聚合与字段裁剪。

## 本地运行

```bash
# 先起 MySQL / Redis，见 ../../docs/LOCAL_DEV.md
cd services
mvn -pl snp-admin-api -am spring-boot:run
mvn -pl snp-app-api -am spring-boot:run
```

打包：

```bash
mvn -DskipTests package
```

环境变量见仓库根目录 `.env` / `.env.stg` / `.env.prod`（数据源、Redis、JWT、微信、CORS）。

默认管理员由 `AdminBootstrap` 创建：`admin` / `admin123`（上线务必改密）。

## 关键业务约束

1. **积分**：只通过 `PointsService.changePoints` 记账；禁止直接改 `points_account.balance` 凑数  
2. **抽奖 / 兑换**：事务内扣积分 + 扣库存；取消兑换按实现退回积分  
3. **CMS / 活动**：`status` + 时间窗控制上线；C 端不返回奖品权重等敏感字段  
4. **密钥**：`WECHAT_SECRET`、JWT、DB 密码只放环境变量 / 本地 yml，不入库  
5. **分层**：管理能力进 `admin-api`；C 端只调 `app-api`

## 配置约定

- 开发：`application.yml` + 环境变量；可选本地 `application-local.yml`（已 gitignore）
- 生产示例：各 API 模块下 `application-prod.yml.example`
- Java 缩进 4 空格（见根目录 `.editorconfig`）

## Docker

镜像构建见仓库 `deploy/Dockerfile.admin-api`、`deploy/Dockerfile.app-api`（context 为 `services/`）。

## 相关

- 库表：[../sql/README.md](../sql/README.md)
- 部署：[../deploy/README.md](../deploy/README.md)
- 协作：[../CONTRIBUTING.md](../CONTRIBUTING.md)
