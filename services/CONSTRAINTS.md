# services 约束摘要

完整约定见 [../CONTRIBUTING.md](../CONTRIBUTING.md) 与 [README.md](./README.md)。

## 模块依赖（禁止反向）

```text
snp-admin-api ──┐
                ├──► snp-service ──► snp-common
snp-app-api   ──┘
```

## 禁止入库

- `application-local.yml` / `application-prod.yml`（可用 `*.example`）
- 微信 Secret、JWT、数据库密码等明文
- `**/target/`

## 业务红线

- 积分只走账本服务，禁止直接改余额
- C 端接口不返回奖品权重、后台专用字段
- 管理端与小程序 API 前缀分离：`/admin/api` vs `/app/api`
