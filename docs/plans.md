2026-10-31 ギア3速：テストもコードもAIが書く。ただし、ステップごとに人間がレビューしながら進める

# plans

TDDの作業用todoリスト（使い捨て）。

## レビュー指摘のバグ修正

- [x] 再生中に曲の選択が解除された状態で曲が終わると、RepeatMode.ONEでNPEになる（LibraryManagerViewModel）
- [ ] 設定ファイルのテーマ名・リピートモード名・パスが不正なとき、RepositoryException以外の例外が漏れる（SettingsRepositoryImpl）

## ライブラリマネージャー

- [ ] お気に入りへ追加機能。


## null安全

- [ ] jOOQのnullable注釈を使う。コード生成で `nullableAnnotationType=org.jspecify.annotations.Nullable` を出力し、NullAwayに `AcknowledgeRestrictiveAnnotations=true` を足す。jOOQ本体のJetBrains注釈（`fetchOne()` など）も有効になるので、まず `WARN` で件数を見る。
