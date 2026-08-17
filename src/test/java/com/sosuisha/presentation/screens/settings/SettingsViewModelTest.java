package com.sosuisha.presentation.screens.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sosuisha.domain.model.Theme;
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
    @DisplayName("フォルダを選択すると、選択したパスがAppModelの音楽ライブラリパスに反映される")
    void selecting_a_folder_updates_the_music_library_path_of_the_app_model() {
        var appModel = new SettingsAppModel(new SettingsRepositoryImpl());
        appModel.musicLibraryPathProperty().set(Path.of("music"));
        var viewModel = new SettingsViewModel(appModel, _ -> Optional.of(Path.of("newMusic")));

        viewModel.selectMusicLibraryFolder(null);

        assertEquals(Path.of("newMusic"), appModel.musicLibraryPathProperty().get());
    }

    @Test
    @DisplayName("フォルダを選択しても、現在のテーマは維持される")
    void selecting_a_folder_keeps_the_current_theme() throws Exception {
        var appModel = new SettingsAppModel(new SettingsRepositoryImpl());
        appModel.musicLibraryPathProperty().set(Path.of("music"));
        var viewModel = new SettingsViewModel(appModel, _ -> Optional.of(Path.of("newMusic")));
        appModel.themeProperty().set(Theme.NORD_DARK);

        viewModel.selectMusicLibraryFolder(null);

        assertEquals(Theme.NORD_DARK, new SettingsRepositoryImpl().load().theme());
    }

    @Test
    @DisplayName("フォルダの選択をキャンセルすると、設定は変わらず、ファイルにも保存されない")
    void canceling_the_folder_selection_keeps_the_settings_unchanged_and_saves_nothing() {
        var appModel = new SettingsAppModel(new SettingsRepositoryImpl());
        appModel.musicLibraryPathProperty().set(Path.of("music"));
        var viewModel = new SettingsViewModel(appModel, _ -> Optional.empty());

        viewModel.selectMusicLibraryFolder(null);

        assertEquals(Path.of("music"), appModel.musicLibraryPathProperty().get());
        assertFalse(Files.exists(folder.resolve("settings.properties")));
    }

    @Test
    @DisplayName("ライブラリパスが未設定のままテーマを変更しても、例外にならず、ファイルにも保存されない")
    void changing_the_theme_throws_nothing_and_saves_nothing_when_the_music_library_path_is_not_set() {
        // A JavaFX property sends a listener exception to the uncaught
        // exception handler instead of the set() caller, so the handler is
        // replaced to catch it.
        var thrown = new AtomicReference<Throwable>();
        var originalHandler = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler((_, e) -> thrown.set(e));
        try {
            var appModel = new SettingsAppModel(new SettingsRepositoryImpl());
            new SettingsViewModel(appModel, _ -> Optional.empty());

            appModel.themeProperty().set(Theme.NORD_DARK);
        } finally {
            Thread.currentThread().setUncaughtExceptionHandler(originalHandler);
        }

        assertNull(thrown.get());
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
