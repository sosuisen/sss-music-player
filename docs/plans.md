# plans

TDDの作業用todoリスト（使い捨て）。

## ライブラリマネージャー

- [ ] お気に入りへ追加機能。


## null安全

- [ ] jOOQのnullable注釈を使う。コード生成で `nullableAnnotationType=org.jspecify.annotations.Nullable` を出力し、NullAwayに `AcknowledgeRestrictiveAnnotations=true` を足す。jOOQ本体のJetBrains注釈（`fetchOne()` など）も有効になるので、まず `WARN` で件数を見る。
