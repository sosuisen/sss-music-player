# plans

TDDの作業用todoリスト（使い捨て）。

## ライブラリマネージャー

- [ ] お気に入りへ追加機能。

## エラー処理（ADR 002に従うリファクタリング）

共通ルール: I/Oに関わる非チェック例外（`RepositoryException` 等）は、Javadocの `@throws` だけでなくメソッドの `throws` 句にも明記する（jOOQ・Springと同じ流儀）。宣言するのは発生源（ドメインのインタフェースとその実装、service層の入口）に限り、素通しする中間層には書かない。

- [ ] `domain.exception.RepositoryException`（非チェック、メッセージ付き）を新設し、`SqliteLibraryRepository` の `IllegalStateException` と `UncheckedIOException`（親フォルダ作成・schema.sql読み込み）を置き換える。
- [ ] `SettingsRepositoryImpl` の `save`／`load` を `IOException` から `RepositoryException` に変える。`SettingsAppModel` の `catch (IOException)` を追従させる（`SettingsException` へ集約するか、そのまま `App` に通すかを決める）。
- [ ] `LibraryIndexer` の `UncheckedIOException`（`Files.walk`・ファイルサイズ読み取りの2箇所）を、ドメイン専用の非チェック例外に変える。
- [ ] `JaudiotaggerTagWriter` の `IllegalStateException` を、`domain.exception` のチェック例外（または結果型）に変え、編集ウィンドウ内に表示する（局所経路）。
- [ ] `ShellFolderOpener.open` の `UncheckedIOException` を、`domain.exception` のチェック例外（または結果型）に変え、呼び出し元の画面内に表示する（局所経路）。
- [ ] `App` の未捕捉例外ハンドラを、ドメイン専用型（または共通の親型）を受けるよう拡張し、スキャン失敗をダイアログに出す。`MusicLibraryAppModel.scanFolder` の `onFailed` で `Task` の例外を投げ直す。
- [ ] `DuplicateFileMover.moveDuplicates` の `IOException` を、`domain.exception` のチェック例外に変えるか検討する。

## その他

- [ ] アプリ起動時のエラー処理。
  - テストリスト
    - [ ] ライブラリDB（SQLite）が開けない場合、エラーダイアログを表示する。
    - [ ] 起動時スキャンが失敗した場合（ライブラリフォルダが読めない等）、エラーダイアログを表示する。現在は無言で握りつぶされる。
