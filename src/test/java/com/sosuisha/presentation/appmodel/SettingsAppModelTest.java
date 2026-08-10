package com.sosuisha.presentation.appmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
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

    @Test
    @DisplayName("設定の保存に失敗すると、エラープロパティにその例外がセットされる")
    void the_exception_is_set_to_the_error_property_when_saving_the_settings_fails() {
        var error = new IOException("read-only-path");
        var appModel = new SettingsAppModel(new SettingsRepository() {
            @Override
            public void save(Settings settings) throws IOException {
                throw error;
            }
        });

        appModel.saveSettings(new Settings(Path.of("music")));

        assertEquals(error, appModel.errorProperty().get());
    }

    @Test
    @DisplayName("設定の保存に成功すると、エラープロパティはクリアされる")
    void the_error_property_is_cleared_when_saving_the_settings_succeeds() {
        var appModel = new SettingsAppModel(new SettingsRepository() {
            @Override
            public void save(Settings settings) {}
        });
        appModel.errorProperty().set(new IOException("read-only-path"));

        appModel.saveSettings(new Settings(Path.of("music")));

        assertNull(appModel.errorProperty().get());
    }
}
