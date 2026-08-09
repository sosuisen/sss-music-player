package com.sosuisha.main;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.sosuisha.presentation.screens.librarymanager.LibraryManagerView;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerViewModel;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX application of SSS Music Player.
 */
public class App extends Application {
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
        var viewModel = new LibraryManagerViewModel();
        // Dummy data for development until the library scan is wired up.
        viewModel.setFiles(List.of(Path.of("dummy1.mp3"), Path.of("dummy2.m4a")));
        var view = new LibraryManagerView(viewModel);
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }
}
