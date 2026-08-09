package com.sosuisha.presentation.appmodel;

import java.util.Objects;

import com.sosuisha.domain.model.Settings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Application-wide state of the settings shared by multiple screens.
 */
public class SettingsAppModel {
    private final ObjectProperty<Settings> settings = new SimpleObjectProperty<>();

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
}
