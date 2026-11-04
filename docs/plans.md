2026-11-01 ギア5速：テストもコードもAIが書く。人間は最初の設計に関わるだけで、コードレビューはしない
2026-10-31 ギア3速：テストもコードもAIが書く。ただし、ステップごとに人間がレビューしながら進める

# plans

TDDの作業用todoリスト（使い捨て）。

## シークバー

- [x] 再生時間を「分:秒」で表す（0:00、1:23、59:59）
- [x] 1時間以上の再生時間は「時:分:秒」で表す（1:02:03）
- [x] 再生位置の通知を受けると、再生位置と曲の長さを公開する（ViewModel）
- [x] 停止すると、再生位置はなくなる
- [x] 別の曲を再生し始めると、新しい曲の通知が来るまで、再生位置はなくなる
- [x] 再生中に次の曲・前の曲へ移ると、再生位置はなくなる
- [x] 再生に失敗すると、再生位置はなくなる
- [x] シークすると、プレイヤーにその位置への移動を頼む
- [x] 再生中は、再生位置と曲の長さを通知する（MediaMusicPlayer）
- [x] シークすると、その位置から再生を続ける（MediaMusicPlayer）
- [x] 一時停止中にシークすると、その位置で一時停止したままである（MediaMusicPlayer）
- [x] 再生位置が分からないとき、シークバーは無効で、時間は0:00である（View）
- [x] 再生位置の通知を受けると、シークバーと時間の表示が変わる（View）
- [x] シークバーのつまみを動かして離すと、その位置へシークする（View）
- [x] つまみをドラッグしている間は、再生位置の通知を受けても、つまみは動かない（View）
- [x] シークバーをクリックすると、その位置へシークする（View）

## m4aを再生する（ADR 005）

- [x] m4aを、同じ周波数とチャンネル数の16bitのPCMのWAVに変換する（WavConverter）
- [x] 変換できないm4aを再生すると、失敗を通知し、何も読み込んでいない状態になる
- [x] m4aを再生すると、変換したWAVをJavaFXで再生する
- [x] 同じm4aをもう一度再生すると、同じ一時WAVを使い回す
- [x] 別の曲を再生すると、前の一時WAVは、JavaFXが手放したあとの再生で消える
- [x] 一時WAVを、指定した専用フォルダに作る（WavConverter、ADR 006）
- [x] 専用フォルダに残った一時WAVをすべて消す（起動時、ADR 006）
- [x] 専用フォルダがまだなければ、何もしない（初回の起動）
- [x] 開かれていて消せない一時WAVは残し、ほかの一時WAVを消す（アプリの同時起動）
- [x] 起動時に、前回までに残った一時WAVを消す（App）
- [x] 変換中に停止すると、変換が終わっても何も読み込まず、一時WAVを消す
- [x] 変換中に一時停止すると、変換が終わっても再生を始めない
- [x] native imageのメタデータを更新し、exeでm4aを再生できることを確かめる

## バグ修正：再生に失敗しても「再生中」のままになる

- [x] 再生の失敗が通知されると、プレイヤーの状態は停止に戻り、エラーメッセージにファイル名と理由を出す
- [x] 再生を始めた時点で失敗が通知されても、プレイヤーの状態は停止になる
- [x] 次の再生が始まると、エラーメッセージは消える
- [x] 試聴の失敗が通知されると、試聴中のファイルはなくなり、エラーメッセージにファイル名と理由を出す
- [x] 試聴を始めた時点で失敗が通知されても、試聴中のファイルはなくなる
- [x] 存在しないファイルを再生すると、例外を投げずに失敗を通知し、何も読み込んでいない状態になる（MediaMusicPlayer）
- [x] 開いたあとに再生に失敗すると、失敗を通知し、何も読み込んでいない状態になる（MediaMusicPlayer）

## バグ修正：お気に入りの追加後、exeが起動しない

- [x] native imageのメタデータに`FavoriteRecord`を登録し、exeのスモークテストを通す

## バグ修正：NFDのパスの曲を再生できない

- [x] 濁点を結合文字で書いた（NFDの）フォルダー名にある曲のMediaを作れる

## お気に入り（Favorites）

- [x] 空のDBでは、お気に入りの一覧は空である（SqliteFavoriteRepository）
- [x] 追加したアルバムと曲のお気に入りを、追加した順に返す（再オープン後も）
- [x] 削除したお気に入りは一覧から消える
- [x] FavoritesAppModelは、作成時にリポジトリのお気に入りを読み込む
- [x] 未登録のお気に入りをトグルすると登録され、リポジトリに保存される
- [x] 登録済みのお気に入りをトグルすると解除され、リポジトリから消える
- [x] ソートキーの項目はAlbum・Artist・Favoritesである
- [x] Favoritesを選ぶと、左のリストは登録順のアルバムと曲になる
- [x] ライブラリに見つからないお気に入りは表示しない
- [x] Favorites表示中に解除しても行は残り、表示を切り替え直すと消える
- [x] 曲の項目を選ぶと、右のリストはその曲だけになり、選択中のアルバムはその曲のアルバムになる
- [x] 曲の項目の行は曲名（空ならファイル名）で表示する
- [x] Favorites表示でも絞り込みが効く（アルバムは名前・アーティスト、曲は曲名・アーティスト）
- [x] アルバム行の☆をクリックすると、そのアルバムがお気に入りに登録される
- [x] 曲行の☆をクリックすると、その曲がお気に入りに登録される
- [x] 星の記号は、登録済みなら★、未登録なら☆である
- [x] 起動時に、ライブラリDBのお気に入りを読み込む（App）

## Favorites表示での連続再生

- [x] アルバムの項目の途中の曲が終わると、その項目の次の曲を再生する（ViewModel）
- [x] アルバムの項目の最後の曲が終わると、次の項目を選び、その先頭の曲を再生する（ViewModel）
- [x] 曲の項目の曲が終わると、次の項目を選び、その先頭の曲を再生する（ViewModel）
- [x] 最後の項目の最後の曲が終わると、先頭の項目の先頭の曲を再生する（ViewModel）
- [x] 1曲リピートでは、項目の最後の曲が終わっても、同じ曲を再生し直す（ViewModel）
- [x] 項目の最後の曲で次の曲へ移ると、次の項目の先頭の曲を選ぶ。再生中ならそれを再生する（ViewModel）
- [x] 停止中に項目の最後の曲で次の曲へ移ると、次の項目の先頭の曲を選ぶだけで再生しない（ViewModel）
- [x] 項目の先頭の曲で前の曲へ移ると、前の項目の最後の曲を選ぶ。先頭の項目なら最後の項目へ戻る（ViewModel）
- [x] 選択中の項目が絞り込みで左のリストから消えていると、今までどおり項目の中で回る（ViewModel）
- [x] Favorites表示でないときは、今までどおりアルバムの中で回る（ViewModel）
- [x] 次の項目へ進むと、左のリストの選択もその項目へ移る（View）

## レビュー指摘のバグ修正

- [x] 再生中に曲の選択が解除された状態で曲が終わると、RepeatMode.ONEでNPEになる（LibraryManagerViewModel）
- [x] 設定ファイルのテーマ名・リピートモード名・パスが不正なとき、RepositoryException以外の例外が漏れる（SettingsRepositoryImpl）

## レビューの残課題（2026-10-31のレビュー）

### 方針判断が要るもの

- [ ] Error Proneの組み込みチェック: ADR-003は全停止、java-coding-conventionsは有効化（`FieldCanBeFinal`・`MethodCanBeStatic`・`PrivateConstructorForUtilityClass`をWARN）。どちらに寄せるか決め、ADRか規約を直す（`pom.xml` `-XepDisableAllChecks`）
- [ ] レイアウト・装飾のテスト9件を残すか決める: `LibraryManagerListTest`の絞り込み欄の位置、`LibraryManagerPlaybackTest`のボタン並び順・存在・アクセントスタイル、`DuplicateListViewTest`の幅固定（期待値が`DetailedPanel.WIDTH`で同語反復）・ボタン文言・赤字、`SettingsViewTest`の幅
- [ ] 例外メッセージ全文を比較しているテスト2件（`SqliteLibraryRepositoryTest:227`、`DuplicateFileMoverTest:120`）をどこまで検証するか決める
- [ ] `MusicFileTest`と`SettingsAppModelTest:42-49`（アクセサ・set/getだけのテスト）を消すか決める
- [ ] PIT（`pitest-maven`）の導入
- [x] CI（GitHub Actions）の導入。`.github/modernize/java-upgrade/`の作業ファイルも不要なら消す（native imageのexeを配る。ADR 004）
- [ ] `classdiagram-maven-plugin`の導入と`docs/architecture.md`の作成
- [ ] プロパティテスト: `SettingsRepositoryImpl`の「保存して読み込むと元に戻る」往復（jqwik等の導入が要る）

### 設計（依存の向き）

- [ ] presentationがservice実装を直接使っている。`domain.service`にインタフェースを置き、`App`から注入する: `MusicLibraryAppModel`→`LibraryIndexer`、`LibraryManagerViewModel`→`AlbumDetector`、`DuplicateListViewModel`→`DuplicateFileMover`と検出器3種（検出器はファクトリ型で注入）
- [ ] `~/.sss-music-player`の組み立てが`App:102`・`SettingsRepositoryImpl:22`・`SqliteLibraryRepository:39`の3か所に重複。1か所にまとめ、`duplicates`フォルダもシステムプロパティで上書きできるようにする
- [ ] `Theme.toString()`がUI表示ラベルを返している。ラベルはpresentation側の`StringConverter`へ

### バグの可能性・ADRとの食い違い

- [ ] `SqliteLibraryRepository:143`: ADR-001は「同じ最終更新時刻」だが実装は`ge`。`eq`に揃えるか、理由を残してADRを更新する
- [ ] `MusicLibraryAppModel:68-88`: 走査の多重起動を防いでいない（2つのTaskが並走し、後から終わった方で上書き）
- [ ] `DuplicateFileMoverTest:97`: 期待値が`LocalDate.now()`で実装と同語反復。`Clock`を注入して固定日付で比較する
- [ ] `LibraryIndexer:131`・`JaudiotaggerTagWriter:34`: `catch (Exception)`がバグ由来の例外も握りつぶす。jaudiotaggerの例外を個別に捕捉する
- [ ] `LibraryIndexer:96-102`・`SqliteLibraryRepository:112`: 操作ごとの接続オープンとN+1削除。ADR-001の高速化目的に反する

### 規約とのずれ（JavaFX）

- [ ] 全画面を`StageStyle.EXTENDED`にする（Windowsのみ）。今は`LibraryManagerView`だけ無条件`EXTENDED`、他3画面は`DECORATED`
- [ ] `LibraryManagerView:102-109`: `HeaderBarBuilder`で構築する規約と「builderがない」コメントのどちらかが古い。確かめて揃える
- [ ] `LibraryManagerView:71,102`・`LibraryManagerWindowTest:208`: 「JavaFX 26のプレビュー」コメントが古い可能性（pomはJavaFX 27）

### pom.xml・ビルド

- [ ] JaCoCoの導入（`prepare-agent`と`report`）。テストしないクラスは理由コメント付きで`<excludes>`へ
- [x] formatter-maven-plugin: 版を2.29.0へ、設定ファイル名を`java-formatter.xml`へ（`.vscode/settings.json`も）、`format`ゴールの`<executions>`を足す
- [x] surefire: `forkCount=1C`に合わせて`javafx.cachedir`をフォークごとに分ける`<argLine>`（`${test.jvm.options}`で追加オプションを渡せる。JaCoCo導入時に`@{argLine}`を足す）
- [x] jpackageの`appVersion`が`1.0.0`固定。仮の値だと分かるコメントを添える（jpackageごと撤去。ADR 004）
- [ ] `maven.compiler.release`が25、READMEも「Java 25」。意図を確認して揃える
- [ ] 依存の最新版の確認（`maven-compiler-plugin` 3.13.0など）

### 重複・YAGNI

- [ ] 検出器3種の「groupingBy → 2件以上 → DuplicatedItems」の重複を共通化し、「2件以上」は`DuplicatedItems`で検査する。「先頭を残す」が`DuplicateFileMover:49`にも分散
- [ ] チェック例外3クラス（`DuplicateRemovalException`・`FolderOpenException`・`TagWriteException`）の`requireMessage`重複。抽象基底を1つ作る
- [ ] `SettingsViewModel:76-77`: musicLibraryPathだけViewModelが`save()`を明示呼び出し。AppModelの`subscribe`に揃える
- [ ] `LibraryManagerViewModel`が478行で責務が多い。再生の状態機械を分ける
- [ ] mainから呼ばれないオーバーロードやテスト専用のpublic: `MusicFile(Path,long)`、`Album(name,artist,files)`、`Settings(Path)`、`Settings(Path,Theme)`、`LibraryIndexer.scan(Path)`、`SqliteLibraryRepository(Path)`、`LibraryManagerViewModel.setFiles/getFiles`、`DetailedPanel.WIDTH`、`SettingsRepositoryImpl.DEFAULT_FILE`
- [ ] `DetailedPanel:46-56`: `updateItem`のたびに行ノードとバインディングを作り直している

### 小さな修正

- [ ] `LibraryIndexer:146`: `isDeclaredLatin1Id3Frame`の引数に`@Nullable`
- [ ] `LibraryIndexer:60,76`: Javadocの`@throws RepositoryException`と`throws`句の不一致
- [ ] インタフェース側の`@throws NullPointerException`の抜け（`MusicPlayer`・`TagWriter`・`FolderOpener`・`LibraryRepository`）
- [ ] `TagRecovery:16`: `SJIS`定数が`MS932`である理由のコメント
- [ ] `AlbumEditView:140`: UI文言だけ日本語
- [ ] `AlbumEditViewModel:58`: Javadocと同内容のWhatコメント

### テストの整理

- [ ] 日本語のコードコメント5か所を英語に（`LibraryManagerPlaybackTest:218`、`LibraryManagerTrackNavigationTest:150`、`AppWithBrokenSettingsFileTest:24`、`AppSettingsSaveErrorTest:48`、`AppWithBrokenLibraryDatabaseTest:25`）
- [ ] 組み立てコードの重複: 各ViewModelTestはファクトリか`@BeforeEach`へ、App系10クラスは`AppTestBase`へ（一時フォルダの削除も）
- [ ] 曲フィクスチャ（`one.mp3`・`two.mp3`）が4クラスで15回以上。`LibraryManagerViewTestBase`にヘルパーを置く
- [ ] グリフで振る舞いを検証しているテストを状態検証に置き換え、アイコンはマッピングテスト1つに絞る（`LibraryManagerPlaybackTest:56-73,172-181`、`DuplicateListViewTest:195-210`、`AppWithSavedRepeatModeTest:45`）
- [ ] 例外テストの名前「〜必要がある／must_have」を事実の形にし、null・blankの契約チェックの3重化を基底1か所に（`LibraryScanExceptionTest`・`RepositoryExceptionTest`・`UnrecoverableExceptionTest`）
- [ ] `AtomicReference`の`@Nullable`抜け6か所、テスト間に挟まったヘルパー6か所、import順の乱れ7ファイル、不要な`throws Exception`
- [ ] `SqliteLibraryRepositoryTest`の準備重複（`brokenDatabase()`・`"Saved Song"`）、`LibraryIndexerTest`の冗長テスト・変数名`scanner`・`Files.size`で算出した期待値
- [ ] 相対パスの`DuplicateFileMover`フィクスチャを`@TempDir`配下に（`DuplicateListViewModelTest:50`、`LibraryManagerViewTestBase:136`、`WindowManagerTest:40`）
- [ ] 実装詳細を見るテスト（`assertInstanceOf(ObservableList.class)`、`assertSame`）を消すか振る舞いのテストに

## ライブラリマネージャー

- [ ] お気に入りへ追加機能。
- [x] アルバムをダブルクリックすると、先頭の曲を再生し始める
  - [x] アルバムの行で再生すると、トラック番号順の先頭の曲を再生し、その曲を選ぶ（ViewModel）
  - [x] 別の曲を再生中に、アルバムの行で再生すると、そのアルバムの先頭の曲を再生し、その曲を選ぶ（ViewModel）
  - [x] 曲の行で再生すると、その曲を再生する（ViewModel）
  - [x] アルバム一覧の行をダブルクリックすると、先頭の曲の再生が始まる（View）
- [ ] 曲名検索


## null安全

- [ ] jOOQのnullable注釈を使う。コード生成で `nullableAnnotationType=org.jspecify.annotations.Nullable` を出力し、NullAwayに `AcknowledgeRestrictiveAnnotations=true` を足す。jOOQ本体のJetBrains注釈（`fetchOne()` など）も有効になるので、まず `WARN` で件数を見る。
