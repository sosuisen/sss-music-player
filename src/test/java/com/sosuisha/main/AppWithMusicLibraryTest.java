package com.sosuisha.main;

import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppWithMusicLibraryTest {

    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        var musicFolder = folder.resolve("music");
        Files.createDirectories(musicFolder.resolve("sub"));
        Files.createFile(musicFolder.resolve("song1.mp3"));
        Files.createFile(musicFolder.resolve("sub").resolve("song2.m4a"));
        var file = folder.resolve("settings.properties");
        var properties = new Properties();
        properties.setProperty("musicLibraryPath", musicFolder.toString());
        try (var writer = Files.newBufferedWriter(file)) {
            properties.store(writer, null);
        }
        System.setProperty("sss.settings.file", file.toString());
        new App().start(stage);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
    }

    @Test
    @DisplayName("起動時に設定ファイルがあると、ライブラリフォルダを走査した結果が一覧に表示される")
    void the_files_scanned_from_the_music_library_folder_are_shown_in_the_list_at_startup(
        FxRobot robot) throws Exception {
        // The scan runs in the background, so wait until the result arrives.
        WaitForAsyncUtils.waitFor(
            5,
            TimeUnit.SECONDS,
            () -> robot.lookup("#fileList").queryListView().getItems().size() == 2
        );
        verifyThat("#fileList", ListViewMatchers.hasItems(2));
    }
}
