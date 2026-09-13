#!/bin/bash
# 修复 / 重装本机 Homebrew（macOS Intel → /usr/local）
# 请在「终端.app」或 iTerm 中手动执行：bash scripts/fix-homebrew.sh
# Cursor 沙箱无法输入 sudo 密码，也无法弹出 xcode-select 图形安装框。

set -euo pipefail

echo "==> 1) 检查 / 安装 Xcode Command Line Tools"
if ! xcrun --version >/dev/null 2>&1; then
  echo "Command Line Tools 缺失或损坏。"
  echo "将尝试弹出安装窗口；若无弹窗，请打开：系统设置 → 通用 → 软件更新"
  echo "或从 https://developer.apple.com/download/all/ 下载「Command Line Tools for Xcode」"
  if [[ -d /Library/Developer/CommandLineTools ]]; then
    echo "发现残缺目录，需要管理员权限删除后重装："
    echo "  sudo rm -rf /Library/Developer/CommandLineTools"
    sudo rm -rf /Library/Developer/CommandLineTools
  fi
  xcode-select --install || true
  echo
  echo "请在弹出窗口完成安装后，重新运行本脚本。"
  echo "安装完成自检：xcrun --version && clang --version"
  exit 1
fi

echo "CLT OK: $(xcrun --version 2>/dev/null || true)"

echo "==> 2) 备份并移除 2018 年旧版 Homebrew（若存在）"
if [[ -d /usr/local/Homebrew ]]; then
  TS=$(date +%Y%m%d%H%M%S)
  echo "移动旧 Homebrew 到 /usr/local/Homebrew.bak.$TS"
  sudo mv /usr/local/Homebrew "/usr/local/Homebrew.bak.$TS" || true
fi
# 旧 symlink
if [[ -L /usr/local/bin/brew ]]; then
  sudo rm -f /usr/local/bin/brew
fi

echo "==> 3) 安装官方最新 Homebrew（NONINTERACTIVE）"
export NONINTERACTIVE=1
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

echo "==> 4) 确保 brew 在 PATH（Intel Mac）"
if [[ -x /usr/local/bin/brew ]]; then
  eval "$(/usr/local/bin/brew shellenv)"
elif [[ -x /opt/homebrew/bin/brew ]]; then
  eval "$(/opt/homebrew/bin/brew shellenv)"
fi

grep -q 'brew shellenv' "${HOME}/.zprofile" 2>/dev/null || {
  if [[ -x /usr/local/bin/brew ]]; then
    echo 'eval "$(/usr/local/bin/brew shellenv)"' >> "${HOME}/.zprofile"
  elif [[ -x /opt/homebrew/bin/brew ]]; then
    echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> "${HOME}/.zprofile"
  fi
}

echo "==> 5) 验证"
brew --version
brew doctor || true

echo
echo "完成。可选下一步："
echo "  brew install --cask docker          # Docker Desktop"
echo "  # 或 brew install --cask orbstack"
echo "然后：cd deploy && docker compose --env-file ../.env up -d mysql redis"
