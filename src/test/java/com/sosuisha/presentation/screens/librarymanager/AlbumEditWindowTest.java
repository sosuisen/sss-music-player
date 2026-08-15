package com.sosuisha.presentation.screens.librarymanager;

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
}
