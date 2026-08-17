package com.sosuisha.presentation.screens.librarymanager.components;

import java.util.Objects;

import com.sosuisha.presentation.screens.librarymanager.LibraryManagerViewModel;

import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.StageBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Modal dialog that shows the file being read by the running library scan.
 */
public class ScanningDialog {
    private static final double WIDTH = 600;
    private static final double PADDING = 20;

    private ScanningDialog() {}

    /**
     * Returns the stage of the dialog.
     *
     * @param viewModel view model of the library manager screen
     * @param ownerScene scene of the owner window that keeps the dialog in
     *            front; the dialog has no owner when the scene is not attached
     *            to a window
     * @return stage of the dialog
     * @throws NullPointerException if viewModel or ownerScene is null
     */
    public static Stage getStage(LibraryManagerViewModel viewModel, Scene ownerScene) {
        Objects.requireNonNull(viewModel, "viewModel must not be null");
        Objects.requireNonNull(ownerScene, "ownerScene must not be null");
        return StageBuilder.create()
            .title("Scanning")
            .width(WIDTH)
            .scene(
                SceneBuilder
                    .withRoot(
                        VBoxBuilder
                            .withChildren(
                                LabelBuilder.create()
                                    .text("Scanning the music library...")
                                    .build(),
                                LabelBuilder.create()
                                    .id("scanningFile")
                                    .wrapText(true)
                                    .textPropertyApply(
                                        prop -> prop.bind(viewModel.scanningFileProperty())
                                    )
                                    .build()
                            )
                            .padding(new Insets(PADDING))
                            .build()
                    )
                    .build()
            )
            .apply(stage -> {
                stage.initModality(Modality.APPLICATION_MODAL);
                if (ownerScene.getWindow() != null) {
                    // The owner keeps the dialog in front of the main window.
                    stage.initOwner(ownerScene.getWindow());
                }
            })
            .build();
    }
}
