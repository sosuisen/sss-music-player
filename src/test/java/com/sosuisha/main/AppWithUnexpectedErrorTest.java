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
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppWithUnexpectedErrorTest {
    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        System.setProperty("sss.settings.file", folder.resolve("settings.properties").toString());
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
    @DisplayName("FXスレッドで想定外の例外が起きると、その内容を含むエラーダイアログが表示される")
    void an_error_dialog_with_the_exception_is_shown_when_an_unexpected_exception_occurs_on_the_fx_thread(
        FxRobot robot) {
        Platform.runLater(() -> {
            throw new IllegalStateException("boom");
        });
        WaitForAsyncUtils.waitForFxEvents();

        var dialogPane = robot.lookup(".dialog-pane").queryAs(DialogPane.class);
        var details = (TextArea) dialogPane.getExpandableContent();
        assertTrue(details.getText().contains("IllegalStateException: boom"));
    }
}
