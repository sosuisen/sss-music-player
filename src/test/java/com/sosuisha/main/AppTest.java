package com.sosuisha.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerView;

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
    @DisplayName("アプリを起動すると、FIRST_VIEW定数で指定したViewのウィンドウが表示される")
    void app_startup_shows_the_window_of_the_view_specified_by_first_view_constant() {
        var expectedTitles = Map.of(
            LibraryManagerView.class, "Library Manager",
            DuplicateListView.class, "Duplicate Files"
        );

        assertTrue(stage.isShowing());
        assertEquals(expectedTitles.get(App.FIRST_VIEW), stage.getTitle());
    }

}
