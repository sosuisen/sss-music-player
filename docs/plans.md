2026-10-31 ギア3速：テストもコードもAIが書く。ただし、ステップごとに人間がレビューしながら進める

# plans

TDDの作業用todoリスト（使い捨て）。

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
- [ ] CI（GitHub Actions）の導入。`.github/modernize/java-upgrade/`の作業ファイルも不要なら消す
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
- [ ] formatter-maven-plugin: 版を2.29.0へ、設定ファイル名を`java-formatter.xml`へ（`.vscode/settings.json`も）、`format`ゴールの`<executions>`を足す
- [ ] surefire: `forkCount=1C`に合わせて`javafx.cachedir`をフォークごとに分ける`<argLine>`（`@{argLine}`と空の`<argLine>`プロパティ）
- [ ] jpackageの`appVersion`が`1.0.0`固定。仮の値だと分かるコメントを添える
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


## null安全

- [ ] jOOQのnullable注釈を使う。コード生成で `nullableAnnotationType=org.jspecify.annotations.Nullable` を出力し、NullAwayに `AcknowledgeRestrictiveAnnotations=true` を足す。jOOQ本体のJetBrains注釈（`fetchOne()` など）も有効になるので、まず `WARN` で件数を見る。
