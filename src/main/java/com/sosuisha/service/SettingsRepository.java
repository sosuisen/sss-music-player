package com.sosuisha.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

import com.sosuisha.domain.model.Settings;

/**
 * Saves and loads the application settings in a properties file.
 */
public class SettingsRepository {
    /** Default properties file: {@code ~/.sss-music-player/settings.properties}. */
    public static final Path DEFAULT_FILE =
        Path.of(System.getProperty("user.home"), ".sss-music-player", "settings.properties");

    private static final String MUSIC_LIBRARY_PATH_KEY = "musicLibraryPath";
    private static final String FILE_PROPERTY = "sss.settings.file";

    private final Path file;

    /**
     * Resolves the path of the settings file. The system property
     * {@code sss.settings.file} takes precedence; otherwise {@link #DEFAULT_FILE}
     * is used.
     *
     * @return path of the settings file
     */
    static Path resolveFile() {
        var override = System.getProperty(FILE_PROPERTY);
        if (override != null) {
            return Path.of(override);
        }
        return DEFAULT_FILE;
    }

    /**
     * Creates the repository. The properties file path is resolved by
     * {@link #resolveFile()}.
     */
    public SettingsRepository() {
        this.file = resolveFile();
    }

    /**
     * Saves the given settings to the properties file.
     *
     * @param settings settings to save
     * @throws NullPointerException if settings is null
     * @throws IOException if the file cannot be written
     */
    public void save(Settings settings) throws IOException {
        Objects.requireNonNull(settings, "settings must not be null");
        var parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        var properties = new Properties();
        properties.setProperty(MUSIC_LIBRARY_PATH_KEY, settings.musicLibraryPath().toString());
        try (var writer = Files.newBufferedWriter(file)) {
            properties.store(writer, null);
        }
    }

    /**
     * Loads the settings from the properties file.
     *
     * @return loaded settings
     * @throws IOException if the file cannot be read
     */
    public Settings load() throws IOException {
        var properties = new Properties();
        try (var reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        }
        return new Settings(Path.of(properties.getProperty(MUSIC_LIBRARY_PATH_KEY)));
    }
}
