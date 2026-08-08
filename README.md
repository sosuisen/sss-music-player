# SSS Music Player

A desktop music player built with JavaFX. Its main purpose is to manage a music library: find duplicate files, fix garbled tags, and play music while you work.

## Features

- **Library scan**: Scans a folder and its subfolders, and lists all supported audio files (mp3 and m4a). File extensions are matched ignoring case.

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
