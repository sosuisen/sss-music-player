# plans

TDDの作業用todoリスト（使い捨て）。

## ライブラリマネージャー

- [ ] お気に入りへ追加機能。

## エラー処理（ADR 002に従うリファクタリング）

共通ルール: I/Oに関わる非チェック例外（`RepositoryException` 等）は、Javadocの `@throws` だけでなくメソッドの `throws` 句にも明記する（jOOQ・Springと同じ流儀）。宣言するのは発生源（ドメインのインタフェースとその実装、service層の入口）に限り、素通しする中間層には書かない。

共通ルール: Javadocの `@throws` も同様に、呼び出し元へスタックを通って伝播する例外だけを、発生源と契約の境界（ドメインのインタフェース、service層の入口）に書く。素通しする中間層には列挙しない。`Task` などで経路が変わる（別スレッドで実行され、FXスレッドの未捕捉例外ハンドラに届く）場合は `@throws` ではなく本文に散文で書く。

- [ ] `App` の未捕捉例外ハンドラを、ドメイン専用型（または共通の親型）を受けるよう拡張し、スキャン失敗をダイアログに出す。`MusicLibraryAppModel.scanFolder` の `onFailed` で `Task` の例外を投げ直す。
- [ ] `App` のハンドラ拡張後、`SettingsAppModel` の `RepositoryException` → `SettingsException` の翻訳がまだ必要か判断する（不要なら `SettingsException` を廃止して素通しにする）。
- [ ] `DuplicateFileMover.moveDuplicates` の `IOException` を、`domain.exception` のチェック例外に変えるか検討する。

## その他

- [ ] アプリ起動時のエラー処理。
  - テストリスト
    - [ ] ライブラリDB（SQLite）が開けない場合、エラーダイアログを表示する。
    - [ ] 起動時スキャンが失敗した場合（ライブラリフォルダが読めない等）、エラーダイアログを表示する。現在は無言で握りつぶされる。
