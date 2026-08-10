package com.sosuisha.presentation.screens.duplicatelist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.ListViewMatchers;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.LibraryScanner;
import com.sosuisha.service.SettingsRepository;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class DuplicateListViewTest {
    private MusicLibraryAppModel appModel;
    private DuplicateListViewModel viewModel;
    private AtomicReference<Path> playedPath;
    private AtomicBoolean stopped;

    @Start
    void setup(Stage stage) {
        appModel = new MusicLibraryAppModel(
            new LibraryScanner(), new SettingsAppModel(new SettingsRepository())
        );
        playedPath = new AtomicReference<>();
        stopped = new AtomicBoolean(false);
        viewModel = new DuplicateListViewModel(appModel, new MusicPlayer() {
            @Override
            public void play(Path path) {
                playedPath.set(path);
            }

            @Override
            public void stop() {
                stopped.set(true);
            }
        });
        var view = new DuplicateListView(viewModel);
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }

    @Test
    @DisplayName("重複確認パネルは、重複候補リストの右側に置かれる")
    void the_confirm_panel_is_placed_to_the_right_of_the_duplicate_list(FxRobot robot) {
        var list = robot.lookup("#duplicateList").query();
        var panel = robot.lookup("#confirmPanel").query();

        var listRightEdge = list.localToScene(list.getBoundsInLocal()).getMaxX();
        var panelLeftEdge = panel.localToScene(panel.getBoundsInLocal()).getMinX();
        assertTrue(panelLeftEdge >= listRightEdge);
    }

    @Test
    @DisplayName("重複候補リストの項目を選択すると、右のパネルに選択したグループのファイルパスが表示される")
    void selecting_an_item_in_the_duplicate_list_shows_the_paths_of_the_group_in_the_confirm_panel(
        FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(
                new MusicFile(Path.of("a/first.mp3"), 100),
                new MusicFile(Path.of("b/first.mp3"), 100)
            )
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));

        robot.clickOn("first.mp3");

        var panel = robot.lookup("#confirmPanel").query();
        assertTrue(
            robot.from(panel).lookup(Path.of("a/first.mp3").toString()).tryQuery().isPresent()
        );
        assertTrue(
            robot.from(panel).lookup(Path.of("b/first.mp3").toString()).tryQuery().isPresent()
        );
    }

    @Test
    @DisplayName("重複確認パネルの各行には、ファイルパスとサイズと再生ボタンが表示される")
    void each_row_of_the_confirm_panel_shows_the_file_path_the_size_and_a_play_button(
        FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(
                new MusicFile(Path.of("a/first.mp3"), 100),
                new MusicFile(Path.of("b/first.mp3"), 200)
            )
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));

        robot.clickOn("first.mp3");

        var panel = robot.lookup("#confirmPanel").query();
        assertTrue(
            robot.from(panel).lookup(Path.of("a/first.mp3").toString()).tryQuery().isPresent()
        );
        assertTrue(robot.from(panel).lookup("100").tryQuery().isPresent());
        assertTrue(robot.from(panel).lookup("200").tryQuery().isPresent());
        assertEquals(2, robot.from(panel).lookup(".play-button").queryAll().size());
    }

    @Test
    @DisplayName("重複確認パネルの行の中では、ファイルパス、サイズ、再生ボタンが縦に並ぶ")
    void the_file_path_the_size_and_the_play_button_are_stacked_vertically_in_a_confirm_panel_row(
        FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(new MusicFile(Path.of("a/first.mp3"), 100))
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));

        robot.clickOn("first.mp3");

        var panel = robot.lookup("#confirmPanel").query();
        var path = robot.from(panel).lookup(Path.of("a/first.mp3").toString()).query();
        var size = robot.from(panel).lookup("100").query();
        var play = robot.from(panel).lookup(".play-button").query();
        var pathBottom = path.localToScene(path.getBoundsInLocal()).getMaxY();
        var sizeTop = size.localToScene(size.getBoundsInLocal()).getMinY();
        var sizeBottom = size.localToScene(size.getBoundsInLocal()).getMaxY();
        var playTop = play.localToScene(play.getBoundsInLocal()).getMinY();
        assertTrue(pathBottom <= sizeTop);
        assertTrue(sizeBottom <= playTop);
    }

    @Test
    @DisplayName("重複確認パネルのファイルパスのラベルは、折り返しが有効である")
    void the_file_path_label_in_the_confirm_panel_wraps_its_text(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(new MusicFile(Path.of("a/first.mp3"), 100))
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));

        robot.clickOn("first.mp3");

        var panel = robot.lookup("#confirmPanel").query();
        var path = robot.from(panel)
            .lookup(Path.of("a/first.mp3").toString())
            .queryAs(Label.class);
        assertTrue(path.isWrapText());
    }

    @Test
    @DisplayName("重複確認パネルの幅は300である")
    void the_width_of_the_confirm_panel_is_300(FxRobot robot) {
        var panel = robot.lookup("#confirmPanel").queryAs(VBox.class);

        assertEquals(300, panel.getWidth(), 0.001);
    }

    @Test
    @DisplayName("重複確認パネルの行の幅は280である")
    void the_width_of_a_confirm_panel_row_is_280(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(new MusicFile(Path.of("a/first.mp3"), 100))
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));

        robot.clickOn("first.mp3");

        var panel = robot.lookup("#confirmPanel").query();
        var row = robot.from(panel).lookup(".confirm-row").queryAs(VBox.class);
        assertEquals(280, row.getWidth(), 0.001);
    }

    @Test
    @DisplayName("再生ボタンを押すと、その行のファイルの再生がプレイヤーに要求される")
    void clicking_the_play_button_requests_the_player_to_play_the_file_of_the_row(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(new MusicFile(Path.of("a/first.mp3"), 100))
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));
        robot.clickOn("first.mp3");

        robot.clickOn(".play-button");

        assertEquals(Path.of("a/first.mp3"), playedPath.get());
    }

    @Test
    @DisplayName("再生ボタンを押すと、そのボタンの表示が停止（■）に変わる")
    void clicking_the_play_button_changes_the_button_to_a_stop_button(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(new MusicFile(Path.of("a/first.mp3"), 100))
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));
        robot.clickOn("first.mp3");

        robot.clickOn(".play-button");

        verifyThat(".play-button", LabeledMatchers.hasText("■"));
    }

    @Test
    @DisplayName("停止ボタン（■）を押すと、プレイヤーに停止が要求され、ボタンは▶に戻る")
    void clicking_the_stop_button_requests_the_player_to_stop_and_the_button_returns_to_play(
        FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(new MusicFile(Path.of("a/first.mp3"), 100))
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));
        robot.clickOn("first.mp3");
        robot.clickOn(".play-button");

        robot.clickOn(".play-button");

        assertTrue(stopped.get());
        verifyThat(".play-button", LabeledMatchers.hasText("▶"));
    }

    @Test
    @DisplayName("重複候補リストの各行には、チェックボックスが表示される")
    void each_row_of_the_duplicate_list_has_a_check_box(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(
                new MusicFile(Path.of("a/first.mp3"), 100),
                new MusicFile(Path.of("b/first.mp3"), 100)
            )
        );
        var second = new DuplicatedItems(
            "second.m4a",
            List.of(
                new MusicFile(Path.of("c/second.m4a"), 200),
                new MusicFile(Path.of("d/second.m4a"), 200)
            )
        );

        robot.interact(() -> viewModel.detect(() -> List.of(first, second)));

        var list = robot.lookup("#duplicateList").query();
        assertEquals(2, robot.from(list).lookup(".check-box").queryAll().size());
    }

    @Test
    @DisplayName("検索ボタンは、重複候補リストの上側に置かれる")
    void the_find_buttons_are_placed_above_the_duplicate_list(FxRobot robot) {
        var button = robot.lookup("#findByFilename").query();
        var list = robot.lookup("#duplicateList").query();

        // Layout bounds exclude decorations such as the focus ring, which
        // extends past the button edge and does not matter for placement.
        var buttonBottom = button.localToScene(button.getLayoutBounds()).getMaxY();
        var listTop = list.localToScene(list.getLayoutBounds()).getMinY();
        assertTrue(
            buttonBottom <= listTop,
            "buttonBottom=" + buttonBottom + ", listTop=" + listTop
        );
    }

    @Test
    @DisplayName("ウィンドウの重複リストには各グループのタイトルが表示される")
    void window_shows_the_title_of_each_duplicated_group(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(
                new MusicFile(Path.of("a/first.mp3"), 100),
                new MusicFile(Path.of("b/first.mp3"), 100)
            )
        );
        var second = new DuplicatedItems(
            "second.m4a",
            List.of(
                new MusicFile(Path.of("c/second.m4a"), 200),
                new MusicFile(Path.of("d/second.m4a"), 200)
            )
        );

        robot.interact(() -> viewModel.detect(() -> List.of(first, second)));

        verifyThat("#duplicateList", ListViewMatchers.hasItems(2));
        verifyThat("first.mp3", NodeMatchers.isVisible());
        verifyThat("second.m4a", NodeMatchers.isVisible());
    }

    @Test
    @DisplayName("Find by Filename and Sizeボタンを押すと、ファイル名とサイズが同じファイルのグループが一覧に表示される")
    void clicking_find_by_filename_and_size_shows_groups_of_files_with_the_same_name_and_size(
        FxRobot robot) {
        robot.interact(
            () -> appModel.setFiles(
                List.of(
                    new MusicFile(Path.of("a/dup.mp3"), 100),
                    new MusicFile(Path.of("b/dup.mp3"), 100),
                    new MusicFile(Path.of("c/dup.mp3"), 200)
                )
            )
        );

        robot.clickOn("#findByFilenameAndSize");

        verifyThat("#duplicateList", ListViewMatchers.hasItems(1));
        verifyThat(
            "#duplicateList",
            ListViewMatchers.hasListCell(
                new DuplicatedItems(
                    "dup.mp3",
                    List.of(
                        new MusicFile(Path.of("a/dup.mp3"), 100),
                        new MusicFile(Path.of("b/dup.mp3"), 100)
                    )
                )
            )
        );
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
                new DuplicatedItems(
                    "dup.mp3",
                    List.of(
                        new MusicFile(Path.of("a/dup.mp3"), 100),
                        new MusicFile(Path.of("b/dup.mp3"), 100)
                    )
                )
            )
        );
    }
}
