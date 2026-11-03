package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import com.sosuisha.domain.model.PlaybackPosition;

import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
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

    @Test
    @DisplayName("再生位置の通知を受けると、シークバーと時間の表示が変わる")
    void a_reported_position_moves_the_seek_bar_and_changes_the_times(FxRobot robot) {
        var seekBar = robot.lookup("#seekBar").queryAs(Slider.class);

        robot.interact(
            () -> reportProgress(
                new PlaybackPosition(Duration.ofSeconds(83), Duration.ofSeconds(245))
            )
        );

        assertAll(
            () -> assertFalse(seekBar.isDisabled()),
            () -> assertEquals(83.0 / 245.0, seekBar.getValue() / seekBar.getMax(), 0.001),
            () -> assertEquals("1:23", label(robot, "#elapsedTime")),
            () -> assertEquals("4:05", label(robot, "#trackLength"))
        );
    }

    @Test
    @DisplayName("シークバーのつまみを動かして離すと、その位置へシークする")
    void releasing_the_dragged_thumb_seeks_to_that_position(FxRobot robot) {
        var seekBar = robot.lookup("#seekBar").queryAs(Slider.class);
        robot.interact(
            () -> reportProgress(new PlaybackPosition(Duration.ZERO, Duration.ofSeconds(100)))
        );

        // The right edge of the bar is past the end of the track, so the thumb
        // stops at the end. The mouse stays on the bar, because the test robot
        // does not send the release to the bar when it is outside.
        var bounds = seekBar.localToScreen(seekBar.getBoundsInLocal());
        robot.drag("#seekBar .thumb", MouseButton.PRIMARY)
            .moveTo(new Point2D(bounds.getMaxX() - 2, bounds.getCenterY()))
            .release(MouseButton.PRIMARY);

        assertEquals(Duration.ofSeconds(100), seekedPosition.get());
    }

    @Test
    @DisplayName("つまみをドラッグしている間は、通知を受けてもつまみは動かない")
    void the_dragged_thumb_does_not_follow_a_reported_position(FxRobot robot) {
        var seekBar = robot.lookup("#seekBar").queryAs(Slider.class);
        robot.interact(
            () -> reportProgress(new PlaybackPosition(Duration.ZERO, Duration.ofSeconds(100)))
        );
        var bounds = seekBar.localToScreen(seekBar.getBoundsInLocal());
        robot.drag("#seekBar .thumb", MouseButton.PRIMARY)
            .moveTo(new Point2D(bounds.getCenterX(), bounds.getCenterY()));
        var draggedValue = seekBar.getValue();

        robot.interact(
            () -> reportProgress(
                new PlaybackPosition(Duration.ofSeconds(1), Duration.ofSeconds(100))
            )
        );

        var valueAfterReport = seekBar.getValue();
        robot.release(MouseButton.PRIMARY);
        assertEquals(draggedValue, valueAfterReport);
    }

    private static String label(FxRobot robot, String query) {
        return robot.lookup(query).queryAs(Label.class).getText();
    }
}
