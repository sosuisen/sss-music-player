package com.sosuisha.domain.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * An audio file in the music library.
 *
 * @param path path of the audio file
 * @param size size of the audio file in bytes
 * @param tag metadata of the audio file
 */
public record MusicFile(Path path, long size, TrackMetadata tag) {
    /**
     * Creates the music file.
     *
     * @throws NullPointerException if path or tag is null
     * @throws IllegalArgumentException if size is negative
     */
    public MusicFile {
        Objects.requireNonNull(path, "path must not be null");
        if (size < 0) { throw new IllegalArgumentException("size must not be negative: " + size); }
        Objects.requireNonNull(tag, "tag must not be null");
    }

    /**
     * Creates the music file with {@link TrackMetadata#EMPTY}.
     *
     * @param path path of the audio file
     * @param size size of the audio file in bytes
     * @throws NullPointerException if path is null
     * @throws IllegalArgumentException if size is negative
     */
    public MusicFile(Path path, long size) {
        this(path, size, TrackMetadata.EMPTY);
    }
}
