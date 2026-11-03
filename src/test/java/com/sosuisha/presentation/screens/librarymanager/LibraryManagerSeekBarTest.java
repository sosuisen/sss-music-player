package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

class LibraryManagerSeekBarTest extends LibraryManagerViewTestBase {
    @Start
    void setup(Stage stage) {
        setUpLibraryManager(stage);
    }

    @Test
    @DisplayName("再生位置が分からないとき、シークバーは無効で、時間は0:00である")
    void the_seek_bar_is_disabled_and_the_times_are_zero_while_the_position_is_unknown(
        FxRobot robot) {
        var seekBar = robot.lookup("#seekBar").queryAs(Slider.class);

        assertAll(
            () -> assertTrue(seekBar.isDisabled()),
            () -> assertEquals("0:00", label(robot, "#elapsedTime")),
            () -> assertEquals("0:00", label(robot, "#trackLength"))
        );
    }

    private static String label(FxRobot robot, String query) {
        return robot.lookup(query).queryAs(Label.class).getText();
    }
}
