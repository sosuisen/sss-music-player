package com.sosuisha.main;

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
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppWithMissingLibraryFolderTest {
    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        var file = folder.resolve("settings.properties");
        var properties = new Properties();
        properties.setProperty("musicLibraryPath", folder.resolve("missing").toString());
        try (var writer = Files.newBufferedWriter(file)) {
            properties.store(writer, null);
        }
        System.setProperty("sss.settings.file", file.toString());
        System.setProperty("sss.library.db", folder.resolve("library.db").toString());
        // The injected primary stage is reused across tests and rejects
        // initStyle, so App gets a fresh stage.
        new App().start(new Stage());
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
        System.clearProperty("sss.library.db");
    }

    @Test
    @DisplayName("起動時にライブラリフォルダが存在しない場合、スキャンの失敗がエラーダイアログで表示される")
    void an_error_dialog_is_shown_when_the_library_folder_does_not_exist_at_startup(
        FxRobot robot) throws Exception {
        // The scan runs in the background, so wait until its failure arrives.
        WaitForAsyncUtils.waitFor(
            5, TimeUnit.SECONDS, () -> robot.lookup(".dialog-pane").tryQuery().isPresent()
        );
    }
}
