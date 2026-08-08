package com.sosuisha.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Scans a music library folder.
 */
public class LibraryScanner {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".mp3", ".m4a");

    /**
     * Returns the paths of all supported audio files (mp3 and m4a) in the given
     * folder and its subfolders. The extension is matched ignoring case.
     *
     * @param folderPath path of the folder to scan
     * @return list of audio file paths in the folder and its subfolders
     * @throws NullPointerException if folderPath is null
     * @throws IOException if the folder cannot be read
     */
    public List<Path> scan(Path folderPath) throws IOException {
        Objects.requireNonNull(folderPath, "folderPath must not be null");
        try (var files = Files.walk(folderPath)) {
            return files
                .filter(Files::isRegularFile)
                .filter(LibraryScanner::isSupportedAudioFile)
                .toList();
        }
    }

    private static boolean isSupportedAudioFile(Path path) {
        var name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
