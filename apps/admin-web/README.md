# admin-web

Start Nine Plus **管理后台**：Vue 3 + Vite + Element Plus + Pinia + Vue Router。

对接后端：`snp-admin-api`（默认 `http://127.0.0.1:8081`，路径前缀 `/admin/api`）。

## 功能

- 登录 / JWT
- Banner、内容块（CMS）
- App 用户列表、积分调账与流水
- 抽奖活动与奖品
- 兑换商品与订单履约/取消

## 开发

```bash
# 建议 Node 20（仓库根目录 .nvmrc）
cd apps/admin-web
npm install
npm run dev
```

- 本地地址：http://localhost:5173  
- Vite 已将 `/admin/api` 代理到 `127.0.0.1:8081`  
- 默认账号：`admin` / `admin123`（由 admin-api 启动时创建）

```bash
npm run build    # 产物 dist/
npm run preview  # 预览构建结果
npm run lint     # ESLint（若已安装依赖）
```

## 目录

```text
src/
  api/        # 按模块封装请求
  layouts/    # 后台布局
  router/
  stores/
  utils/      # axios 封装
  views/      # 页面
```

## 约束

- 请求统一走 `src/utils/request.js`，成功码为 `code === 200`
- Token 存 `localStorage.admin_token`，禁止把密钥写进前端代码
- UI 文案与后台字段保持中文产品用语一致；接口字段用 camelCase（与后端 Jackson 一致）
- 格式化遵循仓库根目录 `.prettierrc.json` 与 `.editorconfig`

## 相关

- 后端：[../../services/README.md](../../services/README.md)
- 本地联调：[../../docs/LOCAL_DEV.md](../../docs/LOCAL_DEV.md)
- 协作约定：[../../CONTRIBUTING.md](../../CONTRIBUTING.md)
