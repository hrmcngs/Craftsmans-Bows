#!/usr/bin/env bash
#
# Craftsman's bows — Forge 1.20.1 ビルドスクリプト
#
#   ./build.sh            オフラインでビルド（既に一度ビルド済みならネット不要）
#   ./build.sh --online   ネットに繋いでビルド（初回、または依存を更新したいとき）
#   ./build.sh --clean     build/ を消してからビルド
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
            sed -n '2,9p' "$0" | sed 's/^# \{0,1\}//'
            exit 0 ;;
        *)
            echo "不明なオプション: $arg（--online / --offline / --clean が使えます）" >&2
            exit 2 ;;
    esac
done

# ---- Java 17 を探す ---------------------------------------------------
find_java_home() {
    # 1) 既に JAVA_HOME が 17 ならそれを使う
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        if "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"17\.'; then
            echo "$JAVA_HOME"; return 0
        fi
    fi
    # 2) macOS の java_home
    if [ -x /usr/libexec/java_home ]; then
        if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
            /usr/libexec/java_home -v 17; return 0
        fi
    fi
    # 3) Homebrew の openjdk@17
    for candidate in /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
                     /usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
                     /usr/lib/jvm/java-17-openjdk-amd64 \
                     /usr/lib/jvm/java-17-openjdk; do
        [ -x "$candidate/bin/java" ] && { echo "$candidate"; return 0; }
    done
    return 1
}

if JAVA_HOME_FOUND="$(find_java_home)"; then
    export JAVA_HOME="$JAVA_HOME_FOUND"
else
    echo "エラー: Java 17 が見つかりません。JAVA_HOME に Java 17 を指定してください。" >&2
    echo "  例: JAVA_HOME=/path/to/jdk-17 ./build.sh" >&2
    exit 1
fi
echo "==> JAVA_HOME = $JAVA_HOME"

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
