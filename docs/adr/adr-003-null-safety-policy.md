# ADR 003: null安全の方針

## Status

Accepted

## Context

SSS Music Player の NPE 対策は契約プログラミングである。public メソッドの冒頭で `Objects.requireNonNull` により引数を検査し、違反は実行時例外として `@throws` に記す。この方式は実行時にしか働かず、null を渡す呼び出しはコンパイルでは検出されない。また、どの引数が null を許すのかがシグネチャに現れず、Javadoc の文章か `requireNonNull` の有無を読んで判断するしかない。

Java には null 性を型に載せる標準として JSpecify 1.0 がある。Spring Framework 7 と Spring Boot 4 は API 全体を JSpecify で注釈し、自身のビルドを NullAway で検査している。アプリケーション開発者にも NullAway の利用を強く推奨している。JDK 本体への取り込み（JEP draft 8303099）は進行中で、Java 25 にはまだ入っていない。

JSpecify の注釈自体は何も強制せず、検査は別のツールが行う。候補は、javac のプラグインとして動く NullAway（Error Prone 上で動作）、同じく javac プラグインの Checker Framework、そして IDE の検査（IntelliJ IDEA、Eclipse JDT）である。本プロジェクトは VS Code と Red Hat の Java 拡張（JDT ベース）で開発しており、JDT の null 解析は JSpecify の注釈を読めるが、解釈が JSpecify 仕様と一部異なる。具体的には、`@NullUnmarked` を認識しない、`@NullMarked` を型引数に適用しない、フィールドの null チェックを引数渡しに反映しない、未注釈ライブラリからの値を non-null に渡すたびに警告する、という違いがあり、JavaFX のような未注釈ライブラリを多用する本プロジェクトでは警告が 100 件を超えた。

コードベースには、`@Nullable` を付けるだけでは済まない箇所もあった。null を「まだ値がない」状態の表現に使うフィールドや、JavaFX の選択解除で null が渡ってくるメソッド、`Path.getParent()` や `Throwable.getMessage()` のように JDK が null を返し得る呼び出しである。null 性の注釈は、これらの構造を見直す機会でもある。

## Decision

我々は、null 性を JSpecify の注釈で表し、Maven ビルドの NullAway でその整合性を検査する。

**検査の正は NullAway とする。** `maven-compiler-plugin` に Error Prone と NullAway を組み込み、NullAway の指摘をコンパイルエラー（`-Xep:NullAway:ERROR`）にする。Error Prone の内蔵チェックはすべて無効にし、NullAway だけを使う。検査対象は `AnnotatedPackages=com.sosuisha` で指定し、テストコードも同じ規則で検査する。VS Code の JDT による null 解析は無効にし、IDE とビルドで異なる規則が並立しないようにする。

**null 安全のための設定は最小にする。** `package-info.java` の `@NullMarked` は置かない。NullAway は `AnnotatedPackages` の範囲を注釈済みとして扱うので、同じ情報を二重に持たない。jOOQ の生成コードは `@Generated` を出力させ、`TreatGeneratedAsUnannotated=true` で検査対象から外す。

**既存の `Objects.requireNonNull` はすべて残す。** 静的検査は、未注釈ライブラリからのコールバック、リフレクション、テストダブルなど、すり抜ける経路を持つ。実行時の検査はそれらに対する最後の防御であり、Kotlin のコンパイラ、Guava、Spring も同じ方針である。

**`@Nullable` を付ける前に、null が生じる構造を消せないかを考える。** 「まだ値がない」を null で表すフィールドは、情報源を一本化する、Null Object を初期値にする、同時に null になるフィールドを record にまとめる、といった変更で消せることが多い。残す `@Nullable` は事実に基づく契約に限る。JavaFX のコールバック（`updateItem` など）は JavaFX の契約に従って `@Nullable` とし、Javadoc に「null で選択解除」と書かれた引数は型でもそう表す。テストの都合で `@Nullable` を付けることはしない。

**テストコードの例外は最小限の印で表す。** NullAway は、`@Nullable` でないフィールドがコンストラクタか「初期化メソッド」で必ず値を持つことを検査する。JUnit の `@BeforeEach` は標準で初期化メソッドとして扱われる。TestFX の `@Start` は標準では扱われないので、`CustomInitializerAnnotations` オプションで登録する。画面テストの共通基底クラスには、もう一段の事情がある。TestFX は `@Start` メソッドをテストクラス自身に書かれたものからしか探さず、基底クラスに書いても見つけない。そのため各サブクラスが `@Start` メソッドを持ち、基底クラスの初期化メソッドを呼ぶだけの構造になっている。NullAway はこの呼び出しを追わないので、基底クラスの初期化メソッドには `nullaway-annotations` の `@Initializer` を付けて初期化メソッドであることを示す。また、実行時契約を確かめるために意図して null を渡すテストクラスには `@NullUnmarked` を付け、JSpecify の管轄外であることを明示する。

## Alternatives

### Alternative 1: 契約プログラミングのみを続ける

`Objects.requireNonNull` による実行時検査だけで対処する現状維持。

- 利点: 設定も注釈も不要。既に全 public メソッドに適用済みで、動作実績がある
- 欠点: null を渡す呼び出しは実行してみるまで分からない。null を許す引数や戻り値がシグネチャに現れず、Javadoc の文章に頼る。Spring 7 以降のライブラリが JSpecify で注釈していく流れから取り残される

選ばなかった理由は、コンパイル時に検出できる誤りを実行時まで持ち越すことと、null 許容の情報が型に載らないことである。

### Alternative 2: JSpecify の注釈と JDT（VS Code）の null 解析だけを使う

ビルド設定を変えず、VS Code の Java 拡張が持つ Eclipse JDT の null 解析を有効にして、エディタ上の警告だけで運用する。

- 利点: pom.xml を変えずに済み、エディタで即座に指摘が出る
- 欠点: `mvn` のビルドでは何も検査されず、CI で強制できない。JDT の解釈が JSpecify 仕様と異なる（`@NullUnmarked` を認識しない、型引数に `@NullMarked` を適用しない、フィールドの null チェックを引数渡しに反映しない）。未注釈ライブラリからの値を non-null に渡すたびに警告し、JavaFX を多用する本プロジェクトでは 100 件を超えた

実際に試したうえで選ばなかった。JDT 固有の回避策を積み重ねることになり、Spring が標準とする NullAway と流儀が乖離するためである。

### Alternative 3: Checker Framework の Nullness Checker

NullAway と同じく javac プラグインとして動く検査器。JSpecify の注釈を読める。

- 利点: NullAway より厳密で、健全性を重視する。ライブラリの null 性をスタブで補える
- 欠点: コンパイル時間が数倍になる。誤検知への対処コストが高い。Spring が採用しているのは NullAway であり、参考にできる設定例が少ない

個人開発の JavaFX アプリでは NullAway の速さと設定の軽さが勝るため選ばなかった。

### Alternative 4: `package-info.java` に `@NullMarked` を置く

Spring と同じく全パッケージに `package-info.java` を置き、`@NullMarked` を宣言する。

- 利点: JSpecify の標準的な宣言であり、NullAway 以外のツール（IntelliJ、Kotlin、将来の Spring Tools）も読める
- 欠点: パッケージごとに 1 ファイル増える。新しいパッケージを作るたびに置き忘れる余地がある。NullAway の `AnnotatedPackages` と情報が重複する

一度は 16 ファイルを作ったが、null 安全のためだけの設定は最小にするという判断で削除した。NullAway 以外のツールを使う段になったら再検討する。

### Alternative 5: `@Nullable` を付けたうえで `requireNonNull` を削る

`@NullMarked` 相当の範囲では型が契約なので、内部呼び出しだけの `requireNonNull` は冗長として削除する。

- 利点: 定型的なチェックが 89 箇所減り、Javadoc の `@throws` も減る
- 欠点: 未注釈のライブラリからのコールバック、リフレクション、`@NullUnmarked` の範囲、テストダブルなど、静的検査の外から null が入る経路に対して無防備になる

静的検査と実行時検査は守備範囲が重なっても同一ではないため、両方を残す判断をした。

## Confidence Level

高い。JSpecify は Spring 7 と Spring Boot 4 が採用した事実上の標準であり、NullAway は Spring 自身のビルドで使われている。本プロジェクトでは main とテストの全体に適用し、`mvn clean test` が指摘 0 で通る状態まで到達した。

次の状況では見直す。

- JSpecify の null 性が Java 言語仕様に取り込まれたとき（JEP draft 8303099）。注釈から言語構文への移行が必要になる
- Spring Tools が Eclipse / VS Code 向けに JSpecify の設定を自動化したとき。IDE 上の検査を再度有効にし、ビルドの NullAway と併用できる可能性がある
- NullAway 以外のツール（IntelliJ、Kotlin など）でこのコードを扱うとき。`package-info.java` の `@NullMarked` を置く判断を再検討する

## Consequences

**良い結果**

- null を渡す呼び出しと、null を返し得る値の未処理が、`mvn clean test` の時点でコンパイルエラーになる
- null を許す引数・戻り値・フィールドが `@Nullable` としてシグネチャに現れ、Javadoc を読まなくても分かる
- `@Nullable` を付ける前に構造を見直した結果、null を状態として持つフィールドが減った。最後に走査したフォルダのキャッシュは設定値の参照に、未選択の検出器と未設定のコールバックは Null Object に、再生中のプレイヤーとパスは 1 つの record に置き換わった。`SettingsViewModel` は JavaFX の `Window` に依存しなくなった
- JDK の `Path.getParent()` や `Throwable.getMessage()` のように null を返し得る呼び出しが、NullAway の組み込み知識により検出される。これまで `requireNonNull` の実行時例外に頼っていた境界が、コンパイル時に見えるようになった
- Spring 7 以降のライブラリと同じ注釈を使うため、それらの API の null 性がそのまま検査に反映される

**悪い結果**

- ビルドが Error Prone と NullAway に依存する。Error Prone は javac の内部 API を使うため、`.mvn/jvm.config` による `--add-exports` の指定が必要で、JDK や Error Prone の更新時に互換性の確認が要る。実際に NullAway 0.12.7 は Error Prone 2.50.0 で動かず、0.14.0 への更新が必要だった
- VS Code の JDT による null 解析を無効にしたため、エディタ上では null に関する指摘が出ない。指摘は `mvn compile` または `mvn test` を実行して初めて分かる
- 未注釈のライブラリ（JavaFX、jaudiotagger、jOOQ 本体）の戻り値は non-null とみなされるため、そこから来る null は検出されない。この境界では `requireNonNull` や明示的な null 処理が引き続き必要になる

**中立的な結果**

- `Objects.requireNonNull` と `@throws NullPointerException` の記述は従来どおり残る。型と実行時検査の二重化ではなく、静的検査が届かない経路への防御として位置づける
- テストコードも検査対象になった。フィクスチャの初期化には `@Initializer`、契約テストには `@NullUnmarked` という印が付く
- jOOQ の生成コードは `@Generated` により検査対象外で、そこから返る値は non-null とみなされる。DB のスキーマが `NOT NULL` であることと `TrackMetadata` の `requireNonNull` がこの前提を支える
- NullAway は javac が動いたときだけ動く。`maven-compiler-plugin` は pom.xml の変更や IDE が `target/` に書いた class ファイルを再コンパイルの理由と見なさないため、`mvn compile` が javac をスキップして成功に見えることがある。NullAway の結果を確認するときは `mvn clean test` を使う
