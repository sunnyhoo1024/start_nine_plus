#!/usr/bin/env bash
# 按环境选择 .env / .env.stg / .env.prod 调用 docker compose
# 用法：
#   ./scripts/dc.sh dev  up -d mysql redis
#   ./scripts/dc.sh stg  up -d --build mysql redis admin-api app-api
#   ./scripts/dc.sh prod up -d --build mysql redis admin-api app-api
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_NAME="${1:-}"
if [[ -z "$ENV_NAME" ]]; then
  echo "Usage: $0 <dev|stg|prod> <docker compose args...>"
  echo "  dev  → .env"
  echo "  stg  → .env.stg"
  echo "  prod → .env.prod"
  exit 1
fi
shift

case "$ENV_NAME" in
  dev|local|development) ENV_FILE=".env" ;;
  stg|stage|staging)     ENV_FILE=".env.stg" ;;
  prod|production)       ENV_FILE=".env.prod" ;;
  *)
    echo "Unknown env: $ENV_NAME (use dev|stg|prod)"
    exit 1
    ;;
esac

ENV_PATH="$ROOT/$ENV_FILE"
if [[ ! -f "$ENV_PATH" ]]; then
  echo "Missing $ENV_PATH"
  exit 1
fi

cd "$ROOT/deploy"
echo "==> docker compose --env-file $ENV_PATH $*"
exec docker compose --env-file "$ENV_PATH" "$@"
