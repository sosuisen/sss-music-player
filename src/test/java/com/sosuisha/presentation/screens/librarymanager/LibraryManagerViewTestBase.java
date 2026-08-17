package com.sosuisha.presentation.screens.librarymanager;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;

import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.domain.service.NullLibraryRepository;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.screens.albumedit.AlbumEditView;
import com.sosuisha.presentation.screens.albumedit.AlbumEditViewModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;
import com.sosuisha.presentation.screens.settings.SettingsView;
import com.sosuisha.presentation.screens.settings.SettingsViewModel;
import com.sosuisha.service.DuplicateFileMover;
import com.sosuisha.service.LibraryIndexer;
import com.sosuisha.domain.service.NullSettingsRepository;

import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

/**
 * Shared setup of the library manager view tests: a stub music player, the
 * app model, and the view shown on a stage.
 */
@ExtendWith(ApplicationExtension.class)
abstract class LibraryManagerViewTestBase {
    @TempDir
    Path folder;

    Stage stage;
    LibraryManagerViewModel viewModel;
    MusicLibraryAppModel appModel;
    AtomicBoolean rescanned;
    AtomicReference<Path> playedPath;
    AtomicBoolean playbackStopped;
    AtomicBoolean playbackPaused;
    AtomicBoolean playbackResumed;
    AtomicReference<Runnable> trackFinishedCallback;
    AtomicReference<Path> loadedPath;
    AtomicReference<Path> openedFolder;

    // TestFX's ApplicationExtension looks up @Start with getDeclaredMethods(),
    // which does not see inherited methods, so each subclass declares a
    // @Start method that delegates here.
    void setUpLibraryManager(Stage stage) {
        this.stage = stage;
        var windowManager = new WindowManager();
        rescanned = new AtomicBoolean(false);
        appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SimpleObjectProperty<>()
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
        trackFinishedCallback = new AtomicReference<>();
        loadedPath = new AtomicReference<>();
        openedFolder = new AtomicReference<>();
        viewModel = new LibraryManagerViewModel(windowManager, appModel, new MusicPlayer() {
            @Override
            public void play(Path path) {
                playedPath.set(path);
                loadedPath.set(path);
            }

            @Override
            public void stop() {
                playbackStopped.set(true);
                loadedPath.set(null);
            }

            @Override
            public void pause() {
                playbackPaused.set(true);
            }

            @Override
            public void resume() {
                playbackResumed.set(true);
            }

            @Override
            public void setOnFinished(Runnable onFinished) {
                trackFinishedCallback.set(onFinished);
            }

            @Override
            public Optional<Path> playingPath() {
                return Optional.ofNullable(loadedPath.get());
            }
        }, openedFolder::set);
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
        var settingsViewModel =
            new SettingsViewModel(new NullSettingsRepository(), _ -> Optional.empty());
        settingsViewModel.musicLibraryPathProperty().set(Path.of("music"));
        windowManager.registerView(new SettingsView(settingsViewModel));
        windowManager.registerView(
            new AlbumEditView(new AlbumEditViewModel(appModel, (_, _, _) -> {
            }))
        );
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }
}
