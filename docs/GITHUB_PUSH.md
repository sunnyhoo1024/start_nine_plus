# 推送到 GitHub（需本机已登录 GitHub）

仓库目标：https://github.com/sunnyhoo1024/start_nine_plus

## 方式一：GitHub CLI（推荐）

```bash
# 安装：https://cli.github.com/
gh auth login
cd /Users/hufu/Documents/gitHubProject/start_nine_plus
gh repo create sunnyhoo1024/start_nine_plus --public --source=. --remote=origin --push
```

若仓库已存在：

```bash
git remote add origin git@github.com:sunnyhoo1024/start_nine_plus.git
# 或
git remote add origin https://github.com/sunnyhoo1024/start_nine_plus.git
git push -u origin main
```

## 方式二：网页创建 + 推送

1. 打开 https://github.com/new ，Repository name 填 `start_nine_plus`，不要勾选 README
2. 本地执行：

```bash
cd /Users/hufu/Documents/gitHubProject/start_nine_plus
git remote add origin https://github.com/sunnyhoo1024/start_nine_plus.git
git push -u origin main
```

## SSH 公钥

若 `Permission denied (publickey)`，将本机公钥加到 GitHub → Settings → SSH keys：

```bash
cat ~/.ssh/id_ed25519.pub
# 或
cat ~/.ssh/id_rsa.pub
```
