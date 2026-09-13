# sql

数据库脚本目录（MySQL 8，`utf8mb4`）。

## 文件

| 文件 | 说明 |
|------|------|
| `schema.sql` | 建库（可选）、业务表、示例 CMS 种子数据 |

表覆盖：后台 RBAC、App 用户、CMS、积分账户/流水、抽奖、兑换商品/订单。

**管理员账号不写死在 SQL 里**，由 `snp-admin-api` 启动时 `AdminBootstrap` 创建（`admin` / `admin123`）。

## 使用

Docker Compose 会把本文件挂到 MySQL 初始化目录（仅**空数据卷首次启动**执行）：

```bash
cd deploy
docker compose --env-file ../.env up -d mysql
```

手动导入：

```bash
mysql -h127.0.0.1 -usnp -p start_nine_plus < schema.sql
# 或 root 执行（脚本含 CREATE DATABASE）
```

## 约束

- 变更表结构请：
  1. 更新 `schema.sql`（新环境一致性）
  2. 已有环境用增量迁移脚本（后续可引入 Flyway/Liquibase；当前第一期以手工/SQL 为准）
- 积分相关表：`points_account.version` 用于乐观锁，勿随意删除
- 种子 Banner/Block 仅用于演示，生产数据由管理后台配置
- 不要把含生产数据的 dump 提交进仓库

## 相关

- 后端实体与服务：[../services/README.md](../services/README.md)
- 本地开发：[../docs/LOCAL_DEV.md](../docs/LOCAL_DEV.md)
