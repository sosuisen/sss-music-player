package com.sosuisha.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.sosuisha.domain.model.MusicFile;

/**
 * Scans a music library folder.
 */
public class LibraryScanner {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".mp3", ".m4a");

    /**
     * Returns all supported audio files (mp3 and m4a) with their sizes in the
     * given folder and its subfolders. The extension is matched ignoring case.
     *
     * @param folderPath path of the folder to scan
     * @return list of audio files in the folder and its subfolders
     * @throws NullPointerException if folderPath is null
     * @throws IOException if the folder cannot be read
     * @throws UncheckedIOException if the size of a found file cannot be read
     */
    public List<MusicFile> scan(Path folderPath) throws IOException {
        Objects.requireNonNull(folderPath, "folderPath must not be null");
        try (var files = Files.walk(folderPath)) {
            return files
                .filter(Files::isRegularFile)
                .filter(LibraryScanner::isSupportedAudioFile)
                .map(LibraryScanner::toMusicFile)
                .toList();
        }
    }

    private static MusicFile toMusicFile(Path path) {
        try {
            return new MusicFile(path, Files.size(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isSupportedAudioFile(Path path) {
        var name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
