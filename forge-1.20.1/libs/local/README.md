# libs/local

動作確認のために一緒に読み込みたい MOD の jar を、このフォルダに置いてください。
`run.sh` で「確認用の MOD を入れますか?」に `y` と答えると読み込まれます。
ここに jar があればネットに繋がなくても起動できます。

- 対象は **Forge 1.20.1 用の製品版 jar** です（ForgeGradle が開発環境用に変換して読み込みます）
- 開発実行でしか使われません。`build.sh` で作られる jar には含まれず、依存にもなりません
- jar は `.gitignore` 済みなのでコミットされません（他人の MOD を再配布しないため）

このフォルダが空の場合は、Modrinth から Xtra Arrows を取得します（初回のみネットが必要）。

## Xtra Arrows

- CurseForge: https://www.curseforge.com/minecraft/mc-mods/xtra-arrows/files/6434918
- Modrinth: https://modrinth.com/mod/xtra-arrows/versions?l=forge&g=1.20.1

ダウンロードした `xtraarrows-x.y.z-forge-mc1.20.1.jar` をこのフォルダに置くだけです。
