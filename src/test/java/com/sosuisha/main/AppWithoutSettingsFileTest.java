package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Modality;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppWithoutSettingsFileTest {
    private Stage stage;

    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
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
    @DisplayName("設定ファイルがない場合、ライブラリ管理ウィンドウの上にモーダルの設定ウィンドウが開く")
    void the_modal_settings_window_is_opened_over_the_library_manager_window_when_the_settings_file_does_not_exist(
        FxRobot robot) {
        var settingsWindow = (Stage) robot.window("Settings");

        assertTrue(stage.isShowing());
        assertTrue(settingsWindow.isShowing());
        assertEquals(Modality.APPLICATION_MODAL, settingsWindow.getModality());
    }
}
