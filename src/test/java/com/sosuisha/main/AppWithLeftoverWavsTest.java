package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppWithLeftoverWavsTest {
    private Path leftover;

    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        var wavFolder = Files.createDirectory(folder.resolve("wav"));
        leftover = Files.write(wavFolder.resolve("left.wav"), new byte[16]);
        System.setProperty("sss.settings.file", folder.resolve("settings.properties").toString());
        System.setProperty("sss.library.db", folder.resolve("library.db").toString());
        System.setProperty("sss.wav.folder", wavFolder.toString());
        // The injected primary stage is reused across tests and rejects
        // initStyle, so App gets a fresh stage.
        new App().start(new Stage());
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
        System.clearProperty("sss.library.db");
        System.clearProperty("sss.wav.folder");
    }

    @Test
    @DisplayName("起動時に、前回までに残った一時WAVを消す")
    void the_temporary_wavs_left_by_earlier_runs_are_deleted_at_startup() {
        assertFalse(Files.exists(leftover));
    }
}
