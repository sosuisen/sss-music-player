package com.sosuisha.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import com.sosuisha.domain.exception.RepositoryException;
import com.sosuisha.domain.model.RepeatMode;
import com.sosuisha.domain.model.Settings;
import com.sosuisha.domain.model.Theme;
import com.sosuisha.domain.repository.SettingsRepository;

/**
 * Settings repository that saves and loads the settings in a properties file.
 */
public class SettingsRepositoryImpl implements SettingsRepository {
    public static final Path DEFAULT_FILE =
        Path.of(System.getProperty("user.home"), ".sss-music-player", "settings.properties");

    private static final String MUSIC_LIBRARY_PATH_KEY = "musicLibraryPath";
    private static final String THEME_KEY = "theme";
    private static final String REPEAT_MODE_KEY = "repeatMode";
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
        if (override != null) { return Path.of(override); }
        return DEFAULT_FILE;
    }

    /**
     * Creates the repository. The properties file path is resolved by
     * {@link #resolveFile()}.
     */
    public SettingsRepositoryImpl() {
        this.file = resolveFile();
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if settings is null
     * @throws RepositoryException if the file cannot be written
     */
    @Override
    public void save(Settings settings) throws RepositoryException {
        Objects.requireNonNull(settings, "settings must not be null");
        try {
            write(settings);
        } catch (IOException e) {
            throw new RepositoryException("Could not write the settings file: " + file, e);
        }
    }

    private void write(Settings settings) throws IOException {
        var parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        var properties = new Properties();
        properties.setProperty(MUSIC_LIBRARY_PATH_KEY, settings.musicLibraryPath().toString());
        properties.setProperty(THEME_KEY, settings.theme().name());
        properties.setProperty(REPEAT_MODE_KEY, settings.repeatMode().name());
        try (var writer = Files.newBufferedWriter(file)) {
            properties.store(writer, null);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return loaded settings, or an empty optional when the file does not
     *         exist or has no music library path, meaning there are no
     *         settings to load
     * @throws RepositoryException if the file cannot be read
     */
    @Override
    public Optional<Settings> load() throws RepositoryException {
        var properties = new Properties();
        try (var reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new RepositoryException("Could not read the settings file: " + file, e);
        }
        var musicLibraryPathText = properties.getProperty(MUSIC_LIBRARY_PATH_KEY);
        if (musicLibraryPathText == null) { return Optional.empty(); }
        var musicLibraryPath = Path.of(musicLibraryPathText);
        var themeName = properties.getProperty(THEME_KEY);
        var theme = themeName == null ? Theme.PRIMER_LIGHT : Theme.valueOf(themeName);
        var repeatModeName = properties.getProperty(REPEAT_MODE_KEY);
        var repeatMode =
            repeatModeName == null ? RepeatMode.ALL : RepeatMode.valueOf(repeatModeName);
        return Optional.of(new Settings(musicLibraryPath, theme, repeatMode));
    }
}
