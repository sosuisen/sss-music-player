# ADR 004: GraalVM native imageのexeだけを配布する

## Status

Accepted

## Context

SSS Music Playerは、音楽ファイルを整理し、再生するJavaFXアプリである。主なユーザーは開発者自身で、日常的にPCで音楽を聴く。音楽を聴きたいと思ったときにさっと開く使い方をするので、起動の待ち時間は短いほうがよい。

これまでは、jpackageでアプリのイメージを作る設定が`pom.xml`にあった。アプリのイメージは、JDKのランタイムを同梱したフォルダである。CIはまだなく、配布の仕組みも整っていなかった。この配り方では、起動のたびにJVMが立ち上がる。

GraalVMのnative imageを使えば、Javaのアプリを、JVMなしで動く実行ファイルにできる。JavaFXのアプリをnative imageにするには、JavaFXの静的なライブラリが要る。その手段は、Gluonの道具か、HebiRoboticsのStaticFX（`jfx-static-feature`と`jfx-static-libs`）である。StaticFXは、`javafx.media`にも対応している。ただしmediaの静的なライブラリはなく、JavaFXのjarに入っているDLLをexeの隣に置いて動的にリンクする。

StaticFXには、JavaFXの版ごとに静的なライブラリが用意される。2026-11-01の時点では、最新の`27-2`はJavaFX `27`用である。このアプリは、すでに`27`を使っていた。

native imageでは、リフレクション、リソース、JNIのメソッドを、ビルド時に知らせる必要がある。知らせ忘れても、ビルドもテストも通る。失敗するのは、exeを実行したときである。この情報は、機能が変わるたびに作り直す必要がある。2026-11-01の時点で、ストーリーマップの主なタスクは実装済みで、機能はほぼ固まっている。

同じ構成（GraalVM CE 25.4とStaticFX）は、同じ作者のsss-wallpaperで2026-10-31に動くことが確かめられている。手元のWindowsでも、GitHub Actionsでも、exeを作れた。

## Decision

配布物を、GraalVMのnative imageで作るexeだけにする。jpackageのアプリのイメージは、作るのも配るのもやめる。

ビルドには、GraalVM CE 25.4を使う。Oracle GraalVMは使わない。Oracle GraalVMのライセンスはGFTCで、配布する前に条件を確かめる必要があるためである。CEのライセンスは、GPLv2とClasspath Exceptionである。

JavaFXの静的なライブラリには、StaticFXを使う。`jfx-static-feature`は1.1.1、`jfx-static-libs`は`27-2`とする。JavaFXの版は、`jfx-static-libs`に合わせて、プロジェクト全体で`27`に固定する。テストする版と配る版を、同じにするためである。

ネイティブビルドは、Mavenの`native`プロファイルに置く。`mvn -Pnative package`で、`native-maven-plugin`がexeを作る。ふだんの`mvn package`では、exeを作らない。テストは、プロファイルなしのまま、TemurinのJDKで動かす。GraalVMが要るのは、exeを作るときだけである。

native imageに知らせる情報は、`src/main/resources/META-INF/native-image/com.sosuisha/sss-music-player/reachability-metadata.json`に置き、リポジトリで管理する。このファイルは、トレーシングエージェントで作る。GUIをテーマごとに起動する経路、設定ファイルなしの経路、設定ファイルが読めない経路、再生の経路に加えて、テストスイートもエージェント付きで動かし、結果を1つに結合する。テストスイートは、画面の操作、重複の除去、タグの書き込みなど、手で通しにくい経路を広く通るためである。新しいリソースや新しいJavaFXのコントロールを使うとき、またはリフレクションを使うライブラリの版を上げるときに、作り直す。

CIでは、`ci.yml`の`package`ジョブが、mainへのpushでexeを作り、1回起動する。このアプリにはウィンドウなしの経路がないので、起動してウィンドウが開くのを待ち、ウィンドウを閉じて終了コードを確かめる。メタデータの不足は実行時にしか分からないので、ここで早く見つける。`release.yml`は、タグでテストとexeのビルドをやり直し、zipにしてGitHub Releaseに置く。

zipには、exeと、その隣に出力されるDLLを入れる。DLLは、`javafx.media`とAWTのために要る。版は、zipの名前とReleaseの題にだけ入れる。exeには版を埋め込まない。

exeのアイコンは、Windowsのリソースとして埋める。`src/windows/sss-music-player.rc`をコンパイルした`.res`を、`-H:NativeLinkerOption`でリンカーに渡す。`.res`はリポジトリで管理する。コンパイルに使う`rc.exe`は、ふつうのシェルにもCIにもPATHが通っていないためである。

## Alternatives

### Alternative 1: jpackageのアプリのイメージを配る

`pom.xml`にあった設定をそのまま使う。メタデータを管理する必要がなく、JavaFXの版も自由に選べる。しかし、起動のたびにJVMが立ち上がるので、起動の速さという目的を果たせない。

### Alternative 2: jpackageとネイティブの両方を配る

利用者が、どちらかを選べる。ネイティブで問題が起きたときの逃げ道にもなる。しかし、CIの時間と、確かめる手間が2倍になる。1人で開発するアプリに、2つの配り方は重い。

### Alternative 3: Gluonの道具（GluonFX、Gluon版GraalVM）を使う

JavaFXのnative imageでは、いちばん実績のある道具である。しかし、対応する版が古いまま更新されていない。Gluon版GraalVMの最新は`gluon-23+25.1-dev`で、2024-09のものである。Java 25とJavaFX 27に対応するという記述は、見つからなかった。

### Alternative 4: Oracle GraalVMを使う

GraalVMの機能がすべて使え、性能も高いとされる。StaticFXの対応表も、Oracle GraalVM向けである。しかし、ライセンスがGFTCで、配布する前に条件を確かめる必要がある。sss-wallpaperでCEで動いたので、確かめる手間をかける理由がない。

### Alternative 5: Liberica NIKを使う

JavaFX入りのNIK-fullがある。しかし、同梱されるJavaFXは独自のビルドで、Maven上のJavaFXとは合わない。テストする版と配る版を、同じにできない。

### Alternative 6: メタデータをGUIの手動操作だけで集める

sss-wallpaperでは、GUIを人が操作して集めた。しかし、このアプリの経路は多い。再生、重複の除去、タグの書き込み、アルバムの編集などを毎回手で通すのは、作り直しのたびに重い。テストスイートをエージェント付きで動かせば、これらの経路を機械的に通せる。テストの道具（JUnit、TestFX）の記録はフィルタで除く。

## Confidence Level

確信度は中程度である。

同じ構成がsss-wallpaperで動いている。手元のWindowsで、exeを作って起動できた。技術的に実現できることには、確信がある。

一方で、次の点が不確かである。

StaticFXは非公式のビルドである。HebiRoboticsが更新をやめれば、JavaFXの版を上げられなくなる。また、StaticFXの対応表はOracle GraalVM向けで、CEで動くのは試した結果であって、保証ではない。

`javafx.media`は動的にリンクされる。sss-wallpaperでは使っていなかったので、このアプリで初めて試した。再生の経路はトレーシングエージェントとexeの手動確認で確かめたが、すべての音楽ファイルで動くとは言い切れない。

テストスイートからの記録は、テスト用のクラスの名前も含みうる。結合のときに除いているが、除き漏れはnative-imageのビルドで警告として現れる。

起動が速くなるという目的は、まだ測っていない。

次のときは、この決定を見直す。

- StaticFXの更新が止まり、JavaFXの版を上げられなくなったとき。
- GraalVM CEで、StaticFXが動かなくなったとき。
- JavaFXが、公式にnative imageへ対応したとき。
- 機能を大きく足すことになり、メタデータの作り直しの手間が、起動の速さに見合わなくなったとき。

## Consequences

### 良い結果

起動のたびにJVMが立ち上がらなくなる。

配布物から、JDKのランタイムがなくなる。zipの中身は、exeとDLLだけになる。

CIとリリースの仕組みが、同時に整う。

### 悪い結果

メタデータを管理する手間が増える。動的な使い方が増えるたびに、トレーシングエージェントで作り直す必要がある。作り直しを忘れても、ビルドもテストも通る。不足に気づくのは、exeを実行したときになる。そのため、手順はREADMEの「Metadata」に残し、CIでexeを1回起動する。

CIで確かめられるのは、起動から最初のウィンドウまでである。CIには音楽ファイルがないので、走査、再生、タグの書き込みの不足は、CIでは見つからない。

JavaFXの版は、StaticFXが出す静的なライブラリに縛られる。

exeを作るには、GraalVM CE 25.4とVisual Studioが要る。手元でexeを作るときは、`JAVA_HOME`をGraalVMに切り替える必要がある。

exeには版の情報がない。

アイコンの`.res`は生成物だが、リポジトリに置く。アイコンを変えたときは、手で作り直す必要がある。

### 中立的な結果

`pom.xml`から、jpackageのための設定を消す。依存のコピー、jarのマニフェスト、jpackage、読み取り専用の解除、OSごとのプロファイルである。

READMEに「Install」「How to Build the Native Exe」「Release」の節を書く。
