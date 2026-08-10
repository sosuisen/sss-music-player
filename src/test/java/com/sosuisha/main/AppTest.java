package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Files;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;

import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerView;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppTest {
    private Stage stage;

    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        var file = folder.resolve("settings.properties");
        Files.writeString(file, "musicLibraryPath=loaded-music");
        System.setProperty("sss.settings.file", file.toString());
        this.stage = stage;
        new App().start(stage);
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
    }

    @Test
    @DisplayName("アプリを起動すると、FIRST_VIEW定数で指定したViewのウィンドウが表示される")
    void app_startup_shows_the_window_of_the_view_specified_by_first_view_constant() {
        var expectedTitles = Map.of(
            LibraryManagerView.class, "Library Manager",
            DuplicateListView.class, "Duplicate Files"
        );

        assertTrue(stage.isShowing());
        assertEquals(expectedTitles.get(App.FIRST_VIEW), stage.getTitle());
    }

    @Test
    @DisplayName("起動時に設定ファイルからロードされた設定が、設定ウィンドウに表示される")
    void settings_loaded_from_the_settings_file_at_startup_are_shown_in_the_settings_window(
        FxRobot robot) {
        robot.clickOn("File").clickOn("Settings...");

        verifyThat("#musicLibraryPath", LabeledMatchers.hasText("loaded-music"));
    }

    @Test
    @DisplayName("設定ファイルがある場合、起動時に設定ウィンドウは開かない")
    void the_settings_window_is_not_opened_at_startup_when_the_settings_file_exists(FxRobot robot) {
        var settingsWindowShown = robot.listWindows().stream()
            .anyMatch(
                window -> window instanceof Stage shown && "Settings".equals(shown.getTitle())
            );

        assertFalse(settingsWindowShown);
    }
}
