package com.sosuisha.presentation.screens.librarymanager;

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

}
