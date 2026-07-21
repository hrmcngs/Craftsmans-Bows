#!/usr/bin/env bash
#
# build.sh / run.sh から source される共通処理。
# Java 17 を探して JAVA_HOME に設定する。見つからなければ終了する。
#
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
    # 3) Homebrew / Linux の定番パス
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
    echo "  例: JAVA_HOME=/path/to/jdk-17 $0" >&2
    exit 1
fi
echo "==> JAVA_HOME = $JAVA_HOME"
