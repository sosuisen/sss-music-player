package com.sosuisha.presentation.screens.settings;

import java.util.Objects;

import com.sosuisha.presentation.View;

import io.github.sosuisen.jfxbuilder.controls.ButtonBuilder;
import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.graphics.GridPaneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Scene;

/**
 * View for the settings screen.
 */
public class SettingsView implements View {
    private static final String TITLE = "Settings";
    private static final double WIDTH = 400;
    private static final double HEIGHT = 300;

    private final SettingsViewModel viewModel;
    private final Scene scene;

    /**
     * Creates the view.
     *
     * @param viewModel view model of the settings screen
     * @throws NullPointerException if viewModel is null
     */
    public SettingsView(SettingsViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.scene = buildSceneGraph();
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
            .create(
                GridPaneBuilder.create()
                    .hgap(10)
                    .vgap(10)
                    .padding(new Insets(10))
                    .addRow(0,
                        LabelBuilder.create()
                            .text("Library path:")
                            .build(),
                        LabelBuilder.create()
                            .textPropertyApply(
                                text -> text.bind(viewModel.musicLibraryPathProperty()))
                            .id("musicLibraryPath")
                            .build(),
                        ButtonBuilder.create()
                            .text("Select folder...")
                            .id("selectFolder")
                            .build())
                    .build(),
                WIDTH,
                HEIGHT
            )
            .build();
    }
}
