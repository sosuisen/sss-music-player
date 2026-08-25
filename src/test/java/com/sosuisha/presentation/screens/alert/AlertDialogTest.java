package com.sosuisha.presentation.screens.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AlertDialogTest {
    @Start
    void setup(Stage stage) {
        // Initializes the JavaFX toolkit. The dialog is shown by the test.
    }

    @Test
    @DisplayName("エラーダイアログのメッセージは、展開可能な領域に表示され、初期状態で展開されている")
    void the_message_of_the_error_dialog_is_shown_in_the_expandable_content_and_expanded(
        FxRobot robot) {
        robot.interact(() -> AlertDialog.showError("boom"));

        var pane = robot.lookup(".dialog-pane").queryAs(DialogPane.class);
        assertTrue(pane.isExpanded());
        var content = assertInstanceOf(Label.class, pane.getExpandableContent());
        assertEquals("boom", content.getText());
    }
}
