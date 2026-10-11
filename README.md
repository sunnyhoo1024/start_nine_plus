# Start Nine Plus

配置驱动的微信小程序内容/活动平台，内置积分、抽奖与兑换能力。后台可配置 Banner、内容块、活动与奖品；C 端小程序完成登录、积分流水、抽奖与兑换履约。

仓库：[https://github.com/sunnyhoo1024/start_nine_plus](https://github.com/sunnyhoo1024/start_nine_plus)

> **安全提示**：仓库中的 `.env` / `.env.stg` / `.env.prod` 仅放占位配置，供本机与流水线按环境选用。**真实密钥**请在服务器或 CI 密钥管理中覆盖，勿把生产 Secret 明文长期写进 Git。`application-prod.yml` 仍建议本地生成且已在 `.gitignore` 中。

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
│   └── miniapp/            # uni-app（Vue3，编译为微信小程序）
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
    ├── PRODUCTION_CHECKLIST.md
    └── GITHUB_PUSH.md
```

---

## 技术栈

| 层级   | 技术                                                 |
| ------ | ---------------------------------------------------- |
| 后端   | Java 17、Spring Boot 3.2、MyBatis-Plus、JJWT、Hutool |
| 数据   | MySQL 8、Redis 7                                     |
| 管理端 | Node 20、Vue + Vite（admin-web）                     |
| C 端   | uni-app Vue3（编译微信小程序 mp-weixin）             |
| 部署   | Docker Compose、Nginx 反代、可选 OSS/CDN             |

---

## 快速开始

### 1. 环境变量

仓库已包含按环境划分的文件（可直接改）：

| 文件        | 用途                                |
| ----------- | ----------------------------------- |
| `.env`      | 本地开发（`WECHAT_MOCK=true` 即可） |
| `.env.stg`  | 预发 / 体验                         |
| `.env.prod` | 生产                                |

```bash
# 按需编辑对应文件中的密码、JWT、微信、CORS 等
```

### 2. 启动 MySQL / Redis

需已安装并**正在运行** [Docker Desktop](https://www.docker.com/products/docker-desktop/)（或 OrbStack）。若终端提示 `command not found: docker`，先安装 Docker，再用下面命令：

```bash
docker --version   # 应能输出版本号
cd deploy
docker compose --env-file ../.env up -d mysql redis
```

说明见 [docs/LOCAL_DEV.md](docs/LOCAL_DEV.md)。

### 3. 启动 API（宿主机）

`mvn` 不会自动读仓库根目录的 `.env`；本地默认已与 `.env` 对齐（用户 `snp`）。若你改过密码，先导出环境变量再启动：

```bash
set -a && source .env && set +a
cd services
mvn -pl snp-admin-api -am spring-boot:run
# 另开终端同样 source .env 后：
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

首次启动 **admin-api** 时，`AdminBootstrap` 会自动创建超级管理员（**上线后请立即修改密码**）：

| 端       | 账号    | 密码       |
| -------- | ------- | ---------- |
| 管理后台 | `admin` | `admin123` |

小程序用户通过微信登录创建，无固定账号；本地 `WECHAT_MOCK=true` 时可用 Mock 登录联调。

---

## 文档

### 子项目 README

- [apps/admin-web](apps/admin-web/README.md)
- [apps/miniapp](apps/miniapp/README.md)
- [services](services/README.md) · [约束](services/CONSTRAINTS.md)
- [deploy](deploy/README.md)
- [sql](sql/README.md)
- [docs](docs/README.md)

### 协作与工程约束

- [CONTRIBUTING.md](CONTRIBUTING.md) — 分支、提交、编码约定
- `.editorconfig` / `.gitattributes` / `.nvmrc` / `.prettierrc.json`

### 运维与联调

- [本地开发](docs/LOCAL_DEV.md)
- [宝塔方案 B 部署](docs/DEPLOY_BAOTA.md)
- [生产上线检查清单](docs/PRODUCTION_CHECKLIST.md)
- [推送到 GitHub](docs/GITHUB_PUSH.md)

---

## License

私有项目，未声明开源协议前请勿擅自分发。
