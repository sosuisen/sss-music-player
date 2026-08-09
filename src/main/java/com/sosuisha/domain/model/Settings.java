package com.sosuisha.domain.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Settings of the application.
 *
 * @param musicLibraryPath path of the music library folder
 */
public record Settings(Path musicLibraryPath) {
    /**
     * Creates the settings.
     *
     * @param musicLibraryPath path of the music library folder
     * @throws NullPointerException if musicLibraryPath is null
     */
    public Settings {
        Objects.requireNonNull(musicLibraryPath, "musicLibraryPath must not be null");
    }
}
