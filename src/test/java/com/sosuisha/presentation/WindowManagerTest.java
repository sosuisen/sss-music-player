package com.sosuisha.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;

import com.sosuisha.domain.repository.NullLibraryRepository;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;
import com.sosuisha.service.DuplicateFileMover;
import com.sosuisha.service.LibraryIndexer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

@ExtendWith(ApplicationExtension.class)
class WindowManagerTest {
    @Test
    @DisplayName("登録したDuplicateListViewをクラス指定で取得できる")
    void returns_registered_duplicate_list_view_by_its_class() {
        var windowManager = new WindowManager();
        var view = new DuplicateListView(
            new DuplicateListViewModel(
                new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SimpleObjectProperty<>()
                ),
                new NullMusicPlayer(),
                new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")),
                _ -> {
                }
            )
        );

        windowManager.registerView(view);

        assertSame(view, windowManager.getView(DuplicateListView.class));
    }

    @Test
    @DisplayName("showWindowすると、DuplicateListViewのウィンドウがタイトルDuplicate Filesで表示される")
    void show_window_displays_the_duplicate_list_view_window(FxRobot robot) {
        var windowManager = new WindowManager();
        var view = new DuplicateListView(
            new DuplicateListViewModel(
                new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SimpleObjectProperty<>()
                ),
                new NullMusicPlayer(),
                new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")),
                _ -> {
                }
            )
        );
        windowManager.registerView(view);

        robot.interact(() -> windowManager.showWindow(DuplicateListView.class, new Stage()));

        var window = robot.window("Duplicate Files");
        assertTrue(window.isShowing());
        assertSame(view.getScene(), window.getScene());
    }

    @Test
    @DisplayName("showWindowは、HeaderBarを持たないViewのウィンドウを通常のタイトルバー付き（DECORATED）で表示する")
    void show_window_uses_the_decorated_style_for_a_view_without_a_header_bar(FxRobot robot) {
        var windowManager = new WindowManager();
        var view = new DuplicateListView(
            new DuplicateListViewModel(
                new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SimpleObjectProperty<>()
                ),
                new NullMusicPlayer(),
                new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")),
                _ -> {
                }
            )
        );
        windowManager.registerView(view);

        robot.interact(() -> windowManager.showWindow(DuplicateListView.class, new Stage()));

        var window = (Stage) robot.window("Duplicate Files");
        assertEquals(StageStyle.DECORATED, window.getStyle());
    }

    @Test
    @DisplayName("showWindowすると、ウィンドウのアイコンにアプリのアイコン（images/icon.png）が設定される")
    void show_window_sets_the_app_icon_on_the_window(FxRobot robot) {
        var windowManager = new WindowManager();
        windowManager.registerView(
            new DuplicateListView(
                new DuplicateListViewModel(
                    new MusicLibraryAppModel(
                        new LibraryIndexer(new NullLibraryRepository()),
                        new SimpleObjectProperty<>()
                    ),
                    new NullMusicPlayer(),
                    new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")),
                    _ -> {
                    }
                )
            )
        );

        robot.interact(() -> windowManager.showWindow(DuplicateListView.class, new Stage()));

        var window = (Stage) robot.window("Duplicate Files");
        assertEquals(1, window.getIcons().size());
        assertEquals(256, window.getIcons().getFirst().getWidth());
    }
}
