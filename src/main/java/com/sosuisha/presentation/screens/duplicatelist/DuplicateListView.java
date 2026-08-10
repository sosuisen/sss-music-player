package com.sosuisha.presentation.screens.duplicatelist;

import java.util.Objects;

import com.sosuisha.presentation.View;
import com.sosuisha.presentation.screens.duplicatelist.components.CandidateList;
import com.sosuisha.presentation.screens.duplicatelist.components.ConfirmPanel;

import io.github.sosuisen.jfxbuilder.controls.ButtonBuilder;
import io.github.sosuisen.jfxbuilder.controls.SplitPaneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;

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
                        SplitPaneBuilder
                            .withItems(
                                CandidateList.getRoot(viewModel),
                                ConfirmPanel.getRoot(viewModel)
                            )
                            .vGrowInVBox(Priority.ALWAYS)
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
