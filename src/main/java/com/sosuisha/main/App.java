package com.sosuisha.main;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.sosuisha.presentation.View;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerView;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerViewModel;
import com.sosuisha.presentation.screens.settings.SettingsView;
import com.sosuisha.presentation.screens.settings.SettingsViewModel;
import com.sosuisha.service.SettingsRepository;

import javafx.application.Application;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * JavaFX application of SSS Music Player.
 */
public class App extends Application {
    /** The view whose window is shown first at startup. Change this constant during development. */
    static final Class<? extends View> FIRST_VIEW = LibraryManagerView.class;

    /**
     * Called when the application is started. Shows the library manager window
     * as the first window. When the settings file does not exist, the modal
     * settings window is opened over the first window.
     *
     * @param stage the primary stage for this application
     * @throws NullPointerException if stage is null
     * @throws UncheckedIOException if the settings file exists but cannot be read
     */
    @Override
    public void start(Stage stage) {
        Objects.requireNonNull(stage, "stage must not be null");
        var windowManager = new WindowManager();
        var appModel = new MusicLibraryAppModel();
        var viewModel = new LibraryManagerViewModel(windowManager, appModel);
        // Dummy data for development until the library scan is wired up.
        // The same file name appears in two folders to exercise duplicate detection.
        appModel.setFiles(
            List.of(
                Path.of("a/dummy1.mp3"),
                Path.of("b/dummy1.mp3"),
                Path.of("dummy2.m4a")
            )
        );
        windowManager.registerView(new LibraryManagerView(viewModel));
        windowManager.registerView(new DuplicateListView(new DuplicateListViewModel(appModel)));
        var settingsAppModel = new SettingsAppModel(new SettingsRepository());
        var loadedSettings = settingsAppModel.loadSettings();
        windowManager.registerView(
            new SettingsView(new SettingsViewModel(settingsAppModel, App::chooseDirectory))
        );
        windowManager.showWindow(FIRST_VIEW, stage);
        if (loadedSettings.isEmpty()) {
            viewModel.openSettingsWindow();
        }
    }

    private static Optional<Path> chooseDirectory(Window ownerWindow) {
        var chooser = new DirectoryChooser();
        return Optional.ofNullable(chooser.showDialog(ownerWindow)).map(File::toPath);
    }
}
