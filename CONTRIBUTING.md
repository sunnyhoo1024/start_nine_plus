# 协作与编码约定

## 仓库结构

| 路径 | 说明 |
|------|------|
| `apps/admin-web` | 管理后台（Vue3） |
| `apps/miniapp` | uni-app 小程序（编译微信） |
| `services` | Spring Boot 多模块后端 |
| `deploy` | Docker / Nginx |
| `sql` | 库表脚本 |
| `docs` | 开发与上线文档 |

各子目录有独立 `README.md`，细节以子 README 为准。

## 分支

- 默认分支：`main`
- 功能开发：`feature/<简短说明>`，合并回 `main`
- 不要把密钥、`.env`、`application-prod.yml` 提交进 Git

## 提交信息

建议：`type(scope): summary`

- `feat` / `fix` / `docs` / `refactor` / `chore` / `build`
- 例：`feat(points): ledger adjust with audit remark`

## 编码

- 根目录 `.editorconfig`：缩进、换行、编码
- 前端：Prettier（`.prettierrc.json`）；`admin-web` 建议配合 ESLint
- 后端：Java 17、4 空格缩进；统一响应 `{ code, message, data }`（成功 `code=200`）；业务错误用 `BizException` + `ErrorCode`
- 积分变动必须走账本 `PointsService.changePoints`，禁止直接改余额凑数
- 小程序正式环境 `baseUrl` 必须是 HTTPS 合法域名；密钥只放服务端

## 本地启动

见根目录 [README.md](./README.md) 与 [docs/LOCAL_DEV.md](./docs/LOCAL_DEV.md)。

## Review 自检

- [ ] 无密钥 / 生产配置入库
- [ ] 接口路径与 admin / app 分层一致（`/admin/api` vs `/app/api`）
- [ ] 积分/抽奖/兑换相关改动带事务与库存/余额校验
- [ ] 文档与 README 若行为变化已同步
