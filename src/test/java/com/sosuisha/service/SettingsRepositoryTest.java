package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sosuisha.domain.model.Settings;

class SettingsRepositoryTest {
    @TempDir
    Path folder;

    private Path file;

    @BeforeEach
    void setup() {
        file = folder.resolve("settings.properties");
        System.setProperty("sss.settings.file", file.toString());
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
    }

    @Test
    @DisplayName("設定を保存すると、解決された保存先のパスにpropertiesファイルが作られる")
    void saving_settings_creates_the_properties_file_at_the_resolved_path() throws Exception {
        var repository = new SettingsRepository();

        repository.save(new Settings(Path.of("music")));

        assertTrue(Files.exists(file));
    }

    @Test
    @DisplayName("デフォルトの保存先は、ユーザホームの.sss-music-player/settings.propertiesである")
    void default_file_is_settings_properties_in_the_sss_music_player_folder_in_the_user_home() {
        assertEquals(
                Path.of(System.getProperty("user.home"), ".sss-music-player", "settings.properties"),
                SettingsRepository.DEFAULT_FILE);
    }

    @Test
    @DisplayName("システムプロパティsss.settings.fileが指定されていると、その値が設定ファイルのパスとして解決される")
    void resolves_the_settings_file_from_the_system_property_when_it_is_set() {
        System.setProperty("sss.settings.file", "custom/settings.properties");

        assertEquals(Path.of("custom", "settings.properties"), SettingsRepository.resolveFile());
    }

    @Test
    @DisplayName("システムプロパティsss.settings.fileが未指定の場合、デフォルトの保存先に解決される")
    void resolves_the_settings_file_to_the_default_file_when_the_system_property_is_not_set() {
        System.clearProperty("sss.settings.file");

        assertEquals(SettingsRepository.DEFAULT_FILE, SettingsRepository.resolveFile());
    }

    @Test
    @DisplayName("保存先の親フォルダがなければ作成される")
    void saving_settings_creates_the_parent_folder_if_it_does_not_exist() throws Exception {
        file = folder.resolve("parent").resolve("settings.properties");
        System.setProperty("sss.settings.file", file.toString());
        var repository = new SettingsRepository();

        repository.save(new Settings(Path.of("music")));

        assertTrue(Files.exists(file));
    }

    @Test
    @DisplayName("設定を保存すると、propertiesファイルに音楽ライブラリのパスが書き込まれる")
    void saving_settings_writes_the_music_library_path_to_the_properties_file() throws Exception {
        var repository = new SettingsRepository();

        repository.save(new Settings(Path.of("music")));

        var properties = new Properties();
        try (var reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        }
        assertEquals("music", properties.getProperty("musicLibraryPath"));
    }

    @Test
    @DisplayName("propertiesファイルから設定をロードすると、音楽ライブラリのパスが復元される")
    void loading_settings_reads_the_music_library_path_from_the_properties_file() throws Exception {
        Files.writeString(file, "musicLibraryPath=music");
        var repository = new SettingsRepository();

        var settings = repository.load();

        assertEquals(new Settings(Path.of("music")), settings);
    }
}
