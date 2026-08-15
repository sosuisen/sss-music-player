package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

import javafx.stage.Stage;

class LibraryManagerTrackNavigationTest extends LibraryManagerViewTestBase {
    @Start
    void setup(Stage stage) {
        setUpLibraryManager(stage);
    }

    @Test
    @DisplayName("次の曲ボタン（▶▶）を押すと、曲リストの選択が次の曲に移る")
    void clicking_the_next_button_moves_the_selection_to_the_next_track(FxRobot robot) {
        var trackOne = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        var trackTwo = new MusicFile(
            Path.of("a/two.mp3"), 200,
            new TrackMetadata("Song Two", "", "Album A", "Artist X", "2", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(trackOne, trackTwo)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("1. Song One");

        robot.clickOn("#nextButton");

        var trackList = robot.lookup("#trackList").queryListView();
        assertEquals(trackTwo, trackList.getSelectionModel().getSelectedItem());
    }

    @Test
    @DisplayName("前の曲ボタン（◀◀）を押すと、曲リストの選択が前の曲に移る")
    void clicking_the_previous_button_moves_the_selection_to_the_previous_track(FxRobot robot) {
        var trackOne = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        var trackTwo = new MusicFile(
            Path.of("a/two.mp3"), 200,
            new TrackMetadata("Song Two", "", "Album A", "Artist X", "2", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(trackOne, trackTwo)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("2. Song Two");

        robot.clickOn("#prevButton");

        var trackList = robot.lookup("#trackList").queryListView();
        assertEquals(trackOne, trackList.getSelectionModel().getSelectedItem());
    }

    @Test
    @DisplayName("末尾の曲で▶▶は先頭へ、先頭の曲で◀◀は末尾へラップする")
    void next_wraps_to_the_first_track_and_previous_wraps_to_the_last_track(FxRobot robot) {
        var trackOne = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        var trackTwo = new MusicFile(
            Path.of("a/two.mp3"), 200,
            new TrackMetadata("Song Two", "", "Album A", "Artist X", "2", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(trackOne, trackTwo)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("2. Song Two");
        var trackList = robot.lookup("#trackList").queryListView();

        robot.clickOn("#nextButton");
        assertEquals(trackOne, trackList.getSelectionModel().getSelectedItem());

        robot.clickOn("#prevButton");
        assertEquals(trackTwo, trackList.getSelectionModel().getSelectedItem());
    }

    @Test
    @DisplayName("再生中に次の曲ボタン（▶▶）を押すと、移動先の曲が即再生される")
    void clicking_the_next_button_while_playing_plays_the_next_track(FxRobot robot) {
        var trackOne = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        var trackTwo = new MusicFile(
            Path.of("a/two.mp3"), 200,
            new TrackMetadata("Song Two", "", "Album A", "Artist X", "2", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(trackOne, trackTwo)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("1. Song One");
        robot.clickOn("#playButton");

        robot.clickOn("#nextButton");

        assertEquals(Path.of("a/two.mp3"), playedPath.get());
        assertEquals(PlayerState.PLAYING, viewModel.playerStateProperty().get());
    }

    @Test
    @DisplayName("1曲の再生が終わると、次の曲が再生される（末尾の曲では先頭へ戻る）")
    void the_next_track_is_played_when_a_track_finishes(FxRobot robot) {
        var trackOne = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        var trackTwo = new MusicFile(
            Path.of("a/two.mp3"), 200,
            new TrackMetadata("Song Two", "", "Album A", "Artist X", "2", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(trackOne, trackTwo)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("1. Song One");
        robot.clickOn("#playButton");

        robot.interact(() -> trackFinishedCallback.get().run());
        assertEquals(Path.of("a/two.mp3"), playedPath.get());

        robot.interact(() -> trackFinishedCallback.get().run());
        assertEquals(Path.of("a/one.mp3"), playedPath.get());
    }

    @Test
    @DisplayName("再生中に別の曲をダブルクリックすると、その曲の再生が開始される")
    void double_clicking_another_track_while_playing_starts_playing_that_track(FxRobot robot) {
        var trackOne = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        var trackTwo = new MusicFile(
            Path.of("a/two.mp3"), 200,
            new TrackMetadata("Song Two", "", "Album A", "Artist X", "2", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(trackOne, trackTwo)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("1. Song One");
        robot.clickOn("#playButton");

        robot.doubleClickOn("2. Song Two");

        assertEquals(Path.of("a/two.mp3"), playedPath.get());
        assertEquals(PlayerState.PLAYING, viewModel.playerStateProperty().get());
    }
}
