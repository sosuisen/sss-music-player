package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.NullLibraryDatabase;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.LibraryScanner;
import com.sosuisha.service.SettingsRepository;

import javafx.collections.ObservableList;

class LibraryManagerViewModelTest {
    @Test
    @DisplayName("受け取った初期リストをObservableListに格納する")
    void stores_received_initial_list_in_observable_list() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryScanner(new NullLibraryDatabase()), new SettingsAppModel(new SettingsRepository())
                )
            );
        var files = List.of(
            new MusicFile(Path.of("first.mp3"), 100),
            new MusicFile(Path.of("second.m4a"), 200)
        );

        viewModel.setFiles(files);

        assertInstanceOf(ObservableList.class, viewModel.getFiles());
        assertEquals(files, viewModel.getFiles());
    }

    @Test
    @DisplayName("AppModelのファイルリストをそのまま返す")
    void returns_the_file_list_of_the_app_model() {
        var appModel = new MusicLibraryAppModel(
            new LibraryScanner(new NullLibraryDatabase()), new SettingsAppModel(new SettingsRepository())
        );
        var viewModel = new LibraryManagerViewModel(new WindowManager(), appModel);

        assertSame(appModel.getFiles(), viewModel.getFiles());
    }
}
