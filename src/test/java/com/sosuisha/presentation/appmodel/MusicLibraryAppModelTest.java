package com.sosuisha.presentation.appmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.sosuisha.service.LibraryScanner;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class MusicLibraryAppModelTest {
    @TempDir
    Path folder;

    @Start
    void setup(Stage stage) {
        // Initializes the JavaFX toolkit, which the background scan needs to
        // update the files on the FX thread.
    }

    @Test
    @DisplayName("フォルダの走査はバックグラウンドで行われ、完了すると一覧が更新される")
    void scanning_a_folder_runs_in_the_background_and_updates_the_files_when_finished(
        FxRobot robot) throws Exception {
        Files.createFile(folder.resolve("song1.mp3"));
        Files.createFile(folder.resolve("song2.m4a"));
        var appModel = new MusicLibraryAppModel(new LibraryScanner());

        // AtomicInteger is a mutable box to carry the size measured on the FX
        // thread out to the test thread. -1 means "not measured yet".
        var sizeRightAfterCall = new AtomicInteger(-1);
        robot.interact(() -> {
            appModel.scanFolder(folder);
            sizeRightAfterCall.set(appModel.getFiles().size());
        });

        assertEquals(0, sizeRightAfterCall.get());
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> appModel.getFiles().size() == 2);
    }
}
