package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.util.WaitForAsyncUtils;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.Settings;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.domain.service.NullLibraryDatabase;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;
import com.sosuisha.presentation.screens.settings.SettingsView;
import com.sosuisha.presentation.screens.settings.SettingsViewModel;
import com.sosuisha.service.DuplicateFileMover;
import com.sosuisha.service.LibraryScanner;
import com.sosuisha.service.SettingsRepository;

import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

@ExtendWith(ApplicationExtension.class)
class LibraryManagerViewTest {
    @TempDir
    Path folder;

    private Stage stage;
    private LibraryManagerViewModel viewModel;
    private MusicLibraryAppModel appModel;
    private AtomicBoolean rescanned;
    private AtomicReference<Path> playedPath;
    private AtomicBoolean playbackStopped;
    private AtomicBoolean playbackPaused;
    private AtomicBoolean playbackResumed;

    @Start
    void setup(Stage stage) {
        this.stage = stage;
        var windowManager = new WindowManager();
        rescanned = new AtomicBoolean(false);
        appModel = new MusicLibraryAppModel(
            new LibraryScanner(new NullLibraryDatabase()),
            new SettingsAppModel(new SettingsRepository())
        ) {
            @Override
            public void rescan() {
                rescanned.set(true);
            }
        };
        playedPath = new AtomicReference<>();
        playbackStopped = new AtomicBoolean(false);
        playbackPaused = new AtomicBoolean(false);
        playbackResumed = new AtomicBoolean(false);
        viewModel = new LibraryManagerViewModel(windowManager, appModel, new MusicPlayer() {
            @Override
            public void play(Path path) {
                playedPath.set(path);
            }

            @Override
            public void stop() {
                playbackStopped.set(true);
            }

            @Override
            public void pause() {
                playbackPaused.set(true);
            }

            @Override
            public void resume() {
                playbackResumed.set(true);
            }
        });
        var view = new LibraryManagerView(viewModel);
        windowManager.registerView(view);
        windowManager.registerView(
            new DuplicateListView(
                new DuplicateListViewModel(
                    appModel,
                    new NullMusicPlayer(),
                    new DuplicateFileMover(Path.of("duplicates"), Path.of("duplicates.log")),
                    _ -> {
                    }
                )
            )
        );
        var settingsAppModel = new SettingsAppModel(new SettingsRepository());
        settingsAppModel.setSettings(new Settings(Path.of("music")));
        windowManager.registerView(
            new SettingsView(new SettingsViewModel(settingsAppModel, _ -> Optional.empty()))
        );
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }

    @Test
    @DisplayName("ライブラリ管理ウィンドウが表示され、タイトルはLibrary Managerである")
    void window_is_shown_with_title_library_manager() {
        assertTrue(stage.isShowing());
        assertEquals("Library Manager", stage.getTitle());
    }

    @Test
    @DisplayName("FileメニューのRescanを選ぶと、ライブラリが再スキャンされる")
    void selecting_rescan_menu_rescans_the_library(FxRobot robot) {
        robot.clickOn("File").clickOn("Rescan");

        assertTrue(rescanned.get());
    }

    @Test
    @DisplayName("FileメニューのRemove duplicate files...を選ぶと、重複リスト表示ウィンドウが開く")
    void selecting_remove_duplicate_files_menu_opens_the_duplicate_list_window(FxRobot robot) {
        robot.clickOn("File").clickOn("Remove duplicate files...");

        var window = robot.window("Duplicate Files");

        assertTrue(window.isShowing());
    }

    @Test
    @DisplayName("FileメニューのSettings...を選ぶと、設定ウィンドウが開く")
    void selecting_settings_menu_opens_the_settings_window(FxRobot robot) {
        robot.clickOn("File").clickOn("Settings...");

        var window = robot.window("Settings");

        assertTrue(window.isShowing());
    }

    @Test
    @DisplayName("設定ウィンドウはアプリケーションモーダルである")
    void the_settings_window_is_application_modal(FxRobot robot) {
        robot.clickOn("File").clickOn("Settings...");

        var window = (Stage) robot.window("Settings");

        assertEquals(Modality.APPLICATION_MODAL, window.getModality());
    }

    @Test
    @DisplayName("スキャン中はモーダルなScanningウィンドウが表示され、完了すると閉じる")
    void modal_scanning_window_is_shown_while_a_scan_runs_and_closed_when_it_finishes(
        FxRobot robot) throws Exception {
        var shownDuringScan = new AtomicBoolean(false);
        var modality = new AtomicReference<Modality>();
        robot.interact(() -> {
            appModel.scanFolder(folder);
            findScanningWindow(robot).ifPresent(window -> {
                shownDuringScan.set(window.isShowing());
                modality.set(window.getModality());
            });
        });

        assertTrue(shownDuringScan.get());
        assertEquals(Modality.APPLICATION_MODAL, modality.get());
        WaitForAsyncUtils.waitFor(
            5,
            TimeUnit.SECONDS,
            () -> findScanningWindow(robot).map(window -> !window.isShowing()).orElse(true)
        );
    }

    @Test
    @DisplayName("スキャン中ダイアログに、読み込み中のファイルパスが表示される")
    void the_scanning_dialog_shows_the_path_of_the_file_being_read(FxRobot robot)
        throws Exception {
        var file = Files.createFile(folder.resolve("song1.mp3"));
        var scanFinished = new CountDownLatch(1);
        var blockingScanner = new LibraryScanner(new NullLibraryDatabase()) {
            @Override
            public List<MusicFile> scan(Path folderPath, Consumer<Path> onFileRead)
                throws IOException {
                var result = super.scan(folderPath, onFileRead);
                // Keeps the scan running until the test releases it, so the
                // dialog stays open while the label is checked.
                try {
                    scanFinished.await();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                return result;
            }
        };
        var blockingAppModel = new MusicLibraryAppModel(
            blockingScanner, new SettingsAppModel(new SettingsRepository())
        );
        var blockingViewModel =
            new LibraryManagerViewModel(
                new WindowManager(), blockingAppModel, new NullMusicPlayer()
            );
        try {
            robot.interact(() -> {
                new LibraryManagerView(blockingViewModel);
                blockingAppModel.scanFolder(folder);
            });

            WaitForAsyncUtils.waitFor(
                5,
                TimeUnit.SECONDS,
                () -> robot.lookup("#scanningFile")
                    .tryQueryAs(Label.class)
                    .map(label -> file.toString().equals(label.getText()))
                    .orElse(false)
            );
        } finally {
            scanFinished.countDown();
        }
        WaitForAsyncUtils.waitFor(
            5,
            TimeUnit.SECONDS,
            () -> findScanningWindow(robot).map(window -> !window.isShowing()).orElse(true)
        );
    }

    @Test
    @DisplayName("Scanningウィンドウのオーナーは、ライブラリ管理ウィンドウである")
    void the_owner_of_the_scanning_window_is_the_library_manager_window(FxRobot robot) {
        var owner = new AtomicReference<Window>();
        robot.interact(() -> {
            appModel.scanFolder(folder);
            findScanningWindow(robot).ifPresent(window -> owner.set(window.getOwner()));
        });

        assertEquals(stage, owner.get());
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

    private static TrackMetadata albumTag(String album, String albumArtist) {
        return new TrackMetadata("", "", album, albumArtist, "", "");
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
    @DisplayName("停止ボタンを押すと、プレイヤーに停止が要求される")
    void clicking_the_stop_button_requests_the_player_to_stop(FxRobot robot) {
        robot.clickOn("#stopButton");

        assertTrue(playbackStopped.get());
    }

    @Test
    @DisplayName("プレイヤーパネルに、再生ボタンと停止ボタンが表示される")
    void the_player_panel_shows_a_play_button_and_a_stop_button(FxRobot robot) {
        var panel = robot.lookup("#playerPanel").query();

        assertTrue(robot.from(panel).lookup("#playButton").tryQuery().isPresent());
        assertTrue(robot.from(panel).lookup("#stopButton").tryQuery().isPresent());
    }

    private static Optional<Stage> findScanningWindow(FxRobot robot) {
        return robot.listWindows().stream()
            .filter(window -> window instanceof Stage shown && "Scanning".equals(shown.getTitle()))
            .map(Stage.class::cast)
            .findFirst();
    }
}
