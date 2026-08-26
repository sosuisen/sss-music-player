package com.sosuisha.presentation.screens.librarymanager;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;
import com.uber.nullaway.annotations.Initializer;

import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.domain.repository.NullLibraryRepository;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.presentation.screens.albumedit.AlbumEditView;
import com.sosuisha.presentation.screens.albumedit.AlbumEditViewModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;
import com.sosuisha.presentation.screens.settings.SettingsView;
import com.sosuisha.presentation.screens.settings.SettingsViewModel;
import com.sosuisha.service.DuplicateFileMover;
import com.sosuisha.service.LibraryIndexer;
import com.sosuisha.domain.repository.NullSettingsRepository;

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
    AlbumEditViewModel albumEditViewModel;
    SimpleObjectProperty<Path> musicLibraryPath;
    MusicLibraryAppModel appModel;
    SettingsAppModel settingsAppModel;
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
    //
    // NullAway reports a field without @Nullable when the constructor does not
    // set it, unless a method that NullAway knows as an "initializer" sets it.
    // In pom.xml, two things define such methods:
    // - the NullAway option CustomInitializerAnnotations=org.testfx.framework.junit5.Start
    //   makes every @Start method an initializer
    // - the test dependency com.uber.nullaway:nullaway-annotations provides
    //   @Initializer, which marks any method as an initializer
    // The @Start methods of the subclasses only call this method, and NullAway
    // does not follow that call, so this method needs @Initializer.
    @Initializer
    void setUpLibraryManager(Stage stage) {
        this.stage = stage;
        var windowManager = new WindowManager();
        rescanned = new AtomicBoolean(false);
        musicLibraryPath = new SimpleObjectProperty<>();
        appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()), musicLibraryPath
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
        settingsAppModel = new SettingsAppModel(new NullSettingsRepository());
        viewModel = new LibraryManagerViewModel(
            windowManager, appModel, settingsAppModel,
            new MusicPlayer() {
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
            }, openedFolder::set
        );
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
        var settingsViewModel = new SettingsViewModel(settingsAppModel, () -> Optional.empty());
        settingsViewModel.musicLibraryPathProperty().set(Path.of("music"));
        windowManager.registerView(new SettingsView(settingsViewModel));
        albumEditViewModel = new AlbumEditViewModel(appModel, (_, _, _) -> {
        });
        windowManager.registerView(new AlbumEditView(albumEditViewModel));
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }

    // The fake music player stores the callback that runs when a track ends.
    void fireTrackFinished() {
        Objects.requireNonNull(trackFinishedCallback.get(), "no track finished callback").run();
    }
}
