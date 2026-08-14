package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import javafx.stage.Window;

@ExtendWith(ApplicationExtension.class)
class AppWithMusicLibraryTest {
    private Stage stage;
    private Window scanningWindowOwner;

    @Start
    void setup(Stage stage) throws Exception {
        this.stage = stage;
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
        System.setProperty("sss.library.db", folder.resolve("library.db").toString());
        new App().start(stage);
        // This method runs on the FX thread, so the startup scan cannot finish
        // yet and the scanning dialog is still open here.
        scanningWindowOwner = Window.getWindows().stream()
            .filter(window -> window instanceof Stage shown && "Scanning".equals(shown.getTitle()))
            .findFirst()
            .map(window -> ((Stage) window).getOwner())
            .orElse(null);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
        System.clearProperty("sss.library.db");
    }

    @Test
    @DisplayName("起動時のスキャン中ダイアログのオーナーは、メインウィンドウである")
    void the_scanning_dialog_at_startup_is_owned_by_the_main_window() {
        assertEquals(stage, scanningWindowOwner);
    }

    @Test
    @DisplayName("起動時に設定ファイルがあると、ライブラリフォルダを走査した結果が一覧に表示される")
    void the_files_scanned_from_the_music_library_folder_are_shown_in_the_list_at_startup(
        FxRobot robot) throws Exception {
        // The scan runs in the background, so wait until the result arrives.
        // The two files have no album tag and are in different folders, so
        // they are shown as two folder-recognized albums.
        WaitForAsyncUtils.waitFor(
            5,
            TimeUnit.SECONDS,
            () -> robot.lookup("#albumList").queryListView().getItems().size() == 2
        );
        verifyThat("#albumList", ListViewMatchers.hasItems(2));
    }
}
