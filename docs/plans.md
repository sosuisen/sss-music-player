# plans

TDDの作業用todoリスト（使い捨て）。

## 再生機能

- [ ] 1曲リピート再生。
  - テストリスト
    - [x] 停止ボタンの右側に、repeat modeのトグルボタンがある。モードは、全曲リピートと1曲リピートの2つだけ。初期値は全曲リピート。
    - [x] 1曲リピートの場合、再生終了後その曲の先頭から再再生。
    - [x] トグルボタンを押すたびにモードがトグル。
    - [x] リピートボタンの表示がモードで変わる：repeat one, repeat all。
    - [x] リピートモードを保存、起動時に再現。
      - [x] SettingsAppModelのrepeatModeを変えると設定ファイルに保存される。
      - [x] 設定ファイルのrepeatModeはloadで復元される（キーがない場合はALL）。
      - [x] LibraryManagerViewModelのトグルがSettingsAppModelのrepeatModeを更新する。
      - [x] 起動時、設定ファイルのrepeatModeがボタン表示に再現される。
- 見た目
  - [ ] きれいなアイコンを利用。
  - [ ] AtlantaFXでボタンを修飾。
- リファクタリング
  - [ ] モデルのパッケージを整理。

## ライブラリマネージャー

- [ ] お気に入りへ追加機能。

## その他

- [ ] アプリ起動時のエラー処理。
- [x] 設定の保存・読込エラーの扱いを変更: SettingsAppModelは独自の非チェック例外を投げ、Appでキャッチしてエラーダイアログを表示。errorPropertyと設定ウィンドウ内のエラー表示は廃止。
  - テストリスト
    - [x] 設定の保存に失敗すると、SettingsAppModelはSettingsException（domain.exception、causeは元のIOException）を投げる。
    - [x] 設定ファイルが存在するが読めない場合、loadSettingsはSettingsExceptionを投げる。
    - [x] 保存に失敗すると、Appはエラーダイアログを表示し、アプリは続行する。
    - [x] 読込に失敗した状態で起動すると、Appは初期設定で起動する旨のダイアログを表示し、続行して設定ウィンドウを開く。
    - [x] 廃止: SettingsAppModelのerrorProperty（テスト2件削除）。
    - [x] 廃止: SettingsViewModelのerrorMessageProperty（テスト2件削除）。
    - [x] 廃止: 設定ウィンドウの#errorMessage表示（テスト1件削除、Viewからラベル削除）。
