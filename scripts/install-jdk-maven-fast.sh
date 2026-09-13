#!/bin/bash
# 从国内镜像快速安装 JDK 17 + Maven（不走 Homebrew / ghcr.io）
# 用法（终端.app）：
#   bash scripts/install-jdk-maven-fast.sh
#
# 若 brew 还在慢下：先在那个终端 Ctrl+C，再跑本脚本。

set -euo pipefail

ARCH="$(uname -m)"
case "$ARCH" in
  x86_64) JDK_ARCH="x64" ;;
  arm64)  JDK_ARCH="aarch64" ;;
  *) echo "不支持的架构: $ARCH"; exit 1 ;;
esac

SDK_ROOT="${SNP_SDK_ROOT:-$HOME/.sdk/start_nine_plus}"
JDK_DIR="$SDK_ROOT/jdk-17"
MVN_DIR="$SDK_ROOT/maven"
TMP_DIR="$(mktemp -d -t snp-sdk)"
trap 'rm -rf "$TMP_DIR"' EXIT

JDK_URL="https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jdk/${JDK_ARCH}/mac/OpenJDK17U-jdk_${JDK_ARCH}_mac_hotspot_17.0.20.1_1.tar.gz"
# 阿里云 / 腾讯云镜像（优先阿里）
MVN_VER="3.9.16"
MVN_URLS=(
  "https://mirrors.aliyun.com/apache/maven/maven-3/${MVN_VER}/binaries/apache-maven-${MVN_VER}-bin.tar.gz"
  "https://mirrors.cloud.tencent.com/apache/maven/maven-3/${MVN_VER}/binaries/apache-maven-${MVN_VER}-bin.tar.gz"
  "https://dlcdn.apache.org/maven/maven-3/${MVN_VER}/binaries/apache-maven-${MVN_VER}-bin.tar.gz"
)

download() {
  local url="$1" out="$2"
  echo "  <- $url"
  curl -fL --http1.1 --retry 5 --retry-all-errors --connect-timeout 15 \
    --progress-bar -o "$out" "$url"
}

echo "==> 安装目录: $SDK_ROOT"
mkdir -p "$SDK_ROOT"

if [[ -x "$JDK_DIR/Contents/Home/bin/java" ]] || [[ -x "$JDK_DIR/bin/java" ]]; then
  echo "==> JDK 17 已存在，跳过下载"
else
  echo "==> 下载 Temurin JDK 17（清华镜像）"
  download "$JDK_URL" "$TMP_DIR/jdk.tgz"
  rm -rf "$JDK_DIR"
  mkdir -p "$JDK_DIR"
  tar -xzf "$TMP_DIR/jdk.tgz" -C "$JDK_DIR" --strip-components=1
fi

if [[ -x "$MVN_DIR/bin/mvn" ]]; then
  echo "==> Maven 已存在，跳过下载"
else
  echo "==> 下载 Apache Maven ${MVN_VER}（国内镜像）"
  ok=0
  for url in "${MVN_URLS[@]}"; do
    if download "$url" "$TMP_DIR/maven.tgz"; then
      ok=1
      break
    fi
    echo "  镜像失败，试下一个…"
  done
  [[ "$ok" -eq 1 ]] || { echo "Maven 下载失败"; exit 1; }
  rm -rf "$MVN_DIR"
  mkdir -p "$MVN_DIR"
  tar -xzf "$TMP_DIR/maven.tgz" -C "$MVN_DIR" --strip-components=1
fi

# macOS Temurin tar 通常是 *.jdk/Contents/Home
if [[ -d "$JDK_DIR/Contents/Home" ]]; then
  JAVA_HOME_PATH="$JDK_DIR/Contents/Home"
elif [[ -x "$JDK_DIR/bin/java" ]]; then
  JAVA_HOME_PATH="$JDK_DIR"
else
  # 有时解压后多一层 jdk-17.0.x+x 目录
  JAVA_HOME_PATH="$(find "$JDK_DIR" -type f -path '*/bin/java' | head -1 | sed 's|/bin/java||')"
fi

[[ -n "${JAVA_HOME_PATH:-}" && -x "$JAVA_HOME_PATH/bin/java" ]] || {
  echo "未找到 java，请检查 $JDK_DIR"
  exit 1
}

PROFILE="$HOME/.zprofile"
touch "$PROFILE"
# 去掉旧的 openjdk@17 / start_nine_plus sdk 段，避免重复
TMP_PROFILE="$(mktemp)"
awk '
  /^# OpenJDK 17 \(Homebrew\)$/ {skip=1; next}
  /^# start_nine_plus SDK$/ {skip=1; next}
  skip && /^export (PATH|JAVA_HOME)=/ {next}
  skip && /^$/ {skip=0; next}
  skip {next}
  {print}
' "$PROFILE" >"$TMP_PROFILE" && mv "$TMP_PROFILE" "$PROFILE"

{
  echo ''
  echo '# start_nine_plus SDK'
  echo "export JAVA_HOME=\"$JAVA_HOME_PATH\""
  echo "export PATH=\"\$JAVA_HOME/bin:$MVN_DIR/bin:\$PATH\""
} >> "$PROFILE"

export JAVA_HOME="$JAVA_HOME_PATH"
export PATH="$JAVA_HOME/bin:$MVN_DIR/bin:$PATH"

echo "==> 验证"
java -version
mvn -version

echo
echo "完成（未使用 Homebrew）。新开终端后 java / mvn 可用。"
echo "当前会话也可直接："
echo "  export JAVA_HOME=\"$JAVA_HOME_PATH\""
echo "  export PATH=\"\$JAVA_HOME/bin:$MVN_DIR/bin:\$PATH\""
