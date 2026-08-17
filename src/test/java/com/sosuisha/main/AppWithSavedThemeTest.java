package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.application.Application;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppWithSavedThemeTest {
    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        var file = folder.resolve("settings.properties");
        Files.writeString(file, "musicLibraryPath=loaded-music\ntheme=NORD_DARK");
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
    @DisplayName("起動時に、設定ファイルに保存されたテーマが適用される")
    void the_theme_saved_in_the_settings_file_is_applied_at_startup() {
        assertTrue(Application.getUserAgentStylesheet().endsWith("nord-dark.css"));
    }
}
