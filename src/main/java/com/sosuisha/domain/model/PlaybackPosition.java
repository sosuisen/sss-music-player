package com.sosuisha.domain.model;

import java.time.Duration;
import java.util.Objects;

/**
 * Position of the playback in the loaded audio file, and the length of the
 * file.
 *
 * @param position time from the start of the file
 * @param length length of the file
 */
public record PlaybackPosition(Duration position, Duration length) {
    /**
     * Creates the playback position.
     *
     * @throws NullPointerException if position or length is null
     */
    public PlaybackPosition {
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(length, "length must not be null");
    }
}
