package com.sosuisha.domain.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Settings of the application.
 *
 * @param musicLibraryPath path of the music library folder
 * @param theme color theme of the application
 */
public record Settings(Path musicLibraryPath, Theme theme) {
    /**
     * Creates the settings.
     *
     * @param musicLibraryPath path of the music library folder
     * @param theme color theme of the application
     * @throws NullPointerException if musicLibraryPath or theme is null
     */
    public Settings {
        Objects.requireNonNull(musicLibraryPath, "musicLibraryPath must not be null");
        Objects.requireNonNull(theme, "theme must not be null");
    }

    /**
     * Creates the settings with the default theme {@link Theme#PRIMER_LIGHT}.
     *
     * @param musicLibraryPath path of the music library folder
     * @throws NullPointerException if musicLibraryPath is null
     */
    public Settings(Path musicLibraryPath) {
        this(musicLibraryPath, Theme.PRIMER_LIGHT);
    }
}
