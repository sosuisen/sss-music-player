package com.sosuisha.main;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.sosuisha.presentation.View;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListView;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerView;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerViewModel;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX application of SSS Music Player.
 */
public class App extends Application {
    /** The view whose window is shown first at startup. Change this constant during development. */
    static final Class<? extends View> FIRST_VIEW = DuplicateListView.class;

    /**
     * Called when the application is started. Shows the library manager window
     * as the first window.
     *
     * @param stage the primary stage for this application
     * @throws NullPointerException if stage is null
     */
    @Override
    public void start(Stage stage) {
        Objects.requireNonNull(stage, "stage must not be null");
        var windowManager = new WindowManager();
        var appModel = new MusicLibraryAppModel();
        var viewModel = new LibraryManagerViewModel(appModel);
        // Dummy data for development until the library scan is wired up.
        appModel.setFiles(List.of(Path.of("dummy1.mp3"), Path.of("dummy2.m4a")));
        windowManager.registerView(new LibraryManagerView(viewModel));
        windowManager.registerView(new DuplicateListView(new DuplicateListViewModel(appModel)));
        windowManager.showWindow(FIRST_VIEW, stage);
    }
}
