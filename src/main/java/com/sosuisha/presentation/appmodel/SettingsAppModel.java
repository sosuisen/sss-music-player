package com.sosuisha.presentation.appmodel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.util.Objects;
import java.util.Optional;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.domain.model.Theme;
import com.sosuisha.domain.service.SettingsRepository;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Application-wide state of the settings shared by multiple screens.
 */
public class SettingsAppModel {
    private final ObjectProperty<Settings> settings = new SimpleObjectProperty<>();
    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>(Theme.PRIMER_LIGHT);
    private final ObjectProperty<Throwable> error = new SimpleObjectProperty<>();
    private final SettingsRepository repository;

    /**
     * Creates the app model.
     *
     * @param repository repository that persists the settings
     * @throws NullPointerException if repository is null
     */
    public SettingsAppModel(SettingsRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    /**
     * Returns the settings property.
     *
     * @return object property of the settings
     */
    public ObjectProperty<Settings> settingsProperty() {
        return settings;
    }

    /**
     * Sets the settings.
     *
     * @param settings settings to set
     * @throws NullPointerException if settings is null
     */
    public void setSettings(Settings settings) {
        this.settings.set(Objects.requireNonNull(settings, "settings must not be null"));
    }

    /**
     * Returns the settings.
     *
     * @return the settings
     */
    public Settings getSettings() {
        return settings.get();
    }

    /**
     * Loads the settings from the settings file and applies them to this app
     * model. The theme property is also updated to the loaded theme.
     *
     * @return loaded settings, or an empty optional when the settings file
     *         does not exist
     * @throws UncheckedIOException if the settings file exists but cannot be read
     */
    public Optional<Settings> loadSettings() {
        try {
            var loaded = repository.load();
            setSettings(loaded);
            theme.set(loaded.theme());
            return Optional.of(loaded);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Sets the given settings and saves them to the settings file. When the
     * settings file cannot be written, the error is set to the error property;
     * when it is saved, the error property is cleared.
     *
     * @param settings settings to save
     * @throws NullPointerException if settings is null
     */
    public void saveSettings(Settings settings) {
        setSettings(settings);
        try {
            repository.save(settings);
            error.set(null);
        } catch (IOException e) {
            error.set(e);
        }
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
     * Returns the error property. It holds the latest error of a settings
     * service call, or null when no error has occurred.
     *
     * @return object property of the error
     */
    public ObjectProperty<Throwable> errorProperty() {
        return error;
    }
}
