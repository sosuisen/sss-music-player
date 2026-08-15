package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.NullLibraryDatabase;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.LibraryIndexer;
import com.sosuisha.repository.SettingsRepository;

import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

class LibraryManagerWindowTest extends LibraryManagerViewTestBase {
    @Start
    void setup(Stage stage) {
        setUpLibraryManager(stage);
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
        var blockingScanner = new LibraryIndexer(new NullLibraryDatabase()) {
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
                new WindowManager(), blockingAppModel, new NullMusicPlayer(), _ -> {
                }
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

    private static Optional<Stage> findScanningWindow(FxRobot robot) {
        return robot.listWindows().stream()
            .filter(window -> window instanceof Stage shown && "Scanning".equals(shown.getTitle()))
            .map(Stage.class::cast)
            .findFirst();
    }
}
