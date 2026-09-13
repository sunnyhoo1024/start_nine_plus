# 生产上线检查清单（Start Nine Plus）

面向微信小程序「配置驱动内容/活动 + 积分抽奖兑换」正式发布前的核对项。按顺序勾选，缺一项不建议提审。

---

## 1. 微信小程序账号与认证

- [ ] 已注册并完成主体认证（企业/个体工商户按业务选择）
- [ ] 小程序类目与业务一致（积分、抽奖、兑换相关类目已开通）
- [ ] 已开通所需接口权限（登录 `code2Session`、用户信息等）
- [ ] 生产环境关闭 Mock：`WECHAT_MOCK=false`
- [ ] `WECHAT_APP_ID` / `WECHAT_SECRET` 使用正式账号密钥，且仅保存在服务器环境变量中
- [ ] 管理员账号与开发者权限已分配，避免单人持有全部密钥

---

## 2. ICP 备案

- [ ] 服务器所在地域与备案主体一致（或按云厂商要求完成接入）
- [ ] 域名已完成 ICP 备案（小程序业务域名通常要求已备案）
- [ ] 备案号可在站点页脚或政策页展示（如监管要求）
- [ ] 公安备案（如当地要求）已评估并完成

---

## 3. 域名与 HTTPS

- [ ] 已购买并解析生产域名（例如 `api.example.com`、`admin.example.com`）
- [ ] 全站 HTTPS（证书有效、自动续期，如 Let’s Encrypt / 云厂商证书）
- [ ] 管理端与 C 端 API 域名策略明确：
  - 推荐：同一域名不同路径（Nginx：`/admin/api`、`/app/api`），或独立子域名
- [ ] 强制 HTTP → HTTPS 跳转
- [ ] HSTS（可选，上线稳定后再开）

---

## 4. 服务器 / MySQL / Redis / OSS / CDN

### 服务器

- [ ] 规格满足峰值（建议至少 2C4G 起，按 DAU 评估）
- [ ] 系统时区 `Asia/Shanghai`
- [ ] Docker / Docker Compose 已安装，或等价 K8s/云托管方案
- [ ] 磁盘独立挂载数据卷（MySQL、日志、上传临时目录）
- [ ] 定期备份策略（见下）

### MySQL

- [ ] 生产库名、账号与 `.env.prod` 一致
- [ ] root 密码强随机，应用仅使用业务账号（最小权限）
- [ ] 已执行 `sql/schema.sql`（或容器首次初始化挂载）
- [ ] 开启自动备份（每日全量 + binlog），并验证过恢复演练
- [ ] 连接数、慢查询监控已开启

### Redis

- [ ] 生产设置 `REDIS_PASSWORD`
- [ ] 仅内网访问，不对外暴露 6379
- [ ] 持久化策略已确认（AOF/RDB 按业务可接受丢数据窗口选择）

### 对象存储 OSS

- [ ] `OSS_ENDPOINT` / `OSS_ACCESS_KEY_*` / `OSS_BUCKET` / `OSS_PUBLIC_BASE_URL` 已配置
- [ ] Bucket 公私权限符合图片/素材访问需求（推荐私有 + 签名 URL，或公共读仅媒体目录）
- [ ] 跨域 CORS 已按管理端与小程序域名配置

### CDN

- [ ] 静态资源（Banner、奖品图）走 CDN
- [ ] 回源 HTTPS，缓存规则与刷新策略已文档化

---

## 5. 安全组 / 网络

- [ ] 公网仅开放 `80` / `443`（以及运维 SSH，限制来源 IP）
- [ ] **不要**对公网开放 MySQL `3306`、Redis `6379`、直连 API `8081`/`8082`（生产建议仅内网）
- [ ] 管理端后台建议额外限制：仅办公网/VPN IP 可访问（Nginx `allow`/`deny` 或 WAF）
- [ ] 云 WAF / 防 CC（可选但推荐）

---

## 6. 环境变量对照表

生产使用仓库中的 `.env.prod`（可提交占位配置；**上线前在服务器改成真实密钥**，或由 CI 注入覆盖）：

```bash
# 编辑 .env.prod 后
docker compose --env-file ../.env.prod up -d --build
# 或：./scripts/dc.sh prod up -d --build
```

| 变量 | 说明 | 生产要求 |
|------|------|----------|
| `MYSQL_HOST` | MySQL 主机 | 内网地址或 Compose 服务名 `mysql` |
| `MYSQL_PORT` | 端口 | 默认 `3306` |
| `MYSQL_DATABASE` | 库名 | `start_nine_plus` |
| `MYSQL_USER` | 业务账号 | 非 root |
| `MYSQL_PASSWORD` | 业务密码 | 强密码 |
| `MYSQL_ROOT_PASSWORD` | root 密码（Compose 初始化用） | 强密码，仅运维掌握 |
| `REDIS_HOST` | Redis 主机 | 内网 / `redis` |
| `REDIS_PORT` | 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | **必须设置** |
| `ADMIN_JWT_SECRET` | 管理端 JWT（compose 注入为 `JWT_SECRET`） | ≥32 位随机串 |
| `APP_JWT_SECRET` | C 端 JWT（compose 注入为 `JWT_SECRET`） | ≥32 位随机串，与管理端不同 |
| `JWT_SECRET` | 本机 `mvn` 运行时使用 | 可与 `ADMIN_JWT_SECRET` 相同 |
| `WECHAT_APP_ID` | 小程序 AppId | 正式号（原文档名 WECHAT_MINI_APPID 已废弃） |
| `WECHAT_SECRET` | 小程序 Secret | 正式号 |
| `WECHAT_MOCK` | 跳过真实微信登录 | **必须 `false`** |
| `OSS_ENDPOINT` | OSS Endpoint | 生产填齐 |
| `OSS_ACCESS_KEY_ID` | AK | 最小权限子账号 |
| `OSS_ACCESS_KEY_SECRET` | SK | 保密 |
| `OSS_BUCKET` | Bucket 名 | 生产桶 |
| `OSS_PUBLIC_BASE_URL` | 公网访问前缀/CDN 域名 | HTTPS |
| `CORS_ORIGINS` | 管理端 CORS | 仅正式管理端域名（如 `https://admin.example.com`） |

API 模块可参考：

- `services/snp-admin-api/src/main/resources/application-prod.yml.example`
- `services/snp-app-api/src/main/resources/application-prod.yml.example`

复制为 `application-prod.yml` 后由 `SPRING_PROFILES_ACTIVE=prod` 加载（该文件已被 `.gitignore` 忽略）。

---

## 7. 微信合法域名配置

微信公众平台 → 开发 → 开发管理 → 开发设置：

- [ ] **request 合法域名**：配置生产 API 域名（如 `https://api.example.com`），须 HTTPS、已备案
- [ ] **uploadFile 合法域名**（若小程序上传）：与 OSS 或上传网关域名一致
- [ ] **downloadFile 合法域名**：CDN / OSS 公网域名
- [ ] **业务域名**（若使用 web-view）：已配置并完成校验文件
- [ ] 开发版/体验版可临时用「不校验合法域名」，**正式版必须校验**

---

## 8. 隐私政策与用户协议

- [ ] 小程序内可访问的《用户协议》《隐私政策》页面已上线
- [ ] 收集项说明完整：openid、昵称头像（如有）、积分流水、兑换收货信息等
- [ ] 首次登录/授权有明确同意路径
- [ ] 微信「用户隐私保护指引」已填写并与实际一致
- [ ] 抽奖/兑换规则、概率说明（如平台要求）已公示

---

## 9. 发布路径：体验版 → 审核 → 发布

1. [ ] 后端生产环境健康检查通过（登录、积分、抽奖、兑换全链路）
2. [ ] 管理端配置好 Banner/内容块/活动/奖品/库存
3. [ ] 微信开发者工具上传代码，填写版本号与备注
4. [ ] 设为**体验版**，邀请内部账号完整回归
5. [ ] 提交**审核**（类目、隐私、测试账号按审核要求填写）
6. [ ] 审核通过后**发布**正式版
7. [ ] 发布后监控错误日志、接口成功率、积分异常告警
8. [ ] 准备回滚方案（上一版小程序码 + 后端镜像 tag）

---

## 10. 管理端 IP 限制（强烈建议）

管理后台暴露面远高于小程序 C 端，建议：

- [ ] Nginx 对 `/`（管理 SPA）与 `/admin/api/` 做 IP 白名单（办公网 / VPN）
- [ ] 或管理端使用独立内网域名 + 零信任/VPN，不对公网开放
- [ ] 强密码 + 定期更换默认 `admin` 账号密码（种子账号见 README）
- [ ] 开启操作日志审计（表 `admin_op_log`）并定期抽查
- [ ] JWT 过期时间不宜过长；异地登录可后续增强

示例（`deploy/nginx/nginx.conf` 中已预留注释）：

```nginx
# allow 203.0.113.0/24;   # 办公出口
# deny all;
```

---

## 11. 部署快速核对

```bash
cd deploy
# 确认 ../.env.prod 已改为生产值
docker compose --env-file ../.env.prod up -d --build
```

- [ ] `http(s)://你的域名/` 可打开管理端
- [ ] `/admin/api/`、`/app/api/` 经 Nginx 反代正常
- [ ] MySQL 初始化数据（含管理员种子）存在
- [ ] 生产真实密钥已在服务器 / CI 配置；未把生产 Secret 明文依赖在公开仓库默认值上
- [ ] 若使用本地 `application-prod.yml`，确认 **未** 误提交

---

## 完成标准

全部关键项勾选，且体验版回归通过后，方可提交微信审核与正式发布。
