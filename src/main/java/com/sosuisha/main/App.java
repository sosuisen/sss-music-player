package com.sosuisha.main;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import com.sosuisha.presentation.View;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.presentation.screens.alert.AlertDialog;
import com.sosuisha.presentation.screens.albumedit.AlbumEditView;
import com.sosuisha.presentation.screens.albumedit.AlbumEditViewModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerView;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerViewModel;
import com.sosuisha.presentation.screens.settings.SettingsView;
import com.sosuisha.presentation.screens.settings.SettingsViewModel;
import com.sosuisha.service.DuplicateFileMover;
import com.sosuisha.service.JaudiotaggerTagWriter;
import com.sosuisha.service.ShellFolderOpener;
import com.sosuisha.service.LibraryIndexer;
import com.sosuisha.service.MediaMusicPlayer;
import com.sosuisha.repository.SettingsRepositoryImpl;
import com.sosuisha.repository.SqliteLibraryRepository;

import com.sosuisha.domain.exception.SettingsException;
import com.sosuisha.domain.model.Settings;
import com.sosuisha.domain.model.Theme;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;

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
     * Called when the application is started. Applies the current theme and
     * re-applies it whenever the theme changes, then shows the library manager
     * window as the first window. When the settings file does not exist, the
     * modal settings window is opened over the first window. When the settings
     * file exists but cannot be read, an error dialog is shown and the
     * application starts with the default settings as if the file did not
     * exist.
     *
     * @param stage the primary stage for this application
     * @throws NullPointerException if stage is null
     */
    @Override
    public void start(Stage stage) {
        Objects.requireNonNull(stage, "stage must not be null");
        Thread.currentThread().setUncaughtExceptionHandler((_, e) -> {
            if (e instanceof SettingsException) {
                AlertDialog.showError(e.getMessage());
            } else {
                e.printStackTrace();
            }
        });
        var settingsAppModel = new SettingsAppModel(new SettingsRepositoryImpl());
        var settingsViewModel = new SettingsViewModel(settingsAppModel, App::chooseDirectory);
        settingsAppModel.themeProperty()
            .subscribe(theme -> setUserAgentStylesheet(toStylesheet(theme)));
        var musicLibAppModel = new MusicLibraryAppModel(
            new LibraryIndexer(new SqliteLibraryRepository()),
            settingsAppModel.musicLibraryPathProperty()
        );

        var windowManager = new WindowManager();
        var folderOpener = new ShellFolderOpener();
        var libraryManagerViewModel =
            new LibraryManagerViewModel(
                windowManager, musicLibAppModel, settingsAppModel, new MediaMusicPlayer(),
                folderOpener
            );
        windowManager.registerView(new LibraryManagerView(libraryManagerViewModel));
        var settingsFolder = Path.of(System.getProperty("user.home"), ".sss-music-player");
        windowManager.registerView(
            new DuplicateListView(
                new DuplicateListViewModel(
                    musicLibAppModel,
                    new MediaMusicPlayer(),
                    new DuplicateFileMover(
                        settingsFolder.resolve("duplicates"),
                        settingsFolder.resolve("duplicates.log")
                    ),
                    folderOpener
                )
            )
        );
        windowManager.registerView(new SettingsView(settingsViewModel));
        windowManager.registerView(
            new AlbumEditView(new AlbumEditViewModel(musicLibAppModel, new JaudiotaggerTagWriter()))
        );
        windowManager.showWindow(FIRST_VIEW, stage);
        // Loading the settings triggers the startup scan, so it runs after the
        // main window is shown and the scanning dialog can be owned by it.
        Optional<Settings> loadedSettings;
        try {
            loadedSettings = settingsAppModel.loadSettings();
        } catch (SettingsException e) {
            AlertDialog.showError(e.getMessage() + ". Starting with default settings.");
            loadedSettings = Optional.empty();
        }
        if (loadedSettings.isEmpty()) {
            libraryManagerViewModel.openSettingsWindow();
        }
    }

    private static String toStylesheet(Theme theme) {
        return switch (theme) {
            case PRIMER_LIGHT -> new PrimerLight().getUserAgentStylesheet();
            case PRIMER_DARK -> new PrimerDark().getUserAgentStylesheet();
            case NORD_LIGHT -> new NordLight().getUserAgentStylesheet();
            case NORD_DARK -> new NordDark().getUserAgentStylesheet();
            case CUPERTINO_LIGHT -> new CupertinoLight().getUserAgentStylesheet();
            case CUPERTINO_DARK -> new CupertinoDark().getUserAgentStylesheet();
            case DRACULA -> new Dracula().getUserAgentStylesheet();
        };
    }

    private static Optional<Path> chooseDirectory(Window ownerWindow) {
        var chooser = new DirectoryChooser();
        return Optional.ofNullable(chooser.showDialog(ownerWindow)).map(File::toPath);
    }
}
