# SSS Music Player

A music file manager built with JavaFX.

- Scans a folder tree for mp3 and m4a files.
- Detects duplicates by file name, size, and metadata.
- Edits metadata.
- Plays music with a basic built-in player.

## Requirements

- Windows 10 or later

To build from source, you also need:

- Java 25
- Maven

JavaFX libraries are downloaded by Maven, so no extra install is needed.

To build the native exe, you also need:

- GraalVM CE 25.4
- The C++ build tools of Visual Studio

## Install

1. Download `sss-music-player-<version>.zip` from [GitHub Releases](https://github.com/sosuisen/sss-music-player/releases).
2. Unzip it anywhere. It holds `sss-music-player.exe` and some `dll` files.
3. Run `sss-music-player.exe`.

The exe uses the `dll` files in the same folder. It does not run alone.

The exe is a GraalVM native image. You do not need to install a JDK.

## How to Run

```bash
mvn javafx:run
```

## How to Test

```bash
mvn test
```

## How to Build the Native Exe

The exe is built with GraalVM native image, so it starts without a JVM. The reasons are in [ADR 4](docs/adr/adr-004-native-image-distribution.md).

Set `JAVA_HOME` to GraalVM CE 25.4 and build with the `native` profile.

```bash
JAVA_HOME=<GraalVM CE 25.4 folder> mvn -Pnative package
```

- The output is under `target/`.
- The exe is `target/sss-music-player.exe`.
- To use it, copy the exe and the `dll` files next to it together. The exe does not run alone.

`mvn clean` deletes `target/`, so copy the files before you clean.

`mvn package` without the `native` profile does not build the exe.

### Metadata

Native image must know at build time which classes are used by reflection, which resources are read, and which native methods are called. That information is in `src/main/resources/META-INF/native-image/com.sosuisha/sss-music-player/reachability-metadata.json`.

A missing entry does not break the build or the tests. It breaks the exe at run time. So rebuild the file when:

- A new resource is read, such as a CSS file or an image.
- A new JavaFX control is used.
- JavaFX, AtlantaFX, Ikonli, jOOQ, sqlite-jdbc, or jaudiotagger is upgraded.
- A new start route or error route is added.

To rebuild it, run the app on the GraalVM JVM with the tracing agent. The agent writes what it sees into the output folder.

1. Compile with `mvn compile`.
2. Build the runtime class path. Remove the test jars (junit, testfx, hamcrest, assertj, opentest4j, apiguardian, osgi, nullaway-annotations) and the `us.hebi` jars from it, and put `target/classes` first.

   ```bash
   mvn -q -B -Pnative dependency:build-classpath -Dmdep.includeScope=runtime -Dmdep.outputFile=<temp file>
   ```

3. Run every route of the app with the agent. Use `config-merge-dir` so that all runs land in one file.

   ```bash
   <GraalVM>/bin/java -agentlib:native-image-agent=config-merge-dir=<output folder> -cp "<class path>" com.sosuisha.main.Launcher
   ```

   - Start with a settings file for each theme. Scan a library that holds mp3 and m4a files, play a track, pause and resume it, edit an album, and remove duplicates.
   - Start without a settings file, so that the settings window opens first.
   - Start with `-Dsss.settings.file` pointing to a folder, so that the error dialog opens.

4. Run the test suite with the agent too. It covers routes that are hard to reach by hand. The test JVMs run in parallel, so each gets its own output folder.

   ```bash
   JAVA_HOME=<GraalVM> mvn test "-Dtest.jvm.options=-agentlib:native-image-agent=config-output-dir=<output folder>-{pid},config-write-period-secs=5,access-filter-file=<filter>,caller-filter-file=<filter>"
   ```

   The filter excludes the test frameworks (`org.junit.**`, `org.testfx.**`, `org.hamcrest.**`, `org.opentest4j.**`, `org.apiguardian.**`, `org.assertj.**`, `org.apache.maven.**`, `com.uber.nullaway.**`).

5. Merge all output folders into the metadata file, and remove the entries for test classes (`com.sosuisha.**Test*`, `Null*` test doubles).

   ```bash
   <GraalVM>/bin/native-image-configure generate --input-dir=<folder 1> --input-dir=<folder 2> ... --output-dir=src/main/resources/META-INF/native-image/com.sosuisha/sss-music-player
   ```

### Icon

The exe icon is `images/icon.ico`. `src/windows/sss-music-player.rc` names it, and the compiled `src/windows/sss-music-player.res` is passed to the linker.

The `.res` file is committed, because `rc.exe` is not on the PATH of a normal shell or of CI. When the icon changes, rebuild it with `rc.exe` from the Windows SDK.

```powershell
cd src\windows
& "C:\Program Files (x86)\Windows Kits\10\bin\<version>\x64\rc.exe" /fo sss-music-player.res sss-music-player.rc
```

## Release

The app version comes from the Git tag only. Do not change the version in `pom.xml`.

To release, create a tag that starts with `v` and push it.

```bash
git tag v1.2.0
git push origin v1.2.0
```

The push starts the `release.yml` workflow of GitHub Actions.

1. It takes `1.2.0` from the tag name.
2. It runs the tests and builds the native exe.
3. It starts the exe once, waits for the window, and closes it.
4. It zips the exe and the `dll` files.
5. It creates a GitHub Release with `sss-music-player-1.2.0.zip` attached.

The version appears only in the zip name and the release title. The exe has no version inside.

A tag must look like `v1.2.0`: `v`, digits, and dots only. A tag of another form fails in the first step of the workflow.

A push to `main` also builds the exe after the tests, and starts it once. The exe can be downloaded from the "Artifacts" of the workflow run for 7 days.
