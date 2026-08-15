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

## How to Test

```bash
mvn test
```

## How to Package

```bash
mvn package
```

This creates a native application image with jpackage under `target/jpackage/`.
