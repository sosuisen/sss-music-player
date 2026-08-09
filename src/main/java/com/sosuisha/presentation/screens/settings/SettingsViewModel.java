package com.sosuisha.presentation.screens.settings;

import java.util.Objects;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.presentation.appmodel.SettingsAppModel;

import javafx.beans.value.ObservableValue;

/**
 * ViewModel for the settings screen.
 */
public class SettingsViewModel {
    private final SettingsAppModel appModel;

    /**
     * Creates the view model.
     *
     * @param appModel application-wide state of the settings
     * @throws NullPointerException if appModel is null
     */
    public SettingsViewModel(SettingsAppModel appModel) {
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
    }

    /**
     * Returns the settings.
     *
     * @return the settings
     */
    public Settings getSettings() {
        return appModel.getSettings();
    }

    /**
     * Returns the music library path as an observable string. The value follows
     * the settings of the app model.
     *
     * @return observable string of the music library path
     */
    public ObservableValue<String> musicLibraryPathProperty() {
        return appModel.settingsProperty()
            .map(settings -> settings.musicLibraryPath().toString());
    }
}
