package com.sosuisha.presentation.screens.settings;

import java.util.Objects;

import com.sosuisha.domain.model.Theme;
import com.sosuisha.presentation.View;

import io.github.sosuisen.jfxbuilder.controls.ButtonBuilder;
import io.github.sosuisen.jfxbuilder.controls.ComboBoxBuilder;
import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.graphics.ColumnConstraintsBuilder;
import io.github.sosuisen.jfxbuilder.graphics.GridPaneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

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
                    .addColumnConstraints(
                        ColumnConstraintsBuilder.create().build(),
                        ColumnConstraintsBuilder.create().hgrow(Priority.ALWAYS).build()
                    )
                    .addRow(
                        0,
                        LabelBuilder.create()
                            .text("Library path:")
                            .build(),
                        LabelBuilder.create()
                            .textPropertyApply(
                                text -> text.bind(viewModel.musicLibraryPathTextProperty())
                            )
                            .id("musicLibraryPath")
                            .build(),
                        ButtonBuilder.create()
                            .text("Select folder...")
                            .id("selectFolder")
                            .onAction(_ -> viewModel.selectMusicLibraryFolder(scene.getWindow()))
                            .build()
                    )
                    .addRow(
                        1,
                        LabelBuilder.create()
                            .text("Theme:")
                            .build(),
                        ComboBoxBuilder.<Theme>create()
                            .id("theme")
                            .apply(comboBox -> comboBox.getItems().addAll(Theme.values()))
                            .valuePropertyApply(
                                prop -> prop.bindBidirectional(viewModel.themeProperty())
                            )
                            .build()
                    )
                    .addRow(
                        2,
                        LabelBuilder.create()
                            .textPropertyApply(
                                text -> text.bind(viewModel.errorMessageProperty())
                            )
                            .id("errorMessage")
                            .columnSpanInGridPane(GridPane.REMAINING)
                            .build()
                    )
                    .build(),
                WIDTH,
                HEIGHT
            )
            .build();
    }
}
