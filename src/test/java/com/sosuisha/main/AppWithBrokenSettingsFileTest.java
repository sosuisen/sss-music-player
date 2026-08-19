package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppWithBrokenSettingsFileTest {
    private Stage stage;

    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        // 読めない設定ファイル: 同じパスにディレクトリを置いて読み込みを失敗させる。
        Files.createDirectory(folder.resolve("settings.properties"));
        System.setProperty("sss.settings.file", folder.resolve("settings.properties").toString());
        System.setProperty("sss.library.db", folder.resolve("library.db").toString());
        this.stage = stage;
        new App().start(stage);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
        System.clearProperty("sss.library.db");
    }

    @Test
    @DisplayName("設定ファイルが読めない場合、エラーダイアログが表示され、初期設定で起動して設定ウィンドウが開く")
    void an_error_dialog_is_shown_and_the_app_starts_with_default_settings_when_the_settings_file_cannot_be_read(
        FxRobot robot) {
        assertTrue(robot.lookup(".dialog-pane").tryQuery().isPresent());

        var settingsWindow = (Stage) robot.window("Settings");
        assertTrue(stage.isShowing());
        assertTrue(settingsWindow.isShowing());
    }
}
