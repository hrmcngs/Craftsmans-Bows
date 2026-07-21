#!/usr/bin/env bash
#
# Craftsman's bows — Forge 1.20.1 開発用 Minecraft 起動スクリプト
#
#   ./forge-1.20.1/run.sh            クライアントを起動（オフライン）
#   ./forge-1.20.1/run.sh server       専用サーバーを起動（オフライン）
#   ./forge-1.20.1/run.sh --online     ネットに繋いで起動（初回、またはアセット取得が必要なとき）
#   ./forge-1.20.1/run.sh --xtra       確認用 MOD を入れて起動（聞かずに yes）
#   ./forge-1.20.1/run.sh --no-xtra    確認用 MOD 無しで起動（聞かずに no）
#
# 起動時に確認用の MOD を一緒に入れるか聞きます。libs/local/ に jar を置いておけば
# それを読み込みます（オフラインで動きます）。空なら Modrinth から Xtra Arrows を
# 取得します（初回のみネットが必要）。
# どちらも動作確認用に読み込むだけで、build.sh で作られる jar には影響しません。
# 作業ディレクトリは run/ です。ワールドや設定はそこに保存されます。
#
set -euo pipefail
cd "$(dirname "$0")"

OFFLINE=1
TARGET=client
XTRA=ask
for arg in "$@"; do
    case "$arg" in
        client|server) TARGET="$arg" ;;
        --online)      OFFLINE=0 ;;
        --offline)     OFFLINE=1 ;;
        --xtra)        XTRA=yes ;;
        --no-xtra)     XTRA=no ;;
        -h|--help)
            sed -n '3,14p' "$0" | sed 's/^# \{0,1\}//'
            exit 0 ;;
        *)
            echo "不明な引数: ${arg}（client / server / --online / --offline / --xtra / --no-xtra が使えます）" >&2
            exit 2 ;;
    esac
done

# ---- Xtra Arrows を入れるか聞く ---------------------------------------
XTRA_ID="$(sed -n 's/^xtra_arrows_version=//p' gradle.properties)"
XTRA_VERSION="$(sed -n 's/^xtra_arrows_display_version=//p' gradle.properties)"

# libs/local/ に jar があればそれを使う（オフラインで動く）。無ければ Modrinth から取得する。
LOCAL_JARS=()
while IFS= read -r j; do [ -n "$j" ] && LOCAL_JARS+=("$j"); done < <(ls libs/local/*.jar 2>/dev/null || true)
if [ ${#LOCAL_JARS[@]} -gt 0 ]; then
    XTRA_SOURCE="libs/local/ の $(printf '%s, ' "${LOCAL_JARS[@]##*/}" | sed 's/, $//')"
    XTRA_LOCAL=1
else
    XTRA_SOURCE="Xtra Arrows ${XTRA_VERSION} / Modrinth から取得"
    XTRA_LOCAL=0
fi

if [ "$XTRA" = "ask" ]; then
    if [ -t 0 ]; then
        printf '確認用の MOD (%s) を入れて起動しますか? [y/N]: ' "$XTRA_SOURCE"
        read -r answer || answer=""
        case "$answer" in
            [yY]|[yY][eE][sS]) XTRA=yes ;;
            *)                 XTRA=no ;;
        esac
    else
        # 対話できない環境では止まらないよう「入れない」で進む
        XTRA=no
    fi
fi

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

if [ "$XTRA" = "yes" ]; then
    GRADLE_ARGS+=(-PwithXtraArrows)
    echo "==> 確認用の MOD を入れて起動します: ${XTRA_SOURCE}"
    if [ "$XTRA_LOCAL" -eq 0 ]; then
        # まだ取得していないならオフラインでは落ちるので、その回だけオンラインに切り替える
        CACHE="$HOME/.gradle/caches/modules-2/files-2.1/maven.modrinth/xtra-arrows/${XTRA_ID}"
        if [ "$OFFLINE" -eq 1 ] && [ ! -d "$CACHE" ]; then
            echo "    まだ取得していないので、今回はオンラインで起動します。"
            echo "    完全にオフラインで動かしたい場合は libs/local/ に jar を置いてください:"
            echo "    https://www.curseforge.com/minecraft/mc-mods/xtra-arrows/files/6434918"
            OFFLINE=0
        fi
    fi
fi

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
