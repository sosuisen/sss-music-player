package com.sosuisha.presentation.screens.librarymanager;

import java.util.Objects;

import com.sosuisha.presentation.View;
import com.sosuisha.presentation.screens.librarymanager.components.AlbumPane;
import com.sosuisha.presentation.screens.librarymanager.components.LibraryMenuBar;
import com.sosuisha.presentation.screens.librarymanager.components.PlayerPanel;
import com.sosuisha.presentation.screens.librarymanager.components.ScanningDialog;
import com.sosuisha.presentation.screens.librarymanager.components.TrackPane;

import io.github.sosuisen.jfxbuilder.controls.SplitPaneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * View for the library manager screen.
 */
public class LibraryManagerView implements View {
    private static final String TITLE = "Library Manager";

    private final LibraryManagerViewModel viewModel;
    private final Scene scene;
    private Stage scanningDialog;

    /**
     * Creates the view.
     *
     * @param viewModel view model of the library manager screen
     * @throws NullPointerException if viewModel is null
     */
    public LibraryManagerView(LibraryManagerViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.scene = buildSceneGraph();
        viewModel.scanningProperty().subscribe(this::updateScanningDialog);
    }

    private void updateScanningDialog(boolean scanning) {
        if (scanning) {
            if (scanningDialog == null) {
                scanningDialog = ScanningDialog.getStage(viewModel, scene);
            }
            scanningDialog.show();
        } else if (scanningDialog != null) {
            scanningDialog.close();
        }
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    private Scene buildSceneGraph() {
        return SceneBuilder
            .withRoot(
                VBoxBuilder
                    .withChildren(
                        LibraryMenuBar.getRoot(viewModel),
                        SplitPaneBuilder
                            .withItems(AlbumPane.getRoot(viewModel), TrackPane.getRoot(viewModel))
                            .vGrowInVBox(Priority.ALWAYS)
                            .build(),
                        PlayerPanel.getRoot(viewModel)
                    )
                    .build()
            )
            .build();
    }
}
