#!/bin/bash
# 完成 Docker Desktop 安装配置，并把 docker 加到 PATH。
# 请在本机「终端.app」执行（需要管理员密码）：
#   bash scripts/setup-docker-desktop.sh

set -euo pipefail

APP="/Applications/Docker.app"
BIN="$APP/Contents/Resources/bin"

if [[ ! -d "$APP" ]]; then
  echo "未找到 $APP"
  echo "请先安装 Docker Desktop: https://www.docker.com/products/docker-desktop/"
  exit 1
fi

echo "==> 1) 以管理员完成 Docker Desktop install config（写入 /usr/local/bin 与 socket）"
# Docker Desktop 4.x 要求 --user（当前登录用户名，不是路径）
DOCKER_USER="${SUDO_USER:-${USER:-$(id -un)}}"
echo "    使用用户: $DOCKER_USER"
sudo "$APP/Contents/MacOS/install" config --user "$DOCKER_USER"

echo "==> 2) 确保 PATH（本会话 + ~/.zprofile）"
export PATH="$BIN:$PATH"
if ! grep -q 'Docker.app/Contents/Resources/bin' "${HOME}/.zprofile" 2>/dev/null; then
  echo 'export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"' >> "${HOME}/.zprofile"
  echo "已写入 ~/.zprofile"
fi
if ! grep -q 'Docker.app/Contents/Resources/bin' "${HOME}/.zshrc" 2>/dev/null; then
  echo 'export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"' >> "${HOME}/.zshrc"
  echo "已写入 ~/.zshrc"
fi

echo "==> 3) 启动 Docker Desktop"
open "$APP/Contents/MacOS/Docker Desktop.app" 2>/dev/null || open -a "Docker Desktop" 2>/dev/null || open "$APP" || true

echo "等待 Docker 引擎就绪（最多约 2 分钟）..."
for i in $(seq 1 24); do
  if docker info >/dev/null 2>&1; then
    echo "Docker 已就绪。"
    docker --version
    docker compose version
    echo
    echo "接下来："
    echo "  cd $(cd "$(dirname "$0")/.." && pwd)/deploy"
    echo "  docker compose --env-file ../.env up -d mysql redis"
    exit 0
  fi
  sleep 5
  echo "  ...仍在等待 ($i/24)"
done

echo "超时：请从启动台手动打开「Docker」，菜单栏鲸鱼图标变亮后再执行 compose。"
echo "临时可用完整路径："
echo "  export PATH=\"$BIN:\$PATH\""
exit 1
