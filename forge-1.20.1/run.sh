#!/usr/bin/env bash
#
# Craftsman's bows — Forge 1.20.1 開発用 Minecraft 起動スクリプト
#
#   ./run.sh              クライアントを起動（オフライン）
#   ./run.sh server       専用サーバーを起動（オフライン）
#   ./run.sh --online     ネットに繋いで起動（初回、またはアセット取得が必要なとき）
#
# 作業ディレクトリは run/ です。ワールドや設定はそこに保存されます。
#
set -euo pipefail
cd "$(dirname "$0")"

OFFLINE=1
TARGET=client
for arg in "$@"; do
    case "$arg" in
        client|server) TARGET="$arg" ;;
        --online)      OFFLINE=0 ;;
        --offline)     OFFLINE=1 ;;
        -h|--help)
            sed -n '3,9p' "$0" | sed 's/^# \{0,1\}//'
            exit 0 ;;
        *)
            echo "不明な引数: ${arg}（client / server / --online / --offline が使えます）" >&2
            exit 2 ;;
    esac
done

# ---- Java 17 を探す ---------------------------------------------------
. ./_java-home.sh

# ---- 専用サーバーは EULA の同意が必要 ---------------------------------
if [ "$TARGET" = "server" ] && ! grep -qi '^eula *= *true' run/eula.txt 2>/dev/null; then
    echo "エラー: 専用サーバーの起動には Minecraft EULA への同意が必要です。" >&2
    echo "  https://aka.ms/MinecraftEULA を読んだうえで、同意する場合は次を実行してください:" >&2
    echo "    mkdir -p run && echo 'eula=true' > run/eula.txt" >&2
    exit 1
fi

# ---- Gradle 実行 ------------------------------------------------------
GRADLE_ARGS=(--console=plain)
[ "$OFFLINE" -eq 1 ] && GRADLE_ARGS+=(--offline)

case "$TARGET" in
    client) TASK=runClient ;;
    server) TASK=runServer ;;
esac

echo "==> ./gradlew ${GRADLE_ARGS[*]} $TASK"
if ! ./gradlew "${GRADLE_ARGS[@]}" "$TASK"; then
    if [ "$OFFLINE" -eq 1 ]; then
        echo >&2
        echo "オフラインでの起動に失敗しました。アセットや依存関係がまだ揃っていない可能性があります。" >&2
        echo "一度ネットに繋いで  ./run.sh $TARGET --online  を実行してください。" >&2
    fi
    exit 1
fi
