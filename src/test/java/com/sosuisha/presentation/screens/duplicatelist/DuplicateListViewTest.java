package com.sosuisha.presentation.screens.duplicatelist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.domain.service.NullLibraryDatabase;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.presentation.screens.duplicatelist.components.DetailedPanel;
import com.sosuisha.service.DuplicateFileMover;
import com.sosuisha.service.LibraryScanner;
import com.sosuisha.repository.SettingsRepository;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class DuplicateListViewTest {
    private MusicLibraryAppModel appModel;
    private DuplicateListViewModel viewModel;
    private AtomicReference<Path> playedPath;
    private AtomicBoolean stopped;
    private AtomicReference<List<DuplicatedItems>> movedGroups;
    private AtomicReference<Path> openedFolder;

    @Start
    void setup(Stage stage) {
        appModel = new MusicLibraryAppModel(
            new LibraryScanner(new NullLibraryDatabase()),
            new SettingsAppModel(new SettingsRepository())
        );
        playedPath = new AtomicReference<>();
        stopped = new AtomicBoolean(false);
        movedGroups = new AtomicReference<>();
        openedFolder = new AtomicReference<>();
        viewModel = new DuplicateListViewModel(appModel, new MusicPlayer() {
            @Override
            public void play(Path path) {
                playedPath.set(path);
            }

            @Override
            public void stop() {
                stopped.set(true);
            }

            @Override
            public void pause() {}

            @Override
            public void resume() {}

            @Override
            public void setOnFinished(Runnable onFinished) {}

            @Override
            public Optional<Path> playingPath() {
                return Optional.empty();
            }
        }, new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")) {
            @Override
            public void moveDuplicates(List<DuplicatedItems> groups) {
                movedGroups.set(groups);
            }
        }, folder -> openedFolder.set(folder));
        var view = new DuplicateListView(viewModel);
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
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

        var panel = robot.lookup("#detailedPanel").query();
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

        var panel = robot.lookup("#detailedPanel").query();
        assertTrue(
            robot.from(panel).lookup(Path.of("a/first.mp3").toString()).tryQuery().isPresent()
        );
        assertTrue(robot.from(panel).lookup("0.00 MB (100 bytes)").tryQuery().isPresent());
        assertTrue(robot.from(panel).lookup("0.00 MB (200 bytes)").tryQuery().isPresent());
        assertEquals(2, robot.from(panel).lookup(".play-button").queryAll().size());
    }

    @Test
    @DisplayName("詳細パネルのファイルサイズは、MB（小数点2桁）とバイト数で表示される")
    void the_file_size_in_the_detailed_panel_is_shown_in_megabytes_and_bytes(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(new MusicFile(Path.of("a/first.mp3"), 1_290_000))
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));

        robot.clickOn("first.mp3");

        var panel = robot.lookup("#detailedPanel").query();
        assertTrue(
            robot.from(panel).lookup("1.23 MB (1290000 bytes)").tryQuery().isPresent()
        );
    }

    @Test
    @DisplayName("重複確認パネルの幅は固定値である")
    void the_width_of_the_confirm_panel_is_fixed(FxRobot robot) {
        var panel = robot.lookup("#detailedPanel").queryAs(VBox.class);

        assertEquals(DetailedPanel.WIDTH, panel.getWidth(), 0.001);
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
    @DisplayName("Remove Duplicatesボタンを押すと、チェックされたグループの重複除去が実行される")
    void clicking_the_remove_duplicates_button_removes_the_checked_duplicates(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(
                new MusicFile(Path.of("a/first.mp3"), 100),
                new MusicFile(Path.of("b/first.mp3"), 100)
            )
        );
        robot.interact(() -> {
            viewModel.detect(() -> List.of(first));
            viewModel.checkedProperty(first).set(true);
        });

        robot.clickOn("#removeDuplicates");

        assertEquals(List.of(first), movedGroups.get());
    }

    @Test
    @DisplayName("チェックが1つもないときRemove checked duplicatesボタンは無効で、チェックすると有効になる")
    void the_remove_button_is_disabled_when_no_group_is_checked_and_enabled_when_one_is_checked(
        FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(
                new MusicFile(Path.of("a/first.mp3"), 100),
                new MusicFile(Path.of("b/first.mp3"), 100)
            )
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));
        var button = robot.lookup("#removeDuplicates").queryAs(Button.class);

        assertTrue(button.isDisabled());

        robot.interact(() -> viewModel.checkedProperty(first).set(true));

        assertFalse(button.isDisabled());
    }


    @Test
    @DisplayName("チェックが1つもない状態でToggle allを押すと、すべてのグループがチェックされる")
    void clicking_toggle_all_checks_all_groups_when_none_is_checked(FxRobot robot) {
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

        robot.clickOn("#toggleAll");

        assertTrue(viewModel.checkedProperty(first).get());
        assertTrue(viewModel.checkedProperty(second).get());
    }

    @Test
    @DisplayName("チェックが1つでもある状態でToggle allを押すと、すべてのグループのチェックが外れる")
    void clicking_toggle_all_unchecks_all_groups_when_any_is_checked(FxRobot robot) {
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
        robot.interact(() -> {
            viewModel.detect(() -> List.of(first, second));
            viewModel.checkedProperty(first).set(true);
        });

        robot.clickOn("#toggleAll");

        assertFalse(viewModel.checkedProperty(first).get());
        assertFalse(viewModel.checkedProperty(second).get());
    }

    @Test
    @DisplayName("Open Folderボタンを押すと、その行のファイルのあるフォルダを開くよう要求される")
    void clicking_the_open_folder_button_requests_to_open_the_folder_of_the_file(FxRobot robot) {
        var first = new DuplicatedItems(
            "first.mp3",
            List.of(new MusicFile(Path.of("a/first.mp3"), 100))
        );
        robot.interact(() -> viewModel.detect(() -> List.of(first)));
        robot.clickOn("first.mp3");

        robot.clickOn(".open-folder-button");

        assertEquals(Path.of("a"), openedFolder.get());
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
    @DisplayName("Find by Metadataボタンが表示される")
    void find_by_metadata_button_is_shown(FxRobot robot) {
        verifyThat("#findByMetadata", LabeledMatchers.hasText("Find by Metadata"));
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

    @Test
    @DisplayName("Find by Metadataボタンを押すと、曲名とアーティストが同じファイルのグループが一覧に表示される")
    void clicking_find_by_metadata_shows_groups_of_files_with_the_same_title_and_artist(
        FxRobot robot) {
        var sameA = new MusicFile(Path.of("a/one.mp3"), 100, tag("Song", "Artist"));
        var sameB = new MusicFile(Path.of("b/two.mp3"), 100, tag("Song", "Artist"));
        var differentArtist = new MusicFile(Path.of("c/three.mp3"), 300, tag("Song", "Other"));
        robot.interact(() -> appModel.setFiles(List.of(sameA, sameB, differentArtist)));

        robot.clickOn("#findByMetadata");

        verifyThat("#duplicateList", ListViewMatchers.hasItems(1));
        verifyThat(
            "#duplicateList",
            ListViewMatchers.hasListCell(
                new DuplicatedItems("Song - Artist", List.of(sameA, sameB))
            )
        );
    }

    private static TrackMetadata tag(String title, String artist) {
        return new TrackMetadata(title, artist, "", "", "", "");
    }
}
