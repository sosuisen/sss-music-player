package com.sosuisha.presentation.screens.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.presentation.appmodel.SettingsAppModel;

class SettingsViewModelTest {
    @Test
    @DisplayName("設定アプリモデルを注入すると、その設定値を返す")
    void returns_the_settings_of_the_injected_settings_app_model() {
        var appModel = new SettingsAppModel();
        appModel.setSettings(new Settings(Path.of("music")));
        var viewModel = new SettingsViewModel(appModel);

        assertEquals(new Settings(Path.of("music")), viewModel.getSettings());
    }
}
