# miniapp（uni-app）

Start Nine Plus **C 端**：uni-app Vue3 + Vite，目标平台 **微信小程序（mp-weixin）**。

对接后端：`snp-app-api`（路径前缀 `/app/api`）。`baseUrl` 由环境文件注入，见下。

## 环境与 baseUrl

仅使用本地和生产两套环境（均在 `apps/miniapp/`），没有 STG 环境：

| 文件              | 对应命令                               | 用途                            |
| ----------------- | -------------------------------------- | ------------------------------- |
| `.env`            | `npm run dev:mp-weixin` / `dev:h5`     | 本地：`http://127.0.0.1:8082`   |
| `.env.production` | `npm run build:mp-weixin` / `build:h5` | 生产：`https://www.starnine.cn` |

产物目录：

| 命令              | 打开目录               |
| ----------------- | ---------------------- |
| `dev:mp-weixin`   | `dist/dev/mp-weixin`   |
| `build:mp-weixin` | `dist/build/mp-weixin` |

变量：`VITE_API_BASE_URL`（不要尾斜杠）。由 `import.meta.env.VITE_API_BASE_URL` 注入。

请求会自动拼接 `/app/api`，生产接口为 `https://www.starnine.cn/app/api/*`。宝塔主站需配置该路径的反代；微信公众平台 request 合法域名填写 `https://www.starnine.cn`。当前 `http://api.starnine.cn/` 只有 HTTP，不用于小程序生产请求。

```bash
cd apps/miniapp
npm run dev:mp-weixin          # 本地 .env → dist/dev/mp-weixin
npm run build:mp-weixin        # 生产 .env.production → dist/build/mp-weixin
```

## 样式（Less）

使用 **Less** 管理主题与拟物样式：

| 文件                        | 作用                                        |
| --------------------------- | ------------------------------------------- |
| `src/styles/variables.less` | 颜色 / 圆角等设计变量                       |
| `src/styles/mixins.less`    | `.neu-convex` / `.neu-concave` / 按钮 mixin |
| `src/styles/neu.less`       | 全局 `page`、工具类、动画                   |

页面内写 `<style lang="less" scoped>`，并通过 Vite `additionalData` 自动注入 `variables.less`。依赖：`less`（devDependency）。

## 页面

软粘土 **Neumorphism**（底色 `#e0e0e0`，灵感 [neumorphism.io](https://neumorphism.io/#e0e0e0)）+ 快餐式热情点缀色 `#ff4d3a`。首页 Banner Ken Burns、抽奖转盘与彩纸庆祝、积分数字滚动等动效见各页面。

| 页面            | 说明                               |
| --------------- | ---------------------------------- |
| `pages/index`   | 首页聚合（Banner、介绍、积分入口） |
| `pages/lottery` | 抽奖                               |
| `pages/redeem`  | 积分兑换                           |
| `pages/points`  | 余额与流水                         |
| `pages/profile` | 登录 / 资料                        |

## 开发

```bash
cd apps/miniapp
npm install
npm run dev:mp-weixin
```

1. 用**微信开发者工具**打开对应产物目录（不要直接把源码根当运行目录）：本地 `dist/dev/mp-weixin`，生产 `dist/build/mp-weixin`
2. 在 `src/manifest.json` → `mp-weixin.appid` 填写小程序 AppID
3. 开发阶段勾选「不校验合法域名」
4. 修改 `.env` 或 `.env.production` 中的 `VITE_API_BASE_URL` 后重新编译；本地真机联调需通过 `.env.local` 改为电脑的局域网 IP（手机不能访问电脑的 `127.0.0.1`）

正式构建：

```bash
npm run build:mp-weixin
# 产物：dist/build/mp-weixin
```

本地后端请保持 `WECHAT_MOCK=true` 便于无 AppSecret 联调。

## 目录

```text
src/
  pages/          # 业务页面（Vue SFC）
  utils/request.js
  utils/auth.js   # uni.login → /app/api/auth/login
  manifest.json
  pages.json
  App.vue
```

## 约束

- **仅提交源码**；`dist/`、`unpackage/`、`node_modules/` 不入库
- 正式环境 `baseUrl` 必须是小程序后台配置的 **HTTPS request 合法域名**
- 微信 `AppSecret`、OSS Key 等**禁止**写入小程序包；只走服务端
- 业务请求统一经 `utils/request.js`；登录态 `app_token`
- 第一期以微信为主；其他 `dev:mp-*` 脚本为模板自带，未做适配前勿当生产入口
- 代码风格遵循仓库根目录 `.editorconfig` / Prettier

## 相关

- 后端 App API：[../../services/README.md](../../services/README.md)
- 上线清单：[../../docs/PRODUCTION_CHECKLIST.md](../../docs/PRODUCTION_CHECKLIST.md)
- 协作约定：[../../CONTRIBUTING.md](../../CONTRIBUTING.md)
