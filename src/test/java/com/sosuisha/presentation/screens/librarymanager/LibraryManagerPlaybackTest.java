package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

class LibraryManagerPlaybackTest extends LibraryManagerViewTestBase {
    @Start
    void setup(Stage stage) {
        setUpLibraryManager(stage);
    }

    @Test
    @DisplayName("再生ボタンを押すと、曲リストで選択中の曲の再生がプレイヤーに要求される")
    void clicking_the_play_button_requests_the_player_to_play_the_selected_track(FxRobot robot) {
        var track = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(track)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("1. Song One");

        robot.clickOn("#playButton");

        assertEquals(Path.of("a/one.mp3"), playedPath.get());
    }

    @Test
    @DisplayName("再生ボタンを押すと、ボタンの表示が一時停止（❘❘）に切り替わる")
    void clicking_the_play_button_changes_the_button_to_a_pause_button(FxRobot robot) {
        var track = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(track)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("1. Song One");

        robot.clickOn("#playButton");

        verifyThat("#playButton", LabeledMatchers.hasText("❘❘"));
    }

    @Test
    @DisplayName("再生中に一時停止ボタン（❘❘）を押すと、プレイヤーに一時停止が要求され、表示は▶に戻る")
    void clicking_the_pause_button_while_playing_requests_the_player_to_pause(FxRobot robot) {
        var track = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(track)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("1. Song One");
        robot.clickOn("#playButton");

        robot.clickOn("#playButton");

        assertTrue(playbackPaused.get());
        verifyThat("#playButton", LabeledMatchers.hasText("▶"));
    }

    @Test
    @DisplayName("一時停止後に再生ボタン（▶）を押すと、続きからの再開がプレイヤーに要求される")
    void clicking_the_play_button_after_pausing_requests_the_player_to_resume(FxRobot robot) {
        var track = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(track)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("1. Song One");
        robot.clickOn("#playButton");
        robot.clickOn("#playButton");

        robot.clickOn("#playButton");

        assertTrue(playbackResumed.get());
        assertEquals(PlayerState.PLAYING, viewModel.playerStateProperty().get());
    }

    @Test
    @DisplayName("一時停止中に別の曲へ移動して▶を押すと、選択中の曲が冒頭から再生される")
    void clicking_play_after_moving_to_another_track_while_paused_plays_the_selected_track(
        FxRobot robot) {
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
        robot.clickOn("#playButton");
        robot.clickOn("#nextButton");

        robot.clickOn("#playButton");

        assertEquals(Path.of("a/two.mp3"), playedPath.get());
        assertFalse(playbackResumed.get());
        assertEquals(PlayerState.PLAYING, viewModel.playerStateProperty().get());
    }

    @Test
    @DisplayName("停止ボタンを押すと、プレイヤーに停止が要求される")
    void clicking_the_stop_button_requests_the_player_to_stop(FxRobot robot) {
        robot.clickOn("#stopButton");

        assertTrue(playbackStopped.get());
    }

    @Test
    @DisplayName("プレイヤーパネルの右端に、Open folderボタンが表示される")
    void the_player_panel_shows_an_open_folder_button_at_the_right_end(FxRobot robot) {
        var panel = robot.lookup("#playerPanel").queryAs(HBox.class);

        var openFolder = robot.from(panel).lookup("#openFolderButton").tryQueryAs(Button.class);

        assertTrue(openFolder.isPresent());
        assertEquals("Open folder", openFolder.get().getText());
        assertEquals(openFolder.get(), panel.getChildren().getLast());
    }

    @Test
    @DisplayName("Open folderボタンを押すと、選択中の曲のフォルダを開く処理が呼ばれる")
    void clicking_the_open_folder_button_opens_the_folder_of_the_selected_track(FxRobot robot) {
        var track = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "")
        );
        robot.interact(() -> viewModel.setFiles(List.of(track)));
        robot.clickOn("Album A - Artist X");
        robot.clickOn("1. Song One");

        robot.clickOn("#openFolderButton");

        assertEquals(Path.of("a"), openedFolder.get());
    }

    @Test
    @DisplayName("曲が未選択のときにOpen folderボタンを押しても、何も起きない")
    void clicking_the_open_folder_button_does_nothing_when_no_track_is_selected(FxRobot robot)
        throws Throwable {
        robot.clickOn("#openFolderButton");

        // FXスレッド上のイベントハンドラで起きた例外（NPE）があれば、投げ直してテスト失敗。
        WaitForAsyncUtils.checkException();
        assertNull(openedFolder.get());
    }

    @Test
    @DisplayName("プレイヤーパネルに、再生ボタンと停止ボタンが表示される")
    void the_player_panel_shows_a_play_button_and_a_stop_button(FxRobot robot) {
        var panel = robot.lookup("#playerPanel").query();

        assertTrue(robot.from(panel).lookup("#playButton").tryQuery().isPresent());
        assertTrue(robot.from(panel).lookup("#stopButton").tryQuery().isPresent());
    }
}
