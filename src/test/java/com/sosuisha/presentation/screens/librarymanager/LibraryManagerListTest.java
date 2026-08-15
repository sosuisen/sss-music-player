package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.ListViewMatchers;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

import javafx.stage.Stage;

class LibraryManagerListTest extends LibraryManagerViewTestBase {
    @Start
    void setup(Stage stage) {
        setUpLibraryManager(stage);
    }

    @Test
    @DisplayName("ライブラリ一覧は、1行につき1アルバムを「アルバム名 - アルバムアーティスト」で表示する")
    void the_library_list_shows_one_album_per_row_as_album_name_and_album_artist(FxRobot robot) {
        var files = List.of(
            new MusicFile(Path.of("a/one.mp3"), 100, albumTag("Album A", "Artist X")),
            new MusicFile(Path.of("a/two.mp3"), 200, albumTag("Album A", "Artist X")),
            new MusicFile(Path.of("b/three.mp3"), 300, albumTag("Album B", "Artist Y"))
        );
        robot.interact(() -> viewModel.setFiles(files));

        verifyThat("#albumList", ListViewMatchers.hasItems(2));
        assertTrue(robot.lookup("Album A - Artist X").tryQuery().isPresent());
        assertTrue(robot.lookup("Album B - Artist Y").tryQuery().isPresent());
    }

    @Test
    @DisplayName("ソートキーのComboBoxの項目は、AlbumとArtistである")
    void the_items_of_the_sort_key_combo_box_are_album_and_artist(FxRobot robot) {
        var sortKey = robot.lookup("#sortKey").queryComboBox();

        assertEquals(List.of(SortKey.ALBUM, SortKey.ARTIST), sortKey.getItems());
    }

    @Test
    @DisplayName("ソートキーのComboBox（初期値Album）が表示され、アルバムリストはアルバム名の昇順（大文字小文字無視）で並ぶ")
    void the_album_list_is_sorted_by_album_name_ignoring_case_with_album_as_the_default_key(
        FxRobot robot) {
        var banana = new MusicFile(Path.of("a/one.mp3"), 100, albumTag("banana", "X"));
        var apple = new MusicFile(Path.of("b/two.mp3"), 200, albumTag("Apple", "X"));
        var cherry = new MusicFile(Path.of("c/three.mp3"), 300, albumTag("Cherry", "X"));
        robot.interact(() -> viewModel.setFiles(List.of(banana, apple, cherry)));

        var sortKey = robot.lookup("#sortKey").queryComboBox();
        assertEquals(SortKey.ALBUM, sortKey.getValue());
        var albumList = robot.lookup("#albumList").queryListView();
        assertEquals(
            List.of(
                new Album("Apple", "X", List.of(apple)),
                new Album("banana", "X", List.of(banana)),
                new Album("Cherry", "X", List.of(cherry))
            ),
            albumList.getItems()
        );
    }

    @Test
    @DisplayName("ソートキーをArtistに変更すると、アルバムリストはアーティスト名の昇順（大文字小文字無視）で並ぶ")
    void changing_the_sort_key_to_artist_sorts_the_album_list_by_artist_name_ignoring_case(
        FxRobot robot) {
        var cider = new MusicFile(Path.of("a/one.mp3"), 100, albumTag("Cider", "cherry"));
        var beer = new MusicFile(Path.of("b/two.mp3"), 200, albumTag("Beer", "apple"));
        var ale = new MusicFile(Path.of("c/three.mp3"), 300, albumTag("Ale", "Banana"));
        robot.interact(() -> viewModel.setFiles(List.of(cider, beer, ale)));

        robot.clickOn("#sortKey").clickOn("Artist");

        var albumList = robot.lookup("#albumList").queryListView();
        assertEquals(
            List.of(
                new Album("Beer", "apple", List.of(beer)),
                new Album("Ale", "Banana", List.of(ale)),
                new Album("Cider", "cherry", List.of(cider))
            ),
            albumList.getItems()
        );
    }

    @Test
    @DisplayName("アルバムを選択すると、右のリストに曲が「トラック番号. 曲名」でトラック番号順に表示される")
    void selecting_an_album_shows_its_tracks_ordered_by_track_number_in_the_track_list(
        FxRobot robot) {
        var trackTwo = new MusicFile(
            Path.of("a/two.mp3"), 100,
            new TrackMetadata("Song Two", "", "Album A", "Artist X", "2", "")
        );
        var trackOne = new MusicFile(
            Path.of("a/one.mp3"), 200,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(trackTwo, trackOne)));

        robot.clickOn("Album A - Artist X");

        var trackList = robot.lookup("#trackList").queryListView();
        assertEquals(List.of(trackOne, trackTwo), trackList.getItems());
        assertTrue(robot.lookup("1. Song One").tryQuery().isPresent());
        assertTrue(robot.lookup("2. Song Two").tryQuery().isPresent());
    }

    @Test
    @DisplayName("アルバムを選択すると、曲リストの最初の曲が選択済みになる")
    void selecting_an_album_selects_the_first_track_in_the_track_list(FxRobot robot) {
        var trackTwo = new MusicFile(
            Path.of("a/two.mp3"), 100,
            new TrackMetadata("Song Two", "", "Album A", "Artist X", "2", "")
        );
        var trackOne = new MusicFile(
            Path.of("a/one.mp3"), 200,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(trackTwo, trackOne)));

        robot.clickOn("Album A - Artist X");

        var trackList = robot.lookup("#trackList").queryListView();
        assertEquals(trackOne, trackList.getSelectionModel().getSelectedItem());
    }

    @Test
    @DisplayName("曲名が空の曲は、ファイル名で表示される")
    void a_track_with_an_empty_title_is_shown_by_its_file_name(FxRobot robot) {
        var untitled = new MusicFile(
            Path.of("a/song.mp3"), 100,
            new TrackMetadata("", "", "Album A", "Artist X", "1", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(untitled)));

        robot.clickOn("Album A - Artist X");

        assertTrue(robot.lookup("1. song.mp3").tryQuery().isPresent());
    }

    @Test
    @DisplayName("アルバムを選択すると、曲リストの上のパネルにアルバム名とアーティスト名が表示される")
    void selecting_an_album_shows_its_name_and_artist_in_the_album_info_panel(FxRobot robot) {
        var files = List.of(
            new MusicFile(Path.of("a/one.mp3"), 100, albumTag("Album A", "Artist X"))
        );
        robot.interact(() -> viewModel.setFiles(files));

        robot.clickOn("Album A - Artist X");

        verifyThat("#albumInfoName", LabeledMatchers.hasText("Album A"));
        verifyThat("#albumInfoArtist", LabeledMatchers.hasText("Artist X"));
    }

    @Test
    @DisplayName("アルバム情報パネルのEditボタンを押すと、メタデータ編集ウィンドウが開く")
    void clicking_the_edit_button_in_the_album_info_panel_opens_the_metadata_edit_window(
        FxRobot robot) {
        var files = List.of(
            new MusicFile(Path.of("a/one.mp3"), 100, albumTag("Album A", "Artist X"))
        );
        robot.interact(() -> viewModel.setFiles(files));
        robot.clickOn("Album A - Artist X");

        robot.clickOn("#editAlbumButton");

        var window = robot.window("Edit Album");
        assertTrue(window.isShowing());
    }

    @Test
    @DisplayName("アルバム未選択のとき、Editボタンは無効である")
    void the_edit_button_is_disabled_when_no_album_is_selected(FxRobot robot) {
        robot.interact(() -> viewModel.selectAlbum(null));

        var editButton = robot.lookup("#editAlbumButton").queryButton();

        assertTrue(editButton.isDisabled());
    }

    private static TrackMetadata albumTag(String album, String albumArtist) {
        return new TrackMetadata("", "", album, albumArtist, "", "");
    }
}
