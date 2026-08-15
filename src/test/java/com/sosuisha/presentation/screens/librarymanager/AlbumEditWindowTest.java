package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.TextInputControlMatchers;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

class AlbumEditWindowTest extends LibraryManagerViewTestBase {
    @Start
    void setup(Stage stage) {
        setUpLibraryManager(stage);
    }

    @Test
    @DisplayName("編集ウィンドウを開くと、選択中のアルバムのアルバム名とアルバムアーティストがフィールドに表示される")
    void the_fields_show_the_name_and_artist_of_the_selected_album_when_the_window_opens(
        FxRobot robot) {
        var files = List.of(
            new MusicFile(
                Path.of("a/one.mp3"), 100,
                new TrackMetadata("", "", "Album A", "Artist X", "", "")
            )
        );
        robot.interact(() -> viewModel.setFiles(files));
        robot.clickOn("Album A - Artist X");

        robot.clickOn("#editAlbumButton");

        verifyThat("#albumNameField", TextInputControlMatchers.hasText("Album A"));
        verifyThat("#albumArtistField", TextInputControlMatchers.hasText("Artist X"));
    }

    @Test
    @DisplayName("アルバムの元の値と異なる値のフィールドだけが、明るい黄色のchanged-fieldスタイルになる")
    void only_a_field_whose_value_differs_from_the_album_has_the_changed_field_style(
        FxRobot robot) {
        var files = List.of(
            new MusicFile(
                Path.of("a/one.mp3"), 100,
                new TrackMetadata("Song One", "Artist X", "Album A", "", "1", "")
            )
        );
        robot.interact(() -> viewModel.setFiles(files));
        robot.clickOn("Album A - ");

        robot.clickOn("#editAlbumButton");

        var nameField = robot.lookup("#albumNameField").queryAs(TextField.class);
        var artistField = robot.lookup("#albumArtistField").queryAs(TextField.class);
        assertFalse(nameField.getStyleClass().contains("changed-field"));
        assertTrue(artistField.getStyleClass().contains("changed-field"));
    }

    @Test
    @DisplayName("フィールドが変更されていないときSaveボタンは無効で、変更すると有効になる")
    void the_save_button_is_enabled_only_while_a_field_differs_from_the_album(FxRobot robot) {
        var files = List.of(
            new MusicFile(
                Path.of("a/one.mp3"), 100,
                new TrackMetadata("Song One", "Artist X", "Album A", "Artist X", "1", "")
            )
        );
        robot.interact(() -> viewModel.setFiles(files));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("#editAlbumButton");

        var saveButton = robot.lookup("#saveButton").queryButton();
        assertTrue(saveButton.isDisabled());

        robot.clickOn("#albumNameField").write("!");

        assertFalse(saveButton.isDisabled());
    }

    @Test
    @DisplayName("Saveすると、Saveボタンの下に「編集ウィンドウを閉じるとライブラリが再読み込みされます」と表示される")
    void saving_shows_the_reload_notice_under_the_save_button(FxRobot robot) {
        var files = List.of(
            new MusicFile(
                Path.of("a/one.mp3"), 100,
                new TrackMetadata("Song One", "Artist X", "Album A", "Artist X", "1", "")
            )
        );
        robot.interact(() -> viewModel.setFiles(files));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("#editAlbumButton");
        var notice = robot.lookup("#reloadNotice").queryAs(Label.class);
        assertFalse(notice.isVisible());
        robot.clickOn("#albumNameField").write("!");

        robot.clickOn("#saveButton");

        assertTrue(notice.isVisible());
        assertEquals("編集ウィンドウを閉じるとライブラリが再読み込みされます", notice.getText());
    }

    @Test
    @DisplayName("編集ウィンドウを閉じて開き直すと、再読み込みの表示は消えている")
    void reopening_the_edit_window_hides_the_reload_notice(FxRobot robot) {
        var files = List.of(
            new MusicFile(
                Path.of("a/one.mp3"), 100,
                new TrackMetadata("Song One", "Artist X", "Album A", "Artist X", "1", "")
            )
        );
        robot.interact(() -> viewModel.setFiles(files));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("#editAlbumButton");
        robot.clickOn("#albumNameField").write("!");
        robot.clickOn("#saveButton");

        robot.interact(() -> robot.window("Edit Album").hide());
        robot.clickOn("#editAlbumButton");

        var notice = robot.lookup("#reloadNotice").queryAs(Label.class);
        assertFalse(notice.isVisible());
    }
}
