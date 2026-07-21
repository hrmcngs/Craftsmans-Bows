#!/usr/bin/env bash
#
# Craftsman's bows — Forge 1.20.1 ビルドスクリプト
#
#   ./forge-1.20.1/build.sh            オフラインでビルド（既に一度ビルド済みならネット不要）
#   ./forge-1.20.1/build.sh --online   ネットに繋いでビルド（初回、または依存を更新したいとき）
#   ./forge-1.20.1/build.sh --clean    build/ を消してからビルド
#
set -euo pipefail
cd "$(dirname "$0")"

OFFLINE=1
CLEAN=0
for arg in "$@"; do
    case "$arg" in
        --online)  OFFLINE=0 ;;
        --offline) OFFLINE=1 ;;
        --clean)   CLEAN=1 ;;
        -h|--help)
            sed -n '3,8p' "$0" | sed 's/^# \{0,1\}//'
            exit 0 ;;
        *)
            echo "不明なオプション: ${arg}（--online / --offline / --clean が使えます）" >&2
            exit 2 ;;
    esac
done

# ---- Java 17 を探す ---------------------------------------------------
. ./_java-home.sh

# ---- Gradle 実行 ------------------------------------------------------
GRADLE_ARGS=(--console=plain)
[ "$OFFLINE" -eq 1 ] && GRADLE_ARGS+=(--offline)

TASKS=()
[ "$CLEAN" -eq 1 ] && TASKS+=(clean)
TASKS+=(build)

echo "==> ./gradlew ${GRADLE_ARGS[*]} ${TASKS[*]}"
if ! ./gradlew "${GRADLE_ARGS[@]}" "${TASKS[@]}"; then
    if [ "$OFFLINE" -eq 1 ]; then
        echo >&2
        echo "オフラインビルドに失敗しました。依存関係がまだキャッシュされていない可能性があります。" >&2
        echo "一度ネットに繋いで  ./build.sh --online  を実行してください。" >&2
    fi
    exit 1
fi

# ---- 結果 -------------------------------------------------------------
JAR="$(ls -t build/libs/*.jar 2>/dev/null | grep -v -- '-sources\.jar$' | head -1 || true)"
if [ -n "$JAR" ]; then
    echo
    echo "==> 完成: $(cd "$(dirname "$JAR")" && pwd)/$(basename "$JAR")"
else
    echo "警告: jar が見つかりませんでした（build/libs/ を確認してください）" >&2
    exit 1
fi
