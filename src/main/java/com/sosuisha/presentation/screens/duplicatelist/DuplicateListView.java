package com.sosuisha.presentation.screens.duplicatelist;

import java.util.Objects;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.presentation.View;

import io.github.sosuisen.jfxbuilder.controls.ButtonBuilder;
import io.github.sosuisen.jfxbuilder.controls.ListViewBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;

/**
 * View for the duplicate file list screen.
 */
public class DuplicateListView implements View {
    private static final String TITLE = "Duplicate Files";

    private final DuplicateListViewModel viewModel;
    private final Scene scene;

    /**
     * Creates the view.
     *
     * @param viewModel view model of the duplicate file list screen
     * @throws NullPointerException if viewModel is null
     */
    public DuplicateListView(DuplicateListViewModel viewModel) {
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
            .withRoot(
                VBoxBuilder
                    .withChildren(
                        ListViewBuilder.create(viewModel.getDuplicatedItems())
                            .id("duplicateList")
                            .cellFactory(_ -> new ListCell<>() {
                                @Override
                                protected void updateItem(DuplicatedItems item, boolean empty) {
                                    super.updateItem(item, empty);
                                    setText(empty || item == null ? null : item.title());
                                }
                            })
                            .build(),
                        ButtonBuilder.create()
                            .text("Find by Filename")
                            .id("findByFilename")
                            .onAction(_ -> viewModel.detectByFilename())
                            .build()
                    )
                    .build()
            )
            .build();
    }
}
