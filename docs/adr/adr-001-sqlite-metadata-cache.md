# ADR 001: SQLite Metadata Cache for Library Scan

## Status

Accepted

## Context

SSS Music Player scans the music library folder at startup and whenever the library folder setting changes. The scan now loads track metadata (title, artist, album, album artist, track number, and year) from every mp3 and m4a file with the jaudiotagger library. Loading metadata opens and parses each file, so a scan is much slower than just listing file paths and sizes. Even with a small library, the startup scan takes more than one minute.

A library is expected to hold about 1,000 files. Metadata-based duplicate detection needs the metadata of all files, so the app cannot skip loading metadata. A future feature may let the user edit metadata and save it. The app already stores its settings in `~/.sss-music-player/settings.properties`, and there is no database in the project yet.

## Decision

We will cache track metadata in a SQLite database. The database file is `~/.sss-music-player/library.db`. We position this file as the library database of the app, not as a throwaway cache. The metadata cache is its first use, and a future metadata editing feature will save into the same database.

During a scan, the app looks up each file in the cache by its path. When the cached row has the same file size and the same last-modified time as the file on disk, the app uses the cached metadata and does not parse the file. Otherwise the app parses the file with jaudiotagger and writes the fresh metadata to the cache.

We will access SQLite through jOOQ with the sqlite-jdbc driver.

## Alternatives

### Alternative 1: No cache

Parse the metadata of every file on every scan. This is the current behavior and the simplest option. It was rejected because even a small library already takes more than one minute to scan at startup, and the same wait repeats on every launch even when no file has changed.

### Alternative 2: Plain text cache file (tab-separated or properties format)

Store the cache in a text file under `~/.sss-music-player/`, in the same style as the settings file. This needs no new dependency and is easy to inspect. It was rejected because a future metadata editing feature would outgrow it: the whole file must be rewritten on every change, and escaping free-form text such as song titles is error-prone.

### Alternative 3: JSON cache file

Store the cache as JSON. The structure is clearer than a hand-made text format, but it still rewrites the whole file on every change and adds a JSON library dependency without solving the editing use case. It was rejected for the same reasons as Alternative 2.

### Alternative 4: Plain JDBC instead of jOOQ

Access SQLite with the sqlite-jdbc driver and hand-written SQL. This keeps the dependency count lower. It was rejected because jOOQ gives type-safe queries, and the team already has working experience with jOOQ from an earlier project.

### Alternative 5: Cache validation by path only, or by content hash

Using only the path is the fastest check, but it misses files whose tags were changed by an external tool. Hashing the file content detects every change, but it reads every file in full, which defeats the purpose of the cache. Comparing path, size, and last-modified time is the middle ground and was chosen.

## Confidence Level

High. The startup delay is measured, not guessed, and a persistent cache is the standard cure. SQLite and jOOQ are proven tools, and the future metadata editing feature strengthens the case for a real database.

Revisit this decision when one of the following happens. The metadata editing feature is designed and its storage needs do not fit the current schema. The jOOQ code generation step becomes a burden in the build. Cache validation by size and last-modified time turns out to miss real changes in practice.

## Consequences

Positive. When no file has changed, the startup scan only checks file sizes and timestamps and reads the rest from the database, so it becomes fast. Rescans triggered by settings changes or duplicate removal get the same speedup. The app also gains a library database that future features, such as metadata editing, can build on.

Negative. The build gains two dependencies (sqlite-jdbc and jOOQ) and a jOOQ code generation step. The scan logic gets a second path — cache hit and cache miss — and tests must cover both. A corrupted database file can break scanning; deleting `library.db` must always be a safe recovery, because the app can rebuild it from the files.

Neutral. A new file `~/.sss-music-player/library.db` appears on the user's machine. The cache lookup joins the scan flow in the service layer, and the wiring follows the existing manual constructor injection in `App.start()`.
