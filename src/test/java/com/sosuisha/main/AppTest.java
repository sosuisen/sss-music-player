package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.testfx.api.FxAssert.verifyThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;

import javafx.scene.control.ListView;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppTest {
    private Stage stage;

    @Start
    void setup(Stage stage) {
        this.stage = stage;
        new App().start(stage);
    }

    @Test
    @DisplayName("アプリを起動すると、ライブラリ管理ウィンドウが表示される")
    void app_startup_shows_the_library_manager_window() {
        assertEquals("Library Manager", stage.getTitle());
        verifyThat("#fileList", NodeMatchers.isVisible());
    }

    @Test
    @DisplayName("アプリを起動すると、ファイルリストに1件以上表示される")
    void app_startup_shows_at_least_one_file_in_the_list(FxRobot robot) {
        ListView<?> listView = robot.lookup("#fileList").query();

        assertFalse(listView.getItems().isEmpty());
    }
}
