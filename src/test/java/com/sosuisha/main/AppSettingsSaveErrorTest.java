package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
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
class AppSettingsSaveErrorTest {
    private Stage stage;
    private Path file;

    @Start
    void setup(Stage stage) throws Exception {
        this.stage = stage;
        var folder = Files.createTempDirectory("sss-music-player-test");
        file = folder.resolve("settings.properties");
        Files.writeString(file, "musicLibraryPath=loaded-music");
        System.setProperty("sss.settings.file", file.toString());
        System.setProperty("sss.library.db", folder.resolve("library.db").toString());
        new App().start(stage);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
        System.clearProperty("sss.library.db");
    }

    @Test
    @DisplayName("設定の保存に失敗すると、エラーダイアログが表示され、アプリは続行する")
    void an_error_dialog_is_shown_and_the_app_continues_when_saving_the_settings_fails(
        FxRobot robot) throws Exception {
        // 設定ファイルを同名のディレクトリに置き換えて、次の保存を失敗させる。
        Files.delete(file);
        Files.createDirectory(file);

        robot.clickOn("#repeatButton");

        WaitForAsyncUtils.waitFor(
            5,
            TimeUnit.SECONDS,
            () -> robot.lookup(".dialog-pane").tryQuery().isPresent()
        );
        assertTrue(stage.isShowing());
    }
}
