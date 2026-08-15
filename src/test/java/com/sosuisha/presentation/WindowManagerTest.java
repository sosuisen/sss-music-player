package com.sosuisha.presentation;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;

import com.sosuisha.domain.service.NullLibraryRepository;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;
import com.sosuisha.service.DuplicateFileMover;
import com.sosuisha.service.LibraryIndexer;
import com.sosuisha.domain.service.NullSettingsRepository;

import javafx.stage.Stage;

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
                    new SettingsAppModel(new NullSettingsRepository())
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
                    new SettingsAppModel(new NullSettingsRepository())
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
}
