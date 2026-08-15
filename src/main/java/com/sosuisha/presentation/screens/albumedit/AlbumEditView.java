package com.sosuisha.presentation.screens.albumedit;

import java.util.Objects;

import com.sosuisha.presentation.View;

import io.github.sosuisen.jfxbuilder.controls.ButtonBuilder;
import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.controls.TextFieldBuilder;
import io.github.sosuisen.jfxbuilder.graphics.ColumnConstraintsBuilder;
import io.github.sosuisen.jfxbuilder.graphics.GridPaneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;

/**
 * View for the album metadata edit screen.
 */
public class AlbumEditView implements View {
    private static final String TITLE = "Edit Album";
    private static final double WIDTH = 400;
    private static final double HEIGHT = 200;
    private static final String CHANGED_FIELD_CLASS = "changed-field";
    // Light yellow marks a field whose value differs from the album.
    private static final String CSS = """
                                      .changed-field {
                                          -fx-control-inner-background: #ffff99;
                                      }
                                      """;

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
        notifyWindowLifecycle();
    }

    // Each showWindow attaches the scene to a new stage, so the lifecycle is
    // subscribed per window. The two-argument subscribe skips the initial
    // value; only real show and hide events reach the view model.
    private void notifyWindowLifecycle() {
        scene.windowProperty().subscribe(window -> {
            if (window == null) { return; }
            window.showingProperty().subscribe((_, showing) -> {
                if (showing) {
                    viewModel.windowOpened();
                } else {
                    viewModel.windowClosed();
                }
            });
        });
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
                            .apply(
                                field -> showChangedStyle(
                                    field, viewModel.albumNameChangedProperty()
                                )
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
                            .apply(
                                field -> showChangedStyle(
                                    field, viewModel.albumArtistChangedProperty()
                                )
                            )
                            .build()
                    )
                    .addRow(
                        2,
                        ButtonBuilder.create()
                            .text("Save")
                            .id("saveButton")
                            .disablePropertyApply(
                                prop -> prop.bind(
                                    viewModel.albumNameChangedProperty()
                                        .or(viewModel.albumArtistChangedProperty())
                                        .not()
                                )
                            )
                            .onAction(_ -> viewModel.save())
                            .columnSpanInGridPane(2)
                            .hAlignmentInGridPane(HPos.RIGHT)
                            .build()
                    )
                    .addRow(
                        3,
                        LabelBuilder.create()
                            .text("編集ウィンドウを閉じるとライブラリが再読み込みされます")
                            .id("reloadNotice")
                            .visiblePropertyApply(
                                prop -> prop.bind(viewModel.libraryChangedProperty())
                            )
                            .columnSpanInGridPane(2)
                            .build()
                    )
                    .build(),
                WIDTH,
                HEIGHT
            )
            .addStylesheetsText(CSS)
            .build();
    }

    private static void showChangedStyle(TextField field, ObservableValue<Boolean> changed) {
        changed.subscribe(isChanged -> {
            if (isChanged) {
                field.getStyleClass().add(CHANGED_FIELD_CLASS);
            } else {
                field.getStyleClass().remove(CHANGED_FIELD_CLASS);
            }
        });
    }
}
