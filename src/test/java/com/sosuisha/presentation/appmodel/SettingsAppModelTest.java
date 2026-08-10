package com.sosuisha.presentation.appmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.service.SettingsRepository;

class SettingsAppModelTest {
    @Test
    @DisplayName("設定をセットすると、同じ設定が取得できる")
    void returns_the_settings_that_were_set() {
        var appModel = new SettingsAppModel(new SettingsRepository());
        var settings = new Settings(Path.of("music"));

        appModel.setSettings(settings);

        assertEquals(settings, appModel.getSettings());
    }

    @Test
    @DisplayName("設定を変更すると、リスナーに新しい設定が通知される")
    void notifies_the_listener_of_the_new_settings_when_the_settings_are_changed() {
        var appModel = new SettingsAppModel(new SettingsRepository());
        // AtomicReference is a mutable box to capture the value from the lambda,
        // which cannot assign to local variables.
        var notified = new AtomicReference<Settings>();
        appModel.settingsProperty().addListener((_, _, newValue) -> notified.set(newValue));

        // JavaFX change listeners run synchronously inside the setter,
        // so the assertion below is deterministic.
        appModel.setSettings(new Settings(Path.of("music")));

        assertEquals(new Settings(Path.of("music")), notified.get());
    }
}
