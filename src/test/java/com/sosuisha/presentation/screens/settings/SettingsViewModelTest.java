package com.sosuisha.presentation.screens.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.repository.SettingsRepositoryImpl;

class SettingsViewModelTest {
    @TempDir
    Path folder;

    @BeforeEach
    void setup() {
        System.setProperty(
            "sss.settings.file",
            folder.resolve("settings.properties").toString()
        );
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
    }

    @Test
    @DisplayName("フォルダを選択すると、選択したパスがAppModelの設定に反映される")
    void selecting_a_folder_updates_the_settings_of_the_app_model_with_the_selected_path() {
        var appModel = new SettingsAppModel(new SettingsRepositoryImpl());
        appModel.setSettings(new Settings(Path.of("music")));
        var viewModel = new SettingsViewModel(appModel, _ -> Optional.of(Path.of("newMusic")));

        viewModel.selectMusicLibraryFolder(null);

        assertEquals(new Settings(Path.of("newMusic")), appModel.getSettings());
    }

    @Test
    @DisplayName("フォルダの選択をキャンセルすると、設定は変わらず、ファイルにも保存されない")
    void canceling_the_folder_selection_keeps_the_settings_unchanged_and_saves_nothing() {
        var appModel = new SettingsAppModel(new SettingsRepositoryImpl());
        appModel.setSettings(new Settings(Path.of("music")));
        var viewModel = new SettingsViewModel(appModel, _ -> Optional.empty());

        viewModel.selectMusicLibraryFolder(null);

        assertEquals(new Settings(Path.of("music")), appModel.getSettings());
        assertFalse(Files.exists(folder.resolve("settings.properties")));
    }

    @Test
    @DisplayName("エラーのとき、エラーメッセージは保存失敗の説明に例外のメッセージを続けたものである")
    void the_error_message_is_the_failed_to_save_text_followed_by_the_exception_message() {
        var appModel = new SettingsAppModel(new SettingsRepositoryImpl());
        var viewModel = new SettingsViewModel(appModel, _ -> Optional.empty());
        appModel.errorProperty().set(new IOException("C:\\somewhere\\settings.properties"));

        assertEquals(
            "Failed to save the settings file: C:\\somewhere\\settings.properties",
            viewModel.errorMessageProperty().getValue()
        );
    }
}
