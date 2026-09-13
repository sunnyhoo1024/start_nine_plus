#!/bin/bash
# 修复 Homebrew 目录权限，并安装 JDK 17 + Maven
# 请在系统「终端.app」执行（需输入密码），且同一时间只跑一份：
#   bash scripts/install-jdk-maven.sh
#
# 若看到 “already locked … Gave up after waiting 180 seconds”：
#   说明另有 brew 正在下载。先在那个终端 Ctrl+C，或等它结束，再重跑本脚本。

set -euo pipefail

USER_NAME="$(id -un)"
CACHE_DL="$HOME/Library/Caches/Homebrew/downloads"
CURLRC="$(mktemp -t brew-curlrc)"
trap 'rm -f "$CURLRC"' EXIT

echo "==> 0) 检查是否已有 brew install 在跑"
if pgrep -f 'brew\.rb install' >/dev/null 2>&1; then
  echo "检测到正在运行的 brew install。请先到对应终端按 Ctrl+C 结束，或等它完成后再重试。"
  pgrep -lf 'brew\.rb install' || true
  exit 1
fi

echo "==> 1) 修复 Homebrew 目录归属为 $USER_NAME"
sudo chown -R "$USER_NAME" \
  "$HOME/Library/Logs/Homebrew" \
  /usr/local/Cellar \
  /usr/local/Frameworks \
  /usr/local/Homebrew \
  /usr/local/bin \
  /usr/local/etc \
  /usr/local/include \
  /usr/local/lib \
  /usr/local/opt \
  /usr/local/sbin \
  /usr/local/share \
  /usr/local/var/homebrew \
  /usr/local/var/log 2>/dev/null || true

sudo chmod u+w \
  "$HOME/Library/Logs/Homebrew" \
  /usr/local/Cellar \
  /usr/local/Frameworks \
  /usr/local/Homebrew \
  /usr/local/bin \
  /usr/local/etc \
  /usr/local/include \
  /usr/local/lib \
  /usr/local/opt \
  /usr/local/sbin \
  /usr/local/share \
  /usr/local/var/homebrew \
  /usr/local/var/log 2>/dev/null || true

# 清掉可能残留的 brew 锁与半截下载（避免 “already locked …incomplete”）
rm -rf /usr/local/var/homebrew/locks 2>/dev/null || true
rm -f "$CACHE_DL"/*openjdk*.incomplete 2>/dev/null || true

# 规避 ghcr.io HTTP/2 PROTOCOL_ERROR
cat >"$CURLRC" <<'EOF'
--http1.1
--retry 8
--retry-all-errors
EOF
export HOMEBREW_CURLRC="$CURLRC"
export HOMEBREW_NO_AUTO_UPDATE=1
export HOMEBREW_CURL_RETRIES=8

echo "==> 2) 安装 openjdk@17（单独装，避免与 maven 抢锁）"
brew install openjdk@17

echo "==> 3) 安装 maven（忽略其默认 openjdk@26 依赖，改用上面的 17）"
brew install --ignore-dependencies maven || brew reinstall --ignore-dependencies maven

echo "==> 4) 写入 shell 环境（JDK 17）"
JDK_PATH="$(brew --prefix openjdk@17)"
sudo ln -sfn "$JDK_PATH/libexec/openjdk.jdk" /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null || true

PROFILE="$HOME/.zprofile"
touch "$PROFILE"
grep -q 'openjdk@17' "$PROFILE" 2>/dev/null || {
  {
    echo ''
    echo '# OpenJDK 17 (Homebrew)'
    echo "export PATH=\"$JDK_PATH/bin:\$PATH\""
    echo "export JAVA_HOME=\"$JDK_PATH\""
  } >> "$PROFILE"
  echo "已写入 $PROFILE"
}

export PATH="$JDK_PATH/bin:$PATH"
export JAVA_HOME="$JDK_PATH"

echo "==> 5) 验证"
java -version
mvn -version

echo
echo "完成。新开终端后也可直接用 java / mvn。"
echo "然后："
echo "  cd $(cd "$(dirname "$0")/.." && pwd)/services"
echo "  mvn -pl snp-admin-api -am spring-boot:run"
