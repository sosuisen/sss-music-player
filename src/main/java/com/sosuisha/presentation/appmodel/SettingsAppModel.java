package com.sosuisha.presentation.appmodel;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import com.sosuisha.domain.exception.SettingsException;
import com.sosuisha.domain.model.RepeatMode;
import com.sosuisha.domain.model.Settings;
import com.sosuisha.domain.model.Theme;
import com.sosuisha.domain.service.SettingsRepository;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Application-wide state of the settings shared by multiple screens. The
 * {@link Settings} record is only assembled and taken apart at the
 * persistence boundary; the screens observe the individual properties.
 */
public class SettingsAppModel {
    private final ObjectProperty<Path> musicLibraryPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>(Theme.PRIMER_LIGHT);
    private final ObjectProperty<RepeatMode> repeatMode =
        new SimpleObjectProperty<>(RepeatMode.ALL);
    private final SettingsRepository repository;

    /**
     * Creates the app model. When the theme or the repeat mode changes, the
     * settings are saved with the new value.
     *
     * @param repository repository that persists the settings
     * @throws NullPointerException if repository is null
     */
    public SettingsAppModel(SettingsRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        theme.subscribe((_, _) -> save());
        repeatMode.subscribe((_, _) -> save());
    }

    /**
     * Returns the music library path property. It holds the path of the music
     * library folder, or null when no folder has been chosen yet.
     *
     * @return object property of the music library path
     */
    public ObjectProperty<Path> musicLibraryPathProperty() {
        return musicLibraryPath;
    }

    /**
     * Returns the theme property. It holds the current theme of the
     * application. The initial value is {@link Theme#PRIMER_LIGHT}.
     *
     * @return object property of the current theme
     */
    public ObjectProperty<Theme> themeProperty() {
        return theme;
    }

    /**
     * Returns the repeat mode property. It holds the repeat mode of the
     * playback. The initial value is {@link RepeatMode#ALL}.
     *
     * @return object property of the repeat mode
     */
    public ObjectProperty<RepeatMode> repeatModeProperty() {
        return repeatMode;
    }

    /**
     * Loads the settings from the settings file and applies them to this app
     * model: the music library path, the theme, and the repeat mode follow the
     * loaded settings.
     *
     * @return loaded settings, or an empty optional when the settings file
     *         does not exist
     * @throws SettingsException if the settings file exists but cannot be read
     */
    public Optional<Settings> loadSettings() {
        try {
            var loaded = repository.load();
            musicLibraryPath.set(loaded.musicLibraryPath());
            theme.set(loaded.theme());
            repeatMode.set(loaded.repeatMode());
            return Optional.of(loaded);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new SettingsException("Failed to load the settings file", e);
        }
    }

    /**
     * Saves the current settings (the music library path, the theme, and the
     * repeat mode) to the settings file. Does nothing when the music library
     * path is not set.
     *
     * @throws SettingsException if the settings file cannot be written. Since
     *             save() is also triggered by property listeners, the exception
     *             is not caught by the callers; App catches it with the
     *             uncaught exception handler of the FX thread and shows an
     *             error dialog
     */
    public void save() {
        var path = musicLibraryPath.get();
        if (path == null) { return; }
        try {
            repository.save(new Settings(path, theme.get(), repeatMode.get()));
        } catch (IOException e) {
            throw new SettingsException("Failed to save the settings file", e);
        }
    }
}
