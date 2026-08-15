package com.sosuisha.presentation.screens.albumedit;

import java.util.Objects;

import com.sosuisha.presentation.View;

import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.controls.TextFieldBuilder;
import io.github.sosuisen.jfxbuilder.graphics.ColumnConstraintsBuilder;
import io.github.sosuisen.jfxbuilder.graphics.GridPaneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;

/**
 * View for the album metadata edit screen.
 */
public class AlbumEditView implements View {
    private static final String TITLE = "Edit Album";
    private static final double WIDTH = 400;
    private static final double HEIGHT = 200;

    private final AlbumEditViewModel viewModel;
    private final Scene scene;

    /**
     * Creates the view.
     *
     * @param viewModel view model of the album metadata edit screen
     * @throws NullPointerException if viewModel is null
     */
    public AlbumEditView(AlbumEditViewModel viewModel) {
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
                            .text("Album:")
                            .build(),
                        TextFieldBuilder.create()
                            .id("albumNameField")
                            .textPropertyApply(
                                prop -> prop.bindBidirectional(viewModel.albumNameProperty())
                            )
                            .build()
                    )
                    .addRow(
                        1,
                        LabelBuilder.create()
                            .text("Album artist:")
                            .build(),
                        TextFieldBuilder.create()
                            .id("albumArtistField")
                            .textPropertyApply(
                                prop -> prop.bindBidirectional(viewModel.albumArtistProperty())
                            )
                            .build()
                    )
                    .build(),
                WIDTH,
                HEIGHT
            )
            .build();
    }
}
