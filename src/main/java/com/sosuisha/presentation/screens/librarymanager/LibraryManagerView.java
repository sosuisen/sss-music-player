package com.sosuisha.presentation.screens.librarymanager;

import java.util.Objects;

import io.github.sosuisen.jfxbuilder.controls.ListViewBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.scene.Scene;

/**
 * View for the library manager screen.
 */
public class LibraryManagerView {
    private static final String TITLE = "Library Manager";

    private final LibraryManagerViewModel viewModel;
    private final Scene scene;

    /**
     * Creates the view.
     *
     * @param viewModel view model of the library manager screen
     * @throws NullPointerException if viewModel is null
     */
    public LibraryManagerView(LibraryManagerViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.scene = buildSceneGraph();
    }

    /**
     * Returns the scene of this view.
     *
     * @return scene of this view
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Returns the window title of this view.
     *
     * @return window title
     */
    public String getTitle() {
        return TITLE;
    }

    private Scene buildSceneGraph() {
        return SceneBuilder
            .withRoot(
                VBoxBuilder
                    .withChildren(
                        ListViewBuilder.create(viewModel.getFiles())
                            .id("fileList")
                            .build()
                    )
                    .build()
            )
            .build();
    }
}
