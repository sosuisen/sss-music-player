package com.sosuisha.presentation.appmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sosuisha.domain.exception.SettingsException;
import com.sosuisha.domain.model.RepeatMode;
import com.sosuisha.domain.model.Settings;
import com.sosuisha.domain.service.NullSettingsRepository;
import com.sosuisha.repository.SettingsRepositoryImpl;

class SettingsAppModelTest {
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
    @DisplayName("音楽ライブラリのパスをセットすると、同じパスが取得できる")
    void returns_the_music_library_path_that_was_set() {
        var appModel = new SettingsAppModel(new NullSettingsRepository());

        appModel.musicLibraryPathProperty().set(Path.of("music"));

        assertEquals(Path.of("music"), appModel.musicLibraryPathProperty().get());
    }

    @Test
    @DisplayName("音楽ライブラリのパスを変更すると、リスナーに新しいパスが通知される")
    void notifies_the_listener_of_the_new_path_when_the_music_library_path_is_changed() {
        var appModel = new SettingsAppModel(new NullSettingsRepository());
        // AtomicReference is a mutable box to capture the value from the lambda,
        // which cannot assign to local variables.
        var notified = new AtomicReference<Path>();
        appModel.musicLibraryPathProperty().addListener((_, _, newValue) -> notified.set(newValue));

        // JavaFX change listeners run synchronously inside the setter,
        // so the assertion below is deterministic.
        appModel.musicLibraryPathProperty().set(Path.of("music"));

        assertEquals(Path.of("music"), notified.get());
    }

    @Test
    @DisplayName("リピートモードを変更すると、設定ファイルに新しいモードが保存される")
    void changing_the_repeat_mode_saves_the_new_mode_to_the_settings_file() throws Exception {
        var appModel = new SettingsAppModel(new SettingsRepositoryImpl());
        appModel.musicLibraryPathProperty().set(Path.of("music"));

        appModel.repeatModeProperty().set(RepeatMode.ONE);

        assertEquals(RepeatMode.ONE, new SettingsRepositoryImpl().load().repeatMode());
    }

    @Test
    @DisplayName("設定ファイルのリピートモードは、loadで復元される")
    void the_repeat_mode_in_the_settings_file_is_restored_by_loading() throws Exception {
        Files.writeString(
            folder.resolve("settings.properties"),
            "musicLibraryPath=music\nrepeatMode=ONE\n"
        );
        var appModel = new SettingsAppModel(new SettingsRepositoryImpl());

        appModel.loadSettings();

        assertEquals(RepeatMode.ONE, appModel.repeatModeProperty().get());
    }

    @Test
    @DisplayName("設定の読み込みに失敗すると、SettingsExceptionが投げられる（causeは元のIOException）")
    void a_settings_exception_is_thrown_when_loading_the_settings_fails() {
        var cause = new IOException("broken-file");
        var appModel = new SettingsAppModel(new NullSettingsRepository() {
            @Override
            public Settings load() throws IOException {
                throw cause;
            }
        });

        var thrown = assertThrows(SettingsException.class, appModel::loadSettings);

        assertSame(cause, thrown.getCause());
    }

    @Test
    @DisplayName("設定の保存に失敗すると、SettingsExceptionが投げられる（causeは元のIOException）")
    void a_settings_exception_is_thrown_when_saving_the_settings_fails() {
        var cause = new IOException("read-only-path");
        var appModel = new SettingsAppModel(new NullSettingsRepository() {
            @Override
            public void save(Settings settings) throws IOException {
                throw cause;
            }
        });
        appModel.musicLibraryPathProperty().set(Path.of("music"));

        var thrown = assertThrows(SettingsException.class, appModel::save);

        assertSame(cause, thrown.getCause());
    }

}
