package com.sosuisha.presentation;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;

import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class WindowManagerTest {
    @Test
    @DisplayName("登録したDuplicateListViewをクラス指定で取得できる")
    void returns_registered_duplicate_list_view_by_its_class() {
        var windowManager = new WindowManager();
        var view = new DuplicateListView();

        windowManager.registerView(view);

        assertSame(view, windowManager.getView(DuplicateListView.class));
    }

    @Test
    @DisplayName("showWindowすると、DuplicateListViewのウィンドウがタイトルDuplicate Filesで表示される")
    void show_window_displays_the_duplicate_list_view_window(FxRobot robot) {
        var windowManager = new WindowManager();
        var view = new DuplicateListView();
        windowManager.registerView(view);

        robot.interact(() -> windowManager.showWindow(DuplicateListView.class, new Stage()));

        var window = robot.window("Duplicate Files");
        assertTrue(window.isShowing());
        assertSame(view.getScene(), window.getScene());
    }
}
