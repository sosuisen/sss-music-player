package com.sosuisha.domain.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Settings of the application.
 *
 * @param musicLibraryPath path of the music library folder
 * @param theme color theme of the application
 * @param repeatMode repeat mode of the playback
 */
public record Settings(Path musicLibraryPath, Theme theme, RepeatMode repeatMode) {
    /**
     * Creates the settings.
     *
     * @param musicLibraryPath path of the music library folder
     * @param theme color theme of the application
     * @param repeatMode repeat mode of the playback
     * @throws NullPointerException if musicLibraryPath, theme, or repeatMode is
     *             null
     */
    public Settings {
        Objects.requireNonNull(musicLibraryPath, "musicLibraryPath must not be null");
        Objects.requireNonNull(theme, "theme must not be null");
        Objects.requireNonNull(repeatMode, "repeatMode must not be null");
    }

    /**
     * Creates the settings with the default repeat mode {@link RepeatMode#ALL}.
     *
     * @param musicLibraryPath path of the music library folder
     * @param theme color theme of the application
     * @throws NullPointerException if musicLibraryPath or theme is null
     */
    public Settings(Path musicLibraryPath, Theme theme) {
        this(musicLibraryPath, theme, RepeatMode.ALL);
    }

    /**
     * Creates the settings with the default theme {@link Theme#PRIMER_LIGHT}
     * and the default repeat mode {@link RepeatMode#ALL}.
     *
     * @param musicLibraryPath path of the music library folder
     * @throws NullPointerException if musicLibraryPath is null
     */
    public Settings(Path musicLibraryPath) {
        this(musicLibraryPath, Theme.PRIMER_LIGHT);
    }
}
