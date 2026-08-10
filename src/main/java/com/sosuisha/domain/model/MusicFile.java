package com.sosuisha.domain.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * An audio file in the music library.
 *
 * @param path path of the audio file
 * @param size size of the audio file in bytes
 */
public record MusicFile(Path path, long size) {
    /**
     * Creates the music file.
     *
     * @throws NullPointerException if path is null
     * @throws IllegalArgumentException if size is negative
     */
    public MusicFile {
        Objects.requireNonNull(path, "path must not be null");
        if (size < 0) { throw new IllegalArgumentException("size must not be negative: " + size); }
    }
}
