package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ListViewMatchers;

import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class LibraryManagerViewTest {
    private Stage stage;
    private LibraryManagerViewModel viewModel;

    @Start
    void setup(Stage stage) {
        this.stage = stage;
        viewModel = new LibraryManagerViewModel(new MusicLibraryAppModel());
        var view = new LibraryManagerView(viewModel);
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }

    @Test
    @DisplayName("ライブラリ管理ウィンドウが表示され、タイトルはLibrary Managerである")
    void window_is_shown_with_title_library_manager() {
        assertTrue(stage.isShowing());
        assertEquals("Library Manager", stage.getTitle());
    }

    @Test
    @DisplayName("ウィンドウにファイルリストが表示される")
    void window_shows_the_file_list(FxRobot robot) {
        var files = List.of(Path.of("first.mp3"), Path.of("second.m4a"));
        robot.interact(() -> viewModel.setFiles(files));

        verifyThat("#fileList", ListViewMatchers.hasItems(2));
        verifyThat("#fileList", ListViewMatchers.hasListCell(Path.of("first.mp3")));
        verifyThat("#fileList", ListViewMatchers.hasListCell(Path.of("second.m4a")));
    }

    @Test
    @org.junit.jupiter.api.Disabled("DuplicateListViewとWindowManagerの実装後に有効化する")
    @DisplayName("FileメニューのRemove duplicate files...を選ぶと、重複リスト表示ウィンドウが開く")
    void selecting_remove_duplicate_files_menu_opens_the_duplicate_list_window(FxRobot robot) {
        robot.clickOn("File").clickOn("Remove duplicate files...");

        var window = robot.window("Duplicate Files");

        assertTrue(window.isShowing());
    }
}
