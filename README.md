# SSS Music Player

A music file manager built with JavaFX.

- Scans a folder tree for mp3 and m4a files.
- Detects duplicates by file name, size, and metadata.
- Edits metadata.
- Plays music with a basic built-in player.

## Requirements

- Java 25
- Maven

JavaFX libraries are downloaded by Maven, so no extra install is needed.

## How to Run

```bash
mvn javafx:run
```

## JavaFX Preview Features

This app uses `StageStyle.EXTENDED` and `HeaderBar`, which are preview
features of JavaFX 26. They need the JVM option
`-Djavafx.enablePreview=true`.

The option is already set in `pom.xml` for `mvn javafx:run`, `mvn test`,
and the packaged app (jpackage).

When you run the app with the VS Code Debugger for Java, the option is
not applied automatically. Add `.vscode/launch.json` with the following
content (`.vscode/` is not committed, so each developer adds it):

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Launch sss-music-player",
      "request": "launch",
      "mainClass": "com.sosuisha.main.Launcher",
      "projectName": "sss-music-player",
      "vmArgs": "-Djavafx.enablePreview=true"
    }
  ]
}
```

## How to Test

```bash
mvn test
```

## How to Package

```bash
mvn package
```

This creates a native application image with jpackage under `target/jpackage/`.
