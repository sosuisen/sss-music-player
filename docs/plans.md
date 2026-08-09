# plans

TDDの作業用todoリスト（使い捨て）。

## ライブラリスキャン

- [ ] 音楽ファイルがあれば、ライブラリ管理画面の一覧に表示される。

## 重複候補の一覧表示

- [ ] 重複判定関数
  - [ ] ファイル名とサイズに基づく判定
  - [ ] ID3タグの曲名、アーティストに基づく判定
- [ ] DuplicateListViewを表示
  - [x] WindowManagerにDuplicateListViewを登録
  - [x] WindowManagerからDuplicateListViewを開く
- [x] アプリ起動時に最初に表示されるウィンドウを定数で変更できるようにする。
- [ ] 重複リストウィンドウには、重複リストが表示される。
- [x] AppModelでファイルリストを共有する
  - [x] ライブラリ管理ViewModelはAppModelのファイルリストを委譲する。
  - [x] DuplicateListViewModelにもAppModelを注入する。
- [ ] ボタンを押したらファイル名を基準とする重複リストが表示される
- [ ] FileメニューのRemove duplicate files...を選ぶと、重複リスト表示ウィンドウが開く
