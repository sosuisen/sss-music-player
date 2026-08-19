package com.sosuisha.main;

import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppWithSavedRepeatModeTest {
    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        var file = folder.resolve("settings.properties");
        Files.writeString(file, "musicLibraryPath=loaded-music\nrepeatMode=ONE");
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
    @DisplayName("起動時に、設定ファイルに保存されたリピートモードがリピートボタンの表示に再現される")
    void the_repeat_mode_saved_in_the_settings_file_is_shown_on_the_repeat_button_at_startup() {
        verifyThat("#repeatButton", LabeledMatchers.hasText("repeat one"));
    }
}
