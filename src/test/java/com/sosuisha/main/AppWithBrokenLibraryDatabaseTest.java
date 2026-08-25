package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
class AppWithBrokenLibraryDatabaseTest {
    private Stage stage;

    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        // 開けないライブラリDB: SQLiteのデータベースでない内容のファイルを置く。
        Files.writeString(folder.resolve("library.db"), "this is not a sqlite database");
        System.setProperty("sss.settings.file", folder.resolve("settings.properties").toString());
        System.setProperty("sss.library.db", folder.resolve("library.db").toString());
        // The injected primary stage is reused across tests and rejects
        // initStyle, so App gets a fresh stage.
        this.stage = new Stage();
        new App().start(this.stage);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
        System.clearProperty("sss.library.db");
    }

    @Test
    @DisplayName("ライブラリDBが開けない場合、エラーダイアログが表示され、メインウィンドウは開かない")
    void an_error_dialog_is_shown_and_the_main_window_is_not_opened_when_the_library_database_cannot_be_opened(
        FxRobot robot) {
        assertTrue(robot.lookup(".dialog-pane").tryQuery().isPresent());
        assertFalse(stage.isShowing());
    }
}
