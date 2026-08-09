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
import org.testfx.matcher.control.ListViewMatchers;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class DuplicateListViewTest {
    private DuplicateListViewModel viewModel;

    @Start
    void setup(Stage stage) {
        viewModel = new DuplicateListViewModel(new MusicLibraryAppModel());
        var view = new DuplicateListView(viewModel);
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }

    @Test
    @DisplayName("ウィンドウに重複リストが表示される")
    void window_shows_the_duplicated_items(FxRobot robot) {
        var first = new DuplicatedItems(List.of(Path.of("a/first.mp3"), Path.of("b/first.mp3")));
        var second = new DuplicatedItems(List.of(Path.of("c/second.m4a"), Path.of("d/second.m4a")));

        robot.interact(() -> viewModel.detect(() -> List.of(first, second)));

        verifyThat("#duplicateList", ListViewMatchers.hasItems(2));
        verifyThat("#duplicateList", ListViewMatchers.hasListCell(first));
        verifyThat("#duplicateList", ListViewMatchers.hasListCell(second));
    }
}
