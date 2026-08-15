package com.sosuisha.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.service.LibraryDatabase;

/**
 * Indexes a music library: scans the audio files of a folder and maintains
 * their metadata cache in the library database.
 */
public class LibraryIndexer {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".mp3", ".m4a");

    private final LibraryDatabase database;

    /**
     * Creates the indexer.
     *
     * @param database library database used as a metadata cache
     * @throws NullPointerException if database is null
     */
    public LibraryIndexer(LibraryDatabase database) {
        this.database = Objects.requireNonNull(database, "database must not be null");
    }

    /**
     * Returns all supported audio files (mp3 and m4a) with their sizes and
     * track metadata in the given folder and its subfolders. The extension is
     * matched ignoring case. A hidden file whose name starts with a dot is
     * skipped. A file whose tag cannot be read gets {@link TrackMetadata#EMPTY}.
     * After the scan, a database entry whose file no longer exists is deleted.
     *
     * @param folderPath path of the folder to scan
     * @return list of audio files in the folder and its subfolders
     * @throws NullPointerException if folderPath is null
     * @throws IOException if the folder cannot be read
     * @throws UncheckedIOException if the size of a found file cannot be read
     */
    public List<MusicFile> scan(Path folderPath) throws IOException {
        return scan(folderPath, _ -> {
        });
    }

    /**
     * Scans like {@link #scan(Path)} and also notifies the path of each file to
     * the given callback just before the file is read.
     *
     * @param folderPath path of the folder to scan
     * @param onFileRead callback that receives the path of each file being read
     * @return list of audio files in the folder and its subfolders
     * @throws NullPointerException if folderPath or onFileRead is null
     * @throws IOException if the folder cannot be read
     * @throws UncheckedIOException if the size of a found file cannot be read
     */
    public List<MusicFile> scan(Path folderPath, Consumer<Path> onFileRead) throws IOException {
        Objects.requireNonNull(folderPath, "folderPath must not be null");
        Objects.requireNonNull(onFileRead, "onFileRead must not be null");
        try (var files = Files.walk(folderPath)) {
            var musicFiles = files
                .filter(Files::isRegularFile)
                .filter(path -> !isHiddenFile(path))
                .filter(LibraryIndexer::isSupportedAudioFile)
                .map(path -> toMusicFile(path, onFileRead))
                .toList();
            deleteEntriesOfMissingFiles();
            return musicFiles;
        }
    }

    private void deleteEntriesOfMissingFiles() {
        for (var path : database.findAllPaths()) {
            if (!Files.exists(path)) {
                database.delete(path);
            }
        }
    }

    private MusicFile toMusicFile(Path path, Consumer<Path> onFileRead) {
        onFileRead.accept(path);
        try {
            var size = Files.size(path);
            var lastModified = Files.getLastModifiedTime(path);
            var cached = database.find(path, size, lastModified);
            if (cached.isPresent()) { return new MusicFile(path, size, cached.get()); }
            var musicFile = new MusicFile(path, size, readTrackMetadata(path));
            database.save(musicFile, lastModified);
            return musicFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static TrackMetadata readTrackMetadata(Path path) {
        try {
            var tag = AudioFileIO.read(path.toFile()).getTag();
            if (tag == null) { return TrackMetadata.EMPTY; }
            return new TrackMetadata(
                tag.getFirst(FieldKey.TITLE),
                tag.getFirst(FieldKey.ARTIST),
                tag.getFirst(FieldKey.ALBUM),
                tag.getFirst(FieldKey.ALBUM_ARTIST),
                tag.getFirst(FieldKey.TRACK),
                tag.getFirst(FieldKey.YEAR)
            );
        } catch (Exception e) {
            // A file whose tag cannot be read is kept in the library with empty metadata.
            return TrackMetadata.EMPTY;
        }
    }

    private static boolean isHiddenFile(Path path) {
        return path.getFileName().toString().startsWith(".");
    }

    private static boolean isSupportedAudioFile(Path path) {
        var name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
