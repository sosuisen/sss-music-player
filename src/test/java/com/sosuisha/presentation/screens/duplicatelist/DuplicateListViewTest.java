package com.sosuisha.presentation.screens.duplicatelist;

import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.ListViewMatchers;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.LibraryScanner;
import com.sosuisha.service.SettingsRepository;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class DuplicateListViewTest {
    private MusicLibraryAppModel appModel;
    private DuplicateListViewModel viewModel;

    @Start
    void setup(Stage stage) {
        appModel = new MusicLibraryAppModel(
            new LibraryScanner(), new SettingsAppModel(new SettingsRepository())
        );
        viewModel = new DuplicateListViewModel(appModel);
        var view = new DuplicateListView(viewModel);
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }

    @Test
    @DisplayName("ウィンドウの重複リストには各グループのタイトルが表示される")
    void window_shows_the_title_of_each_duplicated_group(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(Path.of("a/first.mp3"), Path.of("b/first.mp3"))
        );
        var second = new DuplicatedItems(
            "second.m4a",
            List.of(Path.of("c/second.m4a"), Path.of("d/second.m4a"))
        );

        robot.interact(() -> viewModel.detect(() -> List.of(first, second)));

        verifyThat("#duplicateList", ListViewMatchers.hasItems(2));
        verifyThat("first.mp3", NodeMatchers.isVisible());
        verifyThat("second.m4a", NodeMatchers.isVisible());
    }

    @Test
    @DisplayName("Find by Filenameボタンを押すと、ファイル名が同じファイルのグループが一覧に表示される")
    void clicking_find_by_filename_shows_groups_of_files_with_the_same_name(FxRobot robot) {
        robot.interact(
            () -> appModel.setFiles(
                List.of(
                    new MusicFile(Path.of("a/dup.mp3"), 100),
                    new MusicFile(Path.of("b/dup.mp3"), 100),
                    new MusicFile(Path.of("c/unique.mp3"), 100)
                )
            )
        );

        robot.clickOn("#findByFilename");

        verifyThat("#duplicateList", ListViewMatchers.hasItems(1));
        verifyThat(
            "#duplicateList", ListViewMatchers.hasListCell(
                new DuplicatedItems("dup.mp3", List.of(Path.of("a/dup.mp3"), Path.of("b/dup.mp3")))
            )
        );
    }
}
