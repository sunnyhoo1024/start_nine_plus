# Start Nine Plus

配置驱动的微信小程序内容/活动平台，内置积分、抽奖与兑换能力。后台可配置 Banner、内容块、活动与奖品；C 端小程序完成登录、积分流水、抽奖与兑换履约。

仓库：[https://github.com/sunnyhoo1024/start_nine_plus](https://github.com/sunnyhoo1024/start_nine_plus)

> **安全提示**：密钥与生产配置不要提交到 Git。请使用根目录 `.env.example` 复制为 `.env`，以及各 API 模块的 `application-prod.yml.example` → `application-prod.yml`（二者均已在 `.gitignore` 中忽略）。

---

## 功能概览

- **配置驱动内容**：Banner、CMS Block 等可运营配置，支持上下线与时间窗
- **活动能力**：活动配置与参与记录
- **积分体系**：账户余额、流水账本
- **抽奖 / 兑换**：奖品库存、抽奖记录、兑换订单

---

## 模块结构

```text
start_nine_plus/
├── apps/
│   ├── admin-web/          # 管理端 Web（Vue/Vite）
│   └── miniapp/            # 微信小程序
├── services/               # Maven 多模块（父 POM）
│   ├── snp-common/         # 公共模型与工具
│   ├── snp-service/        # 领域服务与持久化
│   ├── snp-admin-api/      # 管理端 API :8081  /admin/api
│   └── snp-app-api/        # 小程序 API :8082  /app/api
├── sql/
│   └── schema.sql          # 库表与种子数据
├── deploy/
│   ├── docker-compose.yml
│   ├── Dockerfile.*
│   └── nginx/nginx.conf
└── docs/
    ├── LOCAL_DEV.md
    └── PRODUCTION_CHECKLIST.md
```

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3.2、MyBatis-Plus、JJWT、Hutool |
| 数据 | MySQL 8、Redis 7 |
| 管理端 | Node 20、Vue + Vite（admin-web） |
| C 端 | 微信小程序原生 |
| 部署 | Docker Compose、Nginx 反代、可选 OSS/CDN |

---

## 快速开始

### 1. 环境变量

```bash
cp .env.example .env
```

本地可保持 `WECHAT_MOCK=true`。

### 2. 启动 MySQL / Redis

```bash
cd deploy
docker compose --env-file ../.env up -d mysql redis
```

### 3. 启动 API（宿主机）

```bash
cd services
mvn -pl snp-admin-api -am spring-boot:run
mvn -pl snp-app-api -am spring-boot:run
```

### 4. 管理端

```bash
cd apps/admin-web
npm install && npm run dev
```

### 5. 小程序

用微信开发者工具打开 `apps/miniapp`，开发阶段可勾选不校验合法域名，API 指向 `http://127.0.0.1:8082/app/api`。

完整步骤见 [docs/LOCAL_DEV.md](docs/LOCAL_DEV.md)。

### 一键 Compose（含 Nginx）

```bash
cd deploy
docker compose --env-file ../.env up -d --build
```

- 管理端：http://localhost/
- 管理 API：http://localhost/admin/api/
- 小程序 API：http://localhost/app/api/

---

## 默认账号

初始化 SQL 会写入超级管理员（**上线后请立即修改密码**）：

| 端 | 账号 | 密码 |
|----|------|------|
| 管理后台 | `admin` | `admin123` |

小程序用户通过微信登录创建，无固定账号；本地 `WECHAT_MOCK=true` 时可用 Mock 登录联调。

---

## 文档

- [本地开发](docs/LOCAL_DEV.md)
- [生产上线检查清单](docs/PRODUCTION_CHECKLIST.md)

---

## License

私有项目，未声明开源协议前请勿擅自分发。
