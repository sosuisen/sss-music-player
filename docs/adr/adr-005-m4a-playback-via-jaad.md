# ADR 005: JAADでm4aを一時WAVに変換して再生する

## Status

Accepted

## Context

SSS Music Playerは、音楽ファイルを整理し、再生するJavaFXアプリである。管理する音楽ファイルの多くは、iTunesで集めたm4a（AAC）である。再生には、JavaFXの`MediaPlayer`を使っている。WindowsのJavaFXは、GStreamerとWindowsのMedia Foundationを通して音を鳴らす。

開発者のPC（Windows 11 25H2、build 26200）では、m4aがどれも再生できない。`MediaPlayer`は、開いたあとに`ERROR_MEDIA_INVALID`で失敗する。iTunesのファイルだけでなく、ffmpegで作ったAAC-LCのファイルでも同じである。JavaFX `25.0.4`、`26.0.2`、`27`、`27.0.1`のどれでも同じだった。27.0.1はmp4の扱いが改善された版だが、結果は変わらなかった。WindowsのAACデコーダー（`msmpeg2adec.dll`、`msauddecmft.dll`）は入っている。やり直しても結果は変わらない。原因は分かっていない。mp3とWAVは再生できる。

JavaFXの版は、ADR 004で、StaticFXに合わせて`27`に固定している。JavaFXの版を変えて直す道は、取りにくい。

JAADは、Javaだけで書かれたAACのデコーダーである。Java SoundのSPIとして働くので、`AudioSystem.getAudioInputStream`でm4aを読める。`com.tianscar.javasound:jaad:0.9.4`は、元のJAADを引き継いだフォークで、最終リリースは2023年5月である。試したところ、4分の曲を約1.4秒で43MBのWAVに変換できた。そのWAVは、JavaFXの`MediaPlayer`で再生できた。

## Decision

m4aを再生するときは、JAADで一時的なWAVファイルに変換し、そのWAVをJavaFXの`MediaPlayer`で再生する。依存には`com.tianscar.javasound:jaad:0.9.4`を加える。

変換するかどうかは、拡張子で決める。拡張子が`.m4a`のファイルは、JavaFXで試さずに、必ず変換する。mp3やWAVなど、ほかの形式は、これまでどおりJavaFXで直接再生する。

変換は、Java SoundのSPIを通して行う。`AudioSystem.getAudioInputStream`でm4aを読み、16bitリトルエンディアンのPCMに変換して、`AudioSystem.write`でWAVに書く。JAADのクラスを、アプリのコードから直接は呼ばない。

変換には、4分の曲で約1.4秒かかる。この間、画面を止めないように、変換はJavaFXアプリケーションスレッドの外で行う。変換が終わったら、JavaFXアプリケーションスレッドで再生を始める。変換の途中で停止や別の曲の再生が求められたら、その変換の結果は捨てる。

一時WAVは、OSの一時フォルダに作る。直前に変換した曲と同じ曲を再生するときは、変換し直さずに、その一時WAVを使い回す。1曲リピートや、停止してからの再生し直しでは、同じ曲が再生されるためである。別の曲を再生するとき、またはアプリを終了するときに、一時WAVを消す。同時に残る一時WAVは、多くても1つにする。1曲で数十MBになるためである。

変換に失敗したときは、ほかの再生の失敗と同じく、`MusicPlayer`の`onError`で理由を知らせる。JAADが扱えるのはAACだけである。ALACのm4aは変換に失敗し、画面に理由が表示される。

この処理は、`service`パッケージに置く。`MusicPlayer`のインタフェースは変えない。ViewModelは、m4aかどうかを知らなくてよい。

native imageでは、Java SoundのSPIが`ServiceLoader`で読み込まれる。この情報をメタデータに加え、exeでm4aを再生できることを手で確かめる。

## Alternatives

### Alternative 1: JAADで変換しながら、Java Soundで直接鳴らす

一時ファイルを作らず、デコードした音を`SourceDataLine`に流す。ディスクを使わず、再生の開始も早い。しかし、一時停止、シーク、再生位置、曲の終わりの通知を、すべて自分で作る必要がある。m4aとそれ以外で、再生の仕組みが2つになる。

### Alternative 2: JavaFXで再生に失敗したときだけ変換する

JavaFXでm4aを鳴らせる環境では、変換の手間がかからない。しかし、失敗は非同期に知らされるので、失敗を待ってから変換し直す流れが複雑になる。このPCでは、m4aは必ず失敗する。失敗を待つ時間が、毎回むだになる。

### Alternative 3: JavaFXとGStreamerの環境をさらに調べる

原因が分かれば、依存を足さずに直せる。しかし、JavaFX `25.0.4`から`27.0.1`まで同じ結果で、Windowsのデコーダーも入っている。原因の見当がつかず、いつ直せるか分からない。JavaFXの版は、ADR 004でStaticFXに縛られている。

### Alternative 4: ffmpegなどの外部コマンドで変換する

ALACを含め、ほぼすべての形式を変換できる。しかし、利用者のPCにffmpegが要る。exeだけを配るというADR 004の方針と合わない。

## Confidence Level

確信度は中程度である。

手元のPCで、JAADがm4aをWAVに変換し、そのWAVをJavaFXで再生できることを確かめた。4分の曲の変換は約1.4秒で、使い方として許せる長さである。技術的に実現できることには、確信がある。

一方で、次の点が不確かである。

JAADは、更新の少ないライブラリである。最終リリースは2023年5月で、AACのすべての形式に対応しているとは限らない。HE-AACなど、試していない形式がある。手持ちのm4aのすべてで変換できるとは言い切れない。

native imageで、Java SoundのSPIと`javax.sound`が動くかは、まだ試していない。

このPCでJavaFXがm4aを再生できない原因は、分かっていない。ほかのPCでは、JavaFXで直接再生できる可能性がある。その場合でも、この決定では変換を行う。

次のときは、この決定を見直す。

- JavaFXが、このPCでm4aを再生できるようになったとき。
- JAADで変換できないm4aが、多く見つかったとき。
- JAADの不具合が見つかり、更新が見込めないとき。
- native imageで、Java SoundのSPIが動かないと分かったとき。

## Consequences

### 良い結果

このPCで、m4aを再生できるようになる。手持ちの音楽ファイルの多くがm4aなので、アプリを日常的に使えるようになる。

再生の仕組みは、JavaFXの`MediaPlayer`のまま変わらない。一時停止、シーク、再生位置、曲の終わりの通知は、m4aでもこれまでの実装が使える。

`MusicPlayer`のインタフェースは変わらない。ViewModelと画面は、m4aかどうかを知らなくてよい。

### 悪い結果

m4aの再生は、始まるまでに1秒ほど待つ。変換が終わるまで音が出ない。

一時フォルダに、1曲あたり数十MBのWAVが置かれる。アプリが異常終了すると、一時WAVが残ることがある。

依存が1つ増える。JAADは更新の少ないライブラリで、不具合があっても直る見込みは低い。

ALACのm4aは、再生できない。画面には失敗の理由が表示される。

native imageのメタデータに、Java SoundのSPIの情報を加える必要がある。exeでの確認の手間が増える。

### 中立的な結果

JavaFXでm4aを直接再生できる環境でも、変換を行う。

`docs/features.md`に、m4aの再生を加える。
